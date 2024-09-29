document.addEventListener("DOMContentLoaded", function() {
    const statusFilters = document.getElementById("status-filters");
    const currentLocationsButton = document.getElementById("current-locations-button");
    const historyButton = document.getElementById("history-button");
    const resultContainer = document.getElementById("result-container");

    const ownerCarCheckboxes = document.querySelectorAll("#cars-filters .form-check-input");
    const statusCheckboxes = document.querySelectorAll("#status-filters .form-check-input");


    function fetchOwnerReservations() {
        const selectedOwnerCarIds = Array.from(ownerCarCheckboxes)
            .filter(checkbox => checkbox.checked)
            .map(checkbox => checkbox.value);

        console.log("Selected owner car IDs:", selectedOwnerCarIds);

        resultContainer.innerHTML = "";

            document.getElementById('spinner-container').style.display = 'block';

        if (selectedOwnerCarIds.length === 0) {
            resultContainer.innerHTML = `<div class="image-text-container">
                                          <img src="/images/clé-reservation.png" height="200" class="cle-reservation-img" alt="clé">
                                             <p class="centered-text">Vous n'avez pas de réservation en cours.</p>
                                         </div>`;
            return;
        }

        const params = new URLSearchParams();
        selectedOwnerCarIds.forEach(id => params.append("carIds", id));

        fetch(`/api/getOwnerReservations?${params.toString()}`)
            .then(response => response.json())
            .then(data => {
                console.log("Fetched owner reservations:", data);

                document.getElementById('spinner-container').style.display = 'none';

                const updatedReservations = updateReservationStatuses(data);
                displayReservations(updatedReservations, resultContainer, true);
            })
            .catch(error => {
                console.error("Error fetching owner reservations:", error);

                document.getElementById('spinner-container').style.display = 'none';

                resultContainer.innerHTML = `<p>Erreur lors de la récupération des réservations: ${error.message}</p>`;
            });
    }

function fetchRenterReservations() {
    const selectedStatuses = Array.from(statusCheckboxes)
        .filter(checkbox => checkbox.checked)
        .map(checkbox => checkbox.value);

    console.log("Selected statuses:", selectedStatuses);

    resultContainer.innerHTML = "";

    // Afficher le spinner
    const spinner = document.getElementById('loading-spinner');
    if (spinner) {
        spinner.style.display = 'block';
    }

    if (selectedStatuses.length === 0) {
        if (spinner) {
            spinner.style.display = 'none';
        }
        resultContainer.innerHTML = `<div class="image-text-container">
                                      <img src="/images/clé-reservation.png" height="200" class="cle-reservation-img" alt="clé">
                                         <p class="centered-text">Vous n'avez pas de réservation en cours.</p>
                                     </div>`;
        return;
    }

    const params = new URLSearchParams();
    selectedStatuses.forEach(status => params.append("statuses", status));

    fetch(`/api/getRenterReservations?${params.toString()}`)
        .then(response => {
            if (spinner) {
                spinner.style.display = 'none'; // Masquer le spinner
            }

            if (!response.ok) {
                if (response.status === 204) {
                    // Si la réponse est 204 No Content, afficher l'image avec le texte "Aucune réservation trouvée"
                    console.log('Aucune réservation disponible.');
                    resultContainer.innerHTML = `<div class="image-text-container">
                                                       <img src="/images/clé-reservation.png" height="200" class="cle-reservation-img" alt="clé">
                                                     <p class="centered-text">Vous n'avez pas de réservation en cours.</p>
                                                 </div>`;
                    return null; // Retourner null pour arrêter le traitement
                } else {
                    throw new Error('Erreur lors de la récupération des réservations');
                }
            }
            return response.text(); // Utiliser `.text()` au lieu de `.json()` pour éviter l'erreur sur les réponses vides
        })
        .then(text => {
            if (!text) {
                // Si le texte est vide, afficher l'image et le texte "Aucune réservation en cours"
                resultContainer.innerHTML = `<div class="image-text-container">
                                                       <img src="/images/clé-reservation.png" height="200" class="cle-reservation-img" alt="clé">
                                                 <p class="centered-text">Vous n'avez pas de réservation en cours.</p>
                                             </div>`;
                return;
            }

            // Si on a du texte valide, le parser en JSON
            const data = JSON.parse(text);
            console.log("Fetched renter reservations:", data);
            displayReservations(data, resultContainer, false);
        })
        .catch(error => {
            console.error("Error fetching renter reservations:", error);
            resultContainer.innerHTML = `<p>Erreur lors de la récupération des réservations: ${error.message}</p>`;
            if (spinner) {
                spinner.style.display = 'none'; // S'assurer que le spinner est masqué en cas d'erreur
            }
        });
}




    function formatDate(debutLocation, finLocation) {
        return fetch(`/api/format-date?debut=${debutLocation}&fin=${finLocation}`)
            .then(response => response.json())
            .catch(error => {
                console.error("Error formatting dates:", error);
                return {
                    formattedDebutDate: debutLocation, // Fallback in case of error
                    formattedFinDate: finLocation // Fallback in case of error
                };
            });
    }

function displayReservations(reservations, container, isOwner) {
    const spinner = document.getElementById("spinner-container");

    // Afficher le spinner pendant que les données sont chargées
    spinner.style.display = "block";
    container.innerHTML = "";

    console.log("Réservations avant le tri :", reservations);

    // Trier les réservations par date de création (createdAt) de la plus récente à la plus ancienne
    reservations.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

    // Vérification des données après le tri
    console.log("Réservations après le tri :", reservations);

    reservations.forEach(reservation => {
        formatDate(reservation.debutLocation, reservation.finLocation).then(formattedDates => {
            const card = document.createElement("div");
            card.classList.add("card", "card-reservation-profil");
            card.style.cursor = "pointer";
            card.addEventListener("click", () => {
                window.location.href = `/account/car/${reservation.carId}/reservations/${reservation.id}`;
            });

            const row = document.createElement("div");
            row.classList.add("row", "g-0");

            const imgCol = document.createElement("div");
            imgCol.classList.add("col-lg-3");
            const img = document.createElement("img");
            img.src = isOwner ? reservation.userProfileImage || 'default-profile.png' : reservation.carImage || 'default-car.png';
            img.classList.add("img-fluid", isOwner ? "rounded-start" : "rounded-end");
            img.alt = isOwner ? `${reservation.userName}` : `${reservation.carBrand} ${reservation.carModel}`;
            imgCol.appendChild(img);

            const contentCol = document.createElement("div");
            contentCol.classList.add("col-lg-6");

            const statutCol = document.createElement("div");
            statutCol.classList.add("col-lg-3", "text-end");

            const cardBody = document.createElement("div");
            cardBody.classList.add("card-body");

            // Ajouter une badge avec tooltip si le propriétaire est vérifié
            if (isOwner && reservation.isUserVerified) { // Supposons que isUserVerified est une propriété de reservation
                const badge = document.createElement("span");
                badge.classList.add("me-2","position-absolute");
                badge.style.top = "20px";  // Position verticale
                badge.style.left = "20px"; // Position horizontale
                badge.setAttribute("data-bs-toggle", "tooltip");
                badge.setAttribute("data-bs-placement", "top");
                badge.setAttribute("title", "Utilisateur vérifié");
                badge.innerHTML = `<img src="/images/userVerified.png" class="rounded-circle" alt="Verified" style="width: 50px !important; height: 50px; !important">` // Icône de vérification
                cardBody.appendChild(badge);
            }

            cardBody.innerHTML += `
                <span class="d-flex justify-content-between align-items-center fw-bold">
                    <span class="col-lg-5 text-start">${formattedDates.formattedDebutDate}</span><span class="col-lg-2 arrow">→</span><span class="col-lg-5 text-end">${formattedDates.formattedFinDate}</span>
                </span>
                <span class="card-text text-muted fs-13">
                    Réservation n°${reservation.id} | ${reservation.carBrand} ${reservation.carModel} | ${reservation.codePostal ? reservation.codePostal : ''} ${reservation.locality ? reservation.locality : ''}
                </span>
                <span class="card-title fs-13">${isOwner ? reservation.userName : reservation.carPostal + ' ' + reservation.carLocality}</span>`;

            const statutText = document.createElement("span");
            statutText.classList.add("card-text", "status-text");

            if (isOwner && reservation.statut === 'RESPONSE_PENDING' && reservation.modeReservation === 'manuelle') {
                const acceptButton = document.createElement("button");
                acceptButton.classList.add("btn", "btn-success", "me-2");
                acceptButton.innerText = "Accepter";
                acceptButton.addEventListener("click", (e) => {
                    e.stopPropagation();
                    handleReservationAction(reservation.id, 'accept');
                });

                const rejectButton = document.createElement("button");
                rejectButton.classList.add("btn", "btn-danger");
                rejectButton.innerText = "Refuser";
                rejectButton.addEventListener("click", (e) => {
                    e.stopPropagation();
                    handleReservationAction(reservation.id, 'reject');
                });

                statutCol.appendChild(acceptButton);
                statutCol.appendChild(rejectButton);

                // Utiliser createdAt pour le compte à rebours de 24h pour le propriétaire
                const requestTime = new Date(reservation.createdAt); // Utiliser createdAt
                if (isNaN(requestTime.getTime())) {
                    console.error("Invalid createdAt:", reservation.createdAt);
                } else {
                    const expiryTime = new Date(requestTime.getTime() + 24 * 60 * 60 * 1000); // 24 heures après la création de la réservation
                    const countdownElement = document.createElement("div");
                    countdownElement.classList.add("countdown-timer", "text-danger", "mt-2");

                    function updateCountdown() {
                        const now = new Date();
                        const timeRemaining = expiryTime - now;

                        if (timeRemaining <= 0) {
                            countdownElement.innerText = "La demande a expiré.";
                            clearInterval(intervalId); // Arrêter le compte à rebours
                            handleExpiredReservation(reservation.id); // Appeler la fonction pour gérer l'expiration
                        } else {
                            const hours = Math.floor(timeRemaining / (1000 * 60 * 60));
                            const minutes = Math.floor((timeRemaining % (1000 * 60 * 60)) / (1000 * 60));
                            const seconds = Math.floor((timeRemaining % (1000 * 60)) / 1000);

                            countdownElement.innerText = `${hours}h ${minutes}m ${seconds}s restant`;
                        }
                    }

                    // Mettre à jour le compte à rebours toutes les secondes
                    const intervalId = setInterval(updateCountdown, 1000);
                    updateCountdown(); // Mise à jour initiale

                    statutCol.appendChild(countdownElement); // Ajouter le compte à rebours sous les boutons
                }
            } else if (reservation.statut === 'PAYMENT_PENDING') {
                // Conteneur pour le statut "En attente de paiement" avec style
                const paymentStatusElement = document.createElement("div");
                paymentStatusElement.innerText = "En attente de paiement";
                paymentStatusElement.classList.add("payment-status", "text-white", "text-center", "mb-2");

                // Ajouter le style pour le fond rouge, bord arrondi et espacement
                paymentStatusElement.style.backgroundColor = "#dc3545"; // Rouge
                paymentStatusElement.style.padding = "10px";
                paymentStatusElement.style.borderRadius = "10px";

                // Ajout du texte de statut dans la colonne de statut
                statutCol.appendChild(paymentStatusElement);

                // Utiliser createdAt pour le compte à rebours de 24h pour le paiement
                const paymentRequestTime = new Date(reservation.createdAt); // Utiliser createdAt ici aussi
                if (isNaN(paymentRequestTime.getTime())) {
                    console.error("Invalid createdAt:", reservation.createdAt);
                } else {
                    const paymentExpiryTime = new Date(paymentRequestTime.getTime() + 24 * 60 * 60 * 1000); // 24 heures après la création de la réservation
                    const paymentCountdownElement = document.createElement("div");
                    paymentCountdownElement.classList.add("countdown-timer", "text-danger", "mt-2");

                    function updatePaymentCountdown() {
                        const now = new Date();
                        const timeRemaining = paymentExpiryTime - now;

                        if (timeRemaining <= 0) {
                            paymentCountdownElement.innerText = "Le délai de paiement a expiré.";
                            clearInterval(paymentIntervalId);
                            handleExpiredReservation(reservation.id); // Gérer l'expiration
                        } else {
                            const hours = Math.floor(timeRemaining / (1000 * 60 * 60));
                            const minutes = Math.floor((timeRemaining % (1000 * 60 * 60)) / (1000 * 60));
                            const seconds = Math.floor((timeRemaining % (1000 * 60)) / 1000);

                            paymentCountdownElement.innerText = `${hours}h ${minutes}m ${seconds}s restant pour effectuer le paiement`;
                        }
                    }

                    // Mettre à jour le compte à rebours toutes les secondes
                    const paymentIntervalId = setInterval(updatePaymentCountdown, 1000);
                    updatePaymentCountdown(); // Mise à jour initiale

                    // Ajouter le compte à rebours sous le statut
                    statutCol.appendChild(paymentCountdownElement);
                }
            } else if (reservation.statut === 'REJECTED') {
                // Afficher le statut de demande refusée
                statutText.innerText = "Demande refusée";
                statutText.classList.add("text-danger");
                statutCol.appendChild(statutText);
            } else if (reservation.statut === 'EXPIRED') {
                // Afficher le statut de demande expirée
                statutText.innerText = "Demande expirée";
                statutText.classList.add("text-warning");
                statutCol.appendChild(statutText);
            } else {
                statutText.innerHTML = getReservationStatusText(reservation);
                statutText.classList.add(getReservationStatusClass(reservation.statut));
                statutCol.appendChild(statutText);
            }

            contentCol.appendChild(cardBody);
            row.appendChild(imgCol);
            row.appendChild(contentCol);
            row.appendChild(statutCol);
            card.appendChild(row);
            container.appendChild(card);
        });
    });

    // Masquer le spinner une fois les données chargées
    spinner.style.display = "none";

    // Initialiser les tooltips
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    const tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
}






function handleReservationAction(reservationId, action) {
    console.log(`Handling reservation action: ${action} for reservation ${reservationId}`);

    fetch(`/reservation/${reservationId}/action?action=${action}`, {
        method: 'POST',
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json(); // Assurez-vous que la réponse est bien au format JSON
    })
    .then(data => {
        console.log(`Reservation ${action} response:`, data);
        if (data.status === "ok") {
            alert(`Réservation ${action === 'accept' ? 'acceptée' : 'refusée'} avec succès.`);
            fetchOwnerReservations();
        } else {
            alert(`Erreur lors de la ${action === 'accept' ? 'acceptation' : 'refus'} de la réservation: ${data.message}`);
        }
    })
    .catch(error => {
        console.error(`Error handling reservation action ${action} for reservation ${reservationId}:`, error);
        alert(`Erreur lors de la ${action === 'accept' ? 'acceptation' : 'refus'} de la réservation.`);
    });
}

function handleExpiredReservation(reservationId) {
    fetch(`/api/reservation/${reservationId}/expire`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Erreur lors de la mise à jour de la réservation expirée.');
        }
        return response.json();
    })
    .then(data => {
        alert('La réservation a expiré en raison d\'un paiement manquant.');
        // Mettre à jour l'interface utilisateur pour refléter le changement
        fetchOwnerReservations(); // Actualiser les réservations
    })
    .catch(error => {
        console.error('Erreur lors de la mise à jour du statut expiré:', error);
    });
}



    function getReservationStatusText(reservation) {
        switch (reservation.statut) {
            case 'CONFIRMED':
                return 'Location confirmée';
            case 'NOW':
                return 'Location en cours';
            case 'FINISHED':
                return 'Location terminée';
            case 'PAYMENT_PENDING':
                return 'En attente de paiement';
            case 'CANCELLED':
                return 'Annulée';
            case 'RESPONSE_PENDING':
                return 'En attente de réponse';
            case 'REJECTED':
                return 'Demande refusée';
            case 'EXPIRED':
                return 'Demande expirée';
            default:
                return 'Statut inconnu';
        }
    }

    function getReservationStatusClass(status) {
        switch (status) {
            case 'CONFIRMED':
                return 'confirmed';
            case 'NOW':
                return 'now';
            case 'FINISHED':
                return 'finished';
            case 'PAYMENT_PENDING':
                return 'payment-pending';
            case 'CANCELLED':
                return 'cancelled';
            case 'RESPONSE_PENDING':
                return 'response-pending';
            case 'REJECTED':
                return 'rejected';
            case 'EXPIRED':
                return 'expired';
            default:
                return 'unknown';
        }
    }

    function setCurrentLabels() {
        document.getElementById('filter-reponse-attente').nextElementSibling.textContent = 'En attente de réponse';
        document.getElementById('filter-attente-paiement').nextElementSibling.textContent = 'En attente de paiement';
        document.getElementById('filter-confirmed').nextElementSibling.textContent = 'Locations à venir (confirmées)';
        document.getElementById('filter-now').nextElementSibling.textContent = 'Locations en cours';
        document.getElementById('filter-finished').parentElement.style.display = 'none';
        document.getElementById('filter-cancelled').parentElement.style.display = 'none';
        document.getElementById('filter-completed').parentElement.style.display = 'none';
        document.getElementById('filter-pending').parentElement.style.display = 'none';
        document.getElementById('filter-reponse-attente').parentElement.style.display = 'block';
        document.getElementById('filter-attente-paiement').parentElement.style.display = 'block';
        document.getElementById('filter-confirmed').parentElement.style.display = 'block';
        document.getElementById('filter-now').parentElement.style.display = 'block';
    }

    function setHistoryLabels() {
        document.getElementById('filter-finished').nextElementSibling.textContent = 'Location terminées';
        document.getElementById('filter-cancelled').nextElementSibling.textContent = 'Location annulées';
        document.getElementById('filter-completed').nextElementSibling.textContent = 'Demande refusées';
        document.getElementById('filter-pending').nextElementSibling.textContent = 'Demande expirées';
        document.getElementById('filter-reponse-attente').parentElement.style.display = 'none';
        document.getElementById('filter-attente-paiement').parentElement.style.display = 'none';
        document.getElementById('filter-confirmed').parentElement.style.display = 'none';
        document.getElementById('filter-now').parentElement.style.display = 'none';
        document.getElementById('filter-finished').parentElement.style.display = 'block';
        document.getElementById('filter-cancelled').parentElement.style.display = 'block';
        document.getElementById('filter-completed').parentElement.style.display = 'block';
        document.getElementById('filter-pending').parentElement.style.display = 'block';
    }

    currentLocationsButton.addEventListener('click', function() {
        setCurrentLabels();
        clearCheckboxes(statusCheckboxes);
        resultContainer.innerHTML = ""; // Clear the result container
        currentLocationsButton.style.display = 'none';
        historyButton.style.display = 'inline-block';
        fetchOwnerReservations();
    });

    historyButton.addEventListener('click', function() {
        setHistoryLabels();
        clearCheckboxes(statusCheckboxes);
        resultContainer.innerHTML = ""; // Clear the result container
        historyButton.style.display = 'none';
        currentLocationsButton.style.display = 'inline-block';
        fetchRenterReservations();
    });

