document.addEventListener("DOMContentLoaded", function() {
    // Toggle sidebar
    const hamBurger = document.querySelector(".toggle-btn");
    const sidebar = document.querySelector("#sidebar");

    if (hamBurger && sidebar) {
        hamBurger.addEventListener("click", function () {
            sidebar.classList.toggle("expand");
        });
    }

    // Fetch total revenue
    fetch('/api/revenues/total-revenue')
        .then(response => response.json())
        .then(data => {
            const totalRevenueElement = document.getElementById('total-revenue');
            if (totalRevenueElement) {
                totalRevenueElement.textContent = `$${data}`;
            }
        })
        .catch(error => {
            console.error('Error fetching total revenue:', error);
        });

    // Fetch revenue by month and display in a chart
    fetch('/api/revenues/revenue-by-month')
        .then(response => response.json())
        .then(data => {
            const labels = data.map(item => `Month ${item[0]}, ${item[1]}`);
            const revenueData = data.map(item => item[2]);

            const chartElement = document.getElementById('revenueByMonthChart');
            if (chartElement) {
                const ctx = chartElement.getContext('2d');
                new Chart(ctx, {
                    type: 'bar',
                    data: {
                        labels: labels,
                        datasets: [{
                            label: 'Revenue by Month',
                            data: revenueData,
                            backgroundColor: 'rgba(54, 162, 235, 0.2)',
                            borderColor: 'rgba(54, 162, 235, 1)',
                            borderWidth: 1
                        }]
                    },
                    options: {
                        scales: {
                            y: {
                                beginAtZero: true
                            }
                        }
                    }
                });
            }
        })
        .catch(error => {
            console.error('Error fetching revenue by month:', error);
        });

    // Populate year and month selects and handle chart updates
    const currentMonth = new Date().getMonth() + 1; // JavaScript months are 0-based
    const currentYear = new Date().getFullYear();

    const yearSelect = document.getElementById('year-select');
    const monthSelect = document.getElementById('month-select');

    if (yearSelect && monthSelect) {
        // Populate the year select element
        for (let year = 2020; year <= currentYear; year++) {
            const option = document.createElement('option');
            option.value = year;
            option.text = year;
            yearSelect.appendChild(option);
        }
        yearSelect.value = currentYear;

        // Set the default value for the month select
        monthSelect.value = currentMonth;

        let reservationByLocalityChart; // Store the chart instance

        function fetchDataAndRenderChart(year, month) {
            fetch(`/api/reservations/count-by-locality?year=${year}&month=${month}`)
                .then(response => response.json())
                .then(data => {
                    const labels = data.map(item => item.locality);
                    const countData = data.map(item => item.count);

                    const chartElement = document.getElementById('reservationByLocalityChart');
                    if (chartElement) {
                        const ctx = chartElement.getContext('2d');

                        // Destroy existing chart if it exists
                        if (reservationByLocalityChart) {
                            reservationByLocalityChart.destroy();
                        }

                        reservationByLocalityChart = new Chart(ctx, {
                            type: 'bar',
                            data: {
                                labels: labels,
                                datasets: [{
                                    label: 'Réservations par localité',
                                    data: countData,
                                    backgroundColor: 'rgba(75, 192, 192, 0.2)',
                                    borderColor: 'rgba(75, 192, 192, 1)',
                                    borderWidth: 1
                                }]
                            },
                            options: {
                                scales: {
                                    y: {
                                        beginAtZero: true
                                    }
                                }
                            }
                        });
                    }
                })
                .catch(error => {
                    console.error('Error fetching reservation data:', error);
                });
        }

        fetchDataAndRenderChart(currentYear, currentMonth);

        yearSelect.addEventListener('change', function() {
            fetchDataAndRenderChart(yearSelect.value, monthSelect.value);
        });

        monthSelect.addEventListener('change', function() {
            fetchDataAndRenderChart(yearSelect.value, monthSelect.value);
        });
    }
});



