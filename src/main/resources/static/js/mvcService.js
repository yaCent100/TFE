import apiClient from './apiClient';

// Fonction pour obtenir une page MVC
export async function fetchMvcPage(url) {
    try {
        const response = await apiClient.get(url); // Utilisez l'instance d'Axios configurée
        // Injecter la réponse HTML dans le document
        document.open();
        document.write(response.data);
        document.close();
    } catch (error) {
        console.error('Error fetching MVC page:', error);
        if (error.response && error.response.status === 401) {
            window.location.href = '/login';
        }
    }
}

// Fonction pour soumettre un formulaire MVC
export async function submitForm(url, data) {
    try {
        const response = await apiClient.post(url, data, {
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
        });
        return response.data;
    } catch (error) {
        throw error;
    }
}
