package org.example;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HistorialOficinaRepository extends JpaRepository<HistorialOficinaEntity, Long> {
    List<HistorialOficinaEntity> findAllByOrderByFechaDesc();
}
