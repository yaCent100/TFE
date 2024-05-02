// Fonction pour envoyer la requête AJAX au serveur et récupérer les voitures disponibles
function searchAvailableCars() {
    const address = document.getElementById("manualAddress").value;
    const dateDebut = document.getElementById("dateDebut").value;
    const dateFin = document.getElementById("dateFin").value;

    fetch(`/search?address=${address}&dateDebut=${dateDebut}&dateFin=${dateFin}`)
        .then(response => response.json())
        .then(data => {
            const filteredAndSortedCars = filterAndSortCars(data);
            displayCars(filteredAndSortedCars);
        })
        .catch(error => console.error('Une erreur s\'est produite lors de la recherche des voitures :', error));
}

// Fonction pour filtrer et trier les voitures par proximité et disponibilité
// Fonction pour filtrer et trier les voitures par proximité et disponibilité
function filterAndSortCars(cars) {
    // Coordonnées de l'utilisateur (par exemple, Paris)
    const userCoordinates = { latitude: 48.8566, longitude: 2.3522 };

    // Fonction pour calculer la distance entre deux coordonnées géographiques (en kilomètres)
    function calculateDistance(point1, point2) {
        const earthRadiusKm = 6371;

        const lat1 = degreesToRadians(point1.latitude);
        const lon1 = degreesToRadians(point1.longitude);
        const lat2 = degreesToRadians(point2.latitude);
        const lon2 = degreesToRadians(point2.longitude);

        const dLat = lat2 - lat1;
        const dLon = lon2 - lon1;

        const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                  Math.cos(lat1) * Math.cos(lat2) *
                  Math.sin(dLon / 2) * Math.sin(dLon / 2);

        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadiusKm * c;
    }

    // Fonction utilitaire pour convertir degrés en radians
    function degreesToRadians(degrees) {
        return degrees * Math.PI / 180;
    }

    // Filtrer les voitures disponibles
    const availableCars = cars.filter(car => car.available);

    // Trier les voitures par proximité
    const sortedCars = availableCars.sort((car1, car2) => {
        const distanceToCar1 = calculateDistance(userCoordinates, car1.coordinates);
        const distanceToCar2 = calculateDistance(userCoordinates, car2.coordinates);
        return distanceToCar1 - distanceToCar2;
    });

    return sortedCars;
}

// Fonction pour afficher les voitures filtrées et triées
function displayCars(cars) {
    const carlistContainer = document.getElementById("carlist");
    carlistContainer.innerHTML = ""; // Efface le contenu précédent

    cars.forEach(car => {
        const carCard = createCarCard(car);
        carlistContainer.appendChild(carCard);
    });
}

// Fonction pour créer une carte HTML représentant une voiture
function createCarCard(car) {
    const card = document.createElement("div");
    card.classList.add("col-12", "mb-3");

    const innerHTML = `
        <div class="card p-3 rounded">
            <!-- Insérez ici le contenu de la carte représentant la voiture -->
        </div>
    `;
    card.innerHTML = innerHTML;

    // Modifiez le contenu de la carte pour afficher les détails de la voiture

    return card;
}
