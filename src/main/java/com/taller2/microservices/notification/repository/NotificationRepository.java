package com.taller2.microservices.notification.repository;

import com.taller2.microservices.notification.NotificationRecord;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class NotificationRepository {

    private final String dbUrl;

    public NotificationRepository() {
        this("jdbc:sqlite:notifications.db");
    }

    public NotificationRepository(String dbUrl) {
        this.dbUrl = dbUrl;
        initialize();
    }

    public boolean save(String eventId, String recipient, String subject, String body) {
        String sql = """
                INSERT OR IGNORE INTO notifications
                    (event_id, recipient, subject, body, created_at, is_read)
                VALUES (?, ?, ?, ?, ?, 0)
                """;
        try (Connection connection = DriverManager.getConnection(dbUrl);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, eventId);
            statement.setString(2, recipient);
            statement.setString(3, subject);
            statement.setString(4, body);
            statement.setString(5, LocalDateTime.now().toString());
            return statement.executeUpdate() == 1;
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo guardar la notificación.", exception);
        }
    }

    public List<NotificationRecord> findByRecipient(String recipient) {
        String sql = """
                SELECT id, subject, body, created_at, is_read
                FROM notifications
                WHERE recipient = ?
                ORDER BY id DESC
                """;
        List<NotificationRecord> notifications = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(dbUrl);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, recipient);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    notifications.add(new NotificationRecord(
                            result.getInt("id"),
                            result.getString("subject"),
                            result.getString("body"),
                            result.getString("created_at"),
                            result.getInt("is_read") != 0
                    ));
                }
            }
            return List.copyOf(notifications);
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudieron consultar las notificaciones.", exception);
        }
    }

    public boolean markRead(int id, String recipient) {
        String sql = "UPDATE notifications SET is_read = 1 WHERE id = ? AND recipient = ?";
        try (Connection connection = DriverManager.getConnection(dbUrl);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.setString(2, recipient);
            return statement.executeUpdate() == 1;
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo actualizar la notificación.", exception);
        }
    }

    private void initialize() {
        String sql = """
                CREATE TABLE IF NOT EXISTS notifications (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    event_id TEXT NOT NULL,
                    recipient TEXT NOT NULL,
                    subject TEXT NOT NULL,
                    body TEXT NOT NULL,
                    created_at TEXT NOT NULL,
                    is_read INTEGER NOT NULL DEFAULT 0,
                    UNIQUE (event_id, recipient)
                )
                """;
        try (Connection connection = DriverManager.getConnection(dbUrl);
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo inicializar la base de notificaciones.", exception);
        }
    }
}
