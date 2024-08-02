package be.iccbxl.tfe.Driveshare.restController;

import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@org.springframework.web.bind.annotation.RestController
public class BrandAndModelRestController {



    private static final List<String> brands = Arrays.asList(
            "Aixam", "Alfa Romeo", "Aston Martin", "Audi",
            "Bentley", "BMW",
            "Chevrolet", "Chrysler", "Citroën", "Cupra",
            "Dacia", "Daewoo", "Daihatsu", "Dodge", "DS",
            "Ferrari", "Fiat", "Ford",
            "Honda", "Hyundai",
            "Infiniti", "ISUZU", "IVECO",
            "Jaguar", "Jeep",
            "Kia",
            "Lamborghini", "Land Rover", "Lexus",
            "Mazda", "Mercedes-Benz", "MG", "Mini", "Mitsubishi",
            "Nissan",
            "Opel",
            "Peugeot", "Porsche",
            "Renault", "Rolls-Royce", "Rover",
            "Saab", "Skoda", "Smart", "Subaru", "Suzuki",
            "Tesla", "Toyota",
            "Volkswagen", "Volvo"
    );



    private static final Map<String, List<String>> models = Map.ofEntries(
            Map.entry("Aixam", Arrays.asList("CITY GTO", "City PACK", "CITY SPORT", "COUPE EVO", "COUPE CITY", "COUPE PREMIUM", "CROSSLINE EVO", "CROSSLINE PREMIUM", "DTRUCK", "eCITY", "eCOUPE", "eCROSSOVER", "MINAUTO ACCESS", "MINAUTO CROSS", "MINAUTO GT", "S9", "S8")),
            Map.entry("Alfa Romeo", Arrays.asList("Giulia", "Stelvio", "4C", "Giulietta")),
            Map.entry("Aston Martin", Arrays.asList("DB11", "Vantage", "DBS", "DBX")),
            Map.entry("Audi", Arrays.asList("A1", "A3", "A4", "A5", "A6", "A7", "A8", "Q2", "Q3", "Q5", "Q7", "Q8", "TT", "R8")),
            Map.entry("Bentley", Arrays.asList("Continental GT", "Bentayga", "Flying Spur", "Mulsanne")),
            Map.entry("BMW", Arrays.asList("1 Series", "2 Series", "3 Series", "4 Series", "5 Series", "6 Series", "7 Series", "8 Series", "X1", "X2", "X3", "X4", "X5", "X6", "X7", "Z4", "i3", "i8")),
            Map.entry("Chevrolet", Arrays.asList("Spark", "Sonic", "Cruze", "Malibu", "Impala", "Camaro", "Corvette", "Trax", "Equinox", "Blazer", "Traverse", "Tahoe", "Suburban")),
            Map.entry("Chrysler", Arrays.asList("Pacifica", "300", "Voyager")),
            Map.entry("Citroën", Arrays.asList("C1", "C3", "C3 Aircross", "C4", "C4 Cactus", "C5 Aircross")),
            Map.entry("Cupra", Arrays.asList("Leon", "Ateca", "Formentor")),
            Map.entry("Dacia", Arrays.asList("Sandero", "Logan", "Duster", "Spring Electric")),
            Map.entry("Daewoo", Arrays.asList("Matiz", "Lanos", "Nubira", "Leganza")),
            Map.entry("Daihatsu", Arrays.asList("Terios", "Sirion", "Copen", "Mira")),
            Map.entry("Dodge", Arrays.asList("Charger", "Challenger", "Durango", "Journey")),
            Map.entry("DS", Arrays.asList("DS 3", "DS 3 Crossback", "DS 4", "DS 7 Crossback")),
            Map.entry("Ferrari", Arrays.asList("488 GTB", "Portofino", "812 Superfast", "F8 Tributo")),
            Map.entry("Fiat", Arrays.asList("500", "500X", "Panda", "Tipo")),
            Map.entry("Ford", Arrays.asList("Fiesta", "Focus", "Mustang", "Explorer", "Fusion", "Escape", "Edge", "F-150", "Ranger")),
            Map.entry("Honda", Arrays.asList("Civic", "Accord", "CR-V", "Fit", "HR-V", "Pilot", "Odyssey")),
            Map.entry("Hyundai", Arrays.asList("Elantra", "Sonata", "Santa Fe", "Tucson", "Kona", "Veloster", "Palisade")),
            Map.entry("Infiniti", Arrays.asList("Q50", "Q60", "QX50", "QX60", "QX80")),
            Map.entry("ISUZU", Arrays.asList("D-Max", "MU-X")),
            Map.entry("IVECO", Arrays.asList("Daily", "Eurocargo", "Stralis")),
            Map.entry("Jaguar", Arrays.asList("XE", "XF", "XJ", "F-Type", "E-Pace", "F-Pace", "I-Pace")),
            Map.entry("Jeep", Arrays.asList("Wrangler", "Grand Cherokee", "Compass", "Renegade")),
            Map.entry("Kia", Arrays.asList("Rio", "Forte", "Optima", "Sorento", "Sportage", "Telluride", "Stinger")),
            Map.entry("Lamborghini", Arrays.asList("Huracan", "Aventador", "Urus")),
            Map.entry("Land Rover", Arrays.asList("Discovery", "Discovery Sport", "Range Rover", "Range Rover Sport", "Range Rover Velar", "Range Rover Evoque")),
            Map.entry("Lexus", Arrays.asList("IS", "ES", "GS", "LS", "RX", "GX", "LX", "NX", "UX")),
            Map.entry("Mazda", Arrays.asList("Mazda3", "Mazda6", "CX-3", "CX-30", "CX-5", "CX-9", "MX-5 Miata")),
            Map.entry("Mercedes-Benz", Arrays.asList("A-Class", "C-Class", "E-Class", "S-Class", "GLA", "GLC", "GLE", "GLS")),
            Map.entry("Mitsubishi", Arrays.asList("Mirage", "Eclipse Cross", "Outlander", "Outlander Sport", "Pajero")),
            Map.entry("MG", Arrays.asList("MG3", "ZS", "HS", "MG5")),
            Map.entry("Mini", Arrays.asList("Cooper", "Countryman", "Clubman")),
            Map.entry("Nissan", Arrays.asList("Micra", "Leaf", "Juke", "Qashqai", "X-Trail", "Navara", "GT-R")),
            Map.entry("Opel", Arrays.asList("Corsa", "Astra", "Insignia", "Crossland X", "Grandland X")),
            Map.entry("Peugeot", Arrays.asList("208", "308", "2008", "3008", "5008")),
            Map.entry("Porsche", Arrays.asList("911", "Panamera", "Macan", "Cayenne", "Taycan")),
            Map.entry("Renault", Arrays.asList("Clio", "Megane", "Kadjar", "Captur", "Zoe")),
            Map.entry("Rolls-Royce", Arrays.asList("Phantom", "Ghost", "Wraith", "Dawn", "Cullinan")),
            Map.entry("Rover", Arrays.asList("Range Rover", "Range Rover Sport", "Range Rover Evoque")),
            Map.entry("Saab", Arrays.asList("9-3", "9-5")),
            Map.entry("Skoda", Arrays.asList("Fabia", "Octavia", "Superb", "Kodiaq", "Karoq")),
            Map.entry("Smart", Arrays.asList("Fortwo", "Forfour")),
            Map.entry("Subaru", Arrays.asList("Impreza", "Legacy", "Forester", "Outback", "Crosstrek", "Ascent")),
            Map.entry("Suzuki", Arrays.asList("Swift", "Baleno", "Vitara", "SX4 S-Cross", "Jimny")),
            Map.entry("Tesla", Arrays.asList("Model S", "Model 3", "Model X", "Model Y")),
            Map.entry("Toyota", Arrays.asList("Yaris", "Corolla", "Camry", "Prius", "RAV4", "Highlander", "Tacoma", "Tundra")),
            Map.entry("Volkswagen", Arrays.asList("Polo", "Golf", "Passat", "Tiguan", "Touareg", "Arteon")),
            Map.entry("Volvo", Arrays.asList("S60", "S90", "V60", "V90", "XC40", "XC60", "XC90"))
    );




    @GetMapping("/brands")
    public List<String> getAllBrands() {
        return brands;
    }

    @GetMapping("/models")
    public List<String> getModelsByBrand(String brand) {
        return models.getOrDefault(brand, Arrays.asList());
    }


}
