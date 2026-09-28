package ui.swing;

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

import model.Question;
import model.QuestionStatus;
import model.Rol;
import service.QuestionService;

public class PreguntasView extends JFrame {
    private final QuestionService service;
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
    private final Rol rol;
    private final StatisticsView statisticsView;
    private final PieChartView pieChartView;

    public PreguntasView(QuestionService service, Rol rol) {
        this.service = service;
        this.rol = rol;
        statisticsView = new StatisticsView(service);
        pieChartView = new PieChartView(service);
        initComponents();
        configurarRenderizadorEstados();
        cargarPreguntas();
    }

    private void initComponents() {
        setTitle("Banco de preguntas Saber Pro - DCE");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        questions.addActionListener(e -> cargarPreguntaSeleccionada());
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

        if (rol == Rol.ADMINISTRADOR) {
            agregarCampo(form, "Email Revisor Asignado:", reviewerEmail, row++);
        }

        // Botones de acción
        JButton save = new JButton("Guardar / Validar");
        save.addActionListener(e -> guardar());

        JButton updateState = new JButton("Actualizar Estado");
        updateState.addActionListener(e -> actualizarEstado());

        JPanel buttons = new JPanel();
        if (rol == Rol.ADMINISTRADOR) {
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
            int totalResultados = service.contarPorFiltro(searchFilter.getText());
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
                else if (value == QuestionStatus.ARVHIVADA) setForeground(new Color(128, 0, 128));
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

    private void cargarPreguntas() {
        String filtro = searchFilter.getText().trim();
        List<Question> paginadas = service.listarPaginado(filtro, currentPage, pageSize);
        int totalResultados = service.contarPorFiltro(filtro);
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
        correctOption.removeAllItems();
        for (int i = 0; i < size; i++) correctOption.addItem(i);
        if (size > 0) correctOption.setSelectedItem(Math.min(selected, size - 1));
    }

    private List<String> leerOpciones() {
        return new ArrayList<>(Arrays.asList(options.getText().split("\\|", -1)));
    }

    private void guardar() {
        try {
            List<String> questionOptions = leerOpciones();
            
            if (questionOptions.size() != 4) {
                throw new IllegalArgumentException("Debe ingresar exactamente 4 distractores separados por |");
            }
            if (context.getText().isBlank() || text.getText().isBlank() || justification.getText().isBlank()) {
                throw new IllegalArgumentException("Contexto, Pregunta y Justificación son obligatorios.");
            }
            if (competency.getText().isBlank() || topic.getText().isBlank()) {
                throw new IllegalArgumentException("Debe definir Competencia y Tema.");
            }

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
                service.actualizar(nueva);
            } else {
                service.guardar(nueva);
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
        service.cambiarEstado(question, nuevoEstado);
        cargarPreguntas();
        JOptionPane.showMessageDialog(this, "Estado actualizado exitosamente.");
    }

    private void asignarRevisor() {
        Question question = (Question) questions.getSelectedItem();
        if (question == null) return;
        
        if (question.getEstado() != QuestionStatus.PENDIENTE_REVISION) {
            JOptionPane.showMessageDialog(this, "La pregunta debe estar en 'Pendiente de revisión' para asignar un revisor.");
            return;
        }
        
        String email = reviewerEmail.getText().trim();
        if (email.isBlank() || !email.contains("@")) {
            JOptionPane.showMessageDialog(this, "Ingrese un email válido del revisor.");
            return;
        }

        service.asignarRevisorYNotificar(question, email);
        JOptionPane.showMessageDialog(this, "Revisor asignado. Email de notificación enviado a: " + email);
    }
}