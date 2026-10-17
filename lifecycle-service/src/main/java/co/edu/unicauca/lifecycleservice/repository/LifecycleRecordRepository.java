package co.edu.unicauca.lifecycleservice.repository;

import co.edu.unicauca.lifecycleservice.model.LifecycleRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LifecycleRecordRepository extends JpaRepository<LifecycleRecord, Long> {

    Optional<LifecycleRecord> findByQuestionId(Long questionId);
}
