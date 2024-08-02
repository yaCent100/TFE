let map;
const geocodeCache = JSON.parse(localStorage.getItem('geocodeCache')) || {};

async function initMap() {
    const brussels = { lat: 50.8504500, lng: 4.3487800 };

    const { Map } = await google.maps.importLibrary("maps");
    const [{ Autocomplete }] = await Promise.all([google.maps.importLibrary("places")]);

    map = new Map(document.getElementById("map"), {
        center: brussels,
        zoom: 12,
        mapId: "map",
    });

    const response = await fetch('/cars/addresses');
    const cars = await response.json();
    const geocoder = new google.maps.Geocoder();

    for (const car of cars) {
        const address = `${car.adresse}, ${car.codePostal} ${car.locality}`;
        console.log(`Processing address: ${address}`);
        geocodeAddress(geocoder, address, car);
    }

    const adresseInput = document.getElementById('manualAddress');
    const autocomplete = new Autocomplete(adresseInput, {
        types: ['address'],
        componentRestrictions: { country: 'BE' }
    });

    autocomplete.addListener('place_changed', () => {
        const place = autocomplete.getPlace();
        if (!place.geometry) {
            console.error("L'adresse sélectionnée n'a pas de géométrie.");
            return;
        }

        map.setCenter(place.geometry.location);
        map.setZoom(15);
    });

    // Mettre à jour le cache dans le localStorage
    window.addEventListener('beforeunload', () => {
        localStorage.setItem('geocodeCache', JSON.stringify(geocodeCache));
    });
}

function geocodeAddress(geocoder, address, car) {
    if (geocodeCache[address]) {
        console.log(`Using cached coordinates for address: ${address}`);
        createMarker(geocodeCache[address], car);
    } else {
        console.log(`Geocoding address: ${address}`);
        geocoder.geocode({ address: address }, (results, status) => {
            if (status === "OK" && results[0]) {
                geocodeCache[address] = results[0].geometry.location;
                console.log(`Geocoded coordinates stored in cache for address: ${address}`);
                createMarker(results[0].geometry.location, car);
            } else {
                console.error(`Le géocodage de l'adresse de la voiture a échoué : ${status} (Adresse: ${address})`);
            }
        });
    }
}

function createMarker(position, car) {
    const marker = new google.maps.Marker({
        position: position,
        map: map,
        title: `${car.brand} ${car.model}`,
        icon: {
            url: '/icones/mapCar.png',
            scaledSize: new google.maps.Size(30, 30),
        },
    });

    const infowindow = new google.maps.InfoWindow({
        content: `<div>
                    <p>Marque: ${car.brand}</p>
                    <p>Modèle: ${car.model}</p>
                    <p>Adresse: ${car.adresse}, ${car.codePostal} ${car.locality}</p>
                  </div>`
    });

    marker.addListener('click', () => {
        infowindow.open(map, marker);
    });
}

initMap();
