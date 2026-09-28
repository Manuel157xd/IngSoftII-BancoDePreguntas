package com.taller2.negocio.model;

import java.util.ArrayList;
import java.util.List;

public class Question {
    private int id;
    private String nombre;
    private String texto;
    private List<String> opciones;
    private int respuestaCorrecta;
    private QuestionStatus estado;
    private String contexto;
    private String justificacion;
    private String bibliografia;
    private String competencia;
    private String tema;
    private String subtema;
    private String nivelDificultad;
    private String revisorAsignado;
    private Integer autorId;

    public Question(String nombre, String texto, List<String> opciones,
                    int respuestaCorrecta, QuestionStatus estado,
                    String contexto, String justificacion, String bibliografia,
                    String competencia, String tema, String subtema,
                    String nivelDificultad, String revisorAsignado) {
        this(0, nombre, texto, opciones, respuestaCorrecta, estado,
             contexto, justificacion, bibliografia, competencia,
             tema, subtema, nivelDificultad, revisorAsignado);
    }

    public Question(int id, String nombre, String texto, List<String> opciones,
                    int respuestaCorrecta, QuestionStatus estado,
                    String contexto, String justificacion, String bibliografia,
                    String competencia, String tema, String subtema,
                    String nivelDificultad, String revisorAsignado) {
        this.id = id;
        this.nombre = nombre;
        this.texto = texto;
        this.opciones = new ArrayList<>(opciones);
        this.respuestaCorrecta = respuestaCorrecta;
        this.estado = estado;
        this.contexto = contexto;
        this.justificacion = justificacion;
        this.bibliografia = bibliografia;
        this.competencia = competencia;
        this.tema = tema;
        this.subtema = subtema;
        this.nivelDificultad = nivelDificultad;
        this.revisorAsignado = revisorAsignado;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getTexto() { return texto; }
    public List<String> getOpciones() { return new ArrayList<>(opciones); }
    public int getRespuestaCorrecta() { return respuestaCorrecta; }
    public QuestionStatus getEstado() { return estado; }

    public String getContexto() { return contexto; }
    public String getJustificacion() { return justificacion; }
    public String getBibliografia() { return bibliografia; }
    public String getCompetencia() { return competencia; }
    public String getTema() { return tema; }
    public String getSubtema() { return subtema; }
    public String getNivelDificultad() { return nivelDificultad; }
    public String getRevisorAsignado() { return revisorAsignado; }
    public Integer getAutorId() { return autorId; }

    public void setId(int id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setTexto(String texto) { this.texto = texto; }
    public void setOpciones(List<String> opciones) { this.opciones = new ArrayList<>(opciones); }
    public void setRespuestaCorrecta(int respuestaCorrecta) { this.respuestaCorrecta = respuestaCorrecta; }
    public void setEstado(QuestionStatus estado) { this.estado = estado; }

    public void setContexto(String contexto) { this.contexto = contexto; }
    public void setJustificacion(String justificacion) { this.justificacion = justificacion; }
    public void setBibliografia(String bibliografia) { this.bibliografia = bibliografia; }
    public void setCompetencia(String competencia) { this.competencia = competencia; }
    public void setTema(String tema) { this.tema = tema; }
    public void setSubtema(String subtema) { this.subtema = subtema; }
    public void setNivelDificultad(String nivelDificultad) { this.nivelDificultad = nivelDificultad; }
    public void setRevisorAsignado(String revisorAsignado) { this.revisorAsignado = revisorAsignado; }
    public void setAutorId(Integer autorId) { this.autorId = autorId; }

    @Override
    public String toString() {
        return id + " - " + nombre;
    }
}
