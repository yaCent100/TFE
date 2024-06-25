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

    /*----------------- ALL CARS --------------*/


        /*----------------- ANNONCE ----------------*/
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
document.addEventListener('DOMContentLoaded', function() {
    const calendarEl = document.getElementById('calendrier');
    const dateRangesContainer = document.getElementById('date-ranges-container');
    const carIdInput = document.getElementById('car-id');
    const carId = carIdInput.value;
    let selectedStartDate, selectedEndDate;

    const calendar = new FullCalendar.Calendar(calendarEl, {
        schedulerLicenseKey: 'CC-Attribution-NonCommercial-NoDerivatives',
        initialView: 'multiMonthView',
        views: {
            multiMonthView: {
                type: 'multiMonth',
                multiMonthTitleFormat: { month: 'long', year: 'numeric' },
                duration: { months: 12 },
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
        select: function(info) {
            selectedStartDate = info.startStr;
            selectedEndDate = info.endStr;
            $('#availabilityModal').modal('show');
        },
        initialDate: new Date(),
        locale: 'fr',
        events: function(fetchInfo, successCallback, failureCallback) {
            Promise.all([
                fetch('/api/reservations?carId=' + carId)
                    .then(response => {
                        if (!response.ok) {
                            throw new Error("Erreur lors de la récupération des réservations");
                        }
                        return response.json();
                    })
                    .then(data => {
                        return data.map(item => ({
                            id: item.id,
                            start: item.debutLocation,
                            end: item.finLocation,
                            display: 'background',
                            backgroundColor: '#00BFFF'
                        }));
                    }),
                fetch('/api/unavailable-dates?carId=' + carId)
                    .then(response => {
                        if (!response.ok) {
                            throw new Error("Erreur lors de la récupération des dates d'indisponibilité");
                        }
                        return response.json();
                    })
                    .then(data => {
                        return data.map(item => ({
                            id: item.id,
                            start: item.startDate,
                            end: item.endDate,
                            display: 'background',
                            backgroundColor: 'stripe-background'
                        }));
                    })
            ]).then(eventArrays => {
                const allEvents = eventArrays.flat();
                successCallback(allEvents);
            }).catch(error => {
                console.error('Erreur:', error);
                failureCallback(error);
            });
        },
        eventDidMount: function(info) {
            info.el.style.backgroundColor = info.event.backgroundColor;
        }
    });
    calendar.updateSize(); // Force the calendar to update its size
    calendar.render();


      function checkForOverlap(start, end) {
           const events = calendar.getEvents();
           for (const event of events) {
               if (event.backgroundColor === 'gray' || event.backgroundColor === '#00BFFF') {
                   if ((start >= event.startStr && start < event.endStr) || (end > event.startStr && end <= event.endStr)) {
                       return true;
                   }
               }
           }
           return false;
       }

    document.getElementById('modal-mark-unavailable').addEventListener('click', function() {
        if (checkForOverlap(selectedStartDate, selectedEndDate)) {
            alert('Les dates sélectionnées chevauchent une période existante.');
            return;
        }
        addDateRangeToForm('unavailable');
        calendar.addEvent({
            start: selectedStartDate,
            end: selectedEndDate,
            display: 'background',
            backgroundColor: 'gray'
        });
        $('#availabilityModal').modal('hide');
    });

    document.getElementById('modal-mark-available').addEventListener('click', function() {
        if (checkForOverlap(selectedStartDate, selectedEndDate)) {
            alert('Les dates sélectionnées chevauchent une période existante.');
            return;
        }
        addDateRangeToForm('available');
        const events = calendar.getEvents();
        for (const event of events) {
            if (event.startStr === selectedStartDate && event.endStr === selectedEndDate) {
                event.remove();
            }
        }
        $('#availabilityModal').modal('hide');
    });

    function addDateRangeToForm(status) {
        const dateRangeDiv = document.createElement('div');
        dateRangeDiv.classList.add('date-range');
        dateRangeDiv.innerHTML = `
            <input type="hidden" name="startDates" value="${selectedStartDate}">
            <input type="hidden" name="endDates" value="${selectedEndDate}">
            <input type="hidden" name="statuses" value="${status}">
        `;
        dateRangesContainer.appendChild(dateRangeDiv);
    }
});



/*------------- MODE RESERVATION ----------------------*/

document.addEventListener('DOMContentLoaded', function() {
    const autoButton = document.querySelector('.choose-automatic-reservation');
    const manualButton = document.querySelector('.choose-manual-reservation');
    const autoSection = document.querySelector('.reservation-mode-auto');
    const manualSection = document.querySelector('.reservation-mode-manual');
    const bookingModeInput = document.getElementById('booking-mode-input');

    // Initialiser avec le mode actuel actif
    if (bookingModeInput.value === 'automatique') {
        autoButton.classList.add('active-button');
        autoSection.classList.add('show');
        manualSection.classList.add('hide');
    } else {
        manualButton.classList.add('active-button');
        manualSection.classList.add('show');
        autoSection.classList.add('hide');
    }

    autoButton.addEventListener('click', function() {
        autoSection.classList.add('show');
        manualSection.classList.remove('show');
        manualSection.classList.add('hide');
        autoButton.classList.add('active-button');
        manualButton.classList.remove('active-button');
        bookingModeInput.value = 'automatique';
    });

    manualButton.addEventListener('click', function() {
        manualSection.classList.add('show');
        autoSection.classList.remove('show');
        autoSection.classList.add('hide');
        manualButton.classList.add('active-button');
        autoButton.classList.remove('active-button');
        bookingModeInput.value = 'manuelle';
    });
});
