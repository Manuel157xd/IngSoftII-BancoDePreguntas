package com.taller2.negocio.service;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.taller2.negocio.model.Question;
import com.taller2.negocio.model.QuestionStatus;
import com.taller2.persistencia.repository.QuestionRepository;

class QuestionServiceTest {
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
        QuestionService service = new QuestionService(repository);
        List<PropertyChangeEvent> events = new ArrayList<>();
        service.addPropertyChangeListener(events::add);

        service.cambiarEstado(question, QuestionStatus.PENDIENTE_REVISION);

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
