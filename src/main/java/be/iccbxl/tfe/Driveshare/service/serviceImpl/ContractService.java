package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

// Assuming you have these imports:
import be.iccbxl.tfe.Driveshare.model.Reservation;


@Service
public class ContractService {

    private final ReservationService reservationService;
    private final UserService userService;

    public ContractService(ReservationService reservationService, UserService userService) {
        this.reservationService = reservationService;
        this.userService = userService;
    }

    public byte[] generateContractPdf(Long reservationId) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(outputStream)) {
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument, PageSize.A4);

            // Fetch reservation details
            Reservation reservation = reservationService.getReservationById(reservationId);
            if (reservation == null) {
                throw new RuntimeException("Reservation not found");
            }

            // Get tenant and owner details
            String tenantName = reservation.getUser().getNom()+ " " +reservation.getUser().getPrenom();
            String tenantAddress = reservation.getUser().getAdresse() + ", " + reservation.getUser().getCodePostal() +' '+reservation.getUser().getLocality();
            String tenantPhone = reservation.getUser().getTelephoneNumber();
            String tenantEmail = reservation.getUser().getEmail();

            String ownerName = reservation.getCar().getUser().getNom()+ " " +reservation.getCar().getUser().getPrenom();
            String ownerAddress = reservation.getCar().getUser().getAdresse() + ", " + reservation.getCar().getUser().getCodePostal() +' '+reservation.getUser().getLocality();
            String ownerPhone = reservation.getCar().getUser().getTelephoneNumber();
            String ownerEmail = reservation.getCar().getUser().getEmail();

            String carDetails = reservation.getCar().getBrand() + " " + reservation.getCar().getModel();
            String carPlate = reservation.getCar().getPlaqueImmatriculation();
            String startDate = reservation.getDebutLocation().toString();
            String endDate = reservation.getFinLocation().toString();
            String totalAmount = String.valueOf(reservation.getPayment().getPrixTotal());

            InputStream logoStream = getClass().getResourceAsStream("/static/images/DriveShareLogo.png");
            ImageData imageData = ImageDataFactory.create(logoStream.readAllBytes());
            Image logo = new Image(imageData);
            logo.setWidth(100);
            logo.setFontSize(50);
            logo.setFixedPosition(50, PageSize.A4.getHeight() - 50); // Adjust position as needed

            document.add(logo);

            // Title
            document.add(new Paragraph("CONTRAT DE LOCATION")
                    .setBold()
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            document.add(new Paragraph("Réservation n° " + reservationId)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));


            // Vertical Line Divider
            float[] columnWidths = {1, 1};
            Table table = new Table(columnWidths);
            table.setWidth(100);
            table.setRelativePosition(150,0,0,0);



            // Left Side - Tenant (Locataire)
            Cell cellLeft = new Cell()
                    .add(new Paragraph("Locataire")).setFontSize(15)
                    .add(new Paragraph(tenantName))
                    .add(new Paragraph(tenantAddress))
                    .add(new Paragraph(tenantPhone))
                    .add(new Paragraph(tenantEmail))
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.CENTER);

            // Right Side - Owner (Propriétaire)
            Cell cellRight = new Cell()
                    .add(new Paragraph("Propriétaire")).setFontSize(15)
                    .add(new Paragraph(ownerName))
                    .add(new Paragraph( ownerAddress))
                    .add(new Paragraph( ownerPhone))
                    .add(new Paragraph(ownerEmail))
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.CENTER);


            // Add cells to table
            table.addCell(cellLeft).setRelativePosition(100,0,0,0);;
            table.addCell(cellRight);

            // Add the table to the document
            document.add(table);

            // Details of the contract
            document.add(new Paragraph("Détails de la location").setBold().setUnderline().setMarginTop(20));
            document.add(new Paragraph("Voiture: " + carDetails));
            document.add(new Paragraph("Date de début: " + startDate));
            document.add(new Paragraph("Date de fin: " + endDate));
            document.add(new Paragraph("Montant total: " + totalAmount));

            // Line for Signatures
            document.add(new Paragraph(" ").setMarginTop(30));
            table = new Table(new float[]{1, 1});
            table.setWidth(100);

            cellLeft = new Cell()
                    .add(new Paragraph("Signature du locataire:").setMarginTop(20))
                    .add(new Paragraph("____________________________"))
                    .setBorder(Border.NO_BORDER);

            cellRight = new Cell()
                    .add(new Paragraph("Signature du propriétaire:").setMarginTop(20))
                    .add(new Paragraph("____________________________"))
                    .setBorder(Border.NO_BORDER);

            table.addCell(cellLeft);
            table.addCell(cellRight);
            document.add(table);

            // Close the document
            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return outputStream.toByteArray();
    }
}
