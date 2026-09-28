package com.taller2.persistencia.database;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    private DatabaseInitializer() {
    }

    public static void initialize() {

        String usuariosSql = """
                CREATE TABLE IF NOT EXISTS usuarios (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    login TEXT NOT NULL UNIQUE,
                    nombre_completo TEXT NOT NULL,
                    rol TEXT NOT NULL,
                    estado TEXT NOT NULL,
                    password_hash TEXT NOT NULL
                )
                """;
        String preguntasSql = """
                CREATE TABLE IF NOT EXISTS preguntas (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nombre TEXT NOT NULL,
                        texto TEXT NOT NULL,
                        opciones TEXT NOT NULL,
                        respuesta_correcta INTEGER NOT NULL,
                        estado TEXT NOT NULL,
                        contexto TEXT,
                        justificacion TEXT,
                        bibliografia TEXT,
                        competencia TEXT,
                        tema TEXT,
                        subtema TEXT,
                        nivel_dificultad TEXT,
                        revisor_asignado TEXT,
                        autor_id INTEGER REFERENCES usuarios(id)
                )
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                Statement statement = connection.createStatement()
        ) {

            statement.execute(usuariosSql);
            statement.execute(preguntasSql);
            ensureQuestionAuthorColumn(connection);
            migrateArchivedQuestionStatus(connection);

            System.out.println("Base de datos inicializada correctamente.");

        } catch (SQLException e) {

            System.err.println(
                    "Error al inicializar la base de datos: "
                            + e.getMessage()
            );
        }
    }

    static void ensureQuestionAuthorColumn(Connection connection) throws SQLException {
        boolean authorColumnExists = false;
        try (Statement statement = connection.createStatement();
             java.sql.ResultSet columns = statement.executeQuery("PRAGMA table_info(preguntas)")) {
            while (columns.next()) {
                if ("autor_id".equalsIgnoreCase(columns.getString("name"))) {
                    authorColumnExists = true;
                    break;
                }
            }
        }
        if (!authorColumnExists) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("ALTER TABLE preguntas ADD COLUMN autor_id INTEGER REFERENCES usuarios(id)");
            }
        }
    }

    static void migrateArchivedQuestionStatus(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    UPDATE preguntas
                    SET estado = 'ARCHIVADA'
                    WHERE UPPER(estado) = 'ARVHIVADA'
                    """);
        }
    }
}