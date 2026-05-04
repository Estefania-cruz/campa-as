package org.example.service;


import com.itextpdf.tool.xml.XMLWorkerHelper;
import java.io.StringReader;
import java.io.*;

import org.example.repository.CampaniaRepository;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.springframework.stereotype.Service;
import org.example.model.Campania;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.Document;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.beans.factory.annotation.Autowired;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.example.repository.ReporteRepository;
import java.awt.Color;

import java.io.StringReader;


@Service
public class ReporteService {

    @Autowired
    private IAService iaService;

    @Autowired
    private ReporteRepository reporteRepository;
    @Autowired
    private CampaniaRepository campaniaRepository;

    public byte[] generarPdf(List<Campania> campanias) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {

            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();
            Image logo = Image.getInstance("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT3zOuU_TR6lMjs-etsClFuVLsuEvo3iKhSwQ&s");
            logo.scaleToFit(400, 400);
            logo.setAlignment(Element.ALIGN_CENTER);
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            document.add(logo);

            for(int i=0; i<5; i++) document.add(new Paragraph(" "));

            Font fontPortadaTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 32);
            Font fontPortadaSub = FontFactory.getFont(FontFactory.HELVETICA, 18);
/*
            Paragraph pTitulo = new Paragraph("SISTEMA DE GESTIÓN DE MARKETING", fontPortadaTitulo);
            pTitulo.setAlignment(Element.ALIGN_CENTER);
            document.add(pTitulo);
*/
            Paragraph pTitulo = new Paragraph("Reporte Ejecutivo de Campañas", fontPortadaTitulo);
            pTitulo.setAlignment(Element.ALIGN_CENTER);
            document.add(pTitulo);

            document.add(new Paragraph(" "));

            Paragraph pFecha = new Paragraph("Generado el: " + formatearFecha(LocalDate.now()), fontPortadaSub);
            pFecha.setAlignment(Element.ALIGN_CENTER);
            document.add(pFecha);

            document.newPage();
            Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Font subtitulo = FontFactory.getFont(FontFactory.HELVETICA, 14);
            Font encabezado = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font texto = FontFactory.getFont(FontFactory.HELVETICA, 12);

            Paragraph title = new Paragraph("REPORTE DE CAMPAÑAS", titulo);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph fecha = new Paragraph("Fecha de generación: " + formatearFecha(LocalDate.now()), subtitulo);
            fecha.setAlignment(Element.ALIGN_CENTER);
            document.add(fecha);

            document.add(new Paragraph(" "));

            PdfPTable tabla = new PdfPTable(5);
            tabla.setWidthPercentage(100);
            tabla.setSpacingBefore(10);

            tabla.addCell(new PdfPCell(new Phrase("Campaña", encabezado)));
            tabla.addCell(new PdfPCell(new Phrase("Estado", encabezado)));
            tabla.addCell(new PdfPCell(new Phrase("Duración (días)", encabezado)));
            tabla.addCell(new PdfPCell(new Phrase("Vence", encabezado)));
            tabla.addCell(new PdfPCell(new Phrase("Mensaje", encabezado)));

            for (Campania c : campanias) {

                String nombre = c.getNombre() != null ? c.getNombre() : "Se desconoce este dato";
                String mensaje = c.getMensaje() != null ? c.getMensaje() : "Se desconoce este dato";

                String estado = "✖ INACTIVA";
                if ("ACTIVA".equalsIgnoreCase(c.getEstado())) {
                    estado = "✔ ACTIVA";
                }

                String vencimiento = "Se desconoce este dato";
                if (c.getDuracionDias() > 0) {
                    LocalDate vence = LocalDate.now().plusDays(c.getDuracionDias());
                    vencimiento = formatearFecha(vence);
                }

                tabla.addCell(new Phrase(nombre, texto));
                tabla.addCell(new Phrase(estado, texto));
                tabla.addCell(new Phrase(String.valueOf(c.getDuracionDias()), texto));
                tabla.addCell(new Phrase(vencimiento, texto));
                tabla.addCell(new Phrase(mensaje, texto));
            }

            document.add(tabla);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Resumen gráfico del estado de campañas", encabezado));

            byte[] grafica = generarGrafica(campanias);
            Image chartImage = Image.getInstance(grafica);
            chartImage.scaleToFit(450, 350);
            chartImage.setAlignment(Element.ALIGN_CENTER);

