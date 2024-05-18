 document.addEventListener("DOMContentLoaded", function() {
        var tabLinks = document.querySelectorAll('.nav-profil a');

        tabLinks.forEach(function(tabLink) {
            tabLink.addEventListener("click", function(event) {
                // Retire la classe 'active' de tous les liens d'onglet
                tabLinks.forEach(function(link) {
                    link.classList.remove('active');
                });

                // Ajoute la classe 'active' au lien d'onglet qui a été cliqué
                this.classList.add('active');
            });
        });
    });

/* MODIFIER LA PHOTO DE PROFIL*/
$(document).ready(function() {
    $('#editPictureLink').click(function(e) {
        e.preventDefault();  // Empêche le navigateur de suivre le lien
        $('#pictureInput').click();  // Déclenche le clic sur l'input
    });
});


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
            const tabContents = document.querySelectorAll('.tab-content');

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











/* annonce */
 document.querySelectorAll('.no-picture').forEach(function(img) {
    img.addEventListener('click', function() {
      // Récupérer l'identifiant unique de l'image
      const imageId = this.parentNode.parentNode.parentNode.id;

      // Récupérer l'élément d'entrée de fichier correspondant à l'image cliquée
      const fileInput = document.getElementById(imageId + '-input');

      // Simuler un clic sur l'élément d'entrée de fichier pour ouvrir la boîte de dialogue de sélection de fichier
      fileInput.click();
    });
  });

  document.querySelectorAll('.picture-input').forEach(function(input) {
    input.addEventListener('change', function() {
      const file = this.files[0];
      const preview = this.parentNode.querySelector('.no-picture');
      const reader = new FileReader();

      reader.onload = function(event) {
        preview.src = event.target.result;
      };

      reader.readAsDataURL(file);
    });
  });



/* CALENDRIER */
document.addEventListener('DOMContentLoaded', function() {
    const calendarEl = document.getElementById('calendrier');
   const startDateInput = document.getElementById('start-date');
    const endDateInput = document.getElementById('end-date');
    const markUnavailableBtn = document.getElementById('mark-unavailable');
    const markAvailableBtn = document.getElementById('mark-available');

    const calendar = new FullCalendar.Calendar(calendarEl, {
       schedulerLicenseKey: 'CC-Attribution-NonCommercial-NoDerivatives',
       initialView: 'multiMonthView', // Vue initiale

        views: {
            multiMonthView: {
                    type: 'multiMonth',
                    multiMonthTitleFormat: { month: 'long', year: 'numeric' },
                    multiMonthMaxColumns: 2,
                    duration: { months: 6 },
                    dayHeaderContent: function(arg) {
                        const day = arg.date.toLocaleDateString('fr-FR', { weekday: 'long' });
                        const firstLetter = day.charAt(0).toUpperCase();
                        return firstLetter;
                    },
                    fixedWeekCount: false,
                    showNonCurrentDates: false
            }

        },
        selectable: true,
        initialDate: new Date(),
        locale: 'fr',
        events: function(fetchInfo, successCallback, failureCallback) {
            fetch('/api/reservations')
                .then(response => {
                    if (!response.ok) {
                        throw new Error("Erreur lors de la récupération des réservations");
                    }
                    return response.json();
                })
                .then(data => {
                        console.log(data); // Vérifiez les données reçues ici

                    // Transformer les champs pour correspondre à FullCalendar
                    const events = data.map(item => ({
                        id: item.id,
                        start: item.debutLocation,
                        end: item.finLocation,
                        display: 'background'

                    }));
                    successCallback(events); // Transmettre les événements transformés à FullCalendar
                })
                .catch(error => {
                    console.error('Erreur:', error);
                    failureCallback(error);
                });
        },
        eventDidMount: function(info) {

            // Manipulez chaque événement individuellement si nécessaire
            info.el.style.backgroundColor = info.event.backgroundColor;
            console.log(info);
        }
    });
    calendar.setOption('height', 'auto');
    calendar.render();



        // Ajout de l'indisponibilité
       markUnavailableBtn.addEventListener('click', function() {
           const start = startDateInput.value;
           const end = new Date(endDateInput.value);
           end.setDate(end.getDate() + 1); // Ajouter un jour pour inclure la date de fin complète

           if (start && end) {
               // Vérifier si un événement pour ces dates existe déjà
               let isOverlap = false;
               calendar.getEvents().forEach(event => {
                   if (event.startStr === start && event.endStr === end.toISOString().split('T')[0] && event.title === 'Indisponible') {
                       isOverlap = true;
                   }
               });

               if (isOverlap) {
                   alert('Un événement d\'indisponibilité existe déjà pour ces dates.');
               } else {
                   const event = {
                       start: start,
                       end: end.toISOString().split('T')[0],
                       display: 'background',
                       allDay: true
                   };
                   calendar.addEvent(event);
               }
           } else {
               alert('Veuillez choisir les dates de début et de fin.');
           }
       });

        // Suppression de l'indisponibilité
        markAvailableBtn.addEventListener('click', function() {
            const start = startDateInput.value;
            const end = endDateInput.value;
            calendar.getEvents().forEach(event => {
                if (event.startStr === start && event.endStr === end && event.title === 'Indisponible') {
                    event.remove();
                }
            });
        });




});




/* MODE RESERVATION */

document.addEventListener('DOMContentLoaded', function() {
    const autoButton = document.querySelector('.choose-automatic-reservation');
    const manualButton = document.querySelector('.choose-manual-reservation');
    const autoSection = document.querySelector('.reservation-mode-auto');
    const manualSection = document.querySelector('.reservation-mode-manual');

    // Initialiser avec le mode automatique actif
    autoButton.classList.add('active-button');
    autoSection.classList.add('show');
    manualSection.classList.add('hide');

    autoButton.addEventListener('click', function() {
        autoSection.classList.add('show');
        manualSection.classList.remove('show');
        manualSection.classList.add('hide');
        autoButton.classList.add('active-button');
        manualButton.classList.remove('active-button');
    });

    manualButton.addEventListener('click', function() {
        manualSection.classList.add('show');
        autoSection.classList.remove('show');
        autoSection.classList.add('hide');
        manualButton.classList.add('active-button');
        autoButton.classList.remove('active-button');
    });
});
