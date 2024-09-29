package be.iccbxl.tfe.Driveshare.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.file.Files;

@Controller
public class DocumentController {

    @GetMapping("/documents/conditions-generales.pdf")
    @ResponseBody
    public ResponseEntity<byte[]> getConditionsGenerales() throws IOException {
        ClassPathResource pdfFile = new ClassPathResource("static/uploads/documents/conditions-generales.pdf");
        byte[] pdfBytes = Files.readAllBytes(pdfFile.getFile().toPath());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=conditions-generales.pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/documents/politique-de-confidentialite.pdf")
    @ResponseBody
    public ResponseEntity<byte[]> getPolitiqueConfidentialite() throws IOException {
        ClassPathResource pdfFile = new ClassPathResource("static/uploads/documents/politique-de-confidentialite.pdf");
        byte[] pdfBytes = Files.readAllBytes(pdfFile.getFile().toPath());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=politique-de-confidentialite.pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
