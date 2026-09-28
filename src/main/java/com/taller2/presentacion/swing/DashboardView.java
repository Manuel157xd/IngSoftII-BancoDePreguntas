package com.taller2.presentacion.swing;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.taller2.negocio.model.Usuario;
import com.taller2.negocio.service.UsuarioService;
import com.taller2.presentacion.controller.QuestionController;

public class DashboardView extends JFrame {
    private final Usuario usuario;
    private final UsuarioService usuarioService;
    private final QuestionController questionController;

    public DashboardView(Usuario usuario, UsuarioService usuarioService,
                         QuestionController questionController) {
        this.usuario = usuario;
        this.usuarioService = usuarioService;
        this.questionController = questionController;
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
            new LoginView(usuarioService, questionController).setVisible(true);
            dispose();
        });
        panel.add(cerrar);
        setContentPane(panel);
        setSize(420, 260);
        setLocationRelativeTo(null);
    }
}
