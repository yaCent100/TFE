// src/js/auth.js
import apiClient from './apiClient';

// Fonction pour authentifier l'utilisateur
export async function authenticateUser(username, password) {
    try {
        const response = await apiClient.post('/api/authenticate', { username, password });
        return response.data; // Retourne les données de la réponse
    } catch (error) {
        throw error; // Propager l'erreur pour gestion ultérieure
    }
}
