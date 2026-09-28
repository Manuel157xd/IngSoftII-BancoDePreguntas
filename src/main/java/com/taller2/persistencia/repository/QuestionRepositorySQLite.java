package com.taller2.persistencia.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.taller2.persistencia.database.ConnectionProvider;
import com.taller2.persistencia.database.SQLiteConnectionProvider;
import com.taller2.negocio.model.Question;
import com.taller2.negocio.model.QuestionStatus;

public class QuestionRepositorySQLite implements QuestionRepository {
    private final ConnectionProvider connectionProvider;

    private static final String SELECT_ALL_COLUMNS = 
        "SELECT id, nombre, texto, opciones, respuesta_correcta, estado, " +
        "contexto, justificacion, bibliografia, competencia, tema, subtema, " +
        "nivel_dificultad, revisor_asignado FROM preguntas";

    public QuestionRepositorySQLite() {
        this(new SQLiteConnectionProvider());
    }

    public QuestionRepositorySQLite(String databaseUrl) {
        this(new SQLiteConnectionProvider(databaseUrl));
    }

    public QuestionRepositorySQLite(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public void guardar(Question question) {
        String sql = """
                INSERT INTO preguntas
                (nombre, texto, opciones, respuesta_correcta, estado,
                 contexto, justificacion, bibliografia, competencia, tema, subtema, nivel_dificultad, revisor_asignado)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, question.getNombre());
            statement.setString(2, question.getTexto());
            statement.setString(3, serializarOpciones(question.getOpciones()));
            statement.setInt(4, question.getRespuestaCorrecta());
            statement.setString(5, question.getEstado().name());
            statement.setString(6, question.getContexto());
            statement.setString(7, question.getJustificacion());
            statement.setString(8, question.getBibliografia());
            statement.setString(9, question.getCompetencia());
            statement.setString(10, question.getTema());
            statement.setString(11, question.getSubtema());
            statement.setString(12, question.getNivelDificultad());
            statement.setString(13, question.getRevisorAsignado());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar pregunta.", e);
        }
    }

    @Override
    public Optional<Question> buscarPorId(int id) {
        String sql = SELECT_ALL_COLUMNS + " WHERE id = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapear(resultSet))
                        : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar pregunta.", e);
        }
    }

    @Override
    public List<Question> listarTodas() {
        return listar(SELECT_ALL_COLUMNS);
    }

    @Override
    public List<Question> listarPorEstado(QuestionStatus estado) {
        String sql = SELECT_ALL_COLUMNS + " WHERE estado = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, estado.name());
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Question> questions = new ArrayList<>();
                while (resultSet.next()) {
                    questions.add(mapear(resultSet));
                }
                return questions;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar preguntas por estado.", e);
        }
    }

    @Override
    public void actualizar(Question question) {
        String sql = """
                UPDATE preguntas SET nombre = ?, texto = ?, opciones = ?,
                respuesta_correcta = ?, estado = ?, contexto = ?, justificacion = ?,
                bibliografia = ?, competencia = ?, tema = ?, subtema = ?,
                nivel_dificultad = ?, revisor_asignado = ? WHERE id = ?
                """;
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, question.getNombre());
            statement.setString(2, question.getTexto());
            statement.setString(3, serializarOpciones(question.getOpciones()));
            statement.setInt(4, question.getRespuestaCorrecta());
            statement.setString(5, question.getEstado().name());
            statement.setString(6, question.getContexto());
            statement.setString(7, question.getJustificacion());
            statement.setString(8, question.getBibliografia());
            statement.setString(9, question.getCompetencia());
            statement.setString(10, question.getTema());
            statement.setString(11, question.getSubtema());
            statement.setString(12, question.getNivelDificultad());
            statement.setString(13, question.getRevisorAsignado());
            statement.setInt(14, question.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar pregunta.", e);
        }
    }

    @Override
    public void eliminar(int id) {
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM preguntas WHERE id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar pregunta.", e);
        }
    }

    private List<Question> listar(String sql) {
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Question> questions = new ArrayList<>();
            while (resultSet.next()) {
                questions.add(mapear(resultSet));
            }
            return questions;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar preguntas.", e);
        }
    }

    private Question mapear(ResultSet resultSet) throws SQLException {
        return new Question(
                resultSet.getInt("id"),
                resultSet.getString("nombre"),
                resultSet.getString("texto"),
                deserializarOpciones(resultSet.getString("opciones")),
                resultSet.getInt("respuesta_correcta"),
                QuestionStatus.valueOf(resultSet.getString("estado")),
                resultSet.getString("contexto"),
                resultSet.getString("justificacion"),
                resultSet.getString("bibliografia"),
                resultSet.getString("competencia"),
                resultSet.getString("tema"),
                resultSet.getString("subtema"),
                resultSet.getString("nivel_dificultad"),
                resultSet.getString("revisor_asignado")
        );
    }

    private String serializarOpciones(List<String> opciones) {
        return String.join("|", opciones);
    }

    private List<String> deserializarOpciones(String opciones) {
        return opciones == null || opciones.isEmpty()
                ? new ArrayList<>()
                : new ArrayList<>(Arrays.asList(opciones.split("\\|", -1)));
    }
}