// Initialisation après le chargement du DOM
document.addEventListener('DOMContentLoaded', () => {
    // Ajouter des écouteurs d'événements aux liens de catégories
    document.querySelectorAll('.category-link').forEach(link => {
        link.addEventListener('click', (event) => {
            event.preventDefault();
            const category = event.currentTarget.getAttribute('data-category');
            searchCarsByCategory(category);
        });
    });

    // Récupérer et afficher les voitures les mieux notées
    fetchTopRatedCars();
});

// Fonction pour rechercher des voitures par catégorie
function searchCarsByCategory(category) {
    fetch('/api/cars/search', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ categoryName: category })
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        // Stocker les résultats de recherche dans localStorage
        localStorage.setItem('searchResults', JSON.stringify(data));
        // Rediriger vers la page des voitures
        window.location.href = '/cars';
    })
    .catch(error => console.error('Error:', error));
}

// Fonction pour récupérer et afficher les voitures les mieux notées
function fetchTopRatedCars() {
    fetch('/api/cars/top-rated')
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        displayTopRatedCars(data);
    })
    .catch(error => console.error('Error fetching top-rated cars:', error));
}

function displayTopRatedCars(cars) {
    const carListElement = document.getElementById('topRatedCars');

    if (!carListElement) {
        console.error("Element with id 'topRatedCars' not found.");
        return;
    }

    carListElement.innerHTML = ''; // Vider la liste avant d'ajouter les nouvelles voitures

    if (cars.length === 0) {
        carListElement.innerHTML = '<p>Aucune voiture disponible avec 5 étoiles.</p>';
        return;
    }

    cars.forEach(car => {
        const carLink = document.createElement('a');
        carLink.href = `/cars/${car.id}`;
        carLink.classList.add('car-link');

        const carItem = document.createElement('div');
        carItem.classList.add('car-item');

        // Calculer les étoiles
        const stars = '★'.repeat(Math.round(car.averageRating)) + '☆'.repeat(5 - Math.round(car.averageRating));

        carItem.innerHTML = `
            <img src="uploads/${car.photoUrl[0]}" alt="${car.brand} ${car.model}" height="150">
            <h5>${car.brand} ${car.model}</h5>
            <p>${car.locality}</p>
            <div class="car-details">
                <span>${stars}</span>
                <span>${car.price.middlePrice} €/jour</span>
            </div>
        `;

        carLink.appendChild(carItem);
        carListElement.appendChild(carLink);
    });

    carListElement.style.display = 'block';
}
