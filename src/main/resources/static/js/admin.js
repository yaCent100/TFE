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

