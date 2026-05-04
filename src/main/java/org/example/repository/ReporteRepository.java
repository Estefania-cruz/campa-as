package org.example.repository;
import org.example.model.Campania;
import org.example.service.ReporteService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import  org.example.model.Reporte;
@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Long> {
    Campania findByNombre(String nombre);
}
