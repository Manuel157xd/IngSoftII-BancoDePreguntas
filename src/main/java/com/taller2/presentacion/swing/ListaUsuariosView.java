package com.taller2.presentacion.swing;

import com.taller2.negocio.model.Usuario;
import com.taller2.negocio.service.UsuarioService;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;

public class ListaUsuariosView extends JFrame {
    public ListaUsuariosView(UsuarioService service) {
        setTitle("Usuarios registrados");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        DefaultListModel<Usuario> model = new DefaultListModel<>();
        service.listarUsuarios().forEach(model::addElement);
        add(new JScrollPane(new JList<>(model)), BorderLayout.CENTER);
        setSize(560, 360);
        setLocationRelativeTo(null);
    }
}
