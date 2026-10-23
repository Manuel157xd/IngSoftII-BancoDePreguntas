package com.taller2.microservices.identity.repository;

import com.taller2.negocio.model.EstadoUsuario;
import com.taller2.negocio.model.Rol;
import com.taller2.negocio.model.Usuario;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Persistencia independiente para el Microservicio de Usuarios (Database-per-service: identity.db).
 */
@Repository
public class IdentityMicroserviceRepository {

    private final String dbUrl;

    public IdentityMicroserviceRepository() {
        this("jdbc:sqlite:identity.db");
    }

    public IdentityMicroserviceRepository(String dbUrl) {
        this.dbUrl = dbUrl;
        initTable();
    }

    private void initTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                login TEXT UNIQUE NOT NULL,
                nombre_completo TEXT NOT NULL,
                password_hash TEXT NOT NULL,
                rol TEXT NOT NULL,
                estado TEXT NOT NULL
            );
        """;
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            seedDefaultUsers(conn);
             migrateDemoAuthorRole(conn);
        } catch (SQLException e) {
            System.err.println("[IdentityRepository] Error inicializando tabla usuarios: " + e.getMessage());
        }
    }

    private void seedDefaultUsers(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM usuarios")) {
            if (rs.next() && rs.getInt(1) == 0) {
                String insert = """
                    INSERT INTO usuarios (login, nombre_completo, password_hash, rol, estado) VALUES
                    ('admin@unicauca.edu.co', 'Admin General', 'admin123', 'ADMINISTRADOR', 'ACTIVO'),
                    ('autor@unicauca.edu.co', 'Prof. Carlos Autor', 'autor123', 'AUTOR_PREGUNTAS', 'ACTIVO'),
                    ('revisor1@unicauca.edu.co', 'Prof. Revisor Uno', 'revisor123', 'REVISOR', 'ACTIVO'),
                    ('revisor2@unicauca.edu.co', 'Prof. Revisor Dos', 'revisor123', 'REVISOR', 'ACTIVO'),
                    ('revisor3@unicauca.edu.co', 'Prof. Revisor Tres', 'revisor123', 'REVISOR', 'ACTIVO');
                """;
                stmt.execute(insert);
            }
        }
    }

    private void migrateDemoAuthorRole(Connection conn) throws SQLException {
        try (PreparedStatement statement = conn.prepareStatement(
                "UPDATE usuarios SET rol = 'AUTOR_PREGUNTAS' "
                        + "WHERE LOWER(login) = LOWER(?) AND rol = 'DOCENTE'")) {
            statement.setString(1, "autor@unicauca.edu.co");
            statement.executeUpdate();
        }
    }

    public Optional<Usuario> findByLogin(String login) {
        String sql = "SELECT id, login, nombre_completo, password_hash, rol, estado FROM usuarios WHERE LOWER(login) = LOWER(?)";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[IdentityRepository] Error en findByLogin: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Usuario> findByRol(Rol rol) {
        List<Usuario> list = new ArrayList<>();
        String sql = "SELECT id, login, nombre_completo, password_hash, rol, estado FROM usuarios WHERE rol = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rol.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[IdentityRepository] Error en findByRol: " + e.getMessage());
        }
        return list;
    }

    public List<Usuario> findAll() {
        List<Usuario> list = new ArrayList<>();
        String sql = "SELECT id, login, nombre_completo, password_hash, rol, estado FROM usuarios";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[IdentityRepository] Error en findAll: " + e.getMessage());
        }
        return list;
    }

    public Optional<Usuario> findById(int id) {
        String sql = "SELECT id, login, nombre_completo, password_hash, rol, estado FROM usuarios WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando usuario por id: " + e.getMessage(), e);
        }
    }

    public void update(int id, String name, Rol role, EstadoUsuario state) {
        String sql = "UPDATE usuarios SET nombre_completo = ?, rol = ?, estado = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, role.name());
            ps.setString(3, state.name());
            ps.setInt(4, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando usuario: " + e.getMessage(), e);
        }
    }

    public void delete(int id) {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement("DELETE FROM usuarios WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando usuario: " + e.getMessage(), e);
        }
    }

    public Usuario save(Usuario u) {
        String sql = "INSERT INTO usuarios (login, nombre_completo, password_hash, rol, estado) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getLogin());
            ps.setString(2, u.getNombreCompleto());
            ps.setString(3, u.getPasswordHash());
            ps.setString(4, u.getRol().name());
            ps.setString(5, u.getEstado().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    u.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error guardando usuario: " + e.getMessage(), e);
        }
        return u;
    }

    private Usuario mapRow(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getInt("id"),
                rs.getString("login"),
                rs.getString("nombre_completo"),
                Rol.valueOf(rs.getString("rol")),
                EstadoUsuario.valueOf(rs.getString("estado")),
                rs.getString("password_hash")
        );
    }
}
