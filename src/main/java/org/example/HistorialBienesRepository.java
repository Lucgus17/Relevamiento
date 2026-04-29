package org.example;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HistorialBienesRepository extends JpaRepository<HistorialBienesEntity, Long> {
    List<HistorialBienesEntity> findAllByOrderByFechaDesc();
}
