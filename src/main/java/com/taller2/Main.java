package com.taller2;

import javax.swing.SwingUtilities;

import com.taller2.persistencia.remote.MicroserviceIdentityClient;
import com.taller2.persistencia.remote.MicroserviceQuestionRepository;
import com.taller2.persistencia.remote.MicroserviceReviewClient;
import com.taller2.persistencia.remote.RemoteServiceUrls;
import com.taller2.seguridad.Argon2PasswordHasher;
import com.taller2.negocio.service.QuestionService;
import com.taller2.negocio.service.UsuarioService;
import com.taller2.presentacion.swing.LoginView;
import com.taller2.negocio.validation.PasswordValidator;
import com.taller2.negocio.validation.QuestionValidator;
import com.taller2.presentacion.controller.QuestionController;
import com.taller2.presentacion.controller.ReviewController;

public class Main {
    public static void main(String[] args) {
        RemoteServiceUrls serviceUrls = RemoteServiceUrls.localDefaults();
        MicroserviceIdentityClient identityClient = new MicroserviceIdentityClient(serviceUrls.identity());
        MicroserviceReviewClient reviewClient = new MicroserviceReviewClient(serviceUrls.reviews());

        var usuarioRepository = identityClient;
        PasswordValidator passwordValidator = new PasswordValidator();
        Argon2PasswordHasher passwordHasher = new Argon2PasswordHasher();

        UsuarioService usuarioService = new UsuarioService(
                usuarioRepository,
                passwordValidator,
                passwordHasher,
                identityClient
        );
        QuestionService questionService = new QuestionService(
            new MicroserviceQuestionRepository(serviceUrls.questions()),
            new QuestionValidator(),
            reviewClient
        );
        QuestionController questionController = new QuestionController(questionService);
        ReviewController reviewController = new ReviewController(reviewClient);

        SwingUtilities.invokeLater(() -> {
            LoginView loginView = new LoginView(
                    new com.taller2.presentacion.controller.UsuarioController(usuarioService),
                    questionController,
                    reviewController
            );
            loginView.setVisible(true);
        });
    }
}