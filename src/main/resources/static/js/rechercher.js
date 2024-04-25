// Fonction pour envoyer une requête de recherche au serveur
async function searchCars() {
    // Récupérer les valeurs des champs du formulaire
    const address = document.getElementById('manualAddress').value;
    const dateDebut = document.getElementById('dateDebut').value;
    const dateFin = document.getElementById('dateFin').value;

    // Envoyer une requête GET au endpoint /search avec les paramètres de recherche
    const response = await fetch(`/search?address=${encodeURIComponent(address)}&dateDebut=${dateDebut}&dateFin=${dateFin}`);

    if (response.ok) {
        // Convertir la réponse en JSON
        const cars = await response.json();

        // Afficher les voitures disponibles sur la page
        displayCars(cars);
    } else {
        // Afficher un message d'erreur si la requête échoue
        console.error('Une erreur s\'est produite lors de la recherche des voitures.');
    }
}

// Fonction pour afficher les voitures disponibles sur la page
function displayCars(cars) {
    const carListElement = document.getElementById('carList');

    // Effacer la liste des voitures précédentes
    carListElement.innerHTML = '';

    // Parcourir chaque voiture et afficher ses détails sur la page
    cars.forEach(car => {
        const carElement = document.createElement('div');
        carElement.textContent = `${car.brand} ${car.model}`;
        carListElement.appendChild(carElement);
    });
}

// Ajouter un écouteur d'événements au formulaire de recherche
const searchForm = document.getElementById('searchForm');
searchForm.addEventListener('submit', (event) => {
    event.preventDefault(); // Empêcher le rechargement de la page
    searchCars(); // Appeler la fonction de recherche
});
