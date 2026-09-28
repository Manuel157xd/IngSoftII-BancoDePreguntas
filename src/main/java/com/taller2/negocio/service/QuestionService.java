package com.taller2.negocio.service;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.stream.Collectors;

import com.taller2.negocio.model.Question;
import com.taller2.negocio.model.QuestionStatus;
import com.taller2.negocio.model.Rol;
import com.taller2.negocio.model.Usuario;
import com.taller2.persistencia.repository.QuestionRepository;
import com.taller2.negocio.validation.QuestionPolicy;

public class QuestionService {
    public static final String STATUS_PROPERTY = "estado";

    private final QuestionRepository repository;
    private final QuestionPolicy questionPolicy;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    public QuestionService(QuestionRepository repository, QuestionPolicy questionPolicy) {
        this.repository = repository;
        this.questionPolicy = questionPolicy;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public List<Question> listarTodas() { 
        return repository.listarTodas(); 
    }

    public List<Question> listarPorEstado(QuestionStatus estado) {
        return repository.listarPorEstado(estado);
    }

    // --- MÉTODOS DE BÚSQUEDA Y FILTRADO ---
    public List<Question> buscarPorFiltro(String filtro) {
        return buscarPorFiltro(repository.listarTodas(), filtro);
    }

    public List<Question> buscarPorFiltroDeAutor(String filtro, int autorId) {
        validarAutorId(autorId);
        return buscarPorFiltro(repository.listarPorAutor(autorId), filtro);
    }

    private List<Question> buscarPorFiltro(List<Question> questions, String filtro) {
        if (filtro == null || filtro.isBlank()) {
            return questions;
        }

        String term = filtro.trim().toLowerCase();
        return questions.stream()
                .filter(q -> contieneTexto(q.getNombre(), term)
                          || contieneTexto(q.getTema(), term)
                          || contieneTexto(q.getCompetencia(), term)
                          || contieneTexto(q.getSubtema(), term)
                          || contieneTexto(q.getTexto(), term)
                          || contieneTexto(q.getContexto(), term))
                .collect(Collectors.toList());
    }

    public List<Question> listarPaginado(String filtro, int pagina, int tamano) {
        return paginar(buscarPorFiltro(filtro), pagina, tamano);
    }

    public List<Question> listarPaginadoPorAutor(String filtro, int autorId, int pagina, int tamano) {
        return paginar(buscarPorFiltroDeAutor(filtro, autorId), pagina, tamano);
    }

    private List<Question> paginar(List<Question> questions, int pagina, int tamano) {
        if (pagina < 0 || tamano <= 0) {
            throw new IllegalArgumentException("La página debe ser positiva y el tamaño mayor que cero.");
        }
        return questions.stream()
                .skip((long) pagina * tamano)
                .limit(tamano)
                .collect(Collectors.toList());
    }

    public int contarPorFiltro(String filtro) {
        return buscarPorFiltro(filtro).size();
    }

    public int contarPorAutor(String filtro, int autorId) {
        return buscarPorFiltroDeAutor(filtro, autorId).size();
    }

    public List<Question> listarPorAutorYEstado(int autorId, QuestionStatus estado) {
        validarAutorId(autorId);
        return repository.listarPorAutor(autorId).stream()
                .filter(question -> question.getEstado() == estado)
                .collect(Collectors.toList());
    }

    private boolean contieneTexto(String campo, String termino) {
        return campo != null && campo.toLowerCase().contains(termino);
    }

    // --- FIN MÉTODOS DE FILTRADO ---

    public Question buscarPorId(int id) {
        return repository.buscarPorId(id).orElse(null);
    }

    public void guardar(Question question, Usuario actor) {
        validarActor(actor);
        questionPolicy.validar(question);
        validarEstadoDeAutor(actor, question.getEstado());
        question.setAutorId(actor.getId());
        repository.guardar(question);
        support.firePropertyChange(STATUS_PROPERTY, null, question.getEstado());
    }

    public void eliminar(int id) { 
        repository.eliminar(id); 
    }

    public void actualizar(Question question, Usuario actor) {
        validarActor(actor);
        questionPolicy.validar(question);
        Question persisted = repository.buscarPorId(question.getId())
                .orElseThrow(() -> new IllegalArgumentException("La pregunta ya no existe."));
        validarPropiedad(persisted, actor);
        question.setAutorId(persisted.getAutorId());
        repository.actualizar(question);
        if (persisted.getEstado() != question.getEstado()) {
            support.firePropertyChange(STATUS_PROPERTY, persisted.getEstado(), question.getEstado());
        }
    }

    public void cambiarEstado(Question question, QuestionStatus nuevoEstado, Usuario actor) {
        if (question == null || nuevoEstado == null) {
            throw new IllegalArgumentException("La pregunta y el estado son obligatorios.");
        }
        validarActor(actor);
        Question persisted = repository.buscarPorId(question.getId())
                .orElseThrow(() -> new IllegalArgumentException("La pregunta ya no existe."));
        validarPropiedad(persisted, actor);
        if (actor.getRol() == Rol.AUTOR_PREGUNTAS) {
            validarEstadoDeAutor(actor, nuevoEstado);
        } else if (actor.getRol() != Rol.ADMINISTRADOR && actor.getRol() != Rol.REVISOR) {
            throw new IllegalArgumentException("Tu rol no puede cambiar el estado de preguntas.");
        }
        QuestionStatus anterior = persisted.getEstado();
        if (anterior == nuevoEstado) {
            return;
        }
        persisted.setEstado(nuevoEstado);
        repository.actualizar(persisted);
        question.setEstado(nuevoEstado);
        support.firePropertyChange(STATUS_PROPERTY, anterior, nuevoEstado);
    }

    public void asignarRevisorYNotificar(Question question, String email, Usuario actor) {
        validarActor(actor);
        if (actor.getRol() != Rol.ADMINISTRADOR) {
            throw new IllegalArgumentException("Solo un administrador puede asignar revisores.");
        }
        if (question == null) {
            throw new IllegalArgumentException("La pregunta es obligatoria.");
        }
        Question persisted = repository.buscarPorId(question.getId())
                .orElseThrow(() -> new IllegalArgumentException("La pregunta ya no existe."));
        if (persisted.getEstado() != QuestionStatus.PENDIENTE_REVISION) {
            throw new IllegalArgumentException("La pregunta debe estar pendiente de revisión.");
        }
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("Ingrese un email válido del revisor.");
        }
        persisted.setRevisorAsignado(email);
        question.setRevisorAsignado(email);
        repository.actualizar(persisted);
        System.out.println("Enviando email a: " + email);
        System.out.println("Asunto: Nueva pregunta asignada para revisión");
        System.out.println("Cuerpo: Tiene una nueva pregunta en estado 'Pendiente de revisión' con ID " + question.getId());
    }

    private void validarActor(Usuario actor) {
        if (actor == null || actor.getId() <= 0 || actor.getRol() == null) {
            throw new IllegalArgumentException("Se requiere un usuario autenticado.");
        }
    }

    private void validarAutorId(int autorId) {
        if (autorId <= 0) {
            throw new IllegalArgumentException("El usuario autor no es válido.");
        }
    }

    private void validarPropiedad(Question question, Usuario actor) {
        if (actor.getRol() != Rol.ADMINISTRADOR && actor.getRol() != Rol.REVISOR
                && (question.getAutorId() == null || question.getAutorId() != actor.getId())) {
            throw new IllegalArgumentException("Solo puedes editar tus propias preguntas.");
        }
    }

    private void validarEstadoDeAutor(Usuario actor, QuestionStatus estado) {
        if (actor.getRol() == Rol.AUTOR_PREGUNTAS
                && estado != QuestionStatus.BORRADOR
                && estado != QuestionStatus.PENDIENTE_REVISION) {
            throw new IllegalArgumentException("Un autor solo puede guardar borradores o enviar preguntas a revisión.");
        }
    }
}