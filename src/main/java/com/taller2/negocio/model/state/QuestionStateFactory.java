package com.taller2.negocio.model.state;

import com.taller2.negocio.model.QuestionStatus;

/**
 * Factoría para obtener la instancia adecuada de QuestionState a partir de QuestionStatus.
 */
public final class QuestionStateFactory {

    private QuestionStateFactory() {}

    public static QuestionState getState(QuestionStatus status) {
        if (status == null) {
            return new DraftState();
        }
        return switch (status) {
            case BORRADOR -> new DraftState();
            case PENDIENTE_REVISION -> new PendingReviewState();
            case EN_REVISION -> new UnderReviewState();
            case APROBADA -> new ApprovedState();
            case RECHAZADA -> new RejectedState();
            case PUBLICADA -> new PublishedState();
            case ARCHIVADA -> new ArchivedState();
        };
    }
}
