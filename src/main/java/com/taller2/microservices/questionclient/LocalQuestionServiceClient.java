package com.taller2.microservices.questionclient;

import com.taller2.microservices.questions.repository.QuestionMicroserviceRepository;
import com.taller2.negocio.model.Question;
import com.taller2.negocio.model.QuestionStatus;
import com.taller2.negocio.model.state.QuestionState;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Profile("!distributed")
public class LocalQuestionServiceClient implements QuestionServiceClient {

    private final QuestionMicroserviceRepository repository;

    public LocalQuestionServiceClient(QuestionMicroserviceRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<QuestionSnapshot> findById(int questionId) {
        return repository.findById(questionId).map(LocalQuestionServiceClient::snapshot);
    }

    @Override
    public QuestionSnapshot assignReviewers(int questionId, List<String> reviewerEmails) {
        Question question = repository.findById(questionId).orElseThrow();
        if (question.getEstado() != QuestionStatus.PENDIENTE_REVISION
                && question.getEstado() != QuestionStatus.EN_REVISION) {
            throw new IllegalStateException("Solo se pueden asignar revisores a preguntas pendientes de revisión");
        }
        QuestionState state = question.getStateObject();
        state.asignarRevisores(question, String.join(", ", reviewerEmails));
        repository.update(question);
        return snapshot(question);
    }

    @Override
    public QuestionSnapshot evaluate(int questionId, String reviewerEmail, String decision, String observation) {
        Question question = repository.findById(questionId).orElseThrow();
        QuestionState state = question.getStateObject();
        if ("APROBADA".equals(decision)) {
            state.aprobar(question, observation);
        } else if ("RECHAZADA".equals(decision)) {
            state.rechazar(question, observation);
        } else {
            throw new IllegalArgumentException("La decisión debe ser APROBADA o RECHAZADA");
        }
        repository.update(question);
        return snapshot(question);
    }

    private static QuestionSnapshot snapshot(Question question) {
        return new QuestionSnapshot(
                question.getId(), question.getNombre(), question.getTexto(), question.getContexto(),
                question.getOpciones(), question.getRespuestaCorrecta(), question.getJustificacion(),
                question.getBibliografia(), question.getCompetencia(), question.getTema(),
                question.getSubtema(), question.getNivelDificultad(),
                question.getEstado().name(), question.getColorHex(), question.getAutorId()
        );
    }
}
