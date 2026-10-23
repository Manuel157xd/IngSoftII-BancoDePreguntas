package com.taller2.presentacion.controller;

import com.taller2.negocio.service.ReviewGateway;

import java.util.List;

public class ReviewController {
    private final ReviewGateway reviewGateway;

    public ReviewController(ReviewGateway reviewGateway) {
        this.reviewGateway = reviewGateway;
    }

    public List<ReviewGateway.AssignedQuestion> listAssignedQuestions(String reviewerEmail) {
        return reviewGateway.listAssignedQuestions(reviewerEmail);
    }

    public void evaluate(
            int questionId,
            String reviewerEmail,
            String decision,
            String observation
    ) {
        reviewGateway.evaluate(questionId, reviewerEmail, decision, observation);
    }
}
