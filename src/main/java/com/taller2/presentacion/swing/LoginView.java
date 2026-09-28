package com.taller2.presentacion.swing;

import com.taller2.persistencia.repository.QuestionRepositorySQLite;
import com.taller2.negocio.service.QuestionService;
import com.taller2.negocio.service.UsuarioService;
import com.taller2.presentacion.controller.UsuarioController;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class LoginView extends JFrame {
    private final UsuarioService usuarioService;
    private final UsuarioController usuarioController;
    private final QuestionService questionService;
    private final JTextField loginField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);

    public LoginView(UsuarioService usuarioService) {
        this(usuarioService, new QuestionService(new QuestionRepositorySQLite()));
    }

    public LoginView(UsuarioService usuarioService, QuestionService questionService) {
        this(new UsuarioController(usuarioService), questionService);
    }

    public LoginView(UsuarioController usuarioController, QuestionService questionService) {
        this.usuarioController = usuarioController;
        this.usuarioService = usuarioController.getUsuarioService();
        this.questionService = questionService;
        initComponents();
    }

    private void initComponents() {
        setTitle("Taller 2 - Inicio de sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(crearPanel());
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel crearPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        panel.add(new JLabel("INICIAR SESIÓN"), c);
        c.gridwidth = 1;
        c.gridx = 0; c.gridy++;
        panel.add(new JLabel("Usuario:"), c);
        c.gridx = 1;
        panel.add(loginField, c);
        c.gridx = 0; c.gridy++;
        panel.add(new JLabel("Contraseña:"), c);
        c.gridx = 1;
        panel.add(passwordField, c);

        JButton loginButton = new JButton("Iniciar sesión");
        loginButton.addActionListener(e -> iniciarSesion());
        JButton registerButton = new JButton("Registrarse");
        registerButton.addActionListener(e -> {
            new RegistroView(usuarioService).setVisible(true);
            dispose();
        });
        c.gridx = 0; c.gridy++;
        panel.add(loginButton, c);
        c.gridx = 1;
        panel.add(registerButton, c);
        return panel;
    }

    private void iniciarSesion() {
        String login = loginField.getText().trim();
        String password = new String(passwordField.getPassword());

        usuarioController.iniciarSesion(login, password,
                usuario -> {
                    new DashboardView(usuario, usuarioService, questionService).setVisible(true);
                    dispose();
                },
                error -> JOptionPane.showMessageDialog(this, error,
                        "No se pudo iniciar sesión", JOptionPane.WARNING_MESSAGE)
        );
    }
}
