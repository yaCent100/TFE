document.addEventListener('DOMContentLoaded', async () => {
    console.log("Initializing car results script...");

    const carResultsElement = document.getElementById('carList');
    const spinnerElement = document.getElementById('loading-spinner');

    // Initialement, masquer carlist et afficher le spinner
    carResultsElement.style.display = 'none';
    spinnerElement.style.display = 'block';

    const urlParams = new URLSearchParams(window.location.search);
    const address = urlParams.get('address');
    const dateDebut = urlParams.get('dateDebut');
    const dateFin = urlParams.get('dateFin');
    const lat = parseFloat(urlParams.get('lat'));
    const lng = parseFloat(urlParams.get('lng'));

    console.log(`Parameters - Address: ${address}, DateDebut: ${dateDebut}, DateFin: ${dateFin}, Lat: ${lat}, Lng: ${lng}`);

    if (address && dateDebut && dateFin && lat && lng) {
        try {
            const response = await fetch(`/api/cars/search?address=${encodeURIComponent(address)}&lat=${lat}&lng=${lng}&dateDebut=${dateDebut}&dateFin=${dateFin}`);
            const cars = await response.json();
            console.log("Fetched available cars:", cars);
            if (cars && cars.length > 0) {
                cars.forEach(car => {
                    car.distance = calculateDistance(lat, lng, car.latitude, car.longitude);
                });
                displayCarResults(cars);
            } else {
                console.log('No cars found for the given criteria.');
                carResultsElement.innerHTML = '<p>No cars available for the given criteria.</p>';
            }
        } catch (error) {
            console.error('Error fetching car data:', error);
            carResultsElement.innerHTML = '<p>An error occurred while fetching car data. Please try again later.</p>';
        } finally {
            // Masquer le spinner et afficher carlist après le chargement des données
            spinnerElement.style.display = 'none';
            carResultsElement.style.display = 'block';
        }
    } else {
        console.log('Missing required parameters.');
        spinnerElement.style.display = 'none';
        carResultsElement.style.display = 'block';
        carResultsElement.innerHTML = '<p>Please provide all the required search parameters.</p>';
    }

    function displayCarResults(cars) {
        carResultsElement.innerHTML = ''; // Effacer les anciens résultats

        cars.forEach(car => {
            const carCard = `
                <div class="card p-3 rounded">
                    <a href="/cars/${car.id}">
                        <div class="row g-0">
                            <div class="col-md-3">
                                <div class="vehicle-picture">
                                    <img src="${car.photoUrl ? '/uploads/' + car.photoUrl[0] : '/images/carDefault.png'}" alt="Car Photo" loading="lazy">
                                </div>
                            </div>
                            <div class="col-lg-8 col-md-8 col-xs-7 search-list-vehicle-desc-responsive">
                                <div class="search-list-vehicle-desc">
                                    <div class="row">
                                        <div class="col-lg-12">
                                            <div class="row">
                                                <div class="col-lg-8 col-md-12 col-xs-12">
                                                    <div class="row">
                                                        <div class="col-lg-10 col-md-10 col-xs-10 no-padding">
                                                            <p class="fs14 bold c-black">${car.brand} ${car.model}</p>
                                                            <span class="fs11 mobile pdl-5">${car.adresse}, ${car.codePostal} ${car.locality}</span>
                                                            <p class="fs11 mobile pdl-5">
                                                                <span class="fa fa-map-marker mgr-10"></span>
                                                                <span>${car.distance.toFixed(1)} km</span>
                                                            </p>
                                                        </div>
                                                        <div class="col-lg-2 col-md-2 col-xs-2 no-padding">
                                                        </div>
                                                    </div>
                                                </div>
                                                <div class="col-lg-3 col-md-4 col-xs-10 text-right not-on-mobile on-ipad not-mobile">
                                                    <p class="vehicle-pricing fs16 medium">${car.price.middlePrice}€</p>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </a>
                </div>
            `;
            carResultsElement.insertAdjacentHTML('beforeend', carCard);
        });
    }

    function calculateDistance(lat1, lng1, lat2, lng2) {
        const R = 6371; // Radius of the Earth in kilometers
        const dLat = toRad(lat2 - lat1);
        const dLng = toRad(lng2 - lng1);
        const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                  Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
                  Math.sin(dLng / 2) * Math.sin(dLng / 2);
        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Distance in kilometers
    }

    function toRad(value) {
        return value * Math.PI / 180;
    }
});

