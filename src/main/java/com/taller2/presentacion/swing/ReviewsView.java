package com.taller2.presentacion.swing;

import com.taller2.negocio.model.Usuario;
import com.taller2.negocio.service.ReviewGateway;
import com.taller2.presentacion.controller.ReviewController;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

public class ReviewsView extends JFrame {

    private final Usuario reviewer;
    private final ReviewController controller;
    private final DefaultListModel<ReviewGateway.AssignedQuestion> model = new DefaultListModel<>();
    private final JList<ReviewGateway.AssignedQuestion> assignments = new JList<>(model);
    private final JTextArea details = new JTextArea();

    public ReviewsView(Usuario reviewer, ReviewController controller) {
        this.reviewer = reviewer;
        this.controller = controller;
        setTitle("Mis preguntas asignadas");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        details.setEditable(false);
        assignments.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                showSelectedQuestion();
            }
        });

        JButton refresh = new JButton("Actualizar");
        refresh.addActionListener(event -> refreshAssignments());
        JButton approve = new JButton("Aprobar");
        approve.addActionListener(event -> evaluate("APROBADA"));
        JButton reject = new JButton("Rechazar");
        reject.addActionListener(event -> evaluate("RECHAZADA"));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER));
        actions.add(refresh);
        actions.add(approve);
        actions.add(reject);

        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        content.add(new JScrollPane(assignments), BorderLayout.WEST);
        content.add(new JScrollPane(details), BorderLayout.CENTER);
        content.add(actions, BorderLayout.SOUTH);
        setContentPane(content);
        setSize(900, 500);
        setLocationRelativeTo(null);
        refreshAssignments();
    }

    private void refreshAssignments() {
        try {
            model.clear();
            controller.listAssignedQuestions(reviewer.getLogin()).forEach(model::addElement);
            if (!model.isEmpty()) {
                assignments.setSelectedIndex(0);
            } else {
                details.setText("No tienes preguntas asignadas.");
            }
        } catch (RuntimeException exception) {
            showError(exception);
        }
    }

    private void showSelectedQuestion() {
        ReviewGateway.AssignedQuestion question = assignments.getSelectedValue();
        if (question == null) {
            details.setText("");
            return;
        }
        details.setText("""
                Pregunta #%d: %s
                Estado: %s
                Competencia: %s
                Tema / subtema: %s / %s
                Dificultad: %s

                Contexto:
                %s

                Enunciado:
                %s

                Opciones:
                %s

                Respuesta correcta: %d
                Justificación: %s
                Bibliografía: %s
                Observaciones anteriores: %s
                """.formatted(
                question.questionId(), question.name(), question.status(),
                question.competency(), question.topic(), question.subtopic(),
                question.difficulty(), question.context(), question.statement(),
                String.join("\n", question.options()), question.correctAnswer(),
                question.justification(), question.bibliography(), question.observation()
        ));
    }

    private void evaluate(String decision) {
        ReviewGateway.AssignedQuestion question = assignments.getSelectedValue();
        if (question == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una pregunta asignada.");
            return;
        }
        String observation = JOptionPane.showInputDialog(
                this, "Escribe las observaciones para el autor:", "Evaluar pregunta",
                JOptionPane.PLAIN_MESSAGE
        );
        if (observation == null) {
            return;
        }
        if (observation.isBlank()) {
            JOptionPane.showMessageDialog(this, "Las observaciones son obligatorias.");
            return;
        }
        try {
            controller.evaluate(question.questionId(), reviewer.getLogin(), decision, observation.trim());
            JOptionPane.showMessageDialog(this, "Evaluación registrada.");
            refreshAssignments();
        } catch (RuntimeException exception) {
            showError(exception);
        }
    }

    private void showError(RuntimeException exception) {
        JOptionPane.showMessageDialog(
                this, exception.getMessage(), "No se pudo completar la operación",
                JOptionPane.WARNING_MESSAGE
        );
    }
}
