package com.taller2.presentacion.swing;

import com.taller2.negocio.model.Usuario;
import com.taller2.negocio.service.NotificationGateway;
import com.taller2.presentacion.controller.NotificationController;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;

public class NotificationsView extends JFrame {

    private final Usuario user;
    private final NotificationController controller;
    private final DefaultListModel<NotificationGateway.InboxNotification> model = new DefaultListModel<>();
    private final JList<NotificationGateway.InboxNotification> notifications = new JList<>(model);
    private final JTextArea details = new JTextArea();

    public NotificationsView(Usuario user, NotificationController controller) {
        this.user = user;
        this.controller = controller;
        setTitle("Notificaciones");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        details.setEditable(false);
        notifications.setCellRenderer((list, value, index, selected, focus) -> {
            DefaultListCellRenderer renderer = new DefaultListCellRenderer();
            Component component = renderer.getListCellRendererComponent(
                    list, value.read() ? value.subject() : "● " + value.subject(),
                    index, selected, focus
            );
            return component;
        });
        notifications.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                showSelectedNotification();
            }
        });

        JButton refresh = new JButton("Actualizar");
        refresh.addActionListener(event -> refreshNotifications());
        JButton markRead = new JButton("Marcar como leída");
        markRead.addActionListener(event -> markSelectedAsRead());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER));
        actions.add(refresh);
        actions.add(markRead);

        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        content.add(new JScrollPane(notifications), BorderLayout.WEST);
        content.add(new JScrollPane(details), BorderLayout.CENTER);
        content.add(actions, BorderLayout.SOUTH);
        setContentPane(content);
        setSize(760, 420);
        setLocationRelativeTo(null);
        refreshNotifications();
    }

    private void refreshNotifications() {
        try {
            model.clear();
            controller.listForUser(user).forEach(model::addElement);
            if (!model.isEmpty()) {
                notifications.setSelectedIndex(0);
            } else {
                details.setText("No tienes notificaciones.");
            }
        } catch (RuntimeException exception) {
            showError(exception);
        }
    }

    private void showSelectedNotification() {
        NotificationGateway.InboxNotification selected = notifications.getSelectedValue();
        if (selected == null) {
            details.setText("");
            return;
        }
        details.setText("""
                %s
                Fecha: %s
                Estado: %s

                %s
                """.formatted(
                selected.subject(), selected.createdAt(),
                selected.read() ? "Leída" : "No leída", selected.body()
        ));
    }

    private void markSelectedAsRead() {
        NotificationGateway.InboxNotification selected = notifications.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una notificación.");
            return;
        }
        if (selected.read()) {
            return;
        }
        try {
            controller.markAsRead(selected.id(), user);
            refreshNotifications();
        } catch (RuntimeException exception) {
            showError(exception);
        }
    }

    private void showError(RuntimeException exception) {
        JOptionPane.showMessageDialog(
                this, exception.getMessage(), "No se pudieron cargar las notificaciones",
                JOptionPane.WARNING_MESSAGE
        );
    }
}
