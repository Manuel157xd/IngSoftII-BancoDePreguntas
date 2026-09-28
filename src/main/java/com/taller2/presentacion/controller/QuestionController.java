package com.taller2.presentacion.controller;

import java.beans.PropertyChangeListener;
import java.util.List;

import com.taller2.negocio.model.Question;
import com.taller2.negocio.model.QuestionStatus;
import com.taller2.negocio.model.Rol;
import com.taller2.negocio.model.Usuario;
import com.taller2.negocio.service.QuestionService;

public class QuestionController {
    public static final String STATUS_PROPERTY = QuestionService.STATUS_PROPERTY;

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    public List<Question> listarPaginado(Usuario actor, String filtro, int pagina, int tamano) {
        validarActor(actor);
        if (actor.getRol() == Rol.AUTOR_PREGUNTAS) {
            return questionService.listarPaginadoPorAutor(filtro, actor.getId(), pagina, tamano);
        }
        return questionService.listarPaginado(filtro, pagina, tamano);
    }

    public int contarPorFiltro(Usuario actor, String filtro) {
        validarActor(actor);
        if (actor.getRol() == Rol.AUTOR_PREGUNTAS) {
            return questionService.contarPorAutor(filtro, actor.getId());
        }
        return questionService.contarPorFiltro(filtro);
    }

    public List<Question> listarPorEstado(Usuario actor, QuestionStatus estado) {
        validarActor(actor);
        if (actor.getRol() == Rol.AUTOR_PREGUNTAS) {
            return questionService.listarPorAutorYEstado(actor.getId(), estado);
        }
        return questionService.listarPorEstado(estado);
    }

    public void guardar(Usuario actor, Question question) {
        validarActor(actor);
        questionService.guardar(question, actor);
    }

    public void actualizar(Usuario actor, Question question) {
        validarActor(actor);
        questionService.actualizar(question, actor);
    }

    public void cambiarEstado(Usuario actor, Question question, QuestionStatus estado) {
        validarActor(actor);
        questionService.cambiarEstado(question, estado, actor);
    }

    public void asignarRevisor(Usuario actor, Question question, String email) {
        validarActor(actor);
        questionService.asignarRevisorYNotificar(question, email, actor);
    }

    public boolean puedeAsignarRevisor(Usuario actor) {
        return actor != null && actor.getRol() == Rol.ADMINISTRADOR;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        questionService.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        questionService.removePropertyChangeListener(listener);
    }

    private void validarActor(Usuario actor) {
        if (actor == null || actor.getId() <= 0 || actor.getRol() == null) {
            throw new IllegalArgumentException("Se requiere un usuario autenticado.");
        }
    }
}