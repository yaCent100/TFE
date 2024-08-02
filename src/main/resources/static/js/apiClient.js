// src/js/apiClient.js
import axios from 'axios';

// Crée une instance d'Axios avec une base URL et des en-têtes par défaut
const apiClient = axios.create({
    baseURL: 'http://localhost:8080', // Base URL pour les requêtes
    headers: {
        'Content-Type': 'application/json'
    }
});

// Ajouter un intercepteur de requête pour ajouter le JWT dans les en-têtes
apiClient.interceptors.request.use(config => {
    const token = localStorage.getItem('jwtToken');
    if (token) {
        config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
}, error => {
    return Promise.reject(error);
});

// Ajouter un intercepteur de réponse pour gérer les erreurs globalement
apiClient.interceptors.response.use(response => {
    return response;
}, error => {
    if (error.response && error.response.status === 401) {
        window.location.href = '/login'; // Rediriger vers la page de connexion si le token est invalide
    }
    return Promise.reject(error);
});

export default apiClient;
