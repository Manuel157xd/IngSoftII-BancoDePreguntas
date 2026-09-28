package com.taller2.persistencia.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.jupiter.api.Test;

class DatabaseInitializerTest {
    @Test
    void agregaAutorIdSinPerderFilasExistentesYEsIdempotente() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE usuarios (id INTEGER PRIMARY KEY)");
            statement.execute("CREATE TABLE preguntas (id INTEGER PRIMARY KEY, nombre TEXT)");
            statement.execute("INSERT INTO preguntas (id, nombre) VALUES (1, 'Legacy')");

            DatabaseInitializer.ensureQuestionAuthorColumn(connection);
            DatabaseInitializer.ensureQuestionAuthorColumn(connection);

            boolean foundAuthorColumn = false;
            try (ResultSet columns = statement.executeQuery("PRAGMA table_info(preguntas)")) {
                while (columns.next()) {
                    foundAuthorColumn |= "autor_id".equals(columns.getString("name"));
                }
            }
            assertTrue(foundAuthorColumn);
            try (ResultSet legacyRow = statement.executeQuery("SELECT id, nombre, autor_id FROM preguntas")) {
                assertTrue(legacyRow.next());
                assertEquals(1, legacyRow.getInt("id"));
                assertEquals("Legacy", legacyRow.getString("nombre"));
                assertNull(legacyRow.getObject("autor_id"));
            }
        }
    }

    @Test
    void corrigeGrafiaAntiguaDelEstadoArchivadoYEsIdempotente() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE preguntas (id INTEGER PRIMARY KEY, estado TEXT NOT NULL)");
            statement.execute("INSERT INTO preguntas (estado) VALUES ('ARVHIVADA'), ('arvhivada')");

            DatabaseInitializer.migrateArchivedQuestionStatus(connection);
            DatabaseInitializer.migrateArchivedQuestionStatus(connection);

            try (ResultSet result = statement.executeQuery("SELECT estado FROM preguntas ORDER BY id")) {
                assertTrue(result.next());
                assertEquals("ARCHIVADA", result.getString("estado"));
                assertTrue(result.next());
                assertEquals("ARCHIVADA", result.getString("estado"));
            }
        }
    }
}