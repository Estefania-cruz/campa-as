package org.example.service;

import org.example.model.SeguimientoCampania;
import org.example.repository.SeguimientoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SeguimientoService {

    @Autowired
    private SeguimientoRepository repo;

    public void registrar(Long campaniaId,
                          String accion,
                          String estadoAnterior,
                          String estadoNuevo,
                          String comentario,
                          String usuario) {

        System.out.println("🔥 Guardando seguimiento...");

        SeguimientoCampania s = new SeguimientoCampania();
        s.setCampaniaId(campaniaId);
        s.setAccion(accion);
        s.setEstadoAnterior(estadoAnterior);
        s.setEstadoNuevo(estadoNuevo);
        s.setComentario(comentario);
        s.setUsuario(usuario);

        repo.save(s);

        System.out.println("✅ Seguimiento guardado");
    }
}
