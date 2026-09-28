package com.taller2.negocio.validation;

import com.taller2.negocio.model.Question;

public interface QuestionPolicy {
    void validar(Question question);
}