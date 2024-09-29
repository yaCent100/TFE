document.addEventListener("DOMContentLoaded", async () => {
    console.log("Initializing script...");

    let carData = [];
    let currentSort = 'default';
    let currentPage = 1;
    const carsPerPage = 10;
    let isLoading = false;
    let hasMoreCars = true;
    let searchQuery = null;
    let totalCarCount = 0;

    const urlParams = new URLSearchParams(window.location.search);
    const address = urlParams.get('manualAddressCars') || urlParams.get('manualAddress');
    const dateDebut = urlParams.get('dateDebut');
    const dateFin = urlParams.get('dateFin');

    const carsAddressInput = document.getElementById('manualAddressCars');
    const dateDebutInput = document.getElementById('dateDebut');
    const dateFinInput = document.getElementById('dateFin');

    if (address && carsAddressInput) {
        carsAddressInput.value = address;
    }
    if (dateDebut && dateDebutInput) {
        dateDebutInput.value = dateDebut;
    }
    if (dateFin && dateFinInput) {
        dateFinInput.value = dateFin;
    }

    const searchAddress = carsAddressInput?.value || address;
    const searchDateDebut = dateDebutInput?.value || dateDebut;
    const searchDateFin = dateFinInput?.value || dateFin;

    if (searchAddress && searchDateDebut && searchDateFin) {
        initiateSearch(searchAddress, searchDateDebut, searchDateFin);
    } else {
        console.warn('Missing search parameters, loading all cars.');
        await loadAllCars();
    }

async function searchCarsWithGeocode(address, dateDebut, dateFin, page = 1, limit = 10) {
    if (isLoading) return;

    isLoading = true;
    const spinnerContainer = document.getElementById('spinner-container');
    spinnerContainer.style.display = 'block';

    try {
        const accessToken = 'pk.eyJ1Ijoic2FuamkwNTEyIiwiYSI6ImNtMGQwYnhvNTA3NnUybXNjOWs2Mnhmem4ifQ.-JdPS28lnldjDaCwlcCecg';
        const mapboxUrl = `https://api.mapbox.com/geocoding/v5/mapbox.places/${encodeURIComponent(address)}.json?access_token=${accessToken}`;

        const response = await fetch(mapboxUrl);
        if (!response.ok) {
            throw new Error(`Erreur API Mapbox : ${response.statusText}`);
        }
        const data = await response.json();

        if (data.features && data.features.length > 0) {
            const location = data.features[0];
            const [lng, lat] = location.center;

            const carsResponse = await fetch(`/api/cars/search?address=${encodeURIComponent(address)}&lat=${lat}&lng=${lng}&dateDebut=${dateDebut}&dateFin=${dateFin}&page=${page}&limit=${limit}`);
            const carsData = await carsResponse.json();

            if (carsData.cars) {
                if (page === 1) {
                    totalCarCount = carsData.totalPages * carsPerPage;
                }
                carData = page === 1 ? carsData.cars : [...carData, ...carsData.cars];

                const filteredCars = filterCarsForSearch(carData); // Filtrage
                const sortedCars = sortCarsForSearch(filteredCars, currentSort);
                displayCars(sortedCars);

                hasMoreCars = carsData.currentPage < carsData.totalPages;
                currentPage++;
            } else {
                console.warn("Aucune voiture disponible pour les critères de recherche.");
                hasMoreCars = false;
            }
        } else {
            console.error('Adresse non trouvée.');
            alert('Aucune coordonnée trouvée pour l\'adresse saisie.');
        }
    } catch (error) {
        console.error('Erreur lors du géocodage :', error);
        alert('Une erreur est survenue. Veuillez réessayer.');
    } finally {
        isLoading = false;
        spinnerContainer.style.display = 'none';
    }
}


    async function loadAllCars(page = 1, limit = carsPerPage) {
        if (isLoading) return;

        isLoading = true;
        const spinnerContainer = document.getElementById('spinner-container');
        spinnerContainer.style.display = 'block';

        try {
            const response = await fetch(`/api/cars?page=${page}&limit=${limit}`);
            const carsData = await response.json();

            if (carsData.cars) {
                if (page === 1) {
                    totalCarCount = carsData.totalPages * carsPerPage;
                }
                carData = page === 1 ? carsData.cars : [...carData, ...carsData.cars];
                console.log('Voitures après chargement:', carData);
                const sortedCars = sortCarsForLoad(carData, currentSort);
                displayCars(sortedCars);
                hasMoreCars = carsData.currentPage < carsData.totalPages;
                currentPage++;
            } else {
                console.warn("Aucune voiture disponible.");
                hasMoreCars = false;
            }
        } catch (error) {
            console.error('Erreur lors du chargement des voitures :', error);
        } finally {
            isLoading = false;
            spinnerContainer.style.display = 'none';
        }
    }

    window.addEventListener('scroll', () => {
        if (window.innerHeight + window.scrollY >= document.body.offsetHeight - 500 && !isLoading && hasMoreCars) {
            if (searchQuery) {
                searchCarsWithGeocode(searchQuery.address, searchQuery.dateDebut, searchQuery.dateFin, currentPage, carsPerPage);
            } else {
                loadAllCars(currentPage, carsPerPage);
            }
        }
    });

    function initiateSearch(address, dateDebut, dateFin) {
        searchQuery = { address, dateDebut, dateFin };
        currentPage = 1;
        isLoading = false;
        hasMoreCars = true;
        searchCarsWithGeocode(address, dateDebut, dateFin, currentPage, carsPerPage);
    }

    function displayCars(cars) {
        console.log('Démarrage de l\'affichage des voitures...');
        console.log('Voitures à afficher:', cars);

        const carListElement = document.getElementById('carList');
        const carCountMessageElement = document.getElementById('carCountMessage');

        if (!carListElement || !carCountMessageElement) {
            console.error("Element 'carList' or 'carCountMessage' not found.");
            return;
        }

        if (cars.length === 0) {
            carListElement.innerHTML = '<p>Aucune voiture disponible pour les critères de recherche spécifiés.</p>';
            carCountMessageElement.style.display = 'none';
            console.log('Aucune voiture trouvée.');
            return;
        }

        carListElement.innerHTML = '';
        carCountMessageElement.textContent = `+ ${totalCarCount} véhicules correspondent à votre recherche`;
        carCountMessageElement.style.display = 'block';

        cars.forEach((carData, index) => {
            if (!carData) {
                console.error(`Car data is missing at index ${index}.`);
                return;
            }

            const car = carData.car || carData;
            const averageRating = carData.averageRating || 0;
            const reviewCount = carData.reviewCount || 0;
            const distanceValue = carData.distance;

            let formattedDistance = "";
            let distanceHtml = "";

            if (distanceValue !== undefined && distanceValue !== null && distanceValue !== 0) {
                formattedDistance = distanceValue < 1 ? `${Math.round(distanceValue * 1000)} m` : `${distanceValue.toFixed(1)} km`;
                distanceHtml = `
                    <div>
                        <img src="icones/espace-reserve.png" alt="globe" style="width: 20px; height: auto;">
                        ${formattedDistance}
                    </div>`;
            }

            let displayPrice = car.displayPrice !== undefined ? car.displayPrice : 'Prix non disponible';

            const modeReservationText = car.modeReservation === 'automatique'
                ? 'Réservation instantanée'
                : '';

            const carCard = document.createElement('div');
            carCard.classList.add('card', 'mb-3', 'car-card');

            let url = `/cars/${car.id}`;
            if (dateDebut) url += `?dateDebut=${dateDebut}`;
            if (dateFin) url += (dateDebut ? '&' : '?') + `dateFin=${dateFin}`;

            const starsHtml = reviewCount > 0 ? Array.from({ length: 5 }).map((_, i) => {
                const starClass = i < averageRating ? 'fa-star checked' : 'fa-star empty';
                return `<span class="fa ${starClass}"></span>`;
            }).join('') : '';

            carCard.innerHTML = `
                <a href="${url}" class="card-link">
                    <div class="row g-0">
                        <div class="col-md-3 m-auto">
                            <img src="${car.photoUrl && car.photoUrl.length > 0 ? `/uploads/photo-car/${car.photoUrl[0]}` : '/images/carDefault.png'}"
                                 alt="Photo de voiture ${car.brand} ${car.model}" class="card-img-top" style="max-height: 150px;">
                        </div>
                        <div class="col-md-9">
                            <div class="card-body">
                                <div class="d-flex justify-content-between align-items-center">
                                    <h5 class="card-title m-0">${car.brand} ${car.model}</h5>
                                    <div class="priceIndex">${displayPrice} €/jour</div>
                                </div>

                                <div class="d-flex justify-content-start align-items-center mt-2">
                                    ${starsHtml}
                                    <div class="ms-2">
                                        ${reviewCount > 0 ? `(${reviewCount} avis)` : 'Aucun avis'}
                                    </div>
                                </div>

                                <div class="d-flex flex-wrap mt-2 text-muted small">
                                    ${car.features.slice(0, 4).map(feature => `
                                        <div class="me-3 d-flex align-items-center">
                                            <img src="${getIconForFeature(feature.name)}" alt="${feature.name}" style="width: 20px; height: 20px;" class="me-1">
                                            ${feature.description}
                                        </div>
                                    `).join('')}
                                </div>

                                <div class="d-flex justify-content-between mt-3">
                                    ${distanceHtml}
                                ${modeReservationText ? `
                                    <div class="card-text-mode" data-toggle="tooltip" data-placement="top" title="Vous pouvez réserver la voiture immédiatement">
                                        <img src="/icons/eclat.png" alt="eclat" class="icon" style="width: 20px; height: 20px; margin-right: 5px;">
                                        <span>${modeReservationText}</span>
                                    </div>
                                ` : ''}
                                </div>
                            </div>
                        </div>
                    </div>
                </a>
            `;

            carListElement.appendChild(carCard);
        });

        carListElement.style.display = 'block';
    }

    $(document).ready(function () {
        // Initialiser tous les tooltips de la page
        $('[data-toggle="tooltip"]').tooltip();
    });


    function getIconForFeature(featureName) {
        switch (featureName.toLowerCase()) {
            case 'boite':
                return '/icons/boite-de-vitesses.png';
            case 'portes':
                return '/icons/porte-de-voiture.png';
            case 'compteur':
                return '/icons/jauge.png';
            case 'moteur':
                return '/icons/station-essence.png';
            case 'places':
                return '/icons/siege-de-voiture.png';
            default:
                return '/icons/default.png';
        }
    }

function sortCarsForLoad(cars, criteria) {
    if (!Array.isArray(cars) || cars.length === 0) {
        console.warn("Les données des voitures ne sont pas valides pour le tri.");
        return [];
    }

    console.log("Critère de tri (Chargement):", criteria);

    let sortedCars;

    switch (criteria) {
        case 'acceptance':
            // Tri par mode de réservation automatique
            sortedCars = cars.sort((a, b) => {
                const aInstant = a.modeReservation === 'automatique' ? 1 : 0;
                const bInstant = b.modeReservation === 'automatique' ? 1 : 0;
                return bInstant - aInstant;
            });
            break;

        case 'price':
            // Tri par prix croissant
            sortedCars = cars.sort((a, b) => {
                const priceA = a.displayPrice || Infinity;
                const priceB = b.displayPrice || Infinity;
                return priceA - priceB;
            });
            break;

        case 'rating':
            // Tri par évaluation décroissante
            sortedCars = cars.sort((a, b) => {
                const ratingA = a.averageRating || 0;
                const ratingB = b.averageRating || 0;
                return ratingB - ratingA;
            });
            break;

        case 'default':
            // Tri par distance croissante (défaut)
            sortedCars = cars.sort((a, b) => {
                const distanceA = a.distance || Infinity;
                const distanceB = b.distance || Infinity;
                return distanceA - distanceB;
            });
            break;

        default:
            // Aucun tri spécifique, retour des données non triées
            sortedCars = cars;
    }

    console.log("Données triées (Chargement):", sortedCars);
    return sortedCars;
}

function sortCarsForSearch(cars, criteria) {
    if (!Array.isArray(cars) || cars.length === 0) {
        console.warn("Les données des voitures ne sont pas valides pour le tri.");
        return [];
    }

    console.log("Critère de tri (Recherche):", criteria);

    let sortedCars;

    // Déterminer si les voitures sont encapsulées dans une propriété `car`
    const getCarProperty = (car) => car.car;

    switch (criteria) {
        case 'acceptance':
            // Tri par mode de réservation automatique
            sortedCars = cars.sort((a, b) => {
                const carA = getCarProperty(a);
                const carB = getCarProperty(b);
                const aInstant = carA && carA.modeReservation === 'automatique' ? 1 : 0;
                const bInstant = carB && carB.modeReservation === 'automatique' ? 1 : 0;
                return bInstant - aInstant;
            });
            break;

        case 'price':
            // Tri par prix croissant
            sortedCars = cars.sort((a, b) => {
                const carA = getCarProperty(a);
                const carB = getCarProperty(b);
                const priceA = carA && carA.displayPrice || Infinity;
                const priceB = carB && carB.displayPrice || Infinity;
                return priceA - priceB;
            });
            break;

        case 'rating':
            // Tri par évaluation décroissante
            sortedCars = cars.sort((a, b) => {
                const carA = getCarProperty(a);
                const carB = getCarProperty(b);
                const ratingA = carA && carA.averageRating || 0;
                const ratingB = carB && carB.averageRating || 0;
                return ratingB - ratingA;
            });
            break;

        case 'default':
            // Tri par distance croissante (défaut)
            sortedCars = cars.sort((a, b) => {
                const carA = getCarProperty(a);
                const carB = getCarProperty(b);
                const distanceA = carA && carA.distance || Infinity;
                const distanceB = carB && carB.distance || Infinity;
                return distanceA - distanceB;
            });
            break;

        default:
            // Aucun tri spécifique, retour des données non triées
            sortedCars = cars;
    }

    console.log("Données triées (Recherche):", sortedCars);
    return sortedCars;
}





function filterCarsForLoad(carData) {
    console.log('Filtrage des voitures pour chargement...');

    if (!Array.isArray(carData) || carData.length === 0) {
        console.warn("carData n'est pas un tableau valide ou est vide.");
        return [];
    }

    // Récupérer les valeurs sélectionnées des filtres
    const selectedCategories = Array.from(document.querySelectorAll('.category-filter:checked')).map(cb => cb.value);
    const selectedGearboxTypes = Array.from(document.querySelectorAll('.gearbox-filter:checked')).map(cb => cb.value);
    const selectedMotorisations = Array.from(document.querySelectorAll('.motorisation-filter:checked')).map(cb => cb.value);
    const selectedKilometrages = Array.from(document.querySelectorAll('.kilometrage-filter:checked')).map(cb => cb.value);
    const selectedPlaces = Array.from(document.querySelectorAll('.places-filter:checked')).map(cb => parseInt(cb.value, 10));

    // Récupérer la plage de prix
    let priceRange = parseFloat(document.getElementById('priceRange').value);

    if (isNaN(priceRange) || priceRange <= 0) {
        console.warn("Plage de prix invalide, définition à la valeur par défaut élevée.");
        priceRange = 150;
    }

    // Filtrage des voitures
    let filteredCars = carData.filter(car => {
        if (!car || typeof car !== 'object') {
            console.warn("Données de voiture manquantes ou mal formatées:", car);
            return false;
        }

        const { categoryName, features = [], displayPrice } = car;

        if (typeof categoryName !== 'string' || !Array.isArray(features) || typeof displayPrice !== 'number') {
            console.warn("Format des données de voiture incorrect:", car);
            return false;
        }

        const categoryMatch = !selectedCategories.length || selectedCategories.includes(categoryName);
        const gearboxFeature = features.find(f => f.name && f.name.toLowerCase() === 'boîte');
        const gearboxMatch = !selectedGearboxTypes.length || (gearboxFeature && selectedGearboxTypes.includes(gearboxFeature.description));
        const motorisationFeature = features.find(f => f.name && f.name.toLowerCase() === 'moteur');
        const motorisationMatch = !selectedMotorisations.length || (motorisationFeature && selectedMotorisations.includes(motorisationFeature.description));
        const kilometrageFeature = features.find(f => f.name && f.name.toLowerCase() === 'compteur');
        const kilometrageMatch = !selectedKilometrages.length || (kilometrageFeature && selectedKilometrages.includes(kilometrageFeature.description));
        const placesFeature = features.find(f => f.name && f.name.toLowerCase() === 'places');
        const placesMatch = !selectedPlaces.length || (placesFeature && selectedPlaces.includes(parseInt(placesFeature.description, 10)));
        const priceMatch = displayPrice !== undefined && displayPrice <= priceRange;

        return categoryMatch && gearboxMatch && motorisationMatch && kilometrageMatch && placesMatch && priceMatch;
    });

    console.log('Voitures après filtrage pour chargement:', filteredCars);
    return filteredCars;
}


function filterCarsForSearch(carData) {
    console.log('Filtrage des voitures pour recherche...');

    // Récupérer les valeurs sélectionnées des filtres
    const selectedCategories = Array.from(document.querySelectorAll('.category-filter:checked')).map(cb => cb.value);
    const selectedGearboxTypes = Array.from(document.querySelectorAll('.gearbox-filter:checked')).map(cb => cb.value);
    const selectedMotorisations = Array.from(document.querySelectorAll('.motorisation-filter:checked')).map(cb => cb.value);
    const selectedKilometrages = Array.from(document.querySelectorAll('.kilometrage-filter:checked')).map(cb => cb.value);
    const selectedPlaces = Array.from(document.querySelectorAll('.places-filter:checked')).map(cb => parseInt(cb.value, 10));

    // Afficher les valeurs de filtres pour diagnostic
    console.log("Catégories sélectionnées:", selectedCategories);
    console.log("Types de boîte sélectionnés:", selectedGearboxTypes);
    console.log("Motorisations sélectionnées:", selectedMotorisations);
    console.log("Kilométrages sélectionnés:", selectedKilometrages);
    console.log("Places sélectionnées:", selectedPlaces);

    // Récupérer la plage de prix
    let priceRange = parseFloat(document.getElementById('priceRange').value);
    console.log("Plage de prix:", priceRange);

    if (isNaN(priceRange) || priceRange <= 0) {
        console.warn("Plage de prix invalide, définition à la valeur par défaut élevée.");
        priceRange = 150;
    }

    // Filtrage des voitures
let filteredCars = carData.filter(car => {
    if (!car || typeof car !== 'object' || !car.car) {
        console.warn("Données de voiture manquantes ou mal formatées:", car);
        return false;
    }

    const { categoryName, features = [], displayPrice } = car.car;

    const categoryMatch = !selectedCategories.length || selectedCategories.includes(categoryName);
    const gearboxFeature = features.find(f => f.name && f.name.toLowerCase() === 'boîte');
    const gearboxMatch = !selectedGearboxTypes.length || (gearboxFeature && selectedGearboxTypes.includes(gearboxFeature.description));
    const motorisationFeature = features.find(f => f.name && f.name.toLowerCase() === 'moteur');
    const motorisationMatch = !selectedMotorisations.length || (motorisationFeature && selectedMotorisations.includes(motorisationFeature.description));
    const kilometrageFeature = features.find(f => f.name && f.name.toLowerCase() === 'compteur');
    const kilometrageMatch = !selectedKilometrages.length || (kilometrageFeature && selectedKilometrages.includes(kilometrageFeature.description));
    const placesFeature = features.find(f => f.name && f.name.toLowerCase() === 'places');
    const placesMatch = !selectedPlaces.length || (placesFeature && selectedPlaces.includes(parseInt(placesFeature.description, 10)));
    const priceMatch = displayPrice !== undefined && displayPrice <= priceRange;

    return categoryMatch && gearboxMatch && motorisationMatch && kilometrageMatch && placesMatch && priceMatch;
});

    console.log('Voitures après filtrage pour recherche:', filteredCars);
    return filteredCars;
}


function getQueryParam(param) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(param);
}


function filterCars() {
    console.log('Filtrage des voitures...');

    // Vérifiez la présence d'un paramètre spécifique pour déterminer le mode
    const isSearchMode = !!getQueryParam('dateDebut'); // True si le paramètre de recherche est présent

    let filteredCars;

    if (isSearchMode) {
        filteredCars = filterCarsForSearch(carData);
        // Tri pour le mode de recherche
        filteredCars = sortCarsForSearch(filteredCars, currentSort);
    } else {
        filteredCars = filterCarsForLoad(carData);
        // Tri pour le mode de chargement
        filteredCars = sortCarsForLoad(filteredCars, currentSort);
    }

    console.log('Voitures après filtrage et tri:', filteredCars);

    displayCars(filteredCars);
}


document.querySelectorAll('.sort-link').forEach(link => {
    link.addEventListener('click', function (e) {
        e.preventDefault();
        document.querySelectorAll('.sort-link').forEach(l => l.classList.remove('active'));
        this.classList.add('active');
        currentSort = this.getAttribute('data-sort');
        console.log("Critère de tri sélectionné:", currentSort);

        // Déterminer le mode de tri (recherche ou chargement)
        const isSearchMode = !!getQueryParam('dateDebut'); // True si le paramètre de recherche est présent

        let sortedCars;
        if (isSearchMode) {
            sortedCars = sortCarsForSearch(carData, currentSort);
        } else {
            sortedCars = sortCarsForLoad(carData, currentSort);
        }

        console.log("Données triées:", sortedCars);
        displayCars(sortedCars);
    });
});


    document.querySelectorAll('.form-check-input').forEach(input => {
        input.addEventListener('change', filterCars);
    });

    document.getElementById('priceRange').addEventListener('input', () => {
        document.getElementById('priceValue').textContent = document.getElementById('priceRange').value + ' €';
        filterCars();
    });

    document.querySelectorAll('.sort-option').forEach(option => {
        option.addEventListener('change', (event) => {
            currentSort = event.target.value;
            filterCars();
        });
    });

    document.getElementById('resetButton').addEventListener('click', () => {
        document.querySelectorAll('.form-check-input').forEach(checkbox => {
            checkbox.checked = false;
        });

        const priceRangeInput = document.getElementById('priceRange');
        if (priceRangeInput) {
            priceRangeInput.value = 0;
            document.getElementById('priceValue').textContent = '0 €';
        }

        filterCars();
    });




        document.getElementById('clearSearch').addEventListener('click', function() {
          document.getElementById('searchForm').reset();
        });

});
