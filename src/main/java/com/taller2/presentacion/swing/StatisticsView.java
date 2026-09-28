package com.taller2.presentacion.swing;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.taller2.negocio.model.QuestionStatus;
import com.taller2.negocio.model.Usuario;
import com.taller2.presentacion.controller.QuestionController;

public class StatisticsView extends JPanel implements PropertyChangeListener {
    private final QuestionController controller;
    private final Usuario usuario;
    private final JLabel statsLabel = new JLabel();

    public StatisticsView(QuestionController controller, Usuario usuario) {
        this.controller = controller;
        this.usuario = usuario;
        controller.addPropertyChangeListener(this);
        setLayout(new BorderLayout());
        add(statsLabel, BorderLayout.CENTER);
        actualizarEstadisticas();
    }

    private void actualizarEstadisticas() {
        Map<QuestionStatus, Integer> counts = new EnumMap<>(QuestionStatus.class);
        for (QuestionStatus status : QuestionStatus.values()) {
            counts.put(status, controller.listarPorEstado(usuario, status).size());
        }
        statsLabel.setText(String.format("<html>Borrador: %d<br>Pendiente revisión: %d<br>En revisión: %d<br>Rechazada: %d<br>Aprobada: %d<br>Publicada: %d<br>Archivada: %d",
                counts.get(QuestionStatus.BORRADOR),
                counts.get(QuestionStatus.PENDIENTE_REVISION),
                counts.get(QuestionStatus.EN_REVISION),
                counts.get(QuestionStatus.RECHAZADA),
                counts.get(QuestionStatus.APROBADA),
                counts.get(QuestionStatus.PUBLICADA),
                counts.get(QuestionStatus.ARCHIVADA)));
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (QuestionController.STATUS_PROPERTY.equals(event.getPropertyName())) {
            actualizarEstadisticas();
        }
    }
}
