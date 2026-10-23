package com.taller2.persistencia.remote;

import com.taller2.negocio.model.QuestionStatus;
import com.taller2.negocio.service.ReviewGateway;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

public class MicroserviceReviewClient implements ReviewGateway {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<Map<String, Object>>> LIST_TYPE =
            new ParameterizedTypeReference<>() {};
    private final RestClient client;

    public MicroserviceReviewClient(String baseUrl) {
        client = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public QuestionStatus assignReviewers(int questionId, List<String> reviewerEmails) {
        Map<String, Object> response = exchange(() -> client.post()
                .uri("/api/reviews/assign")
                .body(Map.of("preguntaId", questionId, "revisoresEmails", reviewerEmails))
                .retrieve()
                .body(MAP_TYPE));
        return QuestionStatus.valueOf(requiredText(response, "nuevoEstado"));
    }

    @Override
    public List<AssignedQuestion> listAssignedQuestions(String reviewerEmail) {
        List<Map<String, Object>> response = exchange(() -> client.get()
                .uri("/api/reviews/assigned/{email}", reviewerEmail)
                .retrieve()
                .body(LIST_TYPE));
        if (response == null) {
            throw new RemoteServiceException("Revisión devolvió una respuesta vacía.", null);
        }
        return response.stream().map(MicroserviceReviewClient::toAssignedQuestion).toList();
    }

    @Override
    public void evaluate(int questionId, String reviewerEmail, String decision, String observation) {
        exchange(() -> client.post()
                .uri("/api/reviews/evaluate")
                .body(Map.of(
                        "preguntaId", questionId,
                        "revisorEmail", reviewerEmail,
                        "decision", decision,
                        "observaciones", observation
                ))
                .retrieve()
                .toBodilessEntity());
    }

    private static AssignedQuestion toAssignedQuestion(Map<String, Object> value) {
        Object rawOptions = value.get("opciones");
        if (!(rawOptions instanceof List<?> optionValues)) {
            throw new RemoteServiceException("Revisión devolvió opciones de pregunta inválidas.", null);
        }
        return new AssignedQuestion(
                requiredInteger(value, "asignacionId"),
                requiredInteger(value, "preguntaId"),
                requiredText(value, "nombre"),
                optionalText(value, "texto"),
                optionalText(value, "contexto"),
                optionValues.stream().map(String::valueOf).toList(),
                optionalInteger(value, "respuestaCorrecta"),
                optionalText(value, "justificacion"),
                optionalText(value, "bibliografia"),
                optionalText(value, "competencia"),
                optionalText(value, "tema"),
                optionalText(value, "subtema"),
                optionalText(value, "nivelDificultad"),
                optionalText(value, "estadoPregunta"),
                optionalText(value, "colorHex"),
                optionalText(value, "estadoAsignacion"),
                optionalText(value, "observaciones")
        );
    }

    private static int requiredInteger(Map<String, Object> value, String field) {
        Object raw = value.get(field);
        if (!(raw instanceof Number number)) {
            throw new RemoteServiceException("Revisión devolvió inválido el campo " + field + ".", null);
        }
        return number.intValue();
    }

    private static int optionalInteger(Map<String, Object> value, String field) {
        Object raw = value.get(field);
        return raw instanceof Number number ? number.intValue() : 0;
    }

    private static String requiredText(Map<String, Object> value, String field) {
        String result = optionalText(value, field);
        if (result.isBlank()) {
            throw new RemoteServiceException("Revisión devolvió inválido el campo " + field + ".", null);
        }
        return result;
    }

    private static String optionalText(Map<String, Object> value, String field) {
        Object raw = value.get(field);
        return raw == null ? "" : String.valueOf(raw);
    }

    private static <T> T exchange(java.util.function.Supplier<T> operation) {
        try {
            return operation.get();
        } catch (RestClientResponseException exception) {
            throw new RemoteServiceException(
                    "Revisión rechazó la solicitud (" + exception.getStatusCode().value() + "): "
                            + exception.getResponseBodyAsString(),
                    exception
            );
        } catch (RuntimeException exception) {
            if (exception instanceof RemoteServiceException) {
                throw exception;
            }
            throw new RemoteServiceException("No se pudo conectar con el servicio de Revisión.", exception);
        }
    }
}