function updateReservationStatuses(reservations) {
    const now = new Date();

    reservations.forEach(reservation => {
        const debutLocation = new Date(reservation.debutLocation);
        const finLocation = new Date(reservation.finLocation);
        const createdAt = new Date(reservation.createdAt); // Date de création de la réservation

        // Si le statut est déjà PAYMENT_PENDING, CANCELLED, REJECTED, RESPONSE_PENDING, EXPIRED, on ne change pas
        if (['PAYMENT_PENDING', 'CANCELLED', 'REJECTED', 'RESPONSE_PENDING', 'EXPIRED'].includes(reservation.statut)) {
            return;
        }

        // Vérifier si la demande est expirée (ex : en attente depuis plus de 24 heures)
        const timeElapsedSinceCreation = now - createdAt;
        const expirationTime = 24 * 60 * 60 * 1000; // 24 heures en millisecondes
        if (reservation.statut === 'RESPONSE_PENDING' && timeElapsedSinceCreation > expirationTime) {
            reservation.statut = 'EXPIRED'; // Demande expirée après 24 heures sans réponse
            return;
        }

        // Vérifier les dates pour savoir si la location est confirmée, en cours ou terminée
        if (now < debutLocation) {
            reservation.statut = 'CONFIRMED';  // Locations à venir (confirmées)
        } else if (now >= debutLocation && now <= finLocation) {
            reservation.statut = 'NOW';  // Locations en cours
        } else if (now > finLocation) {
            reservation.statut = 'FINISHED';  // Locations terminées
        }
    });

    return reservations;
}



    function clearCheckboxes(checkboxes) {
        checkboxes.forEach(checkbox => {
            checkbox.checked = false;
        });
    }

    ownerCarCheckboxes.forEach(checkbox => checkbox.addEventListener("change", fetchOwnerReservations));
    statusCheckboxes.forEach(checkbox => checkbox.addEventListener("change", fetchRenterReservations));
    currentLocationsButton.addEventListener("click", fetchOwnerReservations);
    historyButton.addEventListener("click", fetchRenterReservations);

    fetchOwnerReservations(); // Fetch initial reservations
});
