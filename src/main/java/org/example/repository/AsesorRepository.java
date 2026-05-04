package org.example.repository;

import org.example.model.Asesor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface AsesorRepository extends JpaRepository<Asesor, Long> {

    @Query("SELECT a FROM Asesor a WHERE a.estatus = 'LIBRE' AND a.leadsAtendidos < 10 ORDER BY a.leadsAtendidos ASC")
    Optional<Asesor> encontrarSiguienteDisponible();

    Optional<Asesor> findByNombreIgnoreCase(String nombre);
   // Asesor findByTelefono(String telefono);
   Optional<Asesor> findByTelefono(String telefono);
}
