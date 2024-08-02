document.addEventListener('DOMContentLoaded', () => {
    console.log("Initializing script...");

    // Fonction de recherche de voitures
    async function searchCars(event) {
        event.preventDefault();

        const address = document.getElementById('manualAddress').value;
        const dateDebut = document.getElementById('dateDebut').value;
        const dateFin = document.getElementById('dateFin').value;

        if (!address || !dateDebut || !dateFin) {
            alert('Veuillez remplir tous les champs.');
            return false;
        }

        const geocoder = new google.maps.Geocoder();
        geocoder.geocode({ 'address': address }, async function(results, status) {
            if (status === 'OK') {
                const location = results[0].geometry.location;
                const userLat = location.lat();
                const userLng = location.lng();

                try {
                    const response = await fetch(`/api/cars/search?address=${encodeURIComponent(address)}&lat=${userLat}&lng=${userLng}&dateDebut=${dateDebut}&dateFin=${dateFin}`);

                    if (!response.ok) {
                        throw new Error('Erreur lors de la récupération des voitures');
                    }

                    const cars = await response.json();
                    console.log('Voitures reçues:', cars);

                    // Stocker les résultats dans localStorage
                    localStorage.setItem('searchResults', JSON.stringify(cars));

                    // Redirection vers la page /cars avec les paramètres de recherche
                    window.location.href = `/cars?address=${encodeURIComponent(address)}&dateDebut=${dateDebut}&dateFin=${dateFin}`;

                } catch (error) {
                    console.error('Erreur lors de la recherche des voitures :', error);
                    alert('Une erreur est survenue lors de la recherche des voitures. Veuillez réessayer.');
                }
            } else {
                console.error('Geocode was not successful for the following reason:', status);
                alert('Une erreur est survenue lors du géocodage de l\'adresse. Veuillez vérifier l\'adresse et réessayer.');
            }
        });

        return false;
    }

    // Ajout de l'écouteur d'événement pour le formulaire
    const searchForm = document.getElementById('rechercheForm');
    if (searchForm) {
        searchForm.addEventListener('submit', searchCars);
    } else {
        console.error("Element with id 'rechercheForm' not found.");
    }
});






document.addEventListener('DOMContentLoaded', function() {
    const adresse = localStorage.getItem('manualAddress');
    const dateDebut = localStorage.getItem('dateDebut');
    const dateFin = localStorage.getItem('dateFin');

    if (adresse) {
        document.getElementById('manualAddress').value = adresse;
    }
    if (dateDebut) {
        document.getElementById('dateDebut').value = dateDebut;
    }
    if (dateFin) {
        document.getElementById('dateFin').value = dateFin;
    }
});



// ALLEZ A LA PAGE RESERVATION

 document.addEventListener('DOMContentLoaded', function() {
        const reservationForm = document.getElementById('reservationForm');
        const dateDebutInput = document.getElementById('dateDebut');
        const dateFinInput = document.getElementById('dateFin');

        // Assurez-vous que les données nécessaires sont disponibles
        const dateDebut = localStorage.getItem('dateDebut') || dateDebutInput.value;
        const dateFin = localStorage.getItem('dateFin') || dateFinInput.value;

        console.log('dateDebut:', dateDebut);
        console.log('dateFin:', dateFin);

        if (dateDebut && dateFin) {
            // Mettez à jour les valeurs des champs de formulaire
            dateDebutInput.value = dateDebut;
            dateFinInput.value = dateFin;
        } else {
            alert('Les dates de début et de fin sont nécessaires pour afficher la réservation.');
        }
    });




document.addEventListener('DOMContentLoaded', () => {
    console.log("Initializing script...");

    const searchForm = document.getElementById('rechercheForm');
    const dateDebutSearch = document.getElementById('dateDebut');
    const dateFinSearch = document.getElementById('dateFin');

    const reservationForm = document.getElementById('reservationForm');
    const dateDebutHidden = document.getElementById('dateDebutHidden');
    const dateFinHidden = document.getElementById('dateFinHidden');
    const dateDebutInput = document.getElementById('dateDebutInput');
    const dateFinInput = document.getElementById('dateFinInput');

    if (searchForm) {
        searchForm.addEventListener('submit', (event) => {
            event.preventDefault(); // Empêche la soumission du formulaire

            const address = document.getElementById('manualAddress').value;
            const dateDebut = dateDebutSearch.value;
            const dateFin = dateFinSearch.value;

            // Stocker dans localStorage
            localStorage.setItem('manualAddress', address);
            localStorage.setItem('dateDebut', dateDebut);
            localStorage.setItem('dateFin', dateFin);

            if (!address || !dateDebut || !dateFin) {
                alert('Veuillez remplir tous les champs.');
                return;
            }

            // Redirection vers la page /cars avec les paramètres de recherche
            window.location.href = `/cars?address=${encodeURIComponent(address)}&dateDebut=${dateDebut}&dateFin=${dateFin}`;
        });
    }

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



