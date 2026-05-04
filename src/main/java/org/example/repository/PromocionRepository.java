package org.example.repository;

import org.example.model.Promocion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromocionRepository extends JpaRepository<Promocion, Long> {
    // Al heredar de JpaRepository, ya tienes acceso a métodos como:
    // .save(entidad) -> Para guardar o actualizar.
    // .findAll()     -> Para obtener todas las promociones.
    // .findById(id)  -> Para buscar una específica.
    Optional<Promocion> findFirstByTelefonoDestinoOrderByFechaCreacionDesc(String telefonoDestino);
}
