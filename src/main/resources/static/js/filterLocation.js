document.addEventListener("DOMContentLoaded", () => {
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
            distanceElement.textContent = `Distance: ${car.distance ? car.distance.toFixed(2) : 'N/A'} km`;
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

    function loadAllCars() {
            fetch('/api/cars')
                .then(response => response.json())
                .then(data => {
                    carData = data.cars.map(car => ({
                        car: car,
                        averageRating: data.averageRatings[car.id] || 0,
                        reviewCount: data.reviewCounts[car.id] || 0,
                        acceptanceRate: data.acceptanceRate ? data.acceptanceRate[car.id] : 0 // Vérifiez si acceptanceRate est présent
                    }));
                    console.log("Loaded all cars:", carData);
                    displayCars(carData);
                    filterCars(); // Appliquer les filtres après chargement des voitures
                })
                .catch(error => console.error('Error fetching cars:', error));
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

    // Fonction de filtrage des voitures
    function filterCars() {
        const selectedCategories = Array.from(document.querySelectorAll('.category-filter:checked')).map(cb => cb.value);
        const selectedGearboxTypes = Array.from(document.querySelectorAll('.gearbox-filter:checked')).map(cb => cb.value);
        const selectedMotorisations = Array.from(document.querySelectorAll('.motorisation-filter:checked')).map(cb => cb.value);
        const selectedKilometrages = Array.from(document.querySelectorAll('.kilometrage-filter:checked')).map(cb => cb.value);
        const selectedPlaces = Array.from(document.querySelectorAll('.places-filter:checked')).map(cb => cb.value);
        let priceRange = parseFloat(document.getElementById('priceRange').value);

        if (isNaN(priceRange) || priceRange <= 0) {
            console.warn("Price range is not valid, setting to default high value.");
            priceRange = 1000;
        }

        console.log("Selected categories:", selectedCategories);
        console.log("Selected gearbox types:", selectedGearboxTypes);
        console.log("Selected motorisations:", selectedMotorisations);
        console.log("Selected kilometrages:", selectedKilometrages);
        console.log("Selected places:", selectedPlaces);
        console.log("Selected price range:", priceRange);

        let filteredCars = carData.filter(item => {
            if (!item.car) {
                console.warn("Item without car data:", item);
                return false;
            }

            const car = item.car;

            console.log("Inspecting car:", car);

            const categoryMatch = matchCategory(selectedCategories, car);
            const gearboxMatch = matchGearbox(selectedGearboxTypes, car);
            const motorisationMatch = matchMotorisation(selectedMotorisations, car);
            const kilometrageMatch = matchKilometrage(selectedKilometrages, car);
            const placesMatch = matchPlaces(selectedPlaces, car);
            const priceMatch = matchPrice(priceRange, car);

            console.log(`Car: ${car.brand} ${car.model}, Category Match: ${categoryMatch}, Gearbox Match: ${gearboxMatch}, Motorisation Match: ${motorisationMatch}, Kilometrage Match: ${kilometrageMatch}, Places Match: ${placesMatch}, Price Match: ${priceMatch}`);

            return categoryMatch && gearboxMatch && motorisationMatch && kilometrageMatch && placesMatch && priceMatch;
        });

        console.log("Filtered cars:", filteredCars);

        filteredCars = sortCars(filteredCars, currentSort);

        displayCars(filteredCars);
    }

    function matchCategory(selectedCategories, car) {
        return selectedCategories.length === 0 || (car.categoryName && selectedCategories.includes(car.categoryName));
    }

    function matchGearbox(selectedGearboxTypes, car) {
        const gearboxType = determineGearboxType(car.features);
        console.log(`Gearbox type for car ${car.brand} ${car.model}:`, gearboxType);
        return selectedGearboxTypes.length === 0 || (gearboxType && selectedGearboxTypes.includes(gearboxType));
    }

    function matchMotorisation(selectedMotorisations, car) {
        const motorisationType = determineMotorisation(car.features);
        console.log(`Motorisation type for car ${car.brand} ${car.model}:`, motorisationType);
        return selectedMotorisations.length === 0 || (motorisationType && selectedMotorisations.includes(motorisationType));
    }

    function matchKilometrage(selectedKilometrages, car) {
        const kilometrage = determineKilometrage(car.features);
        console.log(`Kilometrage for car ${car.brand} ${car.model}:`, kilometrage);
        return selectedKilometrages.length === 0 || selectedKilometrages.includes(kilometrage);
    }

    function matchPlaces(selectedPlaces, car) {
        const places = determinePlaces(car.features);
        console.log(`Places for car ${car.brand} ${car.model}:`, places);
        return selectedPlaces.length === 0 || selectedPlaces.includes(places);
    }

     function matchPrice(priceRange, car) {
             const priceMatch = car.price && car.price.middlePrice <= priceRange;
             console.log(`Price for car ${car.brand} ${car.model}:`, car.price ? car.price.middlePrice : 'No price', `, Price Match: ${priceMatch}`);
             return priceMatch;
     }

    function determineGearboxType(features) {
        if (!features) return null;
        for (const feature of features) {
            if (feature.name.toLowerCase() === 'boite') {
                return feature.description;
            }
        }
        return null;
    }

    function determineKilometrage(features) {
        if (!features) return 'N/A';
        for (const feature of features) {
            if (feature.name.toLowerCase() === 'compteur') {
                return feature.description;
            }
        }
        return 'N/A';
    }

    function determinePlaces(features) {
        if (!features) return 'N/A';
        for (const feature of features) {
            if (feature.name.toLowerCase() === 'places') {
                return feature.description;
            }
        }
        return 'N/A';
    }

    function determineMotorisation(features) {
        if (!features) return null;
        for (const feature of features) {
            if (feature.name.toLowerCase() === 'moteur') {
                return feature.description;
            }
        }
        return null;
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

    // Fetch and populate filters
    function fetchFilters() {
        // Fetch categories and populate the category filter
        fetch('/api/categories')
            .then(response => response.json())
            .then(data => {
                const categoryFilter = document.getElementById('category-filter');
                if (categoryFilter) {
                    data.forEach(category => {
                        const div = document.createElement('div');
                        div.classList.add('form-check');
                        div.innerHTML = `
                            <input class="form-check-input category-filter" type="checkbox" value="${category.category}" id="${category.id}">
                            <label class="form-check-label" for="${category.id}">${category.category}</label>
                        `;
                        categoryFilter.appendChild(div);
                    });
                    // Attachez l'événement change après avoir ajouté les filtres
                    document.querySelectorAll('.category-filter').forEach(filterElement => {
                        filterElement.addEventListener('change', filterCars);
                    });
                } else {
                    console.error('Element with id "category-filter" not found in the DOM.');
                }
            })
            .catch(error => console.error('Error fetching categories:', error));

        // Fetch gearbox types and populate the gearbox filter
        fetch('/api/gearbox')
            .then(response => response.json())
            .then(data => {
                const gearboxFilter = document.getElementById('gearbox-filter');
                if (gearboxFilter) {
                    data.forEach(gearbox => {
                        const div = document.createElement('div');
                        div.classList.add('form-check');
                        div.innerHTML = `
                            <input class="form-check-input gearbox-filter" type="checkbox" value="${gearbox}">
                            <label class="form-check-label">${gearbox}</label>
                        `;
                        gearboxFilter.appendChild(div);
                    });
                    // Attachez l'événement change après avoir ajouté les filtres
                    document.querySelectorAll('.gearbox-filter').forEach(filterElement => {
                        filterElement.addEventListener('change', filterCars);
                    });
                } else {
                    console.error('Element with id "gearbox-filter" not found in the DOM.');
                }
            })
            .catch(error => console.error('Error fetching gearbox types:', error));

        // Fetch motorisation types and populate the motorisation filter
        fetch('/api/motorisation')
            .then(response => response.json())
            .then(data => {
                const motorisationFilter = document.getElementById('motorisation-filter');
                if (motorisationFilter) {
                    data.forEach(motorisation => {
                        const div = document.createElement('div');
                        div.classList.add('form-check');
                        div.innerHTML = `
                            <input class="form-check-input motorisation-filter" type="checkbox" value="${motorisation}">
                            <label class="form-check-label">${motorisation}</label>
                        `;
                        motorisationFilter.appendChild(div);
                    });
                    // Attachez l'événement change après avoir ajouté les filtres
                    document.querySelectorAll('.motorisation-filter').forEach(filterElement => {
                        filterElement.addEventListener('change', filterCars);
                    });
                } else {
                    console.error('Element with id "motorisation-filter" not found in the DOM.');
                }
            })
            .catch(error => console.error('Error fetching motorisation types:', error));

        // Fetch kilometrage options and populate the kilometrage filter
        fetch('/api/kilometrage')
            .then(response => response.json())
            .then(data => {
                const kilometrageFilter = document.getElementById('kilometrage-filter');
                if (kilometrageFilter) {
                    data.forEach(kilometrage => {
                        const div = document.createElement('div');
                        div.classList.add('form-check');
                        div.innerHTML = `
                            <input class="form-check-input kilometrage-filter" type="checkbox" value="${kilometrage}">
                            <label class="form-check-label">${kilometrage}</label>
                        `;
                        kilometrageFilter.appendChild(div);
                    });
                    // Attachez l'événement change après avoir ajouté les filtres
                    document.querySelectorAll('.kilometrage-filter').forEach(filterElement => {
                        filterElement.addEventListener('change', filterCars);
                    });
                } else {
                    console.error('Element with id "kilometrage-filter" not found in the DOM.');
                }
            })
            .catch(error => console.error('Error fetching kilometrage options:', error));

        // Fetch number of places options and populate the places filter
        fetch('/api/places')
            .then(response => response.json())
            .then(data => {
                const placesFilter = document.getElementById('places-filter');
                if (placesFilter) {
                    data.forEach(places => {
                        const div = document.createElement('div');
                        div.classList.add('form-check');
                        div.innerHTML = `
                            <input class="form-check-input places-filter" type="checkbox" value="${places}">
                            <label class="form-check-label">${places}</label>
                        `;
                        placesFilter.appendChild(div);
                    });
                    // Attachez l'événement change après avoir ajouté les filtres
                    document.querySelectorAll('.places-filter').forEach(filterElement => {
                        filterElement.addEventListener('change', filterCars);
                    });
                } else {
                    console.error('Element with id "places-filter" not found in the DOM.');
                }
            })
            .catch(error => console.error('Error fetching places options:', error));
    }

    // Call fetchFilters to initialize filters
    fetchFilters();

    // Initialiser les données de la liste de voitures et mettre à jour les filtres
    filterCars();

    // Charger toutes les voitures depuis l'API et appliquer les filtres et le tri
        if (window.location.pathname === '/cars') {
            loadAllCars();
        }

     const priceRangeInput = document.getElementById('priceRange');
        const priceValueDisplay = document.getElementById('priceValue');

        priceRangeInput.addEventListener('input', () => {
            priceValueDisplay.textContent = `${priceRangeInput.value} €`;
            filterCars(); // Appliquer le filtrage à chaque modification de la valeur
        });

    // Ajouter un écouteur d'événement pour l'onglet "Nos Locations"
    const nosLocationsTab = document.getElementById('nosLocationsTab');
    if (nosLocationsTab) {
        nosLocationsTab.addEventListener('click', loadAllCars);
    }
});
