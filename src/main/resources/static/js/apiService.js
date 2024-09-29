// apiService.js

// Fonction pour récupérer les voitures en fonction des paramètres de recherche
async function searchCarsAPI(address, userLat, userLng, dateDebut, dateFin, page = 1, limit = 10) {
    const response = await fetch(`/api/cars/search?address=${encodeURIComponent(address)}&lat=${userLat}&lng=${userLng}&dateDebut=${dateDebut}&dateFin=${dateFin}&page=${page}&limit=${limit}`);
        if (!response.ok) {
        throw new Error('Erreur lors de la récupération des voitures');
    }
    return await response.json();
}

// Fonction pour récupérer toutes les voitures
async function loadAllCarsAPI(page = 1, limit = 10) {
    const response = await fetch(`/api/cars?page=${page}&limit=${limit}`);
    if (!response.ok) {
        throw new Error('Erreur lors de la récupération des voitures');
    }
    return await response.json();
}


// Fonction pour récupérer les filtres de catégories
async function fetchCategories() {
    const response = await fetch('/api/categories');
    if (!response.ok) {
        throw new Error('Erreur lors de la récupération des catégories');
    }
    return await response.json();
}

// Fonction pour récupérer les filtres de boîte de vitesses
async function fetchGearboxTypes() {
    const response = await fetch('/api/gearbox');
    if (!response.ok) {
        throw new Error('Erreur lors de la récupération des boîtes de vitesses');
    }
    return await response.json();
}

// Fonction pour récupérer les filtres de motorisation
async function fetchMotorisationTypes() {
    const response = await fetch('/api/motorisation');
    if (!response.ok) {
        throw new Error('Erreur lors de la récupération des motorisations');
    }
    return await response.json();
}

// Fonction pour récupérer les filtres de kilométrage
async function fetchKilometrageOptions() {
    const response = await fetch('/api/kilometrage');
    if (!response.ok) {
        throw new Error('Erreur lors de la récupération des kilométrages');
    }
    return await response.json();
}

// Fonction pour récupérer les filtres du nombre de places
async function fetchPlacesOptions() {
    const response = await fetch('/api/places');
    if (!response.ok) {
        throw new Error('Erreur lors de la récupération des places');
    }
    return await response.json();
}
