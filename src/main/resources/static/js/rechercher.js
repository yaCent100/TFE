document.addEventListener('DOMContentLoaded', () => {
    console.log("Initializing script...");

    const searchForm = document.getElementById('rechercheForm');
    const nosLocationsTab = document.getElementById('nosLocationsTab');
    const carListElement = document.getElementById('carList');
    const carList2Element = document.getElementById('carList2');
    const spinnerElement = document.getElementById('loading-spinner');

    if (searchForm) {
        console.log("Form found. Adding event listener...");
        searchForm.addEventListener('submit', searchCars);
    } else {
        console.error("Element with id 'rechercheForm' not found.");
    }

    if (nosLocationsTab) {
        console.log("Nos Locations tab found. Adding event listener...");
        nosLocationsTab.addEventListener('click', (event) => {
            event.preventDefault(); // Empêche le comportement par défaut du lien
            window.location.href = '/cars'; // Redirection vers /cars avec un paramètre pour indiquer que carList2 doit être affiché
        });
    } else {
        console.error("Element with id 'nosLocationsTab' not found.");
    }

    // Vérifiez l'URL pour le paramètre showCarList2
    const urlParams = new URLSearchParams(window.location.search);
    const showCarList2 = urlParams.get('showCarList2');

    if (showCarList2 === 'true') {
        // Afficher carList2 et masquer carList seulement si les éléments existent
        if (carListElement && carList2Element) {
            carListElement.style.display = 'none';
            carList2Element.style.display = 'block';
        } else {
            console.error("Elements 'carList' or 'carList2' not found.");
        }

        // Masquer le spinner si nécessaire
        if (spinnerElement) {
            spinnerElement.style.display = 'none';
        }
    }

    async function searchCars(event) {
        event.preventDefault(); // Empêche la soumission du formulaire

        const address = document.getElementById('manualAddress').value;
        const dateDebut = document.getElementById('dateDebut').value;
        const dateFin = document.getElementById('dateFin').value;

        // Stocker dans localStorage
        localStorage.setItem('manualAddress', address);
        localStorage.setItem('dateDebut', dateDebut);
        localStorage.setItem('dateFin', dateFin);

        if (!address || !dateDebut || !dateFin) {
            alert('Veuillez remplir tous les champs.');
            return;
        }

        const geocoder = new google.maps.Geocoder();

        geocoder.geocode({ 'address': address }, async function(results, status) {
            if (status === 'OK') {
                const location = results[0].geometry.location;
                const userLat = location.lat();
                const userLng = location.lng();

                // Redirection vers la page /cars avec les paramètres de recherche
                window.location.href = `/cars?address=${encodeURIComponent(address)}&lat=${userLat}&lng=${userLng}&dateDebut=${dateDebut}&dateFin=${dateFin}`;
            } else {
                console.error('Geocode was not successful for the following reason:', status);
            }
        });

        return false;
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



/* ALLEZ A LA PAGE RESERVATION */

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
});



