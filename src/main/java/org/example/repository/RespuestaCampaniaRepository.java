package org.example.repository;

import org.example.model.RespuestaCampania;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RespuestaCampaniaRepository extends JpaRepository<RespuestaCampania, Long> {

    /**
     * Ejemplo de consulta personalizada:
     * Busca todos los registros por tipo de respuesta (INTERESADO / NO_INTERESADO)
     */
    List<RespuestaCampania> findByRespuesta(String respuesta);

    /**
     * Busca por teléfono por si quieres saber qué ha contestado un cliente específico
     */
    List<RespuestaCampania> findByTelefonoUsuario(String telefonoUsuario);
}