function approveCar(carId) {
    // Assurez-vous que carId est un nombre entier
    const carIdNumber = parseInt(carId, 10);

    // Vérifiez si la conversion est correcte
    if (isNaN(carIdNumber)) {
        console.error('Invalid car ID:', carId);
        alert('Invalid car ID');
        return;
    }

    // Effectuer la requête fetch
    fetch(`/api/admin/car/approve/${carIdNumber}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
    })
    .then(response => {
        if (!response.ok) {
            // Récupérez le texte de la réponse pour afficher des messages d'erreur
            return response.text().then(text => {
                throw new Error(`Network response was not ok. Status: ${response.status}. Response: ${text}`);
            });
        }
        return response.json();
    })
    .then(data => {
        alert('Car approved successfully');
        // Vous pouvez ajouter ici une logique pour mettre à jour l'interface utilisateur, si nécessaire.
        // Par exemple, vous pouvez recharger la liste des voitures ou mettre à jour l'état du bouton.
    })
    .catch(error => {
        console.error('There was a problem with the fetch operation:', error);
        alert('There was an error approving the car. Check the console for details.');
    });
}



//------------------------------- ONGLET RESERVATION -----------------------------
document.addEventListener("DOMContentLoaded", function() {
    loadAllReservations();
});

async function loadAllReservations() {
    try {
        console.log("Chargement des réservations...");

        const response = await fetch('/api/admin/reservations');
        console.log("Réponse reçue:", response);

        const data = await response.json();
        console.log("Données reçues:", data);

        const reservationsByStatus = {
            RESPONSE_PENDING: [],
            PAYMENT_PENDING: [],
            CONFIRMED: [],
            REJECTED: [],
            CANCELLED: [],
            NOW: [],
            PENDING: [],
            FINISHED: []
        };

        data.forEach(reservation => {
            const status = reservation.statut;
            if (!reservationsByStatus[status]) {
                reservationsByStatus[status] = [];
            }
            reservationsByStatus[status].push(reservation);
        });

        console.log("Réservations par statut:", reservationsByStatus);

        // Affichage des réservations par statut
        displayReservationsByStatus(reservationsByStatus);

        // Calcul des pourcentages de statut
        const totalReservations = data.length;
        const statusPercentages = {
            RESPONSE_PENDING: (reservationsByStatus.RESPONSE_PENDING.length / totalReservations * 100).toFixed(2),
            PAYMENT_PENDING: (reservationsByStatus.PAYMENT_PENDING.length / totalReservations * 100).toFixed(2),
            CONFIRMED: (reservationsByStatus.CONFIRMED.length / totalReservations * 100).toFixed(2),
            REJECTED: (reservationsByStatus.REJECTED.length / totalReservations * 100).toFixed(2),
            CANCELLED: (reservationsByStatus.CANCELLED.length / totalReservations * 100).toFixed(2),
            NOW: (reservationsByStatus.NOW.length / totalReservations * 100).toFixed(2),
            PENDING: (reservationsByStatus.PENDING.length / totalReservations * 100).toFixed(2),
            FINISHED: (reservationsByStatus.FINISHED.length / totalReservations * 100).toFixed(2)
        };

        console.log("Pourcentages de statut:", statusPercentages);

        // Affichage des pourcentages de statut
        displayStatusPercentages(statusPercentages, totalReservations);

    } catch (error) {
        console.error("Erreur lors du chargement des réservations:", error);
    }
}

function getStatusTranslation(status) {
    const translations = {
        RESPONSE_PENDING: "En attente de réponse",
        PAYMENT_PENDING: "En attente de paiement",
        CONFIRMED: "Confirmé",
        REJECTED: "Rejeté",
        CANCELLED: "Annulé",
        NOW: "En cours",
        PENDING: "En attente",
        FINISHED: "Terminé"
    };
    return translations[status] || status;
}

function displayReservationsByStatus(reservationsByStatus) {
    const reservationContainer = document.getElementById('reservation-container');
    reservationContainer.innerHTML = '';

    const statuses = Object.keys(reservationsByStatus);

    for (let i = 0; i < statuses.length; i += 2) {
        const row = document.createElement('div');
        row.classList.add('row');

        for (let j = 0; j < 2; j++) {
            if (i + j < statuses.length) {
                const status = statuses[i + j];
                const col = document.createElement('div');
                col.classList.add('col-md-6');
                col.innerHTML = `
                    <div class="status-section">
                        <h3>${getStatusTranslation(status)}</h3>
                        <table class="display" id="${status}-table">
                            <thead>
                                <tr>
                                    <th>Photo</th>
                                    <th>Nom</th>
                                    <th>Voiture</th>
                                    <th>Date de début</th>
                                    <th>Date de fin</th>
                                </tr>
                            </thead>
                            <tbody id="${status}-list"></tbody>
                        </table>
                    </div>
                `;
                row.appendChild(col);

                reservationContainer.appendChild(row);

                const reservationList = document.getElementById(`${status}-list`);
                reservationsByStatus[status].forEach(reservation => {
                    const reservationItem = document.createElement('tr');
                    reservationItem.innerHTML = `
                        <td><img src="${reservation.userProfileImage}" alt="Photo de profil" width="50" height="50" onerror="this.src='/uploads/profil/defaultPhoto.png';"></td>
                        <td>${reservation.userName}</td>
                        <td>${reservation.carBrand} ${reservation.carModel}</td>
                        <td>${reservation.debutLocation}</td>
                        <td>${reservation.finLocation}</td>
                    `;
                    reservationList.appendChild(reservationItem);
                });

                $(`#${status}-table`).DataTable({
                    scrollY: '200px',
                    scrollCollapse: true,
                    paging: false
                });
            }
        }
    }
}

function displayStatusPercentages(statusPercentages, totalReservations) {
    const percentageContainer = document.getElementById('percentage-container');
    percentageContainer.innerHTML = '';

    const previousPercentages = {
        RESPONSE_PENDING: 20,
        PAYMENT_PENDING: 15,
        CONFIRMED: 30,
        REJECTED: 10,
        CANCELLED: 5,
        NOW: 8,
        PENDING: 12,
        FINISHED: 0
    };

    const container = document.createElement('div');
    container.classList.add('status-container');

    Object.keys(statusPercentages).forEach(status => {
        const currentPercentage = parseFloat(statusPercentages[status]);
        const previousPercentage = previousPercentages[status];
        let arrow = '';

        if (currentPercentage > previousPercentage) {
            arrow = '<span class="arrow-up">↑</span>';
        } else if (currentPercentage < previousPercentage) {
            arrow = '<span class="arrow-down">↓</span>';
        }

        const statusItem = document.createElement('div');
        statusItem.classList.add('status-item');
        statusItem.innerHTML = `
            <span>${getStatusTranslation(status)}</span><br>
            <span>${currentPercentage}%</span>
            ${arrow}
        `;
        container.appendChild(statusItem);
    });

    percentageContainer.appendChild(container);
}