            document.add(chartImage);

            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Resumen gráfico campañas", encabezado));

            byte[] graficas = generarGraficaBarras(campanias);
            Image chartImages = Image.getInstance(graficas);
            chartImages.scaleToFit(500, 650);
            chartImages.setAlignment(Element.ALIGN_CENTER);

            document.add(chartImages);

//ia
            String contexto = generarContexto(campanias);

            String analisisIA = iaService.analizarCampanias(contexto);

            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("ANÁLISIS DE INTELIGENCIA ARTIFICIAL", encabezado));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            XMLWorkerHelper.getInstance().parseXHtml(
                    writer,
                    document,
                    new StringReader(analisisIA)
            );
//ia
            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    public byte[] generarReporteGeneral() {
        List<Campania> campanias = campaniaRepository.findAll();
        return generarPdf(campanias);
    }

    // Generar reporte por campaña
    public byte[] generarReportePorCampania(String nombreCampania) {
        Campania c = campaniaRepository.findByNombre(nombreCampania);
        if (c == null) return null;

        List<Campania> lista = new ArrayList<>();
        lista.add(c);
        return generarPdf(lista);
    }

    private String formatearFecha(LocalDate fecha) {

        String diaSemana = fecha.getDayOfWeek()
                .getDisplayName(TextStyle.FULL, new Locale("es", "ES"));

        String mes = fecha.getMonth()
                .getDisplayName(TextStyle.FULL, new Locale("es", "ES"));

        return diaSemana.substring(0,1).toUpperCase() + diaSemana.substring(1) +
                " " + fecha.getDayOfMonth() +
                " de " + mes.substring(0,1).toUpperCase() + mes.substring(1) +
                " " + fecha.getYear();
    }


    public byte[] generarGrafica(List<Campania> campanias) {

        long activas = campanias.stream()
                .filter(c -> "ACTIVA".equalsIgnoreCase(c.getEstado()))
                .count();

        long inactivas = campanias.stream()
                .filter(c -> "INACTIVA".equalsIgnoreCase(c.getEstado()))
                .count();

        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Activas", activas);
        dataset.setValue("Inactivas", inactivas);

        JFreeChart chart = ChartFactory.createPieChart(
                "Estado de Campañas Activas E Inactivas",
                dataset,
                true,
                true,
                false
        );

        PiePlot plot =(PiePlot) chart.getPlot();
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0}: {1} ({2})",
                new DecimalFormat("0"),
                new DecimalFormat("0%")
        ));

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            ChartUtils.writeChartAsPNG(out, chart, 500, 400);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    public byte[] generarGraficaBarras(List<Campania> campanias) {
        long activas = campanias.stream().filter(c -> "ACTIVA".equalsIgnoreCase(c.getEstado())).count();
        long inactivas = campanias.stream().filter(c -> "INACTIVA".equalsIgnoreCase(c.getEstado())).count();
        long total = activas + inactivas;

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(activas, "Campañas", "Activas");
        dataset.addValue(inactivas, "Campañas", "Inactivas");

        JFreeChart chart = ChartFactory.createBarChart(
                "Estado de Campañas Actuales",       // Título
                "Estado",                   // Etiqueta eje X
                "Cantidad",                 // Etiqueta eje Y
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();

        renderer.setDefaultItemLabelsVisible(true);

        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator(
                "{2}", new DecimalFormat("0")
        ));

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        renderer.setSeriesPaint(0, new Color(238, 10, 57));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ChartUtils.writeChartAsPNG(out, chart, 700, 450);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }


    private String generarContexto(List<Campania> campanias){

        StringBuilder contexto = new StringBuilder();

        for(Campania c : campanias){
            contexto.append("Campaña: ").append(c.getNombre()).append("\n");
            contexto.append("Estado: ").append(c.getEstado()).append("\n");
            contexto.append("Duración: ").append(c.getDuracionDias()).append(" días\n");
            contexto.append("Mensaje: ").append(c.getMensaje()).append("\n\n");
        }

        return contexto.toString();
    }


    public String generarYGuardarReporte(Campania c) {

        byte[] pdf = generarReportePorCampania(c.getNombre());

        if (pdf == null) return null;

        String linkReporte = "https://8755-189-216-169-142.ngrok-free.app/reporte?campania=" + c.getNombre();

        c.setUrlReporte(linkReporte);
        campaniaRepository.save(c);

        return linkReporte;
    }
    //presentacion de powerpoint por ia
}