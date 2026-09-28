package service;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.stream.Collectors;

import model.Question;
import model.QuestionStatus;
import repository.QuestionRepository;

public class QuestionService {
    public static final String STATUS_PROPERTY = "estado";

    private final QuestionRepository repository;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    public QuestionService(QuestionRepository repository) {
        this.repository = repository;
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
        List<Question> todas = repository.listarTodas();
        if (filtro == null || filtro.isBlank()) {
            return todas;
        }

        String term = filtro.trim().toLowerCase();
        return todas.stream()
                .filter(q -> contieneTexto(q.getNombre(), term)
                          || contieneTexto(q.getTema(), term)
                          || contieneTexto(q.getCompetencia(), term)
                          || contieneTexto(q.getSubtema(), term)
                          || contieneTexto(q.getTexto(), term)
                          || contieneTexto(q.getContexto(), term))
                .collect(Collectors.toList());
    }

    public List<Question> listarPaginado(String filtro, int pagina, int tamano) {
        List<Question> filtradas = buscarPorFiltro(filtro);
        return filtradas.stream()
                .skip((long) pagina * tamano)
                .limit(tamano)
                .collect(Collectors.toList());
    }

    public int contarPorFiltro(String filtro) {
        return buscarPorFiltro(filtro).size();
    }

    private boolean contieneTexto(String campo, String termino) {
        return campo != null && campo.toLowerCase().contains(termino);
    }

    // --- FIN MÉTODOS DE FILTRADO ---

    public Question buscarPorId(int id) {
        return repository.buscarPorId(id).orElse(null);
    }

    public void guardar(Question question) { 
        repository.guardar(question); 
    }

    public void eliminar(int id) { 
        repository.eliminar(id); 
    }

    public void actualizar(Question question) { 
        repository.actualizar(question); 
    }

    public void cambiarEstado(Question question, QuestionStatus nuevoEstado) {
        if (question == null || nuevoEstado == null) {
            throw new IllegalArgumentException("La pregunta y el estado son obligatorios.");
        }
        QuestionStatus anterior = question.getEstado();
        if (anterior == nuevoEstado) {
            return;
        }
        question.setEstado(nuevoEstado);
        repository.actualizar(question);
        support.firePropertyChange(STATUS_PROPERTY, anterior, nuevoEstado);
    }

    public void asignarRevisorYNotificar(Question question, String email) {
        question.setRevisorAsignado(email);
        repository.actualizar(question);
        System.out.println("Enviando email a: " + email);
        System.out.println("Asunto: Nueva pregunta asignada para revisión");
        System.out.println("Cuerpo: Tiene una nueva pregunta en estado 'Pendiente de revisión' con ID " + question.getId());
    }
}