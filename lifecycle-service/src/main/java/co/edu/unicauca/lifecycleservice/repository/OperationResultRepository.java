package co.edu.unicauca.lifecycleservice.repository;

import co.edu.unicauca.lifecycleservice.model.OperationResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationResultRepository extends JpaRepository<OperationResult, String> {
}
