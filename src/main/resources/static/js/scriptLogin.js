document.addEventListener("DOMContentLoaded", function() {
        // Récupérer les onglets et leur contenu
        var loginTab = document.getElementById('loginTab');
        var registerTab = document.getElementById('registerTab');
        var loginContent = document.getElementById('loginContent');
        var registerContent = document.getElementById('registerContent');

        // Ajouter des écouteurs d'événements pour les onglets
        loginTab.addEventListener('click', function() {
            loginTab.classList.add('active');
            registerTab.classList.remove('active');
            loginContent.classList.add('active');
            registerContent.classList.remove('active');
        });

        registerTab.addEventListener('click', function() {
            registerTab.classList.add('active');
            loginTab.classList.remove('active');
            registerContent.classList.add('active');
            loginContent.classList.remove('active');
        });

        // Afficher le contenu du formulaire d'inscription par défaut
        registerTab.click();
    });