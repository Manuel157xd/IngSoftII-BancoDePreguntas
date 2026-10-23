package com.taller2.microservices.questionclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Profile("distributed")
public class HttpQuestionServiceClient implements QuestionServiceClient {

    private final RestClient restClient;

    public HttpQuestionServiceClient(
            RestClient.Builder builder,
            @Value("${services.questions.base-url:http://localhost:8081}") String questionBaseUrl
    ) {
        this.restClient = builder.baseUrl(questionBaseUrl).build();
    }

    @Override
    public Optional<QuestionSnapshot> findById(int questionId) {
        try {
            Map<String, Object> response = restClient.get()
                    .uri("/api/questions/{id}", questionId)
                    .retrieve()
                    .body(MAP_TYPE);
            return Optional.of(QuestionSnapshot.fromMap(response));
        } catch (HttpClientErrorException.NotFound exception) {
            return Optional.empty();
        }
    }

    @Override
    public QuestionSnapshot assignReviewers(int questionId, List<String> reviewerEmails) {
        try {
            Map<String, Object> response = restClient.put()
                    .uri("/api/questions/{id}/reviewers", questionId)
                    .body(new AssignReviewersRequest(reviewerEmails))
                    .retrieve()
                    .body(MAP_TYPE);
            return QuestionSnapshot.fromMap(response);
        } catch (HttpClientErrorException exception) {
            throw new IllegalStateException(exception.getResponseBodyAsString(), exception);
        }
    }

    @Override
    public QuestionSnapshot evaluate(int questionId, String reviewerEmail, String decision, String observation) {
        try {
            Map<String, Object> response = restClient.put()
                    .uri("/api/questions/{id}/evaluation", questionId)
                    .body(new EvaluateQuestionRequest(reviewerEmail, decision, observation))
                    .retrieve()
                    .body(MAP_TYPE);
            return QuestionSnapshot.fromMap(response);
        } catch (HttpClientErrorException exception) {
            throw new IllegalStateException(exception.getResponseBodyAsString(), exception);
        }
    }

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    private record AssignReviewersRequest(List<String> reviewerEmails) {}
    private record EvaluateQuestionRequest(String reviewerEmail, String decision, String observation) {}
}
