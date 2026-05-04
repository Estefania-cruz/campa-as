package org.example.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class PdfService {

    private String contenido;

    public String getContenido() {
        return contenido;
    }

    public void cargarPDF() {
        try {
            File file = new File("/Users/aiengine/IdeaProjects/efecampana/landings/resources/Servicios EFV (1).pdf");

            PDDocument document = PDDocument.load(file);

            PDFTextStripper stripper = new PDFTextStripper();
            contenido = stripper.getText(document);

            document.close();

            System.out.println("📄 PDF cargado correctamente");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}