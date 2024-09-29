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
        console.log('Response status:', response.status);  // Ajouter du logging
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        console.log('Top-rated cars data:', data);  // Voir les données reçues
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
        const fullStars = Math.round(car.averageRating);
        const emptyStars = 5 - fullStars;

        let starHtml = '';
        for (let i = 0; i < fullStars; i++) {
            starHtml += '<span class="star filled">★</span>';
        }
        for (let i = 0; i < emptyStars; i++) {
            starHtml += '<span class="star">☆</span>';
        }

        carItem.innerHTML = `
            <img src="uploads/photo-car/${car.photoUrl}" alt="${car.brand} ${car.model}" height="150">
            <div class="p-2">
                <h5>${car.brand} ${car.model}</h5>
                <p class="text-muted">${car.locality}</p>
                <div class="car-details d-flex justify-content-between align-items-center">
                    <div class="stars">${starHtml}</div>
                    <span class="text-end car-detail-price">${car.displayPrice} €/jour</span>
                </div>
            </div>
        `;

        carLink.appendChild(carItem);
        carListElement.appendChild(carLink);
    });

    carListElement.style.display = 'block';
}


});

