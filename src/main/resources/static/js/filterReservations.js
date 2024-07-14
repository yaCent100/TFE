document.addEventListener("DOMContentLoaded", function() {
    const ownerCarCheckboxes = document.querySelectorAll("#cars-filters .form-check-input");
    const statusCheckboxes = document.querySelectorAll("#status-filters .form-check-input");
    const resultContainer = document.getElementById("result-container");

    function fetchOwnerReservations() {
        const selectedOwnerCarIds = Array.from(ownerCarCheckboxes)
            .filter(checkbox => checkbox.checked)
            .map(checkbox => checkbox.value);

        // Clear the result container
        resultContainer.innerHTML = "";

        if (selectedOwnerCarIds.length === 0) {
            resultContainer.innerHTML = "<p>Aucun filtre sélectionné.</p>";
            return;
        }

        const params = new URLSearchParams();
        selectedOwnerCarIds.forEach(id => params.append("carIds", id));

        fetch(`/api/getOwnerReservations?${params.toString()}`)
            .then(response => response.json())
            .then(data => {
                displayReservations(data, resultContainer, true);
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

        // Clear the result container
        resultContainer.innerHTML = "";

        if (selectedStatuses.length === 0) {
            resultContainer.innerHTML = "<p>Aucun filtre sélectionné.</p>";
            return;
        }

        const params = new URLSearchParams();
        selectedStatuses.forEach(status => params.append("statuses", status));

        fetch(`/api/getRenterReservations?${params.toString()}`)
            .then(response => response.json())
            .then(data => {
                displayReservations(data, resultContainer, false);
            })
            .catch(error => {
                console.error("Error fetching renter reservations:", error);
                resultContainer.innerHTML = `<p>Erreur lors de la récupération des réservations: ${error.message}</p>`;
            });
    }

    function displayReservations(reservations, container, isOwner) {
        container.innerHTML = "";
        reservations.forEach(reservation => {
            const card = document.createElement("div");
            card.classList.add("card", "mb-3");
            card.style.cursor = "pointer";
            card.addEventListener("click", () => {
                window.location.href = `/account/car/${reservation.carId}/reservations/${reservation.id}`;
            });

            const row = document.createElement("div");
            row.classList.add("row", "g-0");

            const imgCol = document.createElement("div");
            imgCol.classList.add("col-md-2");
            const img = document.createElement("img");
            img.src = isOwner ? reservation.userProfileImage || 'default-profile.png' : reservation.carImage || 'default-car.png';
            img.classList.add("img-fluid", isOwner ? "rounded-start" : "rounded-end");
            img.alt = isOwner ? `${reservation.userName}` : `${reservation.carBrand} ${reservation.carModel}`;
            imgCol.appendChild(img);

            const contentCol = document.createElement("div");
            contentCol.classList.add("col-md-8");

            const cardBody = document.createElement("div");
            cardBody.classList.add("card-body");

            cardBody.innerHTML = `
                <h5 class="card-title">${isOwner ? reservation.userName : reservation.carBrand + ' ' + reservation.carModel}</h5>
                <p class="card-text"><strong>Début:</strong> ${reservation.debutLocation} <span>→</span> <strong>Fin:</strong> ${reservation.finLocation}</p>
                <p class="card-text"><strong>Statut:</strong> ${reservation.statut}</p>
                <p class="card-text"><small class="text-muted">Réservation n°${reservation.id}</small></p>
            `;

            contentCol.appendChild(cardBody);
            row.appendChild(imgCol);
            row.appendChild(contentCol);
            card.appendChild(row);
            container.appendChild(card);
        });
    }

    ownerCarCheckboxes.forEach(checkbox => {
        checkbox.addEventListener("change", fetchOwnerReservations);
    });

    statusCheckboxes.forEach(checkbox => {
        checkbox.addEventListener("change", fetchRenterReservations);
    });
});
