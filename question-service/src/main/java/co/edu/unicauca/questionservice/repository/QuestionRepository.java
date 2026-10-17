package co.edu.unicauca.questionservice.repository;

import co.edu.unicauca.questionservice.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}
