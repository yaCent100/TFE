document.addEventListener("DOMContentLoaded", function() {
    var tabLinks = document.querySelectorAll('.nav-profil a');

    function setActiveTab() {
        var currentPath = window.location.pathname;

        tabLinks.forEach(function(link) {
            if (link.getAttribute('href') === currentPath) {
                link.classList.add('active');
            } else {
                link.classList.remove('active');
            }
        });
    }

    tabLinks.forEach(function(tabLink) {
        tabLink.addEventListener("click", function() {
            // Simule un clic et met à jour l'onglet actif sans recharger la page
            tabLinks.forEach(function(link) {
                link.classList.remove('active');
            });
            this.classList.add('active');
        });
    });

    setActiveTab();
});


/* MODIFIER les photo cars*/



document.addEventListener('DOMContentLoaded', function() {
    // Récupérer tous les liens d'onglet
    const tabLinks = document.querySelectorAll('.car-detail-onglet');

    // Ajouter un écouteur d'événements à chaque lien d'onglet
    tabLinks.forEach(function(tabLink) {
        tabLink.addEventListener('click', function(event) {
            // Empêcher le comportement par défaut du lien
            event.preventDefault();

            // Supprimer la classe "active" de tous les liens d'onglet
            tabLinks.forEach(function(link) {
                link.classList.remove('active');
            });

            // Ajouter la classe "active" au lien d'onglet cliqué
            tabLink.classList.add('active');

            // Récupérer l'ID de la cible du lien
            const targetId = tabLink.getAttribute('href');

            // Récupérer tous les contenus d'onglet
            const tabContents = document.querySelectorAll('.tab-content-car');

            // Parcourir tous les contenus d'onglet pour afficher celui correspondant à l'onglet actif et masquer les autres
            tabContents.forEach(function(content) {
                if ('#' + content.id === targetId) {
                    content.classList.add('active');
                } else {
                    content.classList.remove('active');
                }
            });
        });
    });
});
// ---------------------------SUPRESSION DU COMPTE --------------
document.getElementById('deleteAccountButton').addEventListener('click', function() {
    if (confirm('Êtes-vous sûr de vouloir supprimer votre compte ? Cette action est irréversible.')) {
        const userId = document.getElementById('userId').value;

        fetch(`/api/users/${userId}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then(response => {
            if (response.ok) {
                alert("Votre compte a été supprimé avec succès.");
                // Redirection après suppression
                window.location.href = "/logout";
            } else {
                response.text().then(text => {
                    alert("Erreur lors de la suppression : " + text);
                });
            }
        })
        .catch(error => {
            console.error('Erreur :', error);
            alert("Une erreur est survenue lors de la suppression de votre compte.");
        });
    }
});
    //----------------- ALL CARS --------------


        //----------------- ANNONCE ----------------
   document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.no-picture').forEach(function(img, index) {
        img.addEventListener('click', function() {
            const fileInput = document.querySelector('#photoInput_' + index);
            if (fileInput) {
                fileInput.click();
            } else {
                console.error('File input not found for index:', index);
            }
        });
    });
});



                /*------------ CALENDRIER ---------------*/





/*------------- CAR - MODE RESERVATION ----------------------*/




//  -----------------------INDEX----------------------------

document.getElementById('editPictureLink').addEventListener('click', function(e) {
    e.preventDefault();
    document.getElementById('pictureInput').click();
});

document.getElementById('pictureInput').addEventListener('change', function() {
    const fileInput = document.getElementById('pictureInput');
    const file = fileInput.files[0];
    const userId = document.getElementById('userId').value;

    console.log(userId);

    if (file && userId) { // Assurez-vous que l'ID de l'utilisateur est bien récupéré
        const formData = new FormData();
        formData.append('file', file);

        fetch(`/api/account/${userId}/update-profile-picture`, {
            method: 'POST',
            body: formData,
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.text();
        })
        .then(result => {
            // Rafraîchit la page pour afficher la nouvelle photo de profil
            window.location.reload();
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Erreur lors de la mise à jour de la photo de profil');
        });
    } else {
        alert('Fichier ou ID utilisateur manquant');
    }
});





