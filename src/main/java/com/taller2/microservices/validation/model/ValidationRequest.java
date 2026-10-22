package com.taller2.microservices.validation.model;

import java.util.List;

public record ValidationRequest(
        String nombre,
        String enunciado,
        String contexto,
        List<String> opciones,
        int respuestaCorrecta,
        String justificacion,
        String bibliografia,
        String competencia,
        String tema,
        String subtema,
        String nivelDificultad
) {}
