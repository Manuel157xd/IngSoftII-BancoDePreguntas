package com.taller2.microservices.identity.controller;

import com.taller2.microservices.identity.repository.IdentityMicroserviceRepository;
import com.taller2.negocio.model.EstadoUsuario;
import com.taller2.negocio.model.Rol;
import com.taller2.negocio.model.Usuario;
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

    public IdentityRestController(IdentityMicroserviceRepository repository) {
        this.repository = repository;
    }

    public record LoginRequest(String correo, String password) {}
    public record RegisterRequest(String nombre, String correo, String password, String rol) {}

    @PostMapping("/login")
    @Operation(summary = "Autenticación de usuario", description = "Valida credenciales y retorna datos de usuario y rol")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Optional<Usuario> opt = repository.findByLogin(req.correo());
        if (opt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales incorrectas"));
        }
        Usuario u = opt.get();
        if (!u.getPasswordHash().equals(req.password()) && !u.getPasswordHash().equals(req.correo())) {
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
        Rol rolEnum;
        try {
            rolEnum = Rol.valueOf(req.rol().toUpperCase());
        } catch (Exception e) {
            rolEnum = Rol.DOCENTE;
        }

        Usuario u = new Usuario(req.correo(), req.nombre(), rolEnum, EstadoUsuario.ACTIVO, req.password());
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
    public ResponseEntity<List<Usuario>> getAllUsers() {
        return ResponseEntity.ok(repository.findAll());
    }
}
