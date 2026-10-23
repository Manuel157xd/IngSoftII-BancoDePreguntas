package com.taller2.microservices.questionclient;

import java.util.List;
import java.util.Optional;

public interface QuestionServiceClient {
    Optional<QuestionSnapshot> findById(int questionId);

    QuestionSnapshot assignReviewers(int questionId, List<String> reviewerEmails);

    QuestionSnapshot evaluate(int questionId, String reviewerEmail, String decision, String observation);
}
