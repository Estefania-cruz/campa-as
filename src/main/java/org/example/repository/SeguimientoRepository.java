package org.example.repository;

import org.example.model.SeguimientoCampania;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeguimientoRepository extends JpaRepository<SeguimientoCampania, Long> {

    List<SeguimientoCampania> findByCampaniaIdOrderByFechaAsc(Long campaniaId);

}