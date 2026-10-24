package com.taller2.presentacion.swing;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.taller2.negocio.model.Usuario;
import com.taller2.negocio.service.UsuarioService;
import com.taller2.presentacion.controller.QuestionController;
import com.taller2.presentacion.controller.ReviewController;
import com.taller2.presentacion.controller.NotificationController;

public class DashboardView extends JFrame {
    private final Usuario usuario;
    private final UsuarioService usuarioService;
    private final QuestionController questionController;
    private final ReviewController reviewController;
    private final NotificationController notificationController;

    public DashboardView(Usuario usuario, UsuarioService usuarioService,
                         QuestionController questionController) {
        this(usuario, usuarioService, questionController, null);
    }

    public DashboardView(Usuario usuario, UsuarioService usuarioService,
                         QuestionController questionController, ReviewController reviewController) {
        this(usuario, usuarioService, questionController, reviewController, null);
    }

    public DashboardView(Usuario usuario, UsuarioService usuarioService,
                         QuestionController questionController, ReviewController reviewController,
                         NotificationController notificationController) {
        this.usuario = usuario;
        this.usuarioService = usuarioService;
        this.questionController = questionController;
        this.reviewController = reviewController;
        this.notificationController = notificationController;
        initComponents();
    }

    private void initComponents() {
        setTitle("Panel principal - " + usuario.getRol());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
        panel.add(new JLabel("Bienvenido, " + usuario.getNombreCompleto()));
        panel.add(new JLabel("Rol: " + usuario.getRol()));

        JButton preguntas = new JButton("Banco de preguntas");
        preguntas.addActionListener(e -> new PreguntasView(questionController, usuario).setVisible(true));
        panel.add(preguntas);
        if (notificationController != null) {
            JButton notifications = new JButton("Notificaciones");
            notifications.addActionListener(e ->
                    new NotificationsView(usuario, notificationController).setVisible(true));
            panel.add(notifications);
        }
        if (usuario.getRol() == com.taller2.negocio.model.Rol.REVISOR && reviewController != null) {
            JButton reviews = new JButton("Mis preguntas asignadas");
            reviews.addActionListener(e -> new ReviewsView(usuario, reviewController).setVisible(true));
            panel.add(reviews);
        }
/* 
        if (kernel != null) {
            JButton plugins = new JButton("Ejecutar plugin");
            plugins.addActionListener(e -> new PluginExecutionView(kernel).setVisible(true));
            panel.add(plugins);
        }
*/
        if (usuario.getRol() == com.taller2.negocio.model.Rol.ADMINISTRADOR) {
            JButton usuarios = new JButton("Gestionar usuarios");
            usuarios.addActionListener(e -> new GestionUsuariosView(usuarioService).setVisible(true));
            panel.add(usuarios);
        }

        JButton cerrar = new JButton("Cerrar sesión");
        cerrar.addActionListener(e -> {
            new LoginView(
                    new com.taller2.presentacion.controller.UsuarioController(usuarioService),
                    questionController,
                    reviewController,
                    notificationController
            ).setVisible(true);
            dispose();
        });
        panel.add(cerrar);
        setContentPane(panel);
        setSize(420, 260);
        setLocationRelativeTo(null);
    }
}
