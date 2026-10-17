package co.edu.unicauca.reviewservice.repository;

import co.edu.unicauca.reviewservice.model.ReviewAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewAssignmentRepository extends JpaRepository<ReviewAssignment, Long> {
}
