package org.example.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.example.model.Chats;
/*
@Repository
public interface ChatRepository extends JpaRepository<Chats, Long> {
    long count();

    long countByEstadoWa(String estadoWa);
    long countByCreatedAtAfter(LocalDateTime fecha);
    long countByRespondido(boolean respondido);
 //   long countByFechaAfter(LocalDateTime fecha);
   // long countByEstadoWaAndFechaAfter(String estado, LocalDateTime fecha);
   // long countByRespondidoAndFechaAfter(boolean respondido, LocalDateTime fecha);
    long countByEstadoWaAndCreatedAtAfter(String estado, LocalDateTime fecha);
    long countByRespondidoAndCreatedAtAfter(boolean respondido, LocalDateTime fecha);
    Page<Chats> findAllByOrderByFechaEnvioDesc(Pageable pageable);
    List<Chats> findByTelefonoUsuarioOrderByFechaEnvioAsc(String telefono);
    Chats findFirstByTelefonoUsuarioAndMensajeContainingOrderByFechaEnvioDesc(String telefono, String contenido);
    Page<Chats> findByMensajeContainingOrderByFechaEnvioDesc(String mensaje, Pageable pageable);
    Optional<Chats> findByMensajeIdWa(String mensajeIdWa);
}*/

@Repository
public interface ChatRepository extends JpaRepository<Chats, Long> {
    long count();
    long countByEstadoWa(String estadoWa);
    long countByRespondido(boolean respondido);

    long countByFechaAfter(LocalDateTime fecha);
    long countByEstadoWaAndFechaAfter(String estado, LocalDateTime fecha);
    long countByRespondidoAndFechaAfter(boolean respondido, LocalDateTime fecha);

    Page<Chats> findAllByOrderByFechaEnvioDesc(Pageable pageable);
    List<Chats> findByTelefonoUsuarioOrderByFechaEnvioAsc(String telefono);
    Chats findFirstByTelefonoUsuarioAndMensajeContainingOrderByFechaEnvioDesc(String telefono, String contenido);
    Page<Chats> findByMensajeContainingOrderByFechaEnvioDesc(String mensaje, Pageable pageable);
    Optional<Chats> findByMensajeIdWa(String mensajeIdWa);


    @Query(value = "SELECT DATE(fecha_envio) as dia, COUNT(*) as total " +
            "FROM chats " +
            "GROUP BY DATE(fecha_envio) " +
            "ORDER BY dia ASC",
            nativeQuery = true)
    List<Object[]> countMessagesByDay();


    @Query("SELECT COUNT(c) FROM Chats c WHERE c.estadoWa = 'failed'")
    long countFailedMessages();

    @Query(value = "SELECT CAST(fecha_envio AS DATE) as fecha, " +
            "COUNT(CASE WHEN estado_wa = 'delivered' THEN 1 END) as entregados, " +
            "COUNT(CASE WHEN estado_wa = 'failed' THEN 1 END) as fallidos " +
            "FROM chats GROUP BY CAST(fecha_envio AS DATE) ORDER BY fecha",
            nativeQuery = true)
    List<Object[]> getRendimientoDiarioConErrores();

    Optional<Chats> findByTelefonoUsuario(String telefonoUsuario);}
