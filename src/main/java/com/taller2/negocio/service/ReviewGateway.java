package com.taller2.negocio.service;

import com.taller2.negocio.model.QuestionStatus;

import java.util.List;

public interface ReviewGateway {
    QuestionStatus assignReviewers(int questionId, List<String> reviewerEmails);

    List<AssignedQuestion> listAssignedQuestions(String reviewerEmail);

    void evaluate(int questionId, String reviewerEmail, String decision, String observation);

    record AssignedQuestion(
            int assignmentId,
            int questionId,
            String name,
            String statement,
            String context,
            List<String> options,
            int correctAnswer,
            String justification,
            String bibliography,
            String competency,
            String topic,
            String subtopic,
            String difficulty,
            String status,
            String colorHex,
            String assignmentStatus,
            String observation
    ) {
        public AssignedQuestion {
            options = options == null ? List.of() : List.copyOf(options);
        }

        @Override
        public String toString() {
            return "#" + questionId + " - " + name + " [" + status + "]";
        }
    }
}
