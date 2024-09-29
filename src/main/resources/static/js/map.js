let map;
const geocodeCache = JSON.parse(localStorage.getItem('geocodeCache')) || {};
const mapboxApiKey = 'pk.eyJ1Ijoic2FuamkwNTEyIiwiYSI6ImNtMGQwYnhvNTA3NnUybXNjOWs2Mnhmem4ifQ.-JdPS28lnldjDaCwlcCecg';

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

      for (const car of cars) {
          const address = `${car.adresse}, ${car.codePostal} ${car.locality}`;
          console.log(`Processing address: ${address}`);
          geocodeAddressWithMapbox(address, car);
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

    configureAutocomplete('manualAddress', map);
}

function configureAutocomplete(elementId, map) {
    const inputElement = document.getElementById(elementId);

    if (inputElement) {
        console.log(`Initializing autocomplete for #${elementId}`);
        const autocomplete = new google.maps.places.Autocomplete(inputElement, {
            types: ['address'],
            componentRestrictions: { country: 'BE' }
        });

        console.log(`Autocomplete initialized for #${elementId}`);

        autocomplete.addListener('place_changed', () => {
            const place = autocomplete.getPlace();
            if (!place.geometry) {
                console.error(`L'adresse sélectionnée pour ${elementId} n'a pas de géométrie.`);
                return;
            }

            const address = place.formatted_address;
            const location = {
                lat: place.geometry.location.lat(),
                lng: place.geometry.location.lng()
            };

            console.log(`Address selected: ${address}`);
            console.log(`Location: ${JSON.stringify(location)}`);

            geocodeAddressWithMapbox(address, {
                brand: 'Example Brand',
                model: 'Example Model',
                adresse: address,
                codePostal: '0000',
                locality: 'Example Locality'
            });
        });
    } else {
        console.error(`Element with ID ${elementId} not found.`);
    }
}

async function geocodeAddressWithMapbox(address, car) {
    console.log(`Geocoding address with Mapbox: ${address}`);

    if (geocodeCache[address]) {
        console.log(`Using cached coordinates for address: ${address}`);
        createMarker(geocodeCache[address], car);
    } else {
        console.log(`Geocoding address: ${address}`);
        try {
            const response = await fetch(`https://api.mapbox.com/geocoding/v5/mapbox.places/${encodeURIComponent(address)}.json?access_token=${mapboxApiKey}`);
            const data = await response.json();

            console.log('Mapbox Geocoding Response:', data);

            if (data && data.features && data.features.length > 0) {
                const location = {
                    lat: data.features[0].geometry.coordinates[1],
                    lng: data.features[0].geometry.coordinates[0]
                };
                geocodeCache[address] = location;
                localStorage.setItem('geocodeCache', JSON.stringify(geocodeCache));
                console.log(`Geocoded coordinates stored in cache for address: ${address}`);
                createMarker(location, car);
            } else {
                console.error(`Le géocodage de l'adresse a échoué pour l'adresse : ${address}`);
            }
        } catch (error) {
            console.error('Erreur lors du géocodage avec Mapbox :', error);
        }
    }
}

function createMarker(position, car) {
    if (!map) {
        console.error('Map is not initialized.');
        return;
    }

    console.log(`Creating marker at position: ${position.lat}, ${position.lng}`);

    const marker = new google.maps.Marker({
        position: new google.maps.LatLng(position.lat, position.lng),
        map: map,
        title: `${car.brand} ${car.model}`,
        icon: {
            url: '/icones/mapCar.png',
            scaledSize: new google.maps.Size(30, 30),
        },
    });

    const infowindow = new google.maps.InfoWindow({
        content: `<div>
                    <p><p>
                    <p>Marque: ${car.brand}</p>
                    <p>Modèle: ${car.model}</p>
                    <p>Adresse: ${car.adresse}, ${car.codePostal} ${car.locality}</p>
                  </div>`
    });

    marker.addListener('click', () => {
        infowindow.open(map, marker);
    });

    console.log(`Marker created at position: ${position.lat}, ${position.lng}`);
}

document.addEventListener("DOMContentLoaded", () => {
    console.log("DOM fully loaded and parsed. Initializing map...");
    initMap();
});
