

// ALLEZ A LA PAGE RESERVATION





document.addEventListener('DOMContentLoaded', () => {
    console.log("Initializing script...");



    if (reservationForm) {
        // Copier les valeurs de localStorage vers le formulaire de réservation
        const storedDateDebut = localStorage.getItem('dateDebut');
        const storedDateFin = localStorage.getItem('dateFin');

        if (storedDateDebut && storedDateFin) {
            dateDebutHidden.value = storedDateDebut;
            dateFinHidden.value = storedDateFin;
            dateDebutInput.value = storedDateDebut;
            dateFinInput.value = storedDateFin;
        }

        reservationForm.addEventListener('submit', (event) => {
            event.preventDefault(); // Empêche la soumission par défaut du formulaire

            // Copier les dates des champs d'entrée vers les champs cachés
            dateDebutHidden.value = dateDebutInput.value;
            dateFinHidden.value = dateFinInput.value;

            // Construire l'URL de redirection
            const carId = reservationForm.getAttribute('action').split('/').pop();
            const reservationUrl = `/reservation/${carId}?dateDebut=${encodeURIComponent(dateDebutHidden.value)}&dateFin=${encodeURIComponent(dateFinHidden.value)}`;

            // Rediriger vers l'URL de réservation
            window.location.href = reservationUrl;
        });
    }

    // Fonction pour reformater les dates
    const formatDate = (dateString) => {
        const date = new Date(dateString);
        const day = String(date.getDate()).padStart(2, '0');
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const year = date.getFullYear();
        return `${day}-${month}-${year}`;
    };

    // Fonction pour initialiser les dates dans le modal
    const initializeModalDates = () => {
        const storedDateDebut = localStorage.getItem('dateDebut');
        const storedDateFin = localStorage.getItem('dateFin');

        if (storedDateDebut && storedDateFin) {
            modalDateDebut.textContent = formatDate(storedDateDebut);
            modalDateFin.textContent = formatDate(storedDateFin);
        }
    };

    // Appeler cette fonction lorsque le modal est affiché
    if (messageModal) {
        messageModal.addEventListener('show.bs.modal', initializeModalDates);
    }
});





document.addEventListener('DOMContentLoaded', function () {
    const dailyPriceElement = document.getElementById('dailyPrice');
        const dateDebutHidden = document.getElementById('dateDebutHidden');
        const dateFinHidden = document.getElementById('dateFinHidden');
    const totalPriceElement = document.getElementById('totalPrice');
    const reserveButton = document.getElementById('reserveButton');

    // Extraire le prix quotidien depuis l'élément HTML
    const dailyPriceText = dailyPriceElement.textContent;
    const dailyPrice = parseFloat(dailyPriceText.split(' ')[0].replace(',', '.'));

    if (isNaN(dailyPrice)) {
        console.error('Daily price is not a valid number');
        return;
    }

    console.log('Daily Price:', dailyPrice); // Log pour vérifier la valeur du prix quotidien

    function calculateTotalPrice() {
        const dateDebut = new Date(dateDebutInput.value);
        const dateFin = new Date(dateFinInput.value);

        console.log('Date Debut:', dateDebut);
        console.log('Date Fin:', dateFin);

        if (dateDebut && dateFin && dateFin >= dateDebut) {
            const timeDiff = dateFin - dateDebut;
            const daysDiff = Math.ceil(timeDiff / (1000 * 3600 * 24)) + 1; // +1 pour inclure le premier jour
            const totalPrice = daysDiff * dailyPrice;
            totalPriceElement.textContent = `${totalPrice.toFixed(2)}€`;
        } else {
            totalPriceElement.textContent = `${dailyPrice.toFixed(2)}€`; // Afficher seulement le prix par jour
        }

        updateReserveButtonState();
    }

    function updateReserveButtonState() {
        const dateDebut = dateDebutInput.value;
        const dateFin = dateFinInput.value;

        if (dateDebut && dateFin && new Date(dateFin) >= new Date(dateDebut)) {
            reserveButton.disabled = false;
        } else {
            reserveButton.disabled = true;
        }
    }

    // Ajouter un écouteur d'événement sur les champs de date
    dateDebutInput.addEventListener('change', calculateTotalPrice);
    dateFinInput.addEventListener('change', calculateTotalPrice);

    // Vérifier si les dates sont déjà remplies au chargement de la page
    const initialDateDebut = dateDebutInput.value;
    const initialDateFin = dateFinInput.value;
    if (initialDateDebut && initialDateFin) {
        calculateTotalPrice();
    } else {
        totalPriceElement.textContent = `${dailyPrice.toFixed(2)}€`; // Afficher seulement le prix par jour si pas de dates
        reserveButton.disabled = true; // Désactiver le bouton de réservation si les dates ne sont pas valides
    }
});







   document.addEventListener('DOMContentLoaded', function() {
        // Récupérer les éléments contenant les dates
        var dateDebutHidden = document.getElementById('dateDebutHidden').value;
        var dateFinHidden = document.getElementById('dateFinHidden').value;

        // Récupérer les éléments <span> où les dates doivent être affichées
        var modalDateDebut = document.getElementById('modalDateDebut');
        var modalDateFin = document.getElementById('modalDateFin');

        // Mettre à jour le contenu des éléments <span> avec les dates
        modalDateDebut.textContent = dateDebutHidden;
        modalDateFin.textContent = dateFinHidden;


    });






