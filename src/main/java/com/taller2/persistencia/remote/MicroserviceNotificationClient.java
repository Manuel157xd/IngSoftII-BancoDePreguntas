package com.taller2.persistencia.remote;

import com.taller2.negocio.model.Rol;
import com.taller2.negocio.model.Usuario;
import com.taller2.negocio.service.NotificationGateway;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

public class MicroserviceNotificationClient implements NotificationGateway {

    private static final ParameterizedTypeReference<List<Map<String, Object>>> LIST_TYPE =
            new ParameterizedTypeReference<>() {};

    private final RestClient client;

    public MicroserviceNotificationClient(String baseUrl) {
        client = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public List<InboxNotification> listForUser(Usuario user) {
        String recipient = recipientFor(user);
        List<Map<String, Object>> response = exchange(() -> client.get()
                .uri(uriBuilder -> uriBuilder.path("/api/notifications/inbox")
                        .queryParam("recipient", recipient)
                        .build())
                .retrieve()
                .body(LIST_TYPE));
        if (response == null) {
            throw new RemoteServiceException("Notificaciones devolvió una respuesta vacía.", null);
        }
        return response.stream().map(MicroserviceNotificationClient::toNotification).toList();
    }

    @Override
    public void markAsRead(int notificationId, Usuario user) {
        exchange(() -> client.patch()
                .uri("/api/notifications/{id}/read", notificationId)
                .body(Map.of("recipient", recipientFor(user)))
                .retrieve()
                .toBodilessEntity());
    }

    static String recipientFor(Usuario user) {
        if (user == null || user.getId() <= 0 || user.getRol() == null) {
            throw new IllegalArgumentException("Se requiere un usuario válido para consultar notificaciones.");
        }
        if (user.getRol() == Rol.REVISOR) {
            if (user.getLogin() == null || user.getLogin().isBlank()) {
                throw new IllegalArgumentException("El revisor debe tener un correo registrado.");
            }
            return user.getLogin().trim().toLowerCase(java.util.Locale.ROOT);
        }
        return "USER_ID:" + user.getId();
    }

    private static InboxNotification toNotification(Map<String, Object> value) {
        return new InboxNotification(
                requiredInteger(value, "id"),
                requiredText(value, "subject"),
                requiredText(value, "body"),
                requiredText(value, "createdAt"),
                Boolean.TRUE.equals(value.get("read"))
        );
    }

    private static int requiredInteger(Map<String, Object> values, String field) {
        Object value = values.get(field);
        if (!(value instanceof Number number)) {
            throw new RemoteServiceException("Notificaciones devolvió inválido el campo " + field + ".", null);
        }
        return number.intValue();
    }

    private static String requiredText(Map<String, Object> values, String field) {
        Object value = values.get(field);
        if (!(value instanceof String text) || text.isBlank()) {
            throw new RemoteServiceException("Notificaciones devolvió inválido el campo " + field + ".", null);
        }
        return text;
    }

    private static <T> T exchange(java.util.function.Supplier<T> operation) {
        try {
            return operation.get();
        } catch (RestClientResponseException exception) {
            throw new RemoteServiceException(
                    "Notificaciones rechazó la solicitud (" + exception.getStatusCode().value() + "): "
                            + exception.getResponseBodyAsString(),
                    exception
            );
        } catch (RuntimeException exception) {
            if (exception instanceof RemoteServiceException) {
                throw exception;
            }
            throw new RemoteServiceException("No se pudo conectar con el servicio de Notificaciones.", exception);
        }
    }
}
