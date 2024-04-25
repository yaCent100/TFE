let map;

async function initMap() {
    // Récupérer la latitude et la longitude de Bruxelles
    const brussels = { lat: 50.8504500, lng: 4.3487800 };

    // Importer les bibliothèques nécessaires
    const { Map, Autocomplete } = await google.maps.importLibrary("maps", "places");

    // Créer la carte centrée sur Bruxelles
    map = new Map(document.getElementById("map"), {
        center: brussels,
        zoom: 12,
        mapId: "map",
    });

    // Récupérer les données des voitures depuis le backend
    const response = await fetch('/available-cars');
    const cars = await response.json();

    cars.forEach(car => {
        // Construction de l'adresse complète
        const address = `${encodeURIComponent(car.adresse)}, ${car.codePostal} ${encodeURIComponent(car.locality)}`;

        // Géocodage de l'adresse de la voiture pour obtenir les coordonnées
        const geocoder = new google.maps.Geocoder();
        geocoder.geocode({ address: address }, (results, status) => {
            if (status === "OK") {
                const carLatLng = results[0].geometry.location;

                const marker = new google.maps.Marker({
                    position: carLatLng,
                    map: map,
                    title: car.brand + ' ' + car.model,
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

                // Ajouter un écouteur d'événements de clic à chaque marqueur
                marker.addListener('click', function() {
                    // Ouvrir l'infobulle au clic
                    infowindow.open(map, marker);
                });

            } else {
                console.error("Le géocodage de l'adresse de la voiture a échoué :", status);
            }
        });
    });

    // Sélection de l'input de l'adresse
    const adresseInput = document.getElementById('manualAddress');

    // Activation de l'autocomplétion pour les adresses en Belgique
    const autocomplete = new Autocomplete(adresseInput, {
        types: ['address'],
        componentRestrictions: { country: 'BE' } // Restriction au pays Belgique (BE)
    });

    // Ajout d'un écouteur d'événements de sélection d'une adresse dans l'autocomplétion
    autocomplete.addListener('place_changed', () => {
        // Obtenir l'objet place de l'autocomplétion
        const place = autocomplete.getPlace();
        if (!place.geometry) {
            console.error("L'adresse sélectionnée n'a pas de géométrie.");
            return;
        }

        // Centrer la carte sur l'adresse sélectionnée
        map.setCenter(place.geometry.location);
        map.setZoom(15); // Zoom sur l'adresse sélectionnée
    });
}

// Appel de la fonction initMap lorsque la page est chargée
initMap();
