package com.taller2.presentacion.controller;

import java.util.function.Consumer;

import com.taller2.negocio.model.Usuario;
import com.taller2.negocio.service.UsuarioService;

public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    public UsuarioService getUsuarioService() {
        return usuarioService;
    }

    public void registrarUsuario(Usuario usuario, String password, Runnable onSuccess, Consumer<String> onError) {
        try {
            usuarioService.registrarUsuario(usuario, password);
            if (onSuccess != null) {
                onSuccess.run();
            }
        } catch (IllegalArgumentException ex) {
            if (onError != null) {
                onError.accept(ex.getMessage());
            }
        }
    }

    public void iniciarSesion(String login, String password, Consumer<Usuario> onSuccess, Consumer<String> onError) {
        try {
            Usuario usuario = usuarioService.iniciarSesion(login, password);
            if (onSuccess != null) {
                onSuccess.accept(usuario);
            }
        } catch (IllegalArgumentException ex) {
            if (onError != null) {
                onError.accept(ex.getMessage());
            }
        }
    }
}
