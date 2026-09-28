package com.taller2.presentacion.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.taller2.negocio.model.EstadoUsuario;
import com.taller2.negocio.model.Question;
import com.taller2.negocio.model.QuestionStatus;
import com.taller2.negocio.model.Rol;
import com.taller2.negocio.model.Usuario;
import com.taller2.negocio.service.QuestionService;
import com.taller2.negocio.validation.QuestionValidator;
import com.taller2.persistencia.repository.QuestionRepository;

class QuestionControllerTest {
    @Test
    void autorVeSoloSusPreguntasYAdministradorVeTodas() {
        InMemoryQuestionRepository repository = new InMemoryQuestionRepository();
        Question own = pregunta(1, 10);
        Question another = pregunta(2, 20);
        repository.guardar(own);
        repository.guardar(another);
        QuestionController controller = new QuestionController(
                new QuestionService(repository, new QuestionValidator()));
        Usuario author = usuario(10, Rol.AUTOR_PREGUNTAS);
        Usuario administrator = usuario(1, Rol.ADMINISTRADOR);

        assertEquals(List.of(own), controller.listarPaginado(author, "", 0, 10));
        assertEquals(2, controller.listarPaginado(administrator, "", 0, 10).size());
        assertEquals(1, controller.contarPorFiltro(author, ""));
    }

    private static Question pregunta(int id, int autorId) {
        Question question = new Question(
                id, "Pregunta " + id, "Texto", List.of("A", "B", "C", "D"), 0,
                QuestionStatus.BORRADOR, "Contexto", "Justificación", "Bibliografía",
                "Competencia", "Tema", "Subtema", "Medio", null
        );
        question.setAutorId(autorId);
        return question;
    }

    private static Usuario usuario(int id, Rol rol) {
        return new Usuario(id, "user" + id, "Usuario", rol, EstadoUsuario.ACTIVO, "hash");
    }

    private static final class InMemoryQuestionRepository implements QuestionRepository {
        private final List<Question> questions = new ArrayList<>();

        @Override
        public void guardar(Question question) {
            questions.add(question);
        }

        @Override
        public Optional<Question> buscarPorId(int id) {
            return questions.stream().filter(question -> question.getId() == id).findFirst();
        }

        @Override
        public List<Question> listarTodas() {
            return questions;
        }

        @Override
        public List<Question> listarPorAutor(int autorId) {
            return questions.stream()
                    .filter(question -> question.getAutorId() != null && question.getAutorId() == autorId)
                    .toList();
        }

        @Override
        public void actualizar(Question question) {
        }

        @Override
        public void eliminar(int id) {
            questions.removeIf(question -> question.getId() == id);
        }

        @Override
        public List<Question> listarPorEstado(QuestionStatus status) {
            return questions.stream().filter(question -> question.getEstado() == status).toList();
        }
    }
}