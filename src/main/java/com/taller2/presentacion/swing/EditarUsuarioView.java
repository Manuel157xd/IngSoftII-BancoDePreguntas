package com.taller2.presentacion.swing;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.taller2.negocio.model.EstadoUsuario;
import com.taller2.negocio.model.Rol;
import com.taller2.negocio.model.Usuario;
import com.taller2.negocio.service.UsuarioService;

public class EditarUsuarioView extends JFrame {
    public EditarUsuarioView(Usuario usuario, UsuarioService service, Runnable onSaved) {
        setTitle("Editar usuario");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JTextField name = new JTextField(usuario.getNombreCompleto());
        JComboBox<Rol> role = new JComboBox<>(Rol.values());
        role.setSelectedItem(usuario.getRol());
        JComboBox<EstadoUsuario> state = new JComboBox<>(EstadoUsuario.values());
        state.setSelectedItem(usuario.getEstado());
        JButton save = new JButton("Guardar");
        save.addActionListener(e -> {
            usuario.setNombreCompleto(name.getText().trim());
            usuario.setRol((Rol) role.getSelectedItem());
            usuario.setEstado((EstadoUsuario) state.getSelectedItem());
            service.actualizarUsuario(usuario);
            onSaved.run();
            dispose();
        });
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("Nombre completo:")); panel.add(name);
        panel.add(new JLabel("Rol:")); panel.add(role);
        panel.add(new JLabel("Estado:")); panel.add(state);
        panel.add(new JLabel()); panel.add(save);
        add(panel);
        setSize(400, 220);
        setLocationRelativeTo(null);
    }
}
