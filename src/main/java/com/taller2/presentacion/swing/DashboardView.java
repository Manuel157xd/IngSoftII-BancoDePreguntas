package com.taller2.presentacion.swing;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.taller2.negocio.model.Usuario;
import com.taller2.negocio.service.QuestionService;
import com.taller2.negocio.service.UsuarioService;

public class DashboardView extends JFrame {
    private final Usuario usuario;
    private final UsuarioService usuarioService;
    private final QuestionService questionService;

    public DashboardView(Usuario usuario, UsuarioService usuarioService) {
        this(usuario, usuarioService, new QuestionService(new com.taller2.persistencia.repository.QuestionRepositorySQLite()));
    }

    public DashboardView(Usuario usuario, UsuarioService usuarioService,
                         QuestionService questionService) {
        this.usuario = usuario;
        this.usuarioService = usuarioService;
        this.questionService = questionService;
        initComponents();
    }

    private void initComponents() {
        setTitle("Panel principal - " + usuario.getRol());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
        panel.add(new JLabel("Bienvenido, " + usuario.getNombreCompleto()));
        panel.add(new JLabel("Rol: " + usuario.getRol()));

        JButton preguntas = new JButton("Banco de preguntas");
        preguntas.addActionListener(e -> new PreguntasView(questionService, usuario.getRol()).setVisible(true));
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
            new LoginView(usuarioService, questionService).setVisible(true);
            dispose();
        });
        panel.add(cerrar);
        setContentPane(panel);
        setSize(420, 260);
        setLocationRelativeTo(null);
    }
}
