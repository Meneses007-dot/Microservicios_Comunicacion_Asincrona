package co.edu.unicauca.reviewservice.repository;

import co.edu.unicauca.reviewservice.model.ReviewRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRecordRepository extends JpaRepository<ReviewRecord, Long> {
}
