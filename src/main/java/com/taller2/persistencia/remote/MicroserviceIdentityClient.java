package com.taller2.persistencia.remote;

import com.taller2.negocio.model.EstadoUsuario;
import com.taller2.negocio.model.Rol;
import com.taller2.negocio.model.Usuario;
import com.taller2.persistencia.repository.UsuarioRepository;
import com.taller2.seguridad.CredentialAuthenticator;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MicroserviceIdentityClient implements UsuarioRepository, CredentialAuthenticator {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<Map<String, Object>>> LIST_TYPE =
            new ParameterizedTypeReference<>() {};
    private final RestClient client;

    public MicroserviceIdentityClient(String baseUrl) {
        client = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public void guardar(Usuario user) {
        withErrorHandling(() -> client.post()
                .uri("/api/auth/register")
                .body(Map.of(
                        "nombre", user.getNombreCompleto(),
                        "correo", user.getLogin(),
                        "password", user.getPasswordHash(),
                        "rol", user.getRol().name(),
                        "estado", user.getEstado().name()
                ))
                .retrieve()
                .toBodilessEntity());
    }

    @Override
    public Optional<Usuario> buscarPorLogin(String login) {
        return listarTodos().stream()
                .filter(user -> user.getLogin().equalsIgnoreCase(login))
                .findFirst();
    }

    @Override
    public List<Usuario> listarTodos() {
        try {
            List<Map<String, Object>> response = client.get()
                    .uri("/api/auth/users")
                    .retrieve()
                    .body(LIST_TYPE);
            if (response == null) {
                throw new RemoteServiceException("Identidad devolvió una respuesta vacía.", null);
            }
            return response.stream().map(MicroserviceIdentityClient::toUser).toList();
        } catch (RestClientResponseException exception) {
            throw serviceError(exception);
        } catch (RuntimeException exception) {
            if (exception instanceof RemoteServiceException) {
                throw exception;
            }
            throw unavailable(exception);
        }
    }

    @Override
    public boolean existePorLogin(String login) {
        return buscarPorLogin(login).isPresent();
    }

    @Override
    public void actualizar(Usuario user) {
        withErrorHandling(() -> client.put()
                .uri("/api/auth/users/{id}", user.getId())
                .body(Map.of(
                        "nombre", user.getNombreCompleto(),
                        "rol", user.getRol().name(),
                        "estado", user.getEstado().name()
                ))
                .retrieve()
                .toBodilessEntity());
    }

    @Override
    public void eliminar(int id) {
        withErrorHandling(() -> client.delete()
                .uri("/api/auth/users/{id}", id)
                .retrieve()
                .toBodilessEntity());
    }

    @Override
    public Usuario autenticar(String login, String password) {
        try {
            Map<String, Object> response = client.post()
                    .uri("/api/auth/login")
                    .body(Map.of("correo", login, "password", password))
                    .retrieve()
                    .body(MAP_TYPE);
            if (response == null) {
                throw new RemoteServiceException("Identidad devolvió una respuesta vacía al autenticar.", null);
            }
            return new Usuario(
                    requiredInteger(response, "id"),
                    requiredText(response, "correo"),
                    requiredText(response, "nombre"),
                    Rol.valueOf(requiredText(response, "rol")),
                    EstadoUsuario.ACTIVO,
                    null
            );
        } catch (HttpClientErrorException.Unauthorized exception) {
            throw new IllegalArgumentException("Usuario o contraseña incorrectos.", exception);
        } catch (RestClientResponseException exception) {
            throw serviceError(exception);
        } catch (RuntimeException exception) {
            if (exception instanceof IllegalArgumentException || exception instanceof RemoteServiceException) {
                throw exception;
            }
            throw unavailable(exception);
        }
    }

    private static Usuario toUser(Map<String, Object> response) {
        return new Usuario(
                requiredInteger(response, "id"),
                requiredText(response, "login"),
                requiredText(response, "nombreCompleto"),
                Rol.valueOf(requiredText(response, "rol")),
                EstadoUsuario.valueOf(requiredText(response, "estado")),
                null
        );
    }

    private static int requiredInteger(Map<String, Object> values, String field) {
        Object value = values.get(field);
        if (!(value instanceof Number number)) {
            throw new RemoteServiceException("Identidad devolvió inválido el campo " + field + ".", null);
        }
        return number.intValue();
    }

    private static String requiredText(Map<String, Object> values, String field) {
        Object value = values.get(field);
        if (!(value instanceof String text) || text.isBlank()) {
            throw new RemoteServiceException("Identidad devolvió inválido el campo " + field + ".", null);
        }
        return text;
    }

    private static void withErrorHandling(Runnable operation) {
        try {
            operation.run();
        } catch (RestClientResponseException exception) {
            throw serviceError(exception);
        } catch (RuntimeException exception) {
            if (exception instanceof RemoteServiceException) {
                throw exception;
            }
            throw unavailable(exception);
        }
    }

    private static RemoteServiceException serviceError(RestClientResponseException exception) {
        return new RemoteServiceException(
                "Identidad rechazó la solicitud (" + exception.getStatusCode().value() + "): "
                        + exception.getResponseBodyAsString(),
                exception
        );
    }

    private static RemoteServiceException unavailable(Throwable exception) {
        return new RemoteServiceException("No se pudo conectar con el servicio de Identidad.", exception);
    }
}
