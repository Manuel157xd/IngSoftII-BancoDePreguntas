package com.taller2.presentacion.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.taller2.negocio.model.Question;
import com.taller2.negocio.model.QuestionStatus;
import com.taller2.negocio.model.Usuario;
import com.taller2.negocio.model.Rol;
import com.taller2.presentacion.controller.QuestionController;

public class PreguntasView extends JFrame {
    private final QuestionController controller;
    private final Usuario usuario;
    private final JComboBox<Question> questions = new JComboBox<>();
    private final JTextField id = new JTextField();
    private final JTextField name = new JTextField();
    
    // Campos Diseño Centrado en Evidencia (DCE)
    private final JTextArea context = new JTextArea(3, 30);
    private final JTextArea text = new JTextArea(3, 30);
    private final JTextField options = new JTextField();
    private final JComboBox<Integer> correctOption = new JComboBox<>();
    private final JTextArea justification = new JTextArea(2, 30);
    private final JTextField bibliography = new JTextField();
    private final JTextField competency = new JTextField();
    private final JTextField topic = new JTextField();
    private final JTextField subtopic = new JTextField();
    private final JComboBox<String> difficulty = new JComboBox<>(new String[]{"Bajo", "Medio", "Alto"});
    
    private final JComboBox<QuestionStatus> status = new JComboBox<>(QuestionStatus.values());
    private final JTextField reviewerEmail = new JTextField();
    
    // Paginación y Filtros
    private int currentPage = 0;
    private final int pageSize = 10;
    private final JTextField searchFilter = new JTextField(15);
    private final JLabel pageLabel = new JLabel("Pág 1");
    private final StatisticsView statisticsView;
    private final PieChartView pieChartView;

    public PreguntasView(QuestionController controller, Usuario usuario) {
        this.controller = controller;
        this.usuario = usuario;
        statisticsView = new StatisticsView(controller, usuario);
        pieChartView = new PieChartView(controller, usuario);
        initComponents();
        configurarRenderizadorEstados();
        cargarPreguntas();
    }

