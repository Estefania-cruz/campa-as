package org.example.utils;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import java.io.FileOutputStream;

public class PDFGenerator {
    public static void generarPDF(String htmlContent, String rutaPDF) throws Exception {
        PdfWriter writer = new PdfWriter(new FileOutputStream(rutaPDF));
        PdfDocument pdfDoc = new PdfDocument(writer);

        ConverterProperties props = new ConverterProperties();

        HtmlConverter.convertToPdf(htmlContent, pdfDoc, props);

        pdfDoc.close();
        System.out.println("PDF generado en: " + rutaPDF);
    }
}