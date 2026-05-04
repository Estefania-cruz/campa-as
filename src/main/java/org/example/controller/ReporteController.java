package org.example.controller;

import org.example.service.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/reporte")
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    @GetMapping("/general")
    public ResponseEntity<ByteArrayResource> reporteGeneral() {
        byte[] pdf = reporteService.generarReporteGeneral();
        ByteArrayResource resource = new ByteArrayResource(pdf);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=reporte_general.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(resource);
    }

    @GetMapping("")
    public ResponseEntity<ByteArrayResource> reportePorCampania(@RequestParam("campania") String nombreCampania) {
        byte[] pdf = reporteService.generarReportePorCampania(nombreCampania);
        if (pdf == null) {
            return ResponseEntity.notFound().build();
        }

        ByteArrayResource resource = new ByteArrayResource(pdf);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=reporte_" + nombreCampania + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(resource);
    }
}