package com.taller2.negocio.model.builder;

import com.taller2.negocio.model.Question;
import com.taller2.negocio.model.QuestionStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Patrón Creacional GoF: Builder
 * Permite construir objetos complejos Question paso a paso para el Diseño Centrado en Evidencia (DCE).
 */
public class QuestionBuilder {
    private int id = 0;
    private String nombre = "";
    private String texto = "";
    private List<String> opciones = new ArrayList<>();
    private int respuestaCorrecta = 0;
    private QuestionStatus estado = QuestionStatus.BORRADOR;
    private String contexto = "";
    private String justificacion = "";
    private String bibliografia = "";
    private String competencia = "";
    private String tema = "";
    private String subtema = "";
    private String nivelDificultad = "Medio";
    private String revisorAsignado = "";
    private Integer autorId = null;

    public static QuestionBuilder builder() {
        return new QuestionBuilder();
    }

    public QuestionBuilder conId(int id) {
        this.id = id;
        return this;
    }

    public QuestionBuilder conNombre(String nombre) {
        this.nombre = nombre;
        return this;
    }

    public QuestionBuilder conTexto(String texto) {
        this.texto = texto;
        return this;
    }

    public QuestionBuilder conOpciones(List<String> opciones) {
        this.opciones = opciones != null ? new ArrayList<>(opciones) : new ArrayList<>();
        return this;
    }

    public QuestionBuilder agregarOpcion(String opcion) {
        if (this.opciones == null) {
            this.opciones = new ArrayList<>();
        }
        this.opciones.add(opcion);
        return this;
    }

    public QuestionBuilder conRespuestaCorrecta(int respuestaCorrecta) {
        this.respuestaCorrecta = respuestaCorrecta;
        return this;
    }

    public QuestionBuilder conEstado(QuestionStatus estado) {
        this.estado = estado != null ? estado : QuestionStatus.BORRADOR;
        return this;
    }

    public QuestionBuilder conContexto(String contexto) {
        this.contexto = contexto;
        return this;
    }

    public QuestionBuilder conJustificacion(String justificacion) {
        this.justificacion = justificacion;
        return this;
    }

    public QuestionBuilder conBibliografia(String bibliografia) {
        this.bibliografia = bibliografia;
        return this;
    }

    public QuestionBuilder conCompetencia(String competencia) {
        this.competencia = competencia;
        return this;
    }

    public QuestionBuilder conTema(String tema) {
        this.tema = tema;
        return this;
    }

    public QuestionBuilder conSubtema(String subtema) {
        this.subtema = subtema;
        return this;
    }

    public QuestionBuilder conNivelDificultad(String nivelDificultad) {
        this.nivelDificultad = nivelDificultad;
        return this;
    }

    public QuestionBuilder conRevisorAsignado(String revisorAsignado) {
        this.revisorAsignado = revisorAsignado;
        return this;
    }

    public QuestionBuilder conAutorId(Integer autorId) {
        this.autorId = autorId;
        return this;
    }

    public Question build() {
        Question q = new Question(
                this.id,
                this.nombre,
                this.texto,
                this.opciones,
                this.respuestaCorrecta,
                this.estado,
                this.contexto,
                this.justificacion,
                this.bibliografia,
                this.competencia,
                this.tema,
                this.subtema,
                this.nivelDificultad,
                this.revisorAsignado
        );
        q.setAutorId(this.autorId);
        return q;
    }
}
