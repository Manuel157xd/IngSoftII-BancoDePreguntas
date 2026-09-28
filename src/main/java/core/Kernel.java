package core;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import core.contracts.Pipeline;
import core.model.Question;
import pipeline.PipelineImpl;
import pipeline.filters.ClassificationFilter;
import pipeline.filters.ContentValidationFilter;
import pipeline.filters.CorrectAnswerValidatorFilter;
import pipeline.filters.OptionsValidatorFilter;

public class Kernel {
  
    private final BancoPreguntas banco;
    private final Pipeline pipeline;
    private final Map<String, Question> questions = new LinkedHashMap<>();

    public Kernel() {
        this(new BancoPreguntas(), crearPipeline());
    }

    public Kernel(BancoPreguntas bancy) {
        this(bancy, crearPipeline());
    }

    public Kernel(BancoPreguntas banco, Pipeline pipeline) {
        this.banco = banco;
        this.pipeline = pipeline;
    }



    public BancoPreguntas getBanco()       { return banco; }
    public Map<String, Question> getQuestions() { return Map.copyOf(questions); }

    private static Pipeline crearPipeline() {
        return new PipelineImpl(List.of(
                new ContentValidationFilter(),
                new OptionsValidatorFilter(),
                new ClassificationFilter(),
                new CorrectAnswerValidatorFilter()
        ));
    }
}
