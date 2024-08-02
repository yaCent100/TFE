import apiClient from './js/apiClient';

// Fonction pour authentifier l'utilisateur
export async function authenticateUser(username, password) {
    try {
        const response = await apiClient.post('/api/authenticate', { username, password });
        return response.data;
    } catch (error) {
        throw error;
    }
}

// Ajouter d'autres fonctions pour les autres appels API si nécessaire
