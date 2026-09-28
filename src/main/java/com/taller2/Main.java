package com.taller2;

import javax.swing.SwingUtilities;

import com.taller2.persistencia.database.DatabaseInitializer;
import com.taller2.persistencia.repository.QuestionRepositorySQLite;
import com.taller2.persistencia.repository.UsuarioRepositorySQLite;
import com.taller2.seguridad.Argon2PasswordHasher;
import com.taller2.negocio.service.QuestionService;
import com.taller2.negocio.service.UsuarioService;
import com.taller2.presentacion.swing.LoginView;
import com.taller2.negocio.validation.PasswordValidator;
import com.taller2.negocio.validation.QuestionValidator;
import com.taller2.presentacion.controller.QuestionController;

public class Main {
    public static void main(String[] args) {
        DatabaseInitializer.initialize();

        UsuarioRepositorySQLite usuarioRepository = new UsuarioRepositorySQLite();
        PasswordValidator passwordValidator = new PasswordValidator();
        Argon2PasswordHasher passwordHasher = new Argon2PasswordHasher();

        UsuarioService usuarioService = new UsuarioService(
                usuarioRepository,
                passwordValidator,
                passwordHasher
        );
        QuestionService questionService = new QuestionService(
            new QuestionRepositorySQLite(),
            new QuestionValidator()
        );
        QuestionController questionController = new QuestionController(questionService);

        SwingUtilities.invokeLater(() -> {
            LoginView loginView = new LoginView(usuarioService, questionController);
            loginView.setVisible(true);
        });
    }
}