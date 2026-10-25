package com.taller2.presentacion.controller;

import com.taller2.negocio.model.EstadoUsuario;
import com.taller2.negocio.model.Rol;
import com.taller2.negocio.model.Usuario;
import com.taller2.negocio.service.NotificationGateway;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificationControllerTest {

    @Test
    void debeListarNotificacionesParaUsuario() {
        Usuario user = new Usuario(5, "autor@unicauca.edu.co", "Docente Autor", Rol.AUTOR_PREGUNTAS, EstadoUsuario.ACTIVO, "hash");
        NotificationGateway.InboxNotification notif = new NotificationGateway.InboxNotification(
                1, "Evaluación Aprobada", "Su pregunta ha sido aprobada.", "2026-10-24T12:00:00", false
        );

        NotificationGateway mockGateway = new NotificationGateway() {
            @Override
            public List<InboxNotification> listForUser(Usuario u) {
                if (u.getId() == 5) {
                    return List.of(notif);
                }
                return List.of();
            }

            @Override
            public void markAsRead(int notificationId, Usuario u) {}
        };

        NotificationController controller = new NotificationController(mockGateway);
        List<NotificationGateway.InboxNotification> result = controller.listForUser(user);

        assertEquals(1, result.size());
        assertEquals("Evaluación Aprobada", result.get(0).subject());
    }

    @Test
    void debeDelegarMarcarComoLeida() {
        Usuario user = new Usuario(5, "autor@unicauca.edu.co", "Docente Autor", Rol.AUTOR_PREGUNTAS, EstadoUsuario.ACTIVO, "hash");
        List<Integer> readIds = new ArrayList<>();

        NotificationGateway mockGateway = new NotificationGateway() {
            @Override
            public List<InboxNotification> listForUser(Usuario u) {
                return List.of();
            }

            @Override
            public void markAsRead(int notificationId, Usuario u) {
                readIds.add(notificationId);
            }
        };

        NotificationController controller = new NotificationController(mockGateway);
        controller.markAsRead(42, user);

        assertEquals(1, readIds.size());
        assertEquals(42, readIds.get(0));
    }
}
