package com.taller2.microservices.events.model;

import java.util.List;

public class ReviewersAssignedEvent extends DomainEvent {
    private final int questionId;
    private final String questionName;
    private final List<String> reviewerEmails;

    public ReviewersAssignedEvent(int questionId, String questionName, List<String> reviewerEmails) {
        super("REVIEWERS_ASSIGNED");
        this.questionId = questionId;
        this.questionName = questionName;
        this.reviewerEmails = reviewerEmails != null ? List.copyOf(reviewerEmails) : List.of();
    }

    public int getQuestionId() { return questionId; }
    public String getQuestionName() { return questionName; }
    public List<String> getReviewerEmails() { return reviewerEmails; }
}
