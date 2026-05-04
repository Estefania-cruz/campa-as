package org.example.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.example.service.BotService;
import org.example.service.CampaniaService;
import java.util.List;

import org.example.model.Campania;

@Service
public class SchedulerService {

    private final CampaniaService campaniaService;
    private final BotService botService;

    public SchedulerService(CampaniaService campaniaService, BotService botService) {
        this.campaniaService = campaniaService;
        this.botService = botService;
    }

    @Scheduled(fixedRate = 3600000)
    public void revisarCampanias(){

        List<Campania> campanias = campaniaService.obtenerActivas();

        for(Campania c : campanias){

            if(campaniaService.estaVencida(c)){

                botService.enviarMensaje(
                        "admin",
                        "La campaña " + c.getNombre() + " ya venció. ¿Deseas reactivarla?"
                );
            }
        }
    }
}