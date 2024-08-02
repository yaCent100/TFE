import { fetchMvcPage } from './mvcService';

document.addEventListener('DOMContentLoaded', () => {
    // Exemple d'appel pour charger une page MVC
    fetchMvcPage('/home').then(() => {
        console.log('Home page fetched successfully');
    }).catch(error => {
        console.error('Error fetching home page:', error);
    });
});
