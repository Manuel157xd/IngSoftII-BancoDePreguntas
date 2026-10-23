package com.taller2.microservices.questionclient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record QuestionSnapshot(
        int id,
        String nombre,
        String texto,
        String contexto,
        List<String> opciones,
        String justificacion,
        String bibliografia,
        String competencia,
        String tema,
        String nivelDificultad,
        String estado,
        String colorHex,
        Integer autorId
) {
    public QuestionSnapshot {
        opciones = opciones == null ? List.of() : List.copyOf(opciones);
    }

    public static QuestionSnapshot fromMap(Map<String, Object> values) {
        if (values == null) {
            throw new IllegalStateException("El servicio de preguntas devolvió una respuesta vacía");
        }
        Object rawOptions = values.get("opciones");
        if (!(rawOptions instanceof List<?> rawOptionList)) {
            throw new IllegalStateException("La respuesta del servicio de preguntas no contiene una lista de opciones");
        }
        List<String> options = new ArrayList<>(rawOptionList.size());
        for (Object option : rawOptionList) {
            if (!(option instanceof String text)) {
                throw new IllegalStateException("La respuesta del servicio de preguntas contiene una opción inválida");
            }
            options.add(text);
        }

        Object rawId = values.get("id");
        if (!(rawId instanceof Number id)) {
            throw new IllegalStateException("La respuesta del servicio de preguntas no contiene un id válido");
        }
        Object rawAuthorId = values.get("autorId");
        Integer authorId = rawAuthorId instanceof Number number ? number.intValue() : null;
        return new QuestionSnapshot(
                id.intValue(),
                text(values.get("nombre")),
                text(values.get("texto")),
                text(values.get("contexto")),
                options,
                text(values.get("justificacion")),
                text(values.get("bibliografia")),
                text(values.get("competencia")),
                text(values.get("tema")),
                text(values.get("nivelDificultad")),
                text(values.get("estado")),
                text(values.get("colorHex")),
                authorId
        );
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
