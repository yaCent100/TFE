/* CALENDRIER */

document.addEventListener('DOMContentLoaded', function() {
    console.log("Le DOM est chargé.");

    const calendarEl = document.getElementById('calendar');
    console.log("Élément du calendrier trouvé :", calendarEl);

    if (!calendarEl) {
        console.error("Erreur : Aucun élément trouvé avec l'ID 'calendar'.");
        return;
    }

    const calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: 'multiMonth',
        views: {
            multiMonth: {
                type: 'multiMonth',
                duration: { months: 6 },
                multiMonthTitleFormat: { month: 'long', year: 'numeric' },
                multiMonthMaxColumns: 2,
                fixedWeekCount: false,
                showNonCurrentDates: false
            }
        },
        selectable: true,
        initialDate: new Date(),
        locale: 'fr',
        dayHeaderContent: function(arg) {
            const day = arg.date.toLocaleDateString('fr-FR', { weekday: 'long' });
            const firstLetter = day.charAt(0).toUpperCase();
            return firstLetter;
        },
        titleFormat: { year: 'numeric', month: 'long' },
        themeSystem: 'bootstrap5',
        headerToolbar: false
    });

    console.log("Calendrier créé :", calendar);

    calendar.render();
    console.log("Calendrier rendu.");
});