document.addEventListener('DOMContentLoaded', () => {
    console.log("Initializing script...");

    const searchForm = document.getElementById('rechercheForm');
    if (searchForm) {
        console.log("Form found. Adding event listener...");
        searchForm.addEventListener('submit', searchCars);
    } else {
        console.error("Element with id 'rechercheForm' not found.");
    }

    async function searchCars(event) {
        event.preventDefault(); // Empêche la soumission du formulaire

        const address = document.getElementById('manualAddress').value;
        const dateDebut = document.getElementById('dateDebut').value;
        const dateFin = document.getElementById('dateFin').value;

        // Stocker dans localStorage
        localStorage.setItem('manualAddress', address);
        localStorage.setItem('dateDebut', dateDebut);
        localStorage.setItem('dateFin', dateFin);

        if (!address || !dateDebut || !dateFin) {
            alert('Veuillez remplir tous les champs.');
            return;
        }

        const geocoder = new google.maps.Geocoder();

        geocoder.geocode({ 'address': address }, async function(results, status) {
            if (status === 'OK') {
                const location = results[0].geometry.location;
                const userLat = location.lat();
                const userLng = location.lng();

                // Redirection vers la page /cars avec les paramètres de recherche
                window.location.href = `/cars?address=${encodeURIComponent(address)}&lat=${userLat}&lng=${userLng}&dateDebut=${dateDebut}&dateFin=${dateFin}`;
            } else {
                console.error('Geocode was not successful for the following reason:', status);
            }
        });

        return false;
    }
});


/* METTRE LES CHAMPS DANS LA PAGE CARS */

document.addEventListener('DOMContentLoaded', () => {
    console.log("Initializing script...");

    const searchForm = document.getElementById('rechercheForm');
    if (searchForm) {
        console.log("Form found. Adding event listener...");
        searchForm.addEventListener('submit', searchCars);
    } else {
        console.error("Element with id 'rechercheForm' not found.");
    }

    async function searchCars(event) {
        event.preventDefault(); // Empêche la soumission du formulaire

        const address = document.getElementById('manualAddress').value;
        const dateDebut = document.getElementById('dateDebut').value;
        const dateFin = document.getElementById('dateFin').value;

        // Stocker dans localStorage
        localStorage.setItem('manualAddress', address);
        localStorage.setItem('dateDebut', dateDebut);
        localStorage.setItem('dateFin', dateFin);

        if (!address || !dateDebut || !dateFin) {
            alert('Veuillez remplir tous les champs.');
            return;
        }

        const geocoder = new google.maps.Geocoder();

        geocoder.geocode({ 'address': address }, async function(results, status) {
            if (status === 'OK') {
                const location = results[0].geometry.location;
                const userLat = location.lat();
                const userLng = location.lng();

                // Redirection vers la page /cars avec les paramètres de recherche
                window.location.href = `/cars?address=${encodeURIComponent(address)}&lat=${userLat}&lng=${userLng}&dateDebut=${dateDebut}&dateFin=${dateFin}&page=0&size=15`;
            } else {
                console.error('Geocode was not successful for the following reason:', status);
            }
        });

        return false;
    }
});


document.addEventListener('DOMContentLoaded', function() {
    const adresse = localStorage.getItem('manualAddress');
    const dateDebut = localStorage.getItem('dateDebut');
    const dateFin = localStorage.getItem('dateFin');

    if (adresse) {
        document.getElementById('manualAddress').value = adresse;
        localStorage.removeItem('manualAddress');
    }
    if (dateDebut) {
        document.getElementById('dateDebut').value = dateDebut;
        localStorage.removeItem('dateDebut');
    }
    if (dateFin) {
        document.getElementById('dateFin').value = dateFin;
        localStorage.removeItem('dateFin');
    }
});
