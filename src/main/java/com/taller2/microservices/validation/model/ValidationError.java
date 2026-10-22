package com.taller2.microservices.validation.model;

public record ValidationError(String field, String message, String severity) {}
