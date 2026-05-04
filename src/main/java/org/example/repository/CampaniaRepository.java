package org.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.example.model.Campania;

import java.util.List;

public interface CampaniaRepository extends JpaRepository<Campania, Long> {

    List<Campania> findByEstado(String estado);
    int countByEstado(String estado);
    List<Campania> findByVisibleTrue();

    Campania findByNombre(String nombre);
    List<Campania> findByEstadoIgnoreCase(String estado);
}