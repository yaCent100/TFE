


document.addEventListener("DOMContentLoaded", function() {
// Obtenez les éléments de sélection
var marqueSelect = document.getElementById("marque");
var modeleSelect = document.getElementById("modele");

// Définir les modèles pour chaque marque
var marques = {
      "Abarth": ["124 Spider", "595", "695"],
            "Aiways": ["U5"],
            "Alfa Romeo": ["Giulia", "Stelvio", "Giulietta", "4C", "MiTo"],
            "Alpine": ["A110"],
            "Artega": ["GT"],
            "Aston Martin": ["DB11", "Vantage", "DBS Superleggera", "DBS GT Zagato", "Rapide"],
            "Audi": ["A3", "A4", "A5", "Q3", "Q5", "Q7", "Q8", "e-tron", "TT"],
            "BAIC": ["EU5", "EU7", "EX5"],
            "BAW": ["Foton"],
            "Bentley": ["Continental GT", "Bentayga", "Flying Spur", "Mulsanne"],
            "Bestune": ["T33", "T77", "T77L"],
            "BMW": ["Série 1", "Série 2", "Série 3", "Série 4", "Série 5", "Série 6", "Série 7", "X1", "X2", "X3", "X4", "X5", "X6", "X7", "Z4", "i3", "i8"],
            "BMW Alpina": ["B3", "B5", "B7", "B8", "D3", "D5"],
            "BYD": ["e2", "e3", "e5", "Tang", "Song", "Han", "Yuan"],
            "Cadillac": ["CTS", "Escalade", "XT5", "XTS", "CT6"],
            "Caterham": ["Seven 270", "Seven 310", "Seven 360", "Seven 420", "Seven 620"],
            "Chevrolet": ["Camaro", "Corvette", "Malibu", "Spark", "Tahoe", "Trax"],
            "Chrysler": ["300", "Pacifica", "Voyager"],
            "Citroën": ["C3", "C4", "C5", "C3 Aircross", "C4 Cactus", "C4 Picasso", "C5 Aircross", "Berlingo", "Jumpy", "SpaceTourer"],
            "Cupra": ["Ateca", "Leon", "Formentor"],
            "Dacia": ["Sandero", "Logan", "Duster"],
            "Daihatsu": ["Cuore", "Sirion", "Terios"],
            "DFSK": ["Glory 330", "580", "Fengon 5", "AX4", "EC35"],
            "Dodge": ["Challenger", "Charger", "Durango", "Journey"],
            "Donkervoort": ["D8 GTO", "D8 GT"],
            "DS": ["3", "7 Crossback"],
                "Ferrari": ["488", "812 Superfast", "F8 Tributo", "Portofino", "SF90 Stradale"],
                "Fiat": ["500", "Panda", "Tipo", "500X", "500L", "Doblo", "124 Spider"],
                "Fisker": ["Karma"],
                "Ford": ["Fiesta", "Focus", "Mondeo", "Kuga", "EcoSport", "Puma", "Mustang", "Ranger"],
                "Forthing": ["E200", "E300"],
                "Honda": ["Civic", "Accord", "HR-V", "CR-V", "Jazz", "e", "NSX"],
                "Hummer": ["H2", "H3"],
                "Hyundai": ["i10", "i20", "i30", "i40", "Kona", "Tucson", "Santa Fe", "Nexo"],
                "Ineos": ["Grenadier"],
                "Infiniti": ["Q50", "Q60", "QX50", "QX60", "QX80"],
                "Isuzu": ["D-Max", "Mux"],
                "Jaguar": ["XE", "XF", "XJ", "F-Type", "E-Pace", "F-Pace", "I-Pace"],
                "Jeep": ["Renegade", "Compass", "Cherokee", "Wrangler", "Grand Cherokee"],
                "KGM": ["Huangai"],
                "KIA": ["Picanto", "Rio", "Ceed", "Niro", "Sportage", "Sorento", "Stonic"],
                "KTM": ["X-Bow"],
                "Lada": ["Granta", "Vesta", "Largus", "4x4", "Niva"],
                "Lamborghini": ["Huracan", "Aventador", "Urus"],
                "Lancia": ["Ypsilon"],
                "Land Rover": ["Discovery", "Discovery Sport", "Defender", "Range Rover", "Range Rover Sport", "Range Rover Velar", "Range Rover Evoque"],
                "Lexus": ["IS", "ES", "GS", "LS", "UX", "NX", "RX", "RC", "LC", "LM"],
                "Lotus": ["Evora", "Exige", "Elise"],
                "Lynk & Co": ["01", "02", "03", "05"],
                "Maserati": ["Ghibli", "Quattroporte", "Levante", "GranTurismo", "GranCabrio"],
                "Maxus": ["EV80", "V80", "T60", "G10"],
                "Mazda": ["2", "3", "6", "CX-3", "CX-30", "CX-5", "MX-5", "MX-30"],
                "McLaren": ["540C", "570S", "600LT", "720S", "Senna", "GT"],
                "Mercedes-Benz": ["Classe A", "Classe B", "Classe C", "Classe E", "Classe S", "CLA", "GLA", "GLC", "GLE", "GLS", "GLE Coupe", "GLS Coupe", "Vito", "Sprinter", "EQC"],
                "MG": ["3", "ZS", "HS"],
                "Mia Electric": ["Mia"],
                "MINI": ["3 portes", "5 portes", "Clubman", "Countryman", "Cabrio", "John Cooper Works"],
                "Mitsubishi": ["Space Star", "ASX", "Eclipse Cross", "Outlander", "L200"],
                "Nissan": ["Micra", "Juke", "Qashqai", "X-Trail", "Leaf", "Navara", "GT-R"],
                "Opel": ["Corsa", "Astra", "Insignia", "Crossland X", "Grandland X", "Mokka"],
                "Peugeot": ["108", "208", "308", "508", "2008", "3008", "5008", "Traveller", "Rifter"],
                "Polestar": ["1"],
                "Porsche": ["911", "Panamera", "Cayenne", "Macan", "Taycan", "718"],
                "Renault": ["Twingo", "Clio", "Megane", "Scenic", "Kadjar", "Captur", "Koleos", "Zoe"],
                "Rolls-Royce": ["Phantom", "Ghost", "Wraith", "Dawn", "Cullinan"],
                "Saab": ["9-3", "9-5"],
                "Seat": ["Mii", "Ibiza", "Leon", "Arona", "Ateca", "Tarraco", "Alhambra"],
                "Seres": ["3"],
                "Skoda": ["Citigo", "Fabia", "Scala", "Octavia", "Superb", "Kamiq", "Karoq", "Kodiaq", "Enyaq iV"],
                "Smart": ["Fortwo", "Forfour"],
                "Ssangyong": ["Tivoli", "Korando", "Rexton", "Musso"],
                "Subaru": ["Impreza", "Legacy", "Outback", "XV", "BRZ", "Forester"],
                "Suzuki": ["Swift", "Ignis", "Baleno", "S-Cross", "Vitara", "Jimny"],
                "SWM": ["G01", "X7"],
                "Tesla": ["Model 3", "Model S", "Model X", "Model Y", "Roadster"],
                "Toyota": ["Aygo", "Yaris", "Corolla", "Camry", "C-HR", "RAV4", "Highlander", "Land Cruiser", "Prius", "Mirai"],
                "Volkswagen": ["Up", "Polo", "Golf", "Passat", "Arteon", "T-Roc", "Tiguan", "Touran", "T-Cross", "ID.3", "ID.4", "Touareg", "Multivan", "Transporter", "Caddy"],
                "Volvo": ["S60", "S90", "V60", "V90", "XC40", "XC60", "XC90"]
    // Ajoutez d'autres marques et leurs modèles ici
};

    // Fonction pour mettre à jour les options de modèle en fonction de la marque sélectionnée
// Fonction pour mettre à jour les options de modèle en fonction de la marque sélectionnée
 function updateModele() {
        console.log("Fonction updateModele() appelée.");
        var selectedMarque = marqueSelect.value.toLowerCase(); // Obtenez la valeur sélectionnée
        console.log("Marque sélectionnée :", selectedMarque);
        modeleSelect.innerHTML = "";

        if (marques[selectedMarque]) {
            console.log("Modèles disponibles pour la marque :", marques[selectedMarque]);
            marques[selectedMarque].forEach(function(modele) {
                var option = document.createElement("option");
                option.text = modele;
                option.value = modele.toLowerCase();
                modeleSelect.add(option);
            });
        } else {
            console.log("Aucun modèle disponible pour la marque sélectionnée.");
            var option = document.createElement("option");
            option.text = "Aucun modèle disponible";
            modeleSelect.add(option);
        }
    }

    // Ajoutez un écouteur d'événements pour détecter les changements dans le sélecteur de marque
    marqueSelect.addEventListener("change", updateModele);

    // Appelez la fonction une fois au chargement de la page pour initialiser les modèles
    updateModele();
});


