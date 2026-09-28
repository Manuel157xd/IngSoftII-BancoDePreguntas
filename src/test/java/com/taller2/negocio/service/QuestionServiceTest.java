package com.taller2.negocio.service;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.taller2.negocio.model.Question;
import com.taller2.negocio.model.QuestionStatus;
import com.taller2.negocio.model.EstadoUsuario;
import com.taller2.negocio.model.Rol;
import com.taller2.negocio.model.Usuario;
import com.taller2.negocio.validation.QuestionValidator;
import com.taller2.persistencia.repository.QuestionRepository;

class QuestionServiceTest {
    @Test
    void debeAsignarElAutorAutenticadoAlGuardar() {
        MemoryRepository repository = new MemoryRepository();
        Question question = pregunta(0, 0, "Pregunta propia");
        QuestionService service = new QuestionService(repository, new QuestionValidator());

        service.guardar(question, usuario(10, Rol.AUTOR_PREGUNTAS));

        assertEquals(10, question.getAutorId());
        assertEquals(1, repository.listarPorAutor(10).size());
        assertTrue(repository.listarPorAutor(20).isEmpty());
    }

    @Test
    void debeImpedirQueUnAutorActualiceUnaPreguntaAjena() {
        MemoryRepository repository = new MemoryRepository();
        Question question = pregunta(4, 20, "Pregunta ajena");
        repository.guardar(question);
        QuestionService service = new QuestionService(repository, new QuestionValidator());

        assertThrows(IllegalArgumentException.class,
                () -> service.actualizar(question, usuario(10, Rol.AUTOR_PREGUNTAS)));
    }

    @Test
    void debeListarSoloPreguntasDelAutorYAplicarFiltroYPaginacion() {
        MemoryRepository repository = new MemoryRepository();
        Question first = pregunta(1, 10, "Álgebra básica");
        Question second = pregunta(2, 10, "Álgebra avanzada");
        Question otherAuthor = pregunta(3, 20, "Álgebra externa");
        repository.guardar(first);
        repository.guardar(second);
        repository.guardar(otherAuthor);
        QuestionService service = new QuestionService(repository, new QuestionValidator());

        List<Question> page = service.listarPaginadoPorAutor("álgebra", 10, 1, 1);

        assertEquals(1, page.size());
        assertEquals(2, page.get(0).getId());
        assertEquals(2, service.contarPorAutor("álgebra", 10));
        assertTrue(service.listarPaginadoPorAutor("externa", 10, 0, 10).isEmpty());
    }

    @Test
    void autorNoPuedeCrearPreguntaPublicadaNiAprobarLaPropia() {
        MemoryRepository repository = new MemoryRepository();
        QuestionService service = new QuestionService(repository, new QuestionValidator());
        Usuario author = usuario(10, Rol.AUTOR_PREGUNTAS);
        Question published = pregunta(0, 10, "Pregunta publicada");
        published.setEstado(QuestionStatus.PUBLICADA);

        assertThrows(IllegalArgumentException.class, () -> service.guardar(published, author));

        Question draft = pregunta(2, 10, "Pregunta propia");
        repository.guardar(draft);
        assertThrows(IllegalArgumentException.class,
                () -> service.cambiarEstado(draft, QuestionStatus.APROBADA, author));
    }

    private Usuario usuario(int id, Rol rol) {
        return new Usuario(id, "autor" + id, "Autor", rol, EstadoUsuario.ACTIVO, "hash");
    }

    private Question pregunta(int id, int authorId, String name) {
        Question question = new Question(
                id, name, "Texto de pregunta", List.of("A", "B", "C", "D"), 0,
                QuestionStatus.BORRADOR, "Contexto", "Justificación", "Bibliografía",
                "Competencia", "Tema", "Subtema", "Medio", null
        );
        question.setAutorId(authorId);
        return question;
    }

  @Test
    void debeNotificarCuandoCambiaEstado() {
        MemoryRepository repository = new MemoryRepository();
        Question question = new Question(
                1,
                "Pregunta 1",
                "Texto de la pregunta",
                List.of("Opción A", "Opción B", "Opción C", "Opción D"),
                0,
                QuestionStatus.BORRADOR,
                "Contexto de prueba",
                "Justificación de la respuesta",
                "Bibliografía de referencia",
                "Competencia",
                "Tema principal",
                "Subtema",
                "Medio",
                null
        );
        repository.questions.add(question);
        QuestionService service = new QuestionService(repository, new QuestionValidator());
        List<PropertyChangeEvent> events = new ArrayList<>();
        service.addPropertyChangeListener(events::add);

        service.cambiarEstado(question, QuestionStatus.PENDIENTE_REVISION,
            usuario(1, Rol.ADMINISTRADOR));

        assertEquals(1, events.size());
        assertEquals("estado", events.get(0).getPropertyName());
        assertEquals(QuestionStatus.BORRADOR, events.get(0).getOldValue());
        assertEquals(QuestionStatus.PENDIENTE_REVISION, events.get(0).getNewValue());
    }

    private static class MemoryRepository implements QuestionRepository {
        private final List<Question> questions = new ArrayList<>();

        @Override
        public void guardar(Question question) { 
            questions.add(question); 
        }

        @Override
        public Optional<Question> buscarPorId(int id) {
            return questions.stream().filter(q -> q.getId() == id).findFirst();
        }

        @Override
        public List<Question> listarTodas() { 
            return questions; 
        }

        @Override
        public List<Question> listarPorAutor(int authorId) {
            return questions.stream()
                .filter(question -> question.getAutorId() != null
                    && question.getAutorId() == authorId)
                .toList();
        }

        @Override
        public void actualizar(Question question) { }

        @Override
        public void eliminar(int id) { 
            questions.removeIf(q -> q.getId() == id); 
        }

        @Override
        public List<Question> listarPorEstado(QuestionStatus status) {
            return questions.stream().filter(q -> q.getEstado() == status).toList();
        }

      
    }
}
