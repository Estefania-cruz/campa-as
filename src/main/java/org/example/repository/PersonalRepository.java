package org.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import org.example.model.PersonalAutorizado;
@Repository
public interface PersonalRepository extends JpaRepository<PersonalAutorizado, Long> {
    @Query("SELECT p FROM PersonalAutorizado p WHERE p.numero LIKE %:diezDigitos")
    Optional<PersonalAutorizado> findByUltimosDiez(@Param("diezDigitos") String diezDigitos);
}