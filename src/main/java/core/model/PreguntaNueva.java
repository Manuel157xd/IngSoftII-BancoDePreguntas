package core.model;

import java.util.ArrayList;
import java.util.List;

import model.Question;
import model.QuestionStatus;

public class PreguntaNueva {
    private final String nombre;
    private final String texto;
    private final List<String> opciones;
    private final int respuestaCorrecta;
    private QuestionStatus estado;

    private final String contexto;
    private final String justificacion;
    private final String bibliografia;
    private final String competencia;
    private final String tema;
    private final String subtema;
    private final String nivelDificultad;
    private String revisorAsignado;

    // Constructor simplificado (asigna estado BORRADOR por defecto)
    public PreguntaNueva(String nombre, String texto, List<String> opciones,
                         int respuestaCorrecta, String contexto, String justificacion,
                         String bibliografia, String competencia, String tema,
                         String subtema, String nivelDificultad) {
        this(nombre, texto, opciones, respuestaCorrecta, QuestionStatus.BORRADOR,
             contexto, justificacion, bibliografia, competencia, tema, subtema, nivelDificultad, null);
    }

    // Constructor completo con estado y revisor
    public PreguntaNueva(String nombre, String texto, List<String> opciones,
                         int respuestaCorrecta, QuestionStatus estado,
                         String contexto, String justificacion, String bibliografia,
                         String competencia, String tema, String subtema,
                         String nivelDificultad, String revisorAsignado) {
        this.nombre = nombre;
        this.texto = texto;
        this.opciones = opciones == null ? new ArrayList<>() : new ArrayList<>(opciones);
        this.respuestaCorrecta = respuestaCorrecta;
        this.estado = estado != null ? estado : QuestionStatus.BORRADOR;
        this.contexto = contexto != null ? contexto : "";
        this.justificacion = justificacion != null ? justificacion : "";
        this.bibliografia = bibliografia != null ? bibliografia : "";
        this.competencia = competencia != null ? competencia : "";
        this.tema = tema != null ? tema : "";
        this.subtema = subtema != null ? subtema : "";
        this.nivelDificultad = nivelDificultad != null ? nivelDificultad : "Medio";
        this.revisorAsignado = revisorAsignado;
    }

    // Getters principales
    public String getNombre() { return nombre; }
    public String getTexto() { return texto; }
    public List<String> getOpciones() { return new ArrayList<>(opciones); }
    public int getRespuestaCorrecta() { return respuestaCorrecta; }
    public QuestionStatus getEstado() { return estado; }
    public void setEstado(QuestionStatus estado) { this.estado = estado; }

    // Getters DCE para consumo del Pipeline
    public String getContexto() { return contexto; }
    public String getJustificacion() { return justificacion; }
    public String getBibliografia() { return bibliografia; }
    public String getCompetencia() { return competencia; }
    public String getTema() { return tema; }
    public String getSubtema() { return subtema; }
    public String getNivelDificultad() { return nivelDificultad; }
    public String getRevisorAsignado() { return revisorAsignado; }
    public void setRevisorAsignado(String revisorAsignado) { this.revisorAsignado = revisorAsignado; }

    // Conversión segura al modelo de persistencia
    public Question comoQuestion() {
        return new Question(
                nombre, texto, opciones, respuestaCorrecta, estado,
                contexto, justificacion, bibliografia, competencia, 
                tema, subtema, nivelDificultad, revisorAsignado
        );
    }
}