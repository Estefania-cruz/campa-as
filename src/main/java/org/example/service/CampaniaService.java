package org.example.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import org.example.model.Campania;
import org.example.repository.CampaniaRepository;

import java.util.List;
import java.time.LocalDate;

@Service
public class CampaniaService {

    @Autowired
    private CampaniaRepository repository;

    @Autowired
    private BotService botService;

    public Campania crear(String nombre, int duracion, String mensaje, String imagen, String numeroUsuario) {

        Campania c = new Campania();
        c.setNombre(nombre);
        c.setDuracionDias(duracion);
        c.setMensaje(mensaje);
        c.setImagen(imagen);
        c.setNumeroUsuario(numeroUsuario);
        String estado = "PENDIENTE".trim().toUpperCase();
        c.setEstado(estado);
        c.setFechaInicio(LocalDate.now());
        c.setUrlLanding(null);
        c.setUrlReporte(null);
        //c.setUsuario(usuario);
        //c.setTipo(tipo);
        //return repository.save(c);
        Campania guardada = repository.save(c);
       // repository.save(c);

        botService.enviarMensaje(
                c.getAsesorTelefono(),
                "⏳ Tu campaña \"" + c.getNombre() + "\" está en revisión. Un supervisor la revisará pronto."
        );
        return guardada;
    }

    @Autowired
    private CampaniaRepository campaniaRepository;

    public List<Campania> obtenerActivas(){
        return campaniaRepository.findByEstado("ACTIVA");
    }

    public List<Campania> obtenerInactivas(){
        return campaniaRepository.findByEstado("INACTIVA");
    }

    public List<Campania> obtenerTodas(){
        return campaniaRepository.findAll();
    }

    public boolean estaVencida(Campania c){

        LocalDate vencimiento =
                c.getFechaInicio().plusDays(c.getDuracionDias());

        return LocalDate.now().isAfter(vencimiento);
    }
    public void activarCampania(Campania c, String numeroUsuario) {

        try {
            c.setEstado("ACTIVA");

            String linkLanding = c.getUrlLanding();
            if (linkLanding == null || linkLanding.isEmpty()) {
                linkLanding = botService.crearLanding(c.getNombre());
                c.setUrlLanding(linkLanding);
            }


            Campania guardada = repository.save(c);
            System.out.println("GUARDADO BD: " + guardada.getUrlLanding());

            botService.enviarMensaje(numeroUsuario,
                    "✅ Campaña activada\n\n" +
                            "Nombre: " + guardada.getNombre() + "\n" +
                            "Landing: " + linkLanding);

        } catch (Exception e) {
            c.setEstado("INACTIVA");
            repository.save(c);
            e.printStackTrace();
        }
    }
   public void aprobarCampania(Long idCampania, String numeroUsuario) {

       Campania c = campaniaRepository.findById(idCampania)
               .orElseThrow(() -> new RuntimeException("Campaña no encontrada"));

       if (!"PENDIENTE".equals(c.getEstado())) {
           System.out.println("⚠️ Campaña ya fue procesada: " + c.getEstado());
           return;
       }

       c.setEstado("APROBADA");
       campaniaRepository.save(c);

       activarCampania(c, numeroUsuario);
   }

    public void activar(String nombre, String numeroUsuario) {
        Campania c = repository.findByNombre(nombre);
        if (c != null) {
            activarCampania(c, numeroUsuario);
        }
    }

    public void desactivar(String nombre) {
        Campania c = repository.findByNombre(nombre);
        if (c != null) {
            c.setEstado("INACTIVA");
            repository.save(c);
        }
    }
}