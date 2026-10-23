package com.taller2.microservices.identity.controller;

import com.taller2.microservices.identity.repository.IdentityMicroserviceRepository;
import com.taller2.negocio.model.EstadoUsuario;
import com.taller2.negocio.model.Rol;
import com.taller2.negocio.model.Usuario;
import com.taller2.seguridad.Argon2PasswordHasher;
import com.taller2.seguridad.PasswordHasher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Contexto: Gestión de Usuarios (Identity & Access)", description = "Microservicio para autenticación, roles y gestión de usuarios")
@CrossOrigin(origins = "*")
public class IdentityRestController {

    private final IdentityMicroserviceRepository repository;
    private final PasswordHasher passwordHasher = new Argon2PasswordHasher();

    public IdentityRestController(IdentityMicroserviceRepository repository) {
        this.repository = repository;
    }

    public record LoginRequest(String correo, String password) {}
    public record RegisterRequest(String nombre, String correo, String password, String rol, String estado) {}
    public record UpdateUserRequest(String nombre, String rol, String estado) {}
    public record UserResponse(int id, String login, String nombreCompleto, String rol, String estado) {}

    @PostMapping("/login")
    @Operation(summary = "Autenticación de usuario", description = "Valida credenciales y retorna datos de usuario y rol")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Optional<Usuario> opt = repository.findByLogin(req.correo());
        if (opt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales incorrectas"));
        }
        Usuario u = opt.get();
        if (u.getEstado() != EstadoUsuario.ACTIVO || !passwordMatches(req.password(), u.getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales incorrectas"));
        }
        return ResponseEntity.ok(Map.of(
                "id", u.getId(),
                "nombre", u.getNombreCompleto(),
                "correo", u.getLogin(),
                "rol", u.getRol().name(),
                "token", "jwt-mock-token-" + u.getId()
        ));
    }

    @PostMapping("/register")
    @Operation(summary = "Registro de usuario", description = "Registra un nuevo autor, revisor o administrador")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (repository.findByLogin(req.correo()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El correo ya está registrado"));
        }
        if (req.rol() == null || req.password() == null || req.password().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nombre, correo, contraseña y rol son obligatorios"));
        }
        Rol rolEnum;
        try {
            rolEnum = Rol.valueOf(req.rol().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "El rol indicado no es válido"));
        }

        EstadoUsuario userState;
        try {
            userState = req.estado() == null || req.estado().isBlank()
                    ? EstadoUsuario.ACTIVO
                    : EstadoUsuario.valueOf(req.estado().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "El estado indicado no es válido"));
        }
        Usuario u = new Usuario(req.correo(), req.nombre(), rolEnum, userState, req.password());
        Usuario guardado = repository.save(u);
        return ResponseEntity.ok(Map.of(
                "id", guardado.getId(),
                "nombre", guardado.getNombreCompleto(),
                "correo", guardado.getLogin(),
                "rol", guardado.getRol().name()
        ));
    }

    @GetMapping("/users/reviewers")
    @Operation(summary = "Listar revisores disponibles (HU4)", description = "Retorna los docentes con rol REVISOR para ser asignados por el Administrador")
    public ResponseEntity<List<Map<String, Object>>> getReviewers() {
        List<Usuario> revisores = repository.findByRol(Rol.REVISOR);
        List<Map<String, Object>> resp = new ArrayList<>();
        for (Usuario r : revisores) {
            resp.add(Map.of(
                    "id", r.getId(),
                    "nombre", r.getNombreCompleto(),
                    "correo", r.getLogin()
            ));
        }
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/users")
    @Operation(summary = "Listar todos los usuarios", description = "Lista usuarios registrados en identity.db")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = repository.findAll().stream()
                .map(user -> new UserResponse(
                        user.getId(), user.getLogin(), user.getNombreCompleto(),
                        user.getRol().name(), user.getEstado().name()
                ))
                .toList();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{id}")
    @Operation(summary = "Actualizar usuario")
    public ResponseEntity<?> updateUser(@PathVariable int id, @RequestBody UpdateUserRequest request) {
        if (request.nombre() == null || request.nombre().isBlank()
                || request.rol() == null || request.estado() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nombre, rol y estado son obligatorios"));
        }
        try {
            repository.update(id, request.nombre(), Rol.valueOf(request.rol()),
                    EstadoUsuario.valueOf(request.estado()));
            return repository.findById(id)
                    .<ResponseEntity<?>>map(user -> ResponseEntity.ok(new UserResponse(
                            user.getId(), user.getLogin(), user.getNombreCompleto(),
                            user.getRol().name(), user.getEstado().name()
                    )))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("error", "El rol o estado no es válido"));
        }
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Eliminar usuario")
    public ResponseEntity<Void> deleteUser(@PathVariable int id) {
        if (repository.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        repository.delete(id);
        return ResponseEntity.noContent().build();
    }

    private boolean passwordMatches(String supplied, String stored) {
        if (supplied == null || stored == null) {
            return false;
        }
        if (stored.startsWith("$argon2")) {
            return passwordHasher.verificar(supplied, stored);
        }
        return stored.equals(supplied);
    }
}
