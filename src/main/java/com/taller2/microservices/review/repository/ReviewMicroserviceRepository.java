package com.taller2.microservices.review.repository;

import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistencia independiente para el Microservicio de Revisión por Pares (Database-per-service: reviews.db).
 */
@Repository
public class ReviewMicroserviceRepository {

    private final String dbUrl;

    public ReviewMicroserviceRepository() {
        this("jdbc:sqlite:reviews.db");
    }

    public ReviewMicroserviceRepository(String dbUrl) {
        this.dbUrl = dbUrl;
        initTable();
    }

    private void initTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS asignaciones_revision (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                pregunta_id INTEGER NOT NULL,
                revisor_email TEXT NOT NULL,
                fecha_asignacion TEXT NOT NULL,
                estado TEXT NOT NULL,
                calificacion INTEGER,
                observaciones TEXT,
                fecha_evaluacion TEXT
            );
        """;
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("[ReviewMicroserviceRepository] Error inicializando tabla revisiones: " + e.getMessage());
        }
    }

    public record ReviewAssignment(
            int id,
            int preguntaId,
            String revisorEmail,
            String fechaAsignacion,
            String estado,
            Integer calificacion,
            String observaciones,
            String fechaEvaluacion
    ) {}

    public void createAssignment(int preguntaId, String revisorEmail) {
        String checkSql = "SELECT COUNT(*) FROM asignaciones_revision WHERE pregunta_id = ? AND LOWER(revisor_email) = LOWER(?)";
        String insertSql = """
            INSERT INTO asignaciones_revision (pregunta_id, revisor_email, fecha_asignacion, estado)
            VALUES (?, ?, ?, 'PENDIENTE')
        """;
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setInt(1, preguntaId);
                checkPs.setString(2, revisorEmail.trim());
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return; // Ya se encuentra asignado previamente
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setInt(1, preguntaId);
                ps.setString(2, revisorEmail.trim());
                ps.setString(3, LocalDateTime.now().toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error asignando revisor: " + e.getMessage(), e);
        }
    }

    public List<ReviewAssignment> findByRevisor(String revisorEmail) {
        List<ReviewAssignment> list = new ArrayList<>();
        String sql = "SELECT * FROM asignaciones_revision WHERE LOWER(revisor_email) = LOWER(?) ORDER BY id DESC";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, revisorEmail);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ReviewAssignment(
                            rs.getInt("id"),
                            rs.getInt("pregunta_id"),
                            rs.getString("revisor_email"),
                            rs.getString("fecha_asignacion"),
                            rs.getString("estado"),
                            rs.getObject("calificacion") != null ? rs.getInt("calificacion") : null,
                            rs.getString("observaciones"),
                            rs.getString("fecha_evaluacion")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ReviewMicroserviceRepository] Error en findByRevisor: " + e.getMessage());
        }
        return list;
    }

    public List<ReviewAssignment> findByPreguntaId(int preguntaId) {
        List<ReviewAssignment> list = new ArrayList<>();
        String sql = "SELECT * FROM asignaciones_revision WHERE pregunta_id = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, preguntaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ReviewAssignment(
                            rs.getInt("id"),
                            rs.getInt("pregunta_id"),
                            rs.getString("revisor_email"),
                            rs.getString("fecha_asignacion"),
                            rs.getString("estado"),
                            rs.getObject("calificacion") != null ? rs.getInt("calificacion") : null,
                            rs.getString("observaciones"),
                            rs.getString("fecha_evaluacion")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ReviewMicroserviceRepository] Error en findByPreguntaId: " + e.getMessage());
        }
        return list;
    }

    public void recordEvaluation(int preguntaId, String revisorEmail, String resultado, String observaciones) {
        String sql = """
            UPDATE asignaciones_revision SET
                estado = ?,
                observaciones = ?,
                fecha_evaluacion = ?
            WHERE pregunta_id = ? AND LOWER(revisor_email) = LOWER(?)
        """;
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, resultado);
            ps.setString(2, observaciones);
            ps.setString(3, LocalDateTime.now().toString());
            ps.setInt(4, preguntaId);
            ps.setString(5, revisorEmail);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error registrando evaluacion: " + e.getMessage(), e);
        }
    }
}