    private void initComponents() {
        setTitle(usuario.getRol() == Rol.AUTOR_PREGUNTAS
            ? "Mis preguntas Saber Pro - DCE"
            : "Banco de preguntas Saber Pro - DCE");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        questions.addActionListener(e -> cargarPreguntaSeleccionada());
        vincularOpcionesCorrectas(options, correctOption);
        id.setEditable(false);

        // Panel superior: Filtros y selección
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Filtro (Tema/Competencia/Texto):"));
        top.add(searchFilter);
        
        JButton btnSearch = new JButton("Buscar");
        btnSearch.addActionListener(e -> { 
            currentPage = 0; 
            cargarPreguntas(); 
        });
        top.add(btnSearch);
        
        JButton btnClearFilter = new JButton("Limpiar Filtro");
        btnClearFilter.addActionListener(e -> {
            searchFilter.setText("");
            currentPage = 0;
            cargarPreguntas();
        });
        top.add(btnClearFilter);

        top.add(new JLabel(" | Pregunta:"));
        top.add(questions);
        JButton nueva = new JButton("Nueva");
        nueva.addActionListener(e -> limpiarFormulario());
        top.add(nueva);

        // Formulario central
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        int row = 0;
        agregarCampo(form, "Id:", id, row++);
        agregarCampo(form, "Nombre:", name, row++);
        agregarCampo(form, "Competencia:", competency, row++);
        agregarCampo(form, "Tema:", topic, row++);
        agregarCampo(form, "Subtema:", subtopic, row++);
        agregarCampo(form, "Nivel Dificultad:", difficulty, row++);
        agregarCampo(form, "Contexto:", new JScrollPane(context), row++);
        agregarCampo(form, "Pregunta Directa:", new JScrollPane(text), row++);
        agregarCampo(form, "4 Distractores (separados por |):", options, row++);
        agregarCampo(form, "Respuesta correcta (0-3):", correctOption, row++);
        agregarCampo(form, "Justificación:", new JScrollPane(justification), row++);
        agregarCampo(form, "Bibliografía:", bibliography, row++);
        agregarCampo(form, "Estado:", status, row++);

        if (controller.puedeAsignarRevisor(usuario)) {
            agregarCampo(form, "Email Revisor Asignado:", reviewerEmail, row++);
        }

        // Botones de acción
        JButton save = new JButton("Guardar / Validar");
        save.addActionListener(e -> guardar());

        JButton updateState = new JButton("Actualizar Estado");
        updateState.addActionListener(e -> actualizarEstado());

        JPanel buttons = new JPanel();
        if (controller.puedeAsignarRevisor(usuario)) {
            JButton assignReviewer = new JButton("Asignar Revisor");
            assignReviewer.addActionListener(e -> asignarRevisor());
            buttons.add(assignReviewer);
        }
        buttons.add(save);
        buttons.add(updateState);

        GridBagConstraints bc = new GridBagConstraints();
        bc.gridx = 1; bc.gridy = row; bc.insets = new Insets(5, 5, 5, 5);
        form.add(buttons, bc);

        // Panel inferior: Estadísticas y Paginación
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton prev = new JButton("< Anterior");
        prev.addActionListener(e -> { 
            if (currentPage > 0) { 
                currentPage--; 
                cargarPreguntas(); 
            } 
        });
        JButton next = new JButton("Siguiente >");
        next.addActionListener(e -> { 
            int totalResultados = controller.contarPorFiltro(usuario, searchFilter.getText());
            int totalPaginas = (int) Math.ceil((double) totalResultados / pageSize);
            if (currentPage + 1 < totalPaginas) {
                currentPage++; 
                cargarPreguntas(); 
            }
        });
        paginationPanel.add(prev);
        paginationPanel.add(pageLabel);
        paginationPanel.add(next);
        
        JPanel observers = new JPanel(new GridLayout(1, 2));
        observers.add(statisticsView);
        observers.add(pieChartView);
        
        bottomPanel.add(observers, BorderLayout.CENTER);
        bottomPanel.add(paginationPanel, BorderLayout.SOUTH);

        JScrollPane scrollForm = new JScrollPane(form);
        JSplitPane center = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollForm, bottomPanel);
        center.setResizeWeight(0.75);
        
        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        setSize(1000, 800);
        setLocationRelativeTo(null);
    }

    private void configurarRenderizadorEstados() {
        status.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == QuestionStatus.BORRADOR) setForeground(Color.GRAY);
                else if (value == QuestionStatus.PENDIENTE_REVISION) setForeground(new Color(204, 102, 0));
                else if (value == QuestionStatus.RECHAZADA) setForeground(Color.RED);
                else if (value == QuestionStatus.APROBADA) setForeground(Color.GREEN);
                else if (value == QuestionStatus.EN_REVISION) setForeground(Color.BLUE);
                else if (value == QuestionStatus.PUBLICADA) setForeground(new Color(0, 128, 128));
                else if (value == QuestionStatus.ARCHIVADA) setForeground(new Color(128, 0, 128));
                else setForeground(Color.BLACK);
                
                if (isSelected) setForeground(Color.WHITE);
                return c;
            }
        });
    }

    private void agregarCampo(JPanel panel, String label, Component component, int row) {
        GridBagConstraints left = new GridBagConstraints();
        left.gridx = 0; left.gridy = row; left.anchor = GridBagConstraints.NORTHWEST;
        left.insets = new Insets(5, 5, 5, 5);
        panel.add(new JLabel(label), left);
        GridBagConstraints right = new GridBagConstraints();
        right.gridx = 1; right.gridy = row; right.weightx = 1; right.fill = GridBagConstraints.HORIZONTAL;
        right.insets = new Insets(5, 5, 5, 5);
        panel.add(component, right);
    }

    static void seleccionarPrimeraOpcionSiExiste(JComboBox<?> combo) {
        if (combo != null && combo.getItemCount() > 0) {
            combo.setSelectedIndex(0);
        }
    }

    static void vincularOpcionesCorrectas(JTextField options, JComboBox<Integer> correctOption) {
        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                actualizarDesdeTexto();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                actualizarDesdeTexto();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                actualizarDesdeTexto();
            }

            private void actualizarDesdeTexto() {
                long count = Arrays.stream(options.getText().split("\\|", -1))
                        .filter(option -> !option.isBlank())
                        .count();
                actualizarOpcionesCorrectas(correctOption, (int) count,
                        correctOption.getSelectedIndex());
            }
        };
        options.getDocument().addDocumentListener(listener);
        listener.insertUpdate(null);
    }

    private void cargarPreguntas() {
        String filtro = searchFilter.getText().trim();
        List<Question> paginadas = controller.listarPaginado(usuario, filtro, currentPage, pageSize);
        int totalResultados = controller.contarPorFiltro(usuario, filtro);
        int totalPaginas = (int) Math.ceil((double) totalResultados / pageSize);
        if (totalPaginas == 0) totalPaginas = 1;

        questions.setModel(new DefaultComboBoxModel<>(paginadas.toArray(new Question[0])));
        pageLabel.setText("Pág " + (currentPage + 1) + " de " + totalPaginas);
        
        if (questions.getItemCount() > 0) {
            questions.setSelectedIndex(0);
        } else {
            limpiarFormulario();
        }
    }

    private void cargarPreguntaSeleccionada() {
        Question question = (Question) questions.getSelectedItem();
        if (question == null) return;

        id.setText(String.valueOf(question.getId()));
        name.setText(question.getNombre() != null ? question.getNombre() : "");
        text.setText(question.getTexto() != null ? question.getTexto() : "");
        
        List<String> opciones = question.getOpciones();
        if (opciones != null) {
            options.setText(String.join("|", opciones));
            actualizarOpcionesCorrectas(opciones.size(), question.getRespuestaCorrecta());
        } else {
            options.setText("");
            actualizarOpcionesCorrectas(0, 0);
        }

        status.setSelectedItem(question.getEstado());

        context.setText(question.getContexto() != null ? question.getContexto() : "");
        competency.setText(question.getCompetencia() != null ? question.getCompetencia() : "");
        topic.setText(question.getTema() != null ? question.getTema() : "");
        subtopic.setText(question.getSubtema() != null ? question.getSubtema() : "");
        difficulty.setSelectedItem(question.getNivelDificultad());
        justification.setText(question.getJustificacion() != null ? question.getJustificacion() : "");
        bibliography.setText(question.getBibliografia() != null ? question.getBibliografia() : "");
        reviewerEmail.setText(question.getRevisorAsignado() != null ? question.getRevisorAsignado() : "");
    }

    private void limpiarFormulario() {
        questions.setSelectedItem(null);
        id.setText(""); name.setText(""); text.setText(""); options.setText("");
        context.setText(""); justification.setText(""); bibliography.setText("");
        competency.setText(""); topic.setText(""); subtopic.setText(""); reviewerEmail.setText("");
        seleccionarPrimeraOpcionSiExiste(difficulty);
        actualizarOpcionesCorrectas(0, 0);
        if (status.getItemCount() > 0) {
            status.setSelectedItem(QuestionStatus.BORRADOR);
        }
    }

    private void actualizarOpcionesCorrectas(int size, int selected) {
        actualizarOpcionesCorrectas(correctOption, size, selected);
    }

    private static void actualizarOpcionesCorrectas(JComboBox<Integer> correctOption,
            int size, int selected) {
        int previousSelection = selected;
        correctOption.removeAllItems();
        for (int i = 0; i < size; i++) {
            correctOption.addItem(i);
        }
        if (size > 0) {
            correctOption.setSelectedIndex(Math.min(Math.max(previousSelection, 0), size - 1));
        }
    }

    private List<String> leerOpciones() {
        return new ArrayList<>(Arrays.asList(options.getText().split("\\|", -1)));
    }

    private void guardar() {
        try {
            List<String> questionOptions = leerOpciones();

            int selectedOption = correctOption.getSelectedItem() instanceof Integer
                    ? (Integer) correctOption.getSelectedItem() : 0;
            actualizarOpcionesCorrectas(questionOptions.size(), selectedOption);
            
            Question nueva = new Question(
                    name.getText().trim(),
                    text.getText().trim(),
                    questionOptions,
                    selectedOption,
                    (QuestionStatus) status.getSelectedItem(),
                    context.getText().trim(),
                    justification.getText().trim(),
                    bibliography.getText().trim(),
                    competency.getText().trim(),
                    topic.getText().trim(),
                    subtopic.getText().trim(),
                    (String) difficulty.getSelectedItem(),
                    reviewerEmail.getText().trim()
            );

            if (!id.getText().isBlank()) {
                nueva.setId(Integer.parseInt(id.getText()));
                controller.actualizar(usuario, nueva);
            } else {
                controller.guardar(usuario, nueva);
            }
            
            cargarPreguntas();
            JOptionPane.showMessageDialog(this, "Pregunta guardada correctamente.");
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Error de Validación",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void actualizarEstado() {
        Question question = (Question) questions.getSelectedItem();
        if (question == null) return;
       
        QuestionStatus nuevoEstado = (QuestionStatus) status.getSelectedItem();
        try {
            controller.cambiarEstado(usuario, question, nuevoEstado);
            cargarPreguntas();
            JOptionPane.showMessageDialog(this, "Estado actualizado exitosamente.");
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "No se pudo actualizar",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void asignarRevisor() {
        Question question = (Question) questions.getSelectedItem();
        if (question == null) return;
        
        String email = reviewerEmail.getText().trim();
        try {
            controller.asignarRevisor(usuario, question, email);
            JOptionPane.showMessageDialog(this, "Revisor asignado. Email de notificación enviado a: " + email);
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "No se pudo asignar el revisor",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
}