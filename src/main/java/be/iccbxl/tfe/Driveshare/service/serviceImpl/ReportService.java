package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.DTO.PaymentDTO;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    public ByteArrayInputStream generatePaymentReport(List<PaymentDTO> payments) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Title
            document.add(new Paragraph("Rapport des Paiements")
                    .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            // Summary Statistics
            document.add(new Paragraph("Résumé des Statistiques")
                    .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                    .setFontSize(16)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(10));

            int totalPayments = payments.size();
            double totalAmount = payments.stream().mapToDouble(PaymentDTO::getPrixTotal).sum();
            long successfulPayments = payments.stream().filter(p -> "succeeded".equalsIgnoreCase(p.getStatut())).count();
            long failedPayments = payments.stream().filter(p -> "failed".equalsIgnoreCase(p.getStatut())).count();

            document.add(new Paragraph("Nombre total de paiements : " + totalPayments));
            document.add(new Paragraph("Montant total des paiements : " + totalAmount + " EUR"));
            document.add(new Paragraph("Paiements réussis : " + successfulPayments));
            document.add(new Paragraph("Paiements échoués : " + failedPayments));
            document.add(new Paragraph(" ").setMarginBottom(10));

            // Payment Table
            Table table = new Table(UnitValue.createPercentArray(new float[]{10, 15, 10, 15, 20, 30}));
            table.setWidth(UnitValue.createPercentValue(100));

            // Table Header
            table.addHeaderCell(new Cell().add(new Paragraph("ID").setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Prix Total")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Statut")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Mode de Paiement")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Date de Création")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Détails")).setBackgroundColor(ColorConstants.LIGHT_GRAY));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // Table Rows
            for (PaymentDTO payment : payments) {
                table.addCell(new Cell().add(new Paragraph(payment.getId().toString())));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(payment.getPrixTotal()))));
                table.addCell(new Cell().add(new Paragraph(payment.getStatut())));
                table.addCell(new Cell().add(new Paragraph(payment.getPaiementMode())));
                table.addCell(new Cell().add(new Paragraph(payment.getCreatedAt().format(formatter))));
                table.addCell(new Cell().add(new Paragraph("ID Réservation : " + payment.getReservationId())));
            }

            document.add(table);

            // Add Charts Section
            document.add(new Paragraph(" ").setMarginBottom(20));
            document.add(new Paragraph("Graphiques").setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setFontSize(16).setTextAlignment(TextAlignment.LEFT));

            // Generate and add daily payments chart
            byte[] dailyChart = createDailyPaymentsChart(payments);
            ImageData dailyImageData = ImageDataFactory.create(dailyChart);
            Image dailyImage = new Image(dailyImageData);
            document.add(dailyImage.setAutoScale(true).setMarginBottom(20));

            // Generate and add monthly payments chart
            byte[] monthlyChart = createMonthlyPaymentsChart(payments);
            ImageData monthlyImageData = ImageDataFactory.create(monthlyChart);
            Image monthlyImage = new Image(monthlyImageData);
            document.add(monthlyImage.setAutoScale(true).setMarginBottom(20));

            document.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private byte[] createDailyPaymentsChart(List<PaymentDTO> payments) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Map<LocalDate, Double> dailyData = payments.stream()
                .collect(Collectors.groupingBy(
                        payment -> payment.getCreatedAt().toLocalDate(),
                        Collectors.summingDouble(PaymentDTO::getPrixTotal)
                ));

        LocalDate today = LocalDate.now();
        for (int i = 0; i < 30; i++) {
            LocalDate date = today.minusDays(i);
            dataset.addValue(dailyData.getOrDefault(date, 0.0), "Paiements quotidiens", date.toString());
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "Paiements quotidiens (30 derniers jours)",
                "Date",
                "Total des Paiements",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        chart.setBackgroundPaint(Color.white);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(out, chart, 600, 400);
        return out.toByteArray();
    }

    private byte[] createMonthlyPaymentsChart(List<PaymentDTO> payments) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        Map<String, Double> monthlyData = payments.stream()
                .collect(Collectors.groupingBy(
                        payment -> payment.getCreatedAt().toLocalDate().withDayOfMonth(1).format(formatter),
                        Collectors.summingDouble(PaymentDTO::getPrixTotal)
                ));

        LocalDate today = LocalDate.now();
        for (int i = 0; i < 12; i++) {
            LocalDate date = today.minusMonths(i).withDayOfMonth(1);
            dataset.addValue(monthlyData.getOrDefault(date.format(formatter), 0.0), "Paiements mensuels", date.format(formatter));
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "Paiements mensuels (12 derniers mois)",
                "Date",
                "Total des Paiements",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        chart.setBackgroundPaint(Color.white);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(out, chart, 600, 400);
        return out.toByteArray();
    }
}