document.addEventListener("DOMContentLoaded", function() {
    // Sélecteur des codes postaux
    var codePostalSelect = document.getElementById("code_postal");
    // Sélecteur des communes
    var communeSelect = document.getElementById("commune");

    // Liste des codes postaux et des communes de Bruxelles
    var codesPostauxCommunes = {
        "1000": "Bruxelles",
        "1020": "Laeken",
        "1030": "Schaerbeek",
        "1040": "Etterbeek",
        "1050": "Ixelles",
        "1060": "Saint-Gilles",
        "1070": "Anderlecht",
        "1080": "Molenbeek-Saint-Jean",
        "1081": "Koekelberg",
        "1082": "Berchem-Sainte-Agathe",
        "1083": "Ganshoren",
        "1090": "Jette",
        "1120": "Neder-Over-Heembeek",
        "1130": "Haren"
        // Ajoutez d'autres codes postaux et communes si nécessaire
    };

    // Fonction pour ajouter les options des codes postaux
    function addOptions(select, options) {
        for (var codePostal in options) {
            var option = document.createElement("option");
            option.value = codePostal;
            option.textContent = codePostal;
            select.appendChild(option);
        }
    }

    // Ajout des options pour les codes postaux
    addOptions(codePostalSelect, codesPostauxCommunes);

    // Écouteur d'événement pour la sélection d'un code postal
    codePostalSelect.addEventListener("change", function() {
        var codePostal = this.value;
        var commune = codesPostauxCommunes[codePostal];
        if (commune) {
            // Efface les options actuelles
            communeSelect.innerHTML = "";
            // Ajoute la commune correspondante comme option sélectionnée
            var option = document.createElement("option");
            option.value = commune;
            option.textContent = commune;
            communeSelect.appendChild(option);
        }
    });

    // Écouteur d'événement pour la sélection d'une commune
    communeSelect.addEventListener("change", function() {
        var commune = this.value;
        // Recherche du code postal correspondant à la commune
        var codePostal = Object.keys(codesPostauxCommunes).find(key => codesPostauxCommunes[key] === commune);
        if (codePostal) {
            // Sélectionne le code postal correspondant dans le premier select
            codePostalSelect.value = codePostal;
        }
    });
});
