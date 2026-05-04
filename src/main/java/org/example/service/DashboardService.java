package org.example.service;

import org.example.model.Chats;
import org.example.repository.CampaniaRepository;
import org.example.repository.ChatRepository;
import org.example.repository.ReporteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class DashboardService {

    @Autowired
    private CampaniaRepository campaniaRepository;

    @Autowired
    private ReporteRepository reporteRepository;

    @Autowired
    private ChatRepository chatRepository;

    /*
    public Map<String, Object> obtenerDatosDashboard(int page){
        Pageable pageable = PageRequest.of(page, 10);
        Page<Chats> paginaChats = chatRepository.findAllByOrderByFechaEnvioDesc(pageable);

        long total = campaniaRepository.count();
        int activas = campaniaRepository.countByEstado("ACTIVA");
        int inactivas = campaniaRepository.countByEstado("INACTIVA");
        int aprobadas = campaniaRepository.countByEstado("APROBADA");
        int pendientes = campaniaRepository.countByEstado("PENDIENTE");
        int negadas = campaniaRepository.countByEstado("NEGADA");
        int reportes = (int) reporteRepository.count();


        List<Chats> statsPromociones = chatRepository.findAll();

        long mensajesLeidos = chatRepository.countByEstadoWa("read");
        long interacciones = chatRepository.countByRespondido(true);
        long fallidos = chatRepository.countByEstadoWa("failed");

        Map<String,Object> datos = new HashMap<>();

        datos.put("statsPromociones", paginaChats.getContent()); // Los 10 registros
        datos.put("totalPaginas", paginaChats.getTotalPages());   // Para el paginador de Angular
        datos.put("paginaActual", paginaChats.getNumber());


        datos.put("total", total);
        datos.put("activas", activas);
        datos.put("inactivas", inactivas);
        datos.put("aprobadas", aprobadas);
        datos.put("pendientes", pendientes);
        datos.put("negadas", negadas);
        datos.put("reportes", reportes);


        datos.put("statsPromociones", statsPromociones);
        datos.put("mensajesLeidos", mensajesLeidos);
        datos.put("interacciones", interacciones);
        datos.put("fallidos", fallidos);


        return datos;
    }
*/
    public Map<String, Object> obtenerDatosDashboard(int page){
        Pageable pageable = PageRequest.of(page, 10);
        Page<Chats> paginaChats = chatRepository.findAllByOrderByFechaEnvioDesc(pageable);

        long total = campaniaRepository.count();
        int activas = campaniaRepository.countByEstado("ACTIVA");
        int inactivas = campaniaRepository.countByEstado("INACTIVA");
        int aprobadas = campaniaRepository.countByEstado("APROBADA");
        int pendientes = campaniaRepository.countByEstado("PENDIENTE");
        int negadas = campaniaRepository.countByEstado("NEGADA");
        int reportes = (int) reporteRepository.count();

        long mensajesLeidos = chatRepository.countByEstadoWa("read");
        long interacciones = chatRepository.countByRespondido(true);
        long fallidos = chatRepository.countByEstadoWa("failed");

        Map<String,Object> datos = new HashMap<>();

        datos.put("statsPromociones", paginaChats.getContent());
        datos.put("totalPaginas", paginaChats.getTotalPages());
        datos.put("paginaActual", paginaChats.getNumber());

        datos.put("total", total);
        datos.put("activas", activas);
        datos.put("inactivas", inactivas);
        datos.put("aprobadas", aprobadas);
        datos.put("pendientes", pendientes);
        datos.put("negadas", negadas);
        datos.put("reportes", reportes);

        datos.put("mensajesLeidos", mensajesLeidos);
        datos.put("interacciones", interacciones);
        datos.put("fallidos", fallidos);

        return datos;
    }

    public Map<String, Object> getResumenRendimiento() {
        Map<String, Object> resumen = new HashMap<>();

        long enviados = chatRepository.count();

        long leidos = chatRepository.countByEstadoWa("read");

        long respuestas = chatRepository.countByRespondido(true);

        resumen.put("enviados", enviados);
        resumen.put("leidos", leidos);
        resumen.put("respuestas", respuestas);

        double tasaLectura = enviados > 0 ? (double) leidos / enviados * 100 : 0;
        resumen.put("tasaLectura", Math.round(tasaLectura));

        return resumen;
    }

}
