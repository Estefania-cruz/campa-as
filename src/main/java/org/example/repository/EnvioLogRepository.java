package org.example.repository;

import org.example.model.EnvioLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnvioLogRepository extends JpaRepository<EnvioLog, Long> {
    EnvioLog findFirstByTelefonoDestinoOrderByFechaEnvioDesc(String telefonoDestino);
    Optional<EnvioLog> findByMensajeIdWa(String mensajeIdWa);
    @Query("SELECT e FROM EnvioLog e WHERE e.telefonoDestino LIKE %:telDestino% ORDER BY e.fechaEnvio DESC")
    List<EnvioLog> buscarUltimoPorDiezDigitos(@Param("telDestino") String telDestino, Pageable pageable);


}
