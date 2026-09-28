package core.model;

import java.util.ArrayList;
import java.util.List;

public class QuestionRequest {
    private final String title;
    private final String content;
    private final String type;
    private final List<String> options;
    private final int correctAnswer;
    
    // Campos del Diseño Centrado en Evidencia (DCE)
    private final String contexto;
    private final String justificacion;
    private final String bibliografia;
    private final String competencia;
    private final String tema;
    private final String subtema;
    private final String nivelDificultad;

    // Constructor básico
    public QuestionRequest(String title, String content, String type) {
        this(title, content, type, List.of(), 0, "", "", "", "", "", "", "Medio");
    }

    // Constructor previo (mantenido para retrocompatibilidad)
    public QuestionRequest(String title, String content, String type,
                           List<String> options, int correctAnswer) {
        this(title, content, type, options, correctAnswer, "", "", "", "", "", "", "Medio");
    }

    // Constructor completo con todos los atributos DCE
    public QuestionRequest(String title, String content, String type,
                           List<String> options, int correctAnswer,
                           String contexto, String justificacion, String bibliografia,
                           String competencia, String tema, String subtema, String nivelDificultad) {
        this.title = title;
        this.content = content;
        this.type = type;
        this.options = options == null ? new ArrayList<>() : new ArrayList<>(options);
        this.correctAnswer = correctAnswer;
        this.contexto = contexto != null ? contexto : "";
        this.justificacion = justificacion != null ? justificacion : "";
        this.bibliografia = bibliografia != null ? bibliografia : "";
        this.competencia = competencia != null ? competencia : "";
        this.tema = tema != null ? tema : "";
        this.subtema = subtema != null ? subtema : "";
        this.nivelDificultad = nivelDificultad != null ? nivelDificultad : "Medio";
    }

    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getType() { return type; }
    public List<String> getOptions() { return new ArrayList<>(options); }
    public int getCorrectAnswer() { return correctAnswer; }

    // Getters para campos DCE
    public String getContexto() { return contexto; }
    public String getJustificacion() { return justificacion; }
    public String getBibliografia() { return bibliografia; }
    public String getCompetencia() { return competencia; }
    public String getTema() { return tema; }
    public String getSubtema() { return subtema; }
    public String getNivelDificultad() { return nivelDificultad; }
}