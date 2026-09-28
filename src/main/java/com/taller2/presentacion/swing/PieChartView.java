package com.taller2.presentacion.swing;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import com.taller2.negocio.model.QuestionStatus;
import com.taller2.negocio.model.Usuario;
import com.taller2.presentacion.controller.QuestionController;

public class PieChartView extends JPanel implements PropertyChangeListener {
    private final QuestionController controller;
    private final Usuario usuario;
    private final ChartPanel chartPanel;

    public PieChartView(QuestionController controller, Usuario usuario) {
        this.controller = controller;
        this.usuario = usuario;
        controller.addPropertyChangeListener(this);
        setLayout(new BorderLayout());
        chartPanel = new ChartPanel(crearGrafico());
        add(chartPanel, BorderLayout.CENTER);
    }

    private JFreeChart crearGrafico() {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        dataset.setValue("Borrador", controller.listarPorEstado(usuario, QuestionStatus.BORRADOR).size());
        dataset.setValue("Pendiente revisión", controller.listarPorEstado(usuario, QuestionStatus.PENDIENTE_REVISION).size());
        dataset.setValue("Rechazada", controller.listarPorEstado(usuario, QuestionStatus.RECHAZADA).size());
        dataset.setValue("Aprobada", controller.listarPorEstado(usuario, QuestionStatus.APROBADA).size());
        dataset.setValue("En revisión", controller.listarPorEstado(usuario, QuestionStatus.EN_REVISION).size());
        dataset.setValue("Publicada", controller.listarPorEstado(usuario, QuestionStatus.PUBLICADA).size());
        dataset.setValue("Archivada", controller.listarPorEstado(usuario, QuestionStatus.ARCHIVADA).size());
        return ChartFactory.createPieChart("Distribución de preguntas", dataset, true, true, false);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (QuestionController.STATUS_PROPERTY.equals(event.getPropertyName())) {
            chartPanel.setChart(crearGrafico());
        }
    }
}
