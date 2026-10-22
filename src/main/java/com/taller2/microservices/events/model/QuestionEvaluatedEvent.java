package com.taller2.microservices.events.model;

public class QuestionEvaluatedEvent extends DomainEvent {
    private final int questionId;
    private final String questionName;
    private final String reviewerEmail;
    private final String outcome; // APROBADA o RECHAZADA
    private final String observation;
    private final Integer authorId;

    public QuestionEvaluatedEvent(int questionId, String questionName, String reviewerEmail,
                                  String outcome, String observation, Integer authorId) {
        super("QUESTION_EVALUATED");
        this.questionId = questionId;
        this.questionName = questionName;
        this.reviewerEmail = reviewerEmail;
        this.outcome = outcome;
        this.observation = observation;
        this.authorId = authorId;
    }

    public int getQuestionId() { return questionId; }
    public String getQuestionName() { return questionName; }
    public String getReviewerEmail() { return reviewerEmail; }
    public String getOutcome() { return outcome; }
    public String getObservation() { return observation; }
    public Integer getAuthorId() { return authorId; }
}
