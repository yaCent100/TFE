window.onload = function() {
    showConnexion(); // Appel initial pour configurer l'état de la page
};

function showConnexion() {
    // Gérer l'affichage des sections
    document.querySelector('.signin-layout-connexion').style.display = 'block';
    document.querySelector('.signin-layout-inscription').style.display = 'none';

    // Mettre à jour les classes actives
    document.getElementById('connexion-link').classList.add('active');
    document.getElementById('inscription-link').classList.remove('active');

    // Mettre à jour les images
    document.getElementById('connexion-image').style.display = 'block';
    document.getElementById('inscription-image').style.display = 'none';
}

function showInscription() {
    // Gérer l'affichage des sections
    document.querySelector('.signin-layout-connexion').style.display = 'none';
    document.querySelector('.signin-layout-inscription').style.display = 'block';

    // Mettre à jour les classes actives
    document.getElementById('connexion-link').classList.remove('active');
    document.getElementById('inscription-link').classList.add('active');

    // Mettre à jour les images
    document.getElementById('connexion-image').style.display = 'none';
    document.getElementById('inscription-image').style.display = 'block';
}

document.addEventListener('DOMContentLoaded', function() {
    // Ajouter les écouteurs d'événements
    document.getElementById('connexion-link').addEventListener('click', function(event) {
        event.preventDefault();
        showConnexion();
    });
    document.getElementById('inscription-link').addEventListener('click', function(event) {
        event.preventDefault();
        showInscription();
    });
});
