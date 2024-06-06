package be.iccbxl.tfe.Driveshare.controller;

import be.iccbxl.tfe.Driveshare.model.Category;
import be.iccbxl.tfe.Driveshare.model.Price;
import be.iccbxl.tfe.Driveshare.repository.CategoryRepository;
import be.iccbxl.tfe.Driveshare.repository.PriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class PriceRestController {

    @Autowired
    PriceRepository priceRepository;

    @Autowired
    CategoryRepository categoryRepository;



}
