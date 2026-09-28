package ui.swing;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import model.QuestionStatus;
import service.QuestionService;

public class PieChartView extends JPanel implements PropertyChangeListener {
    private final QuestionService service;
    private final ChartPanel chartPanel;

    public PieChartView(QuestionService service) {
        this.service = service;
        service.addPropertyChangeListener(this);
        setLayout(new BorderLayout());
        chartPanel = new ChartPanel(crearGrafico());
        add(chartPanel, BorderLayout.CENTER);
    }

    private JFreeChart crearGrafico() {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        dataset.setValue("Borrador", service.listarPorEstado(QuestionStatus.BORRADOR).size());
        dataset.setValue("Pendiente revisión", service.listarPorEstado(QuestionStatus.PENDIENTE_REVISION).size());
        dataset.setValue("Rechazada", service.listarPorEstado(QuestionStatus.RECHAZADA).size());
        dataset.setValue("Aprobada", service.listarPorEstado(QuestionStatus.APROBADA).size());
        dataset.setValue("En revisión", service.listarPorEstado(QuestionStatus.EN_REVISION).size());
        dataset.setValue("Publicada", service.listarPorEstado(QuestionStatus.PUBLICADA).size());
        dataset.setValue("Archivada", service.listarPorEstado(QuestionStatus.ARVHIVADA).size());
        return ChartFactory.createPieChart("Distribución de preguntas", dataset, true, true, false);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (QuestionService.STATUS_PROPERTY.equals(event.getPropertyName())) {
            chartPanel.setChart(crearGrafico());
        }
    }
}
