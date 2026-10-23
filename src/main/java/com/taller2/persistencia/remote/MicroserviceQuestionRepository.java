package com.taller2.persistencia.remote;

import com.taller2.negocio.model.Question;
import com.taller2.negocio.model.QuestionStatus;
import com.taller2.persistencia.repository.QuestionRepository;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MicroserviceQuestionRepository implements QuestionRepository {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final int PAGE_SIZE = 100;
    private final RestClient client;

    public MicroserviceQuestionRepository(String baseUrl) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public void guardar(Question question) {
        exchange(() -> client.post()
                .uri("/api/questions")
                .body(toRequest(question))
                .retrieve()
                .toBodilessEntity());
    }

    @Override
    public Optional<Question> buscarPorId(int id) {
        try {
            Map<String, Object> response = client.get()
                    .uri("/api/questions/{id}", id)
                    .retrieve()
                    .body(MAP_TYPE);
            return Optional.of(fromMap(response));
        } catch (HttpClientErrorException.NotFound exception) {
            return Optional.empty();
        } catch (RestClientResponseException exception) {
            throw serviceError("Preguntas", exception);
        } catch (RuntimeException exception) {
            if (exception instanceof RemoteServiceException) {
                throw exception;
            }
            throw unavailable("Preguntas", exception);
        }
    }

    @Override
    public List<Question> listarTodas() {
        return listQuestions(Map.of());
    }

    @Override
    public List<Question> listarPorAutor(int authorId) {
        return listQuestions(Map.of("autorId", authorId));
    }

    @Override
    public List<Question> listarPorEstado(QuestionStatus status) {
        return listQuestions(Map.of("estado", status.name()));
    }

    @Override
    public void actualizar(Question question) {
        if (question.getEstado() == QuestionStatus.PENDIENTE_REVISION) {
            Optional<Question> current = buscarPorId(question.getId());
            if (current.isPresent()
                    && current.get().getEstado() == QuestionStatus.BORRADOR) {
                exchange(() -> client.patch()
                        .uri("/api/questions/{id}/enviar-revision", question.getId())
                        .retrieve()
                        .toBodilessEntity());
                return;
            }
        }
        exchange(() -> client.put()
                .uri("/api/questions/{id}", question.getId())
                .body(toRequest(question))
                .retrieve()
                .toBodilessEntity());
    }

    @Override
    public void eliminar(int id) {
        exchange(() -> client.delete()
                .uri("/api/questions/{id}", id)
                .retrieve()
                .toBodilessEntity());
    }

    private List<Question> listQuestions(Map<String, ?> filters) {
        List<Question> questions = new ArrayList<>();
        int totalPages = 1;
        for (int page = 0; page < totalPages; page++) {
            int currentPage = page;
            Map<String, Object> response;
            try {
                response = client.get()
                        .uri(uriBuilder -> {
                            var builder = uriBuilder.path("/api/questions")
                                    .queryParam("page", currentPage)
                                    .queryParam("size", PAGE_SIZE);
                            filters.forEach(builder::queryParam);
                            return builder.build();
                        })
                        .retrieve()
                        .body(MAP_TYPE);
            } catch (RestClientResponseException exception) {
                throw serviceError("Preguntas", exception);
            } catch (RuntimeException exception) {
                throw unavailable("Preguntas", exception);
            }
            if (response == null || !(response.get("preguntas") instanceof List<?> items)) {
                throw new RemoteServiceException("Respuesta inválida del servicio de Preguntas.", null);
            }
            for (Object item : items) {
                if (!(item instanceof Map<?, ?> map)) {
                    throw new RemoteServiceException("El servicio de Preguntas devolvió un registro inválido.", null);
                }
                questions.add(fromMap(castMap(map)));
            }
            Object pages = response.get("totalPaginas");
            if (pages instanceof Number number) {
                totalPages = Math.max(1, number.intValue());
            } else {
                throw new RemoteServiceException("El servicio de Preguntas no devolvió totalPaginas.", null);
            }
        }
        return questions;
    }

    private static Map<String, Object> toRequest(Question question) {
        Map<String, Object> request = new java.util.LinkedHashMap<>();
        request.put("nombre", value(question.getNombre()));
        request.put("texto", value(question.getTexto()));
        request.put("contexto", value(question.getContexto()));
        request.put("opciones", question.getOpciones());
        request.put("respuestaCorrecta", question.getRespuestaCorrecta());
        request.put("justificacion", value(question.getJustificacion()));
        request.put("bibliografia", value(question.getBibliografia()));
        request.put("competencia", value(question.getCompetencia()));
        request.put("tema", value(question.getTema()));
        request.put("subtema", value(question.getSubtema()));
        request.put("nivelDificultad", value(question.getNivelDificultad()));
        request.put("estado", question.getEstado().name());
        request.put("revisorAsignado", value(question.getRevisorAsignado()));
        request.put("autorId", question.getAutorId());
        return request;
    }

    private static Question fromMap(Map<String, Object> values) {
        if (values == null) {
            throw new RemoteServiceException("El servicio de Preguntas devolvió una respuesta vacía.", null);
        }
        Object rawOptions = values.get("opciones");
        if (!(rawOptions instanceof List<?> optionValues)) {
            throw new RemoteServiceException("El servicio de Preguntas devolvió opciones inválidas.", null);
        }
        List<String> options = optionValues.stream().map(String::valueOf).toList();
        Question question = new Question(
                integer(values, "id"),
                string(values, "nombre"),
                string(values, "texto"),
                options,
                integer(values, "respuestaCorrecta"),
                QuestionStatus.valueOf(string(values, "estado")),
                string(values, "contexto"),
                string(values, "justificacion"),
                string(values, "bibliografia"),
                string(values, "competencia"),
                string(values, "tema"),
                string(values, "subtema"),
                string(values, "nivelDificultad"),
                string(values, "revisorAsignado")
        );
        Object authorId = values.get("autorId");
        if (authorId instanceof Number number) {
            question.setAutorId(number.intValue());
        }
        return question;
    }

    private static int integer(Map<String, Object> values, String field) {
        Object value = values.get(field);
        if (!(value instanceof Number number)) {
            throw new RemoteServiceException("El servicio de Preguntas devolvió inválido el campo " + field + ".", null);
        }
        return number.intValue();
    }

    private static String string(Map<String, Object> values, String field) {
        Object value = values.get(field);
        return value == null ? "" : String.valueOf(value);
    }

    private static Map<String, Object> castMap(Map<?, ?> values) {
        Map<String, Object> result = new java.util.HashMap<>();
        values.forEach((key, value) -> result.put(String.valueOf(key), value));
        return result;
    }

    private static String value(String value) {
        return value == null ? "" : value;
    }

    private static void exchange(Runnable operation) {
        try {
            operation.run();
        } catch (RestClientResponseException exception) {
            throw serviceError("Preguntas", exception);
        } catch (RuntimeException exception) {
            if (exception instanceof RemoteServiceException) {
                throw exception;
            }
            throw unavailable("Preguntas", exception);
        }
    }

    private static RemoteServiceException serviceError(String service, RestClientResponseException exception) {
        return new RemoteServiceException(
                service + " rechazó la solicitud (" + exception.getStatusCode().value() + "): "
                        + exception.getResponseBodyAsString(),
                exception
        );
    }

    private static RemoteServiceException unavailable(String service, Throwable exception) {
        return new RemoteServiceException("No se pudo conectar con el servicio de " + service + ".", exception);
    }
}
