package com.taller2.microservices.questions.repository;

import com.taller2.negocio.model.Question;
import com.taller2.negocio.model.QuestionStatus;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Persistencia independiente para el Microservicio de Gestión de Preguntas (Database-per-service: questions.db).
 */
@Repository
public class QuestionMicroserviceRepository {

    private final String dbUrl;

    public QuestionMicroserviceRepository() {
        this("jdbc:sqlite:questions.db");
    }

    public QuestionMicroserviceRepository(String dbUrl) {
        this.dbUrl = dbUrl;
        initTable();
    }

    private void initTable() {
        String sql = """
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
                autor_id INTEGER
            );
        """;
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            seedDefaultQuestions(conn);
        } catch (SQLException e) {
            System.err.println("[QuestionMicroserviceRepository] Error inicializando tabla preguntas: " + e.getMessage());
        }
    }

    private void seedDefaultQuestions(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM preguntas")) {
            if (rs.next() && rs.getInt(1) == 0) {
                // Insertar preguntas de prueba DCE
                String insert = """
                    INSERT INTO preguntas (nombre, texto, opciones, respuesta_correcta, estado, contexto, justificacion, bibliografia, competencia, tema, subtema, nivel_dificultad, revisor_asignado, autor_id) VALUES
                    ('Dijkstra Heap', '¿Cuál es la complejidad temporal de Dijkstra con min-heap binario?', 'O(V^2);;O((V+E) log V);;O(E log E);;O(V log V)', 1, 'BORRADOR', 'En un grafo ponderado conexo con V vértices y E aristas con pesos positivos...', 'Cada inserción y actualización de clave en min-heap toma log V para cada una de las aristas y vértices.', 'Cormen, T. Introduction to Algorithms (3rd ed.)', 'Algoritmos y Complejidad', 'Grafos', 'Caminos Mínimos', 'Medio', '', 2),
                    ('Polimorfismo POO', '¿Qué principio de POO se manifiesta mediante el enlace dinámico en tiempo de ejecución?', 'Encapsulamiento;;Herencia estática;;Polimorfismo dinámico;;Abstracción pura', 2, 'PENDIENTE_REVISION', 'En el desarrollo orientado a objetos, el comportamiento de un método puede variar según la subclase receptora.', 'El polimorfismo dinámico permite despachar métodos según la clase de la instancia real.', 'Gamma et al. Design Patterns: Elements of Reusable Object-Oriented Software', 'Ingeniería de Software', 'Diseño OO', 'Polimorfismo', 'Fácil', '', 2);
                """;
                stmt.execute(insert);
            }
        }
    }

    public List<Question> findWithFilters(String competencia, String tema, String nivel, String estado, Integer autorId, int page, int size) {
        List<Question> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM preguntas WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (competencia != null && !competencia.isBlank()) {
            sql.append("AND LOWER(competencia) LIKE LOWER(?) ");
            params.add("%" + competencia.trim() + "%");
        }
        if (tema != null && !tema.isBlank()) {
            sql.append("AND LOWER(tema) LIKE LOWER(?) ");
            params.add("%" + tema.trim() + "%");
        }
        if (nivel != null && !nivel.isBlank()) {
            sql.append("AND LOWER(nivel_dificultad) = LOWER(?) ");
            params.add(nivel.trim());
        }
        if (estado != null && !estado.isBlank()) {
            sql.append("AND estado = ? ");
            params.add(estado.trim().toUpperCase());
        }
        if (autorId != null) {
            sql.append("AND autor_id = ? ");
            params.add(autorId);
        }

        // Paginación (HU3)
        sql.append("ORDER BY id DESC LIMIT ? OFFSET ?");
        params.add(size);
        params.add(page * size);

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[QuestionMicroserviceRepository] Error en findWithFilters: " + e.getMessage());
        }
        return list;
    }

    public long countWithFilters(String competencia, String tema, String nivel, String estado, Integer autorId) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM preguntas WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (competencia != null && !competencia.isBlank()) {
            sql.append("AND LOWER(competencia) LIKE LOWER(?) ");
            params.add("%" + competencia.trim() + "%");
        }
        if (tema != null && !tema.isBlank()) {
            sql.append("AND LOWER(tema) LIKE LOWER(?) ");
            params.add("%" + tema.trim() + "%");
        }
        if (nivel != null && !nivel.isBlank()) {
            sql.append("AND LOWER(nivel_dificultad) = LOWER(?) ");
            params.add(nivel.trim());
        }
        if (estado != null && !estado.isBlank()) {
            sql.append("AND estado = ? ");
            params.add(estado.trim().toUpperCase());
        }
        if (autorId != null) {
            sql.append("AND autor_id = ? ");
            params.add(autorId);
        }

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[QuestionMicroserviceRepository] Error contando preguntas: " + e.getMessage());
        }
        return 0;
    }

    public Optional<Question> findById(int id) {
        String sql = "SELECT * FROM preguntas WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[QuestionMicroserviceRepository] Error en findById: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Question save(Question q) {
        String sql = """
            INSERT INTO preguntas (nombre, texto, opciones, respuesta_correcta, estado, contexto, justificacion, bibliografia, competencia, tema, subtema, nivel_dificultad, revisor_asignado, autor_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, q.getNombre());
            ps.setString(2, q.getTexto());
            ps.setString(3, String.join(";;", q.getOpciones()));
            ps.setInt(4, q.getRespuestaCorrecta());
            ps.setString(5, q.getEstado().name());
            ps.setString(6, q.getContexto());
            ps.setString(7, q.getJustificacion());
            ps.setString(8, q.getBibliografia());
            ps.setString(9, q.getCompetencia());
            ps.setString(10, q.getTema());
            ps.setString(11, q.getSubtema());
            ps.setString(12, q.getNivelDificultad());
            ps.setString(13, q.getRevisorAsignado());
            ps.setObject(14, q.getAutorId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    q.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error guardando pregunta: " + e.getMessage(), e);
        }
        return q;
    }

    public void update(Question q) {
        String sql = """
            UPDATE preguntas SET
                nombre = ?, texto = ?, opciones = ?, respuesta_correcta = ?, estado = ?,
                contexto = ?, justificacion = ?, bibliografia = ?, competencia = ?, tema = ?,
                subtema = ?, nivel_dificultad = ?, revisor_asignado = ?
            WHERE id = ?
        """;
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, q.getNombre());
            ps.setString(2, q.getTexto());
            ps.setString(3, String.join(";;", q.getOpciones()));
            ps.setInt(4, q.getRespuestaCorrecta());
            ps.setString(5, q.getEstado().name());
            ps.setString(6, q.getContexto());
            ps.setString(7, q.getJustificacion());
            ps.setString(8, q.getBibliografia());
            ps.setString(9, q.getCompetencia());
            ps.setString(10, q.getTema());
            ps.setString(11, q.getSubtema());
            ps.setString(12, q.getNivelDificultad());
            ps.setString(13, q.getRevisorAsignado());
            ps.setInt(14, q.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando pregunta: " + e.getMessage(), e);
        }
    }

    private Question mapRow(ResultSet rs) throws SQLException {
        String ops = rs.getString("opciones");
        List<String> opcionesList = (ops != null && !ops.isEmpty())
                ? Arrays.asList(ops.split(";;"))
                : new ArrayList<>();

        Question q = new Question(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("texto"),
                opcionesList,
                rs.getInt("respuesta_correcta"),
                QuestionStatus.valueOf(rs.getString("estado")),
                rs.getString("contexto"),
                rs.getString("justificacion"),
                rs.getString("bibliografia"),
                rs.getString("competencia"),
                rs.getString("tema"),
                rs.getString("subtema"),
                rs.getString("nivel_dificultad"),
                rs.getString("revisor_asignado")
        );
        int autor = rs.getInt("autor_id");
        if (!rs.wasNull()) {
            q.setAutorId(autor);
        }
        return q;
    }
}
