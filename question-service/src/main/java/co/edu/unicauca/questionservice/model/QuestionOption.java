package co.edu.unicauca.questionservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Opción de una pregunta, embebida en la pregunta (base de datos propia). */
@Embeddable
public class QuestionOption {

    @Column(name = "option_id", nullable = false)
    private String optionId;

    @Column(name = "option_text", nullable = false, length = 500)
    private String text;

    public QuestionOption() {
    }

    public QuestionOption(String optionId, String text) {
        this.optionId = optionId;
        this.text = text;
    }

    public String getOptionId() {
        return optionId;
    }

    public void setOptionId(String optionId) {
        this.optionId = optionId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
