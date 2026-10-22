package com.taller2.microservices.events.model;

import java.util.List;

public class QuestionSubmittedEvent extends DomainEvent {
    private final int questionId;
    private final String questionName;
    private final Integer authorId;

    public QuestionSubmittedEvent(int questionId, String questionName, Integer authorId) {
        super("QUESTION_SUBMITTED_FOR_REVIEW");
        this.questionId = questionId;
        this.questionName = questionName;
        this.authorId = authorId;
    }

    public int getQuestionId() { return questionId; }
    public String getQuestionName() { return questionName; }
    public Integer getAuthorId() { return authorId; }
}
