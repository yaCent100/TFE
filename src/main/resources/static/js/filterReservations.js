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

        if (selectedOwnerCarIds.length === 0) {
            resultContainer.innerHTML = `<div class="image-text-container">
                                             <img src="images/clé-reservation.png" class="centered-image" alt="clé">
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
                const updatedReservations = updateReservationStatuses(data);
                displayReservations(updatedReservations, resultContainer, true);
            })
            .catch(error => {
                console.error("Error fetching owner reservations:", error);
                resultContainer.innerHTML = `<p>Erreur lors de la récupération des réservations: ${error.message}</p>`;
            });
    }

    function fetchRenterReservations() {
        const selectedStatuses = Array.from(statusCheckboxes)
            .filter(checkbox => checkbox.checked)
            .map(checkbox => checkbox.value);

        console.log("Selected statuses:", selectedStatuses);

        resultContainer.innerHTML = "";

        if (selectedStatuses.length === 0) {
            resultContainer.innerHTML = `<div class="image-text-container">
                                             <img src="images/clé-reservation.png" class="centered-image" alt="clé">
                                             <p class="centered-text">Vous n'avez pas de réservation en cours.</p>
                                         </div>`;
            return;
        }

        const params = new URLSearchParams();
        selectedStatuses.forEach(status => params.append("statuses", status));

        fetch(`/api/getRenterReservations?${params.toString()}`)
            .then(response => response.json())
            .then(data => {
                console.log("Fetched renter reservations:", data);
                displayReservations(data, resultContainer, false);
            })
            .catch(error => {
                console.error("Error fetching renter reservations:", error);
                resultContainer.innerHTML = `<p>Erreur lors de la récupération des réservations: ${error.message}</p>`;
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
        container.innerHTML = "";
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
                statutCol.classList.add("col-lg-3", "d-flex", "align-items-center", "justify-content-center");

                const cardBody = document.createElement("div");
                cardBody.classList.add("card-body");

                cardBody.innerHTML = `
                    <span class="d-flex justify-content-between align-items-center fw-bold">
                        <span class="col-lg-5 text-start">${formattedDates.formattedDebutDate}</span><span class="col-lg-2 arrow">→</span><span class="col-lg-5 text-end">${formattedDates.formattedFinDate}</span>
                    </span>
                    <span class="card-text text-muted fs-13">
                        Réservation n°${reservation.id} | ${reservation.carBrand} ${reservation.carModel} | ${reservation.codePostal ? reservation.codePostal : ''} ${reservation.locality ? reservation.locality : '' }
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
    }

    function handleReservationAction(reservationId, action) {
        console.log(`Handling reservation action: ${action} for reservation ${reservationId}`);
        fetch(`/reservation/${reservationId}/${action}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
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

            if (['PAYMENT_PENDING', 'CANCELLED', 'REJECTED', 'RESPONSE_PENDING'].includes(reservation.statut)) {
                // Ne change pas le statut si c'est en attente de paiement, annulé, refusé ou en attente de réponse
                return;
            }

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
