package co.edu.unicauca.notificationservice.repository;

import co.edu.unicauca.notificationservice.model.NotificationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationRecord, Long> {
}
