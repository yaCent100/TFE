document.addEventListener('DOMContentLoaded', function() {
    const calendarEl = document.getElementById('calendar');

    if (!calendarEl) {
        console.error("Erreur : Aucun élément trouvé avec l'ID 'calendar'.");
        return;
    }

    const calendar = new FullCalendar.Calendar(calendarEl, {
        schedulerLicenseKey: 'CC-Attribution-NonCommercial-NoDerivatives',
        initialView: 'multiMonth',
        views: {
            multiMonth: {
                type: 'multiMonth',
                duration: { months: 6 },
                multiMonthTitleFormat: { month: 'long', year: 'numeric' },
                multiMonthMaxColumns: 2, // 2 mois côte à côte
                fixedWeekCount: false,
                showNonCurrentDates: false
            }
        },
        selectable: true,
        initialDate: new Date(),
        locale: 'fr',
        dayHeaderContent: function(arg) {
            const day = arg.date.toLocaleDateString('fr-FR', { weekday: 'long' });
            return day.charAt(0).toUpperCase();
        },
        titleFormat: { year: 'numeric', month: 'long' },
        themeSystem: 'bootstrap5',
        headerToolbar: false,
        select: function(info) {
            $('#availabilityModal').modal('show');

            $('#availableButton').off('click').on('click', function() {
                markAvailability(info, true);
                $('#availabilityModal').modal('hide');
            });

            $('#unavailableButton').off('click').on('click', function() {
                markAvailability(info, false);
                $('#availabilityModal').modal('hide');
            });
        }
    });

    calendar.setOption('height', 'auto');
    calendar.render();

    const unavailableRanges = [];

    function markAvailability(info, isAvailable) {
        const className = isAvailable ? 'default' : 'unavailable';

        // Ajuste la date de début
        let startDate = new Date(info.start);
        startDate.setDate(startDate.getDate() + 1);  // Ajouter 1 jour

        let endDate = new Date(info.end);

        if (!isAvailable) {
            unavailableRanges.push({
                start: startDate.toISOString().split('T')[0],
                end: endDate.toISOString().split('T')[0]
            });
            updateHiddenInputs();
        }

        let current = new Date(startDate);
        while (current <= endDate) {
            const dateString = current.toISOString().split('T')[0];
            const dayCell = calendarEl.querySelector(`[data-date="${dateString}"]`);

            if (dayCell) {
                dayCell.classList.remove('default', 'unavailable');
                dayCell.classList.add(className);

                if (!isAvailable) {
                    dayCell.classList.add('unavailable');
                } else {
                    dayCell.style.background = '';
                    dayCell.style.color = '';
                }
            }

            current.setDate(current.getDate() + 1); // Incrémenter la date
        }

        console.log(`Dates du ${startDate.toISOString().split('T')[0]} au ${endDate.toISOString().split('T')[0]} marquées comme ${isAvailable ? 'disponibles' : 'indisponibles'}.`);
    }

    function updateHiddenInputs() {
        const startDates = [];
        const endDates = [];

        unavailableRanges.forEach(range => {
            startDates.push(range.start);
            endDates.push(range.end);
        });

        document.getElementById('indisponibleDatesStart').value = startDates.join(',');
        document.getElementById('indisponibleDatesEnd').value = endDates.join(',');
    }
});
