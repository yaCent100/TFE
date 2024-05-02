document.addEventListener('DOMContentLoaded', function() {
  const calendarEl = document.getElementById('calendar');
  const calendar = new FullCalendar.Calendar(calendarEl, {
    schedulerLicenseKey: 'CC-Attribution-NonCommercial-NoDerivatives',
    initialView: 'multiMonthFourMonth',
    selectable: true,
    selectMirror: true,
    initialDate: new Date(),
    locale: 'fr',
    dayHeaderContent: function(arg) {
      const day = arg.date.toLocaleDateString('fr-FR', { weekday: 'long' });
      const firstLetter = day.charAt(0).toUpperCase();
      return firstLetter;
    },
    titleFormat: { year: 'numeric', month: 'long' },
    views: {
      multiMonthFourMonth: {
        type: 'multiMonth',
        duration: { months: 6 },
        dayMaxEventRows: 2,
        multiMonthTitleFormat: { month: 'long', year: 'numeric' },
        showNonCurrentDates: false
      }
    },
    headerToolbar: false,
    selectOverlap: function(event) {
      return true; // Permettre la sélection même si elle chevauche plusieurs mois
    },
    contentHeight: 'auto'
  });
  calendar.render();
});
