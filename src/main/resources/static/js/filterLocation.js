document.addEventListener('DOMContentLoaded', () => {
    console.log("Initializing cars page script...");

    let carData = [];
    let currentSort = 'default';

    // Fonction d'affichage des voitures
    function displayCars(cars) {
        console.log("Displaying cars:", cars);

        const carListElement = document.getElementById('carList');

        if (!carListElement) {
            console.error("Element with id 'carList' not found.");
            return;
        }

        carListElement.innerHTML = ''; // Vider la liste avant d'ajouter les nouvelles voitures

        if (cars.length === 0) {
            carListElement.innerHTML = '<p>Aucune voiture disponible pour les critères de recherche spécifiés.</p>';
            return;
        }

        cars.forEach(item => {
            const car = item.car;
            const averageRating = item.averageRating || 0;
            const reviewCount = item.reviewCount || 0;
            const acceptanceRate = item.acceptanceRate || 0;

            const carCard = document.createElement('div');
            carCard.classList.add('card', 'p-3', 'rounded', 'mb-3');

            const carLink = document.createElement('a');
            carLink.classList.add('card-link');
            carLink.href = `/cars/${car.id}`;

            const rowDiv = document.createElement('div');
            rowDiv.classList.add('row', 'g-0');

            // Colonne pour l'image
            const colImage = document.createElement('div');
            colImage.classList.add('col-md-3');
            const carImage = document.createElement('img');
            carImage.classList.add('card-img-top');
            carImage.style.maxHeight = '150px';
            carImage.src = car.photoUrl && car.photoUrl.length > 0 ? `/uploads/${car.photoUrl[0]}` : '/images/carDefault.png';
            carImage.alt = `Photo de voiture`;
            colImage.appendChild(carImage);

            // Colonne pour le contenu de la carte
            const colContent = document.createElement('div');
            colContent.classList.add('col-md-9');

            const cardBody = document.createElement('div');
            cardBody.classList.add('card-body');

            const rowContent = document.createElement('div');
            rowContent.classList.add('row');

            const colText = document.createElement('div');
            colText.classList.add('col-lg-8', 'col-md-7');

            const carTitle = document.createElement('h5');
            carTitle.classList.add('card-title');
            carTitle.textContent = `${car.brand} ${car.model}`;
            colText.appendChild(carTitle);

            // Display ratings
            const ratingsDiv = document.createElement('div');
            for (let i = 1; i <= 5; i++) {
                const star = document.createElement('span');
                star.classList.add('fa', i <= averageRating ? 'fa-star' : 'fa-star-o');
                ratingsDiv.appendChild(star);
            }
            colText.appendChild(ratingsDiv);

            // Display number of reviews
            const reviewCountElement = document.createElement('div');
            reviewCountElement.textContent = `(${reviewCount} avis)`;
            colText.appendChild(reviewCountElement);

            // Display price
            const colPrice = document.createElement('div');
            colPrice.classList.add('col-lg-3', 'col-md-4');
            const priceElement = document.createElement('p');
            priceElement.classList.add('priceIndex');

            // Vérification de l'existence de `car.price` et `car.price.middlePrice`
            if (car.price && car.price.middlePrice !== undefined) {
                priceElement.textContent = `${car.price.middlePrice} €/jour`;
            } else {
                priceElement.textContent = 'Prix non disponible';
            }
            colPrice.appendChild(priceElement);

            rowContent.appendChild(colText);
            rowContent.appendChild(colPrice);

            cardBody.appendChild(rowContent);

            // Add category and distance
            const colDetails = document.createElement('div');
            colDetails.classList.add('col-lg-9');
            const details = document.createElement('p');
            // Vérifie si `car.category` est défini et contient un champ `category`
            if (car.categoryName) {
                console.log("Car category:", car.categoryName);
                details.innerHTML = `<img src="icones/voiture-electrique.png" alt="" height="15"> ${car.categoryName || 'Catégorie non spécifiée'}`;
            } else {
                console.warn("Category name is undefined for car:", car);
                details.innerHTML = `<img src="icones/voiture-electrique.png" alt="" height="15"> Catégorie non spécifiée`;
            }
            colDetails.appendChild(details);

            // Afficher la distance
            const distanceElement = document.createElement('p');
            distanceElement.textContent = `Distance: ${car.distance.toFixed(2)} km`;
            colDetails.appendChild(distanceElement);

            cardBody.appendChild(colDetails);

            colContent.appendChild(cardBody);

            rowDiv.appendChild(colImage);
            rowDiv.appendChild(colContent);

            carLink.appendChild(rowDiv);
            carCard.appendChild(carLink);

            carListElement.appendChild(carCard);
        });

        carListElement.style.display = 'block';
    }

    // Récupérer les résultats de recherche stockés dans localStorage
    const searchResults = localStorage.getItem('searchResults');
    if (searchResults) {
        const cars = JSON.parse(searchResults);
        carData = cars;
        console.log("Loaded search results from localStorage:", carData);
        filterCars();

        // Optionnel : Supprimer les résultats de localStorage après affichage
        localStorage.removeItem('searchResults');
    } else {
        console.error('Aucun résultat de recherche trouvé dans localStorage.');
    }


    // Fonction pour déterminer le type de boîte de vitesses à partir des features
    function determineGearboxType(features) {
        let gearboxType = 'inconnu'; // Valeur par défaut

        if (!features || features.length === 0) {
            console.warn("No features available or features list is empty.");
            return gearboxType;
        }

        features.forEach((feature, index) => {
            if (feature && feature.name) {
                const name = feature.name.toLowerCase();
                console.log(`Inspecting feature name at index ${index}: ${name}`);
                if (name.includes('manuelle')) {
                    gearboxType = 'manuelle';
                } else if (name.includes('automatique')) {
                    gearboxType = 'automatique';
                }
            } else {
                console.warn(`Feature or feature name is undefined at index ${index}. Feature data:`, feature);
            }
        });

        console.log(`Determined gearbox type: ${gearboxType}`);
        return gearboxType;
    }

    // Fonction pour déterminer la motorisation
    function determineMotorisation(features) {
        let motorisation = 'inconnu'; // Valeur par défaut

        if (!features || features.length === 0) {
            console.warn("No features available or features list is empty.");
            return motorisation;
        }

        features.forEach((feature, index) => {
            if (feature && feature.name) {
                const name = feature.name.toLowerCase();
                console.log(`Inspecting feature name at index ${index}: ${name}`);
                if (['essence', 'diesel', 'hybride', 'electrique'].includes(name)) {
                    motorisation = name;
                }
            } else {
                console.warn(`Feature or feature name is undefined at index ${index}. Feature data:`, feature);
            }
        });

        console.log(`Determined motorisation: ${motorisation}`);
        return motorisation;
    }

    function determineKilometrage(features) {
        let kilometrage = 'inconnu'; // Valeur par défaut

        if (!features || features.length === 0) {
            console.warn("No features available or features list is empty.");
            return kilometrage;
        }

        features.forEach((feature, index) => {
            if (feature && feature.name) {
                let name = feature.name.trim();
                console.log(`Inspecting feature name at index ${index}: ${name}`);

                // Comparaison directe avec les valeurs définies
                if (name === '0 - 15 000 km' || name === '15 000 - 50 000 km' ||
                    name === '50 000 - 100 000 km' || name === '100 000 - 150 000 km' ||
                    name === 'Plus de 150 000 km') {
                    kilometrage = name;
                } else {
                    console.log(`Kilometrage format not recognized for feature name at index ${index}. Feature data:`, feature);
                }
            } else {
                console.warn(`Feature or feature name is undefined at index ${index}. Feature data:`, feature);
            }
        });

        console.log(`Determined kilometrage: ${kilometrage}`);
        return kilometrage;
    }


    function determinePlaces(features) {
        let places = 'inconnu'; // Valeur par défaut

        if (!features || features.length === 0) {
            console.warn("No features available or features list is empty.");
            return places;
        }

        features.forEach((feature, index) => {
            if (feature && feature.name) {
                let name = feature.name.toLowerCase().replace(/ /g, "");
                console.log(`Inspecting feature name at index ${index}: ${name}`);
                if (['3', '4', '5', '7'].includes(name)) {
                    places = name + " places";
                }
            } else {
                console.warn(`Feature or feature name is undefined at index ${index}. Feature data:`, feature);
            }
        });

        console.log(`Determined places: ${places}`);
        return places;
    }


    function filterCars() {
        const selectedCategories = Array.from(document.querySelectorAll('.category-filter:checked')).map(cb => cb.value);
        const selectedGearboxTypes = Array.from(document.querySelectorAll('.gearbox-filter:checked')).map(cb => cb.value);
        const selectedMotorisations = Array.from(document.querySelectorAll('.motorisation-filter:checked')).map(cb => cb.value);
        const selectedKilometrages = Array.from(document.querySelectorAll('.kilometrage-filter:checked')).map(cb => cb.value);
        const selectedPlaces = Array.from(document.querySelectorAll('.places-filter:checked')).map(cb => cb.value);
        let priceRange = parseFloat(document.getElementById('priceRange').value);

        // Assurez-vous que priceRange est une valeur réaliste
        if (isNaN(priceRange) || priceRange <= 0) {
            console.warn("Price range is not valid, setting to default high value.");
            priceRange = 1000; // Une valeur élevée par défaut pour le test
        }

        console.log("Selected categories:", selectedCategories);
        console.log("Selected gearbox types:", selectedGearboxTypes);
        console.log("Selected motorisations:", selectedMotorisations);
        console.log("Selected kilometrages:", selectedKilometrages);
        console.log("Selected places:", selectedPlaces);
        console.log("Selected price range:", priceRange);

        let filteredCars = carData.filter(item => {
            const car = item.car;

            console.log("Inspecting car:", car);

            const categoryMatch = selectedCategories.length === 0 || (car.categoryName && selectedCategories.includes(car.categoryName));
            console.log(`Car: ${car.brand} ${car.model}, Category: ${car.categoryName || 'undefined'}, Category Match: ${categoryMatch}`);

            const gearboxType = determineGearboxType(car.features);
            const gearboxMatch = selectedGearboxTypes.length === 0 || (gearboxType && selectedGearboxTypes.includes(gearboxType));
            console.log(`Car: ${car.brand} ${car.model}, Gearbox Match: ${gearboxMatch}`);

            const motorisationMatch = selectedMotorisations.length === 0 || car.features.some(feature => selectedMotorisations.includes(feature.name.toLowerCase()));
            console.log(`Car: ${car.brand} ${car.model}, Motorisation Match: ${motorisationMatch}`);

            const kilometrage = determineKilometrage(car.features);
            const kilometrageMatch = selectedKilometrages.length === 0 || selectedKilometrages.includes(kilometrage);
            console.log(`Car: ${car.brand} ${car.model}, Kilometrage Match: ${kilometrageMatch}`);

            const places = determinePlaces(car.features);
            const placesMatch = selectedPlaces.length === 0 || selectedPlaces.includes(places);
            console.log(`Car: ${car.brand} ${car.model}, Places Match: ${placesMatch}`);

            const priceMatch = car.price && car.price.middlePrice <= priceRange;
            console.log(`Car: ${car.brand} ${car.model}, Price Match: ${priceMatch}`);

            return categoryMatch && gearboxMatch && motorisationMatch && kilometrageMatch && placesMatch && priceMatch;
        });

        console.log("Filtered cars:", filteredCars);

        // Sort cars based on currentSort value
        filteredCars = sortCars(filteredCars, currentSort);

        displayCars(filteredCars);
    }

    // Function to sort cars
    function sortCars(cars, criteria) {
        switch(criteria) {
            case 'acceptance':
                return cars.sort((a, b) => b.acceptanceRate - a.acceptanceRate);
            case 'price-asc':
                return cars.sort((a, b) => {
                    if (!a.car.price || a.car.price.middlePrice === undefined) return 1;
                    if (!b.car.price || b.car.price.middlePrice === undefined) return -1;
                    return a.car.price.middlePrice - b.car.price.middlePrice;
                });
            case 'rating':
                return cars.sort((a, b) => b.averageRating - a.averageRating);
            default:
                return cars;
        }
    }

    // Ajout des écouteurs d'événements pour les filtres
    document.querySelectorAll('.category-filter, .gearbox-filter, .fuel-filter, .motorisation-filter, .kilometrage-filter, .places-filter, #priceRange').forEach(filterElement => {
        filterElement.addEventListener('change', filterCars);
    });

    // Réinitialisation des filtres
    document.getElementById('resetButton').addEventListener('click', () => {
        // Désélectionner tous les filtres
        document.querySelectorAll('.category-filter, .gearbox-filter, .fuel-filter, .motorisation-filter, .kilometrage-filter, .places-filter').forEach(filterElement => {
            filterElement.checked = false;
        });

        // Réinitialiser le range de prix
        const priceRangeElement = document.getElementById('priceRange');
        priceRangeElement.value = priceRangeElement.max;

        // Mettre à jour la valeur affichée pour le prix
        document.getElementById('priceValue').textContent = `${priceRangeElement.max} €`;

        // Réappliquer le filtre pour montrer toutes les voitures
        filterCars();
    });

    // Ajout des écouteurs d'événements pour les liens de tri
    document.querySelectorAll('.sort-link').forEach(link => {
        link.addEventListener('click', (event) => {
            event.preventDefault();
            document.querySelectorAll('.sort-link').forEach(link => link.classList.remove('active'));
            link.classList.add('active');
            currentSort = link.getAttribute('data-sort');
            filterCars();
        });
    });

    // Initialiser les données de la liste de voitures et mettre à jour les filtres
    filterCars();
});
