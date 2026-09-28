package com.taller2.presentacion.swing;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.taller2.negocio.model.EstadoUsuario;
import com.taller2.negocio.model.Rol;
import com.taller2.negocio.model.Usuario;
import com.taller2.negocio.service.UsuarioService;
import com.taller2.presentacion.controller.UsuarioController;

public class RegistroView extends JFrame {
    private final UsuarioService usuarioService;
    private final UsuarioController usuarioController;
    private final JTextField login = new JTextField(18);
    private final JTextField nombre = new JTextField(18);
    private final JComboBox<Rol> rol = new JComboBox<>(Rol.values());
    private final JComboBox<EstadoUsuario> estado = new JComboBox<>(EstadoUsuario.values());
    private final JPasswordField password = new JPasswordField(18);
    private final JPasswordField confirmacion = new JPasswordField(18);

    public RegistroView(UsuarioService usuarioService) {
        this(new UsuarioController(usuarioService));
    }

    public RegistroView(UsuarioController usuarioController) {
        this.usuarioController = usuarioController;
        this.usuarioService = usuarioController.getUsuarioService();
        estado.setSelectedItem(EstadoUsuario.ACTIVO);
        initComponents();
    }

    private void initComponents() {
        setTitle("Registrar usuario");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        String[] labels = {"Usuario:", "Nombre completo:", "Rol:", "Estado:",
                "Contraseña:", "Confirmar contraseña:"};
        Component[] fields = {login, nombre, rol, estado, password, confirmacion};
        for (int i = 0; i < labels.length; i++) {
            GridBagConstraints c = constraint(0, i);
            panel.add(new JLabel(labels[i]), c);
            c = constraint(1, i);
            panel.add(fields[i], c);
        }
        JButton registrar = new JButton("Registrar");
        registrar.addActionListener(e -> registrar());
        JButton volver = new JButton("Volver");
        volver.addActionListener(e -> {
            new LoginView(usuarioService).setVisible(true);
            dispose();
        });
        GridBagConstraints c = constraint(0, labels.length);
        panel.add(registrar, c);
        c = constraint(1, labels.length);
        panel.add(volver, c);
        setContentPane(panel);
        pack();
        setLocationRelativeTo(null);
    }

    private GridBagConstraints constraint(int x, int y) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x; c.gridy = y; c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        return c;
    }

    private void registrar() {
        String passwordValue = new String(password.getPassword());
        if (!passwordValue.equals(new String(confirmacion.getPassword()))) {
            JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden.");
            return;
        }

        Usuario nuevoUsuario = new Usuario(
                login.getText().trim(),
                nombre.getText().trim(),
                (Rol) rol.getSelectedItem(),
                (EstadoUsuario) estado.getSelectedItem(),
                null
        );

        usuarioController.registrarUsuario(
                nuevoUsuario,
                passwordValue,
                () -> {
                    JOptionPane.showMessageDialog(this, "Usuario registrado correctamente.");
                    new LoginView(usuarioService).setVisible(true);
                    dispose();
                },
                error -> JOptionPane.showMessageDialog(this, error,
                        "Registro no válido", JOptionPane.WARNING_MESSAGE)
        );
    }
}
