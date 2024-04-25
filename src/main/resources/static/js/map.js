let map;

async function initMap() {
  // Récupérer la latitude et la longitude de Bruxelles
  const brussels = { lat: 50.8504500, lng: 4.3487800 };

  const { Map } = await google.maps.importLibrary("maps");

  // Créer la carte centrée sur Bruxelles
  map = new Map(document.getElementById("map"), {
    center: brussels,
    zoom: 12, // Ajuster le niveau de zoom selon vos préférences
    mapId: "map",
  });

  // Récupérer les données des voitures depuis le backend
  const response = await fetch('/cars/addresses');
  const cars = await response.json();

  const infowindow = new google.maps.InfoWindow();


cars.forEach(car => {
    // Construction de l'adresse complète
    const address = `${encodeURIComponent(car.adresse)}, ${car.postalCode} ${encodeURIComponent(car.locality)}`;

    console.log("Adresse complète :", address);

    // Géocodage de l'adresse de la voiture pour obtenir les coordonnées
    const geocoder = new google.maps.Geocoder();
    geocoder.geocode({ address: address }, (results, status) => {
        if (status === "OK") {
            const carLatLng = results[0].geometry.location;

            const marker = new google.maps.Marker({
                position: carLatLng,
                map: map,
                title: car.brand + ' ' + car.model, // Titre de l'infobulle affichée lorsque l'utilisateur clique sur l'icône
                icon: {
                    url: '/icones/mapCar.png', // Chemin vers votre icône personnalisée
                    scaledSize: new google.maps.Size(30, 30), // Taille de l'icône en pixels (largeur, hauteur)
                },
            });

            const infowindow = new google.maps.InfoWindow({
                content: `<div>
                            <p>Marque: ${car.brand}</p>
                            <p>Modèle: ${car.model}</p>
                            <p>Adresse: ${car.adresse}, ${car.postalCode} ${car.locality}</p>
                          </div>`
            });

            // Ajouter un écouteur d'événements domready pour l'infobulle
            google.maps.event.addListenerOnce(infowindow, 'domready', () => {
                const infowindowContent = document.querySelector('.gm-style-iw');
                infowindowContent.style.cursor = 'pointer'; // Ajouter le curseur pointer
                infowindowContent.addEventListener('click', () => {
                    window.location.href = `/cars/${car.id}`;
                });
            });

            // Ajouter un écouteur d'événements de clic à chaque marqueur
            marker.addListener('click', function() {
                // Ouvrir l'infobulle
                infowindow.open(map, marker);
            });

        } else {
            console.error("Le géocodage de l'adresse de la voiture a échoué :", status);
        }
    });
});



}

// Appel de la fonction initMap lorsque la page est chargée
initMap();


// Sélection de l'input de l'adresse
const adresseInput = document.querySelector('.adresse');

// Ajout d'un gestionnaire d'événements de focus à l'input de l'adresse
adresseInput.addEventListener('focus', () => {
    // Utilisation de l'API de géolocalisation pour obtenir les coordonnées de l'utilisateur
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition((position) => {
            const userPosition = {
                lat: position.coords.latitude,
                lng: position.coords.longitude
            };

            // Centrer la carte sur la position actuelle de l'utilisateur
            map.setCenter(userPosition);

            // Création de l'option "Position actuelle" pour l'autocomplétion
            const currentPositionOption = document.createElement('div');
            currentPositionOption.classList.add('cobalt-Autocomplete__item');
            currentPositionOption.innerHTML = '<span class="cobalt-Autocomplete__item-value">Position actuelle</span>';

            // Ajout d'un gestionnaire d'événements de clic à l'option "Position actuelle"
            currentPositionOption.addEventListener('click', () => {
                adresseInput.value = 'Position actuelle';
            });

            // Sélection de la liste d'autocomplétion et ajout de l'option "Position actuelle"
            const autoCompleteContainer = document.querySelector('.cobalt-Autocomplete');
            autoCompleteContainer.prepend(currentPositionOption);
        }, () => {
            // Gérer les erreurs liées à la géolocalisation ici
            console.error('La géolocalisation a échoué');
        });
    } else {
        // Gérer le cas où la géolocalisation n'est pas prise en charge par le navigateur
        console.error('La géolocalisation n\'est pas prise en charge');
    }
});




