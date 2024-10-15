//------------------ADMIN SECTION DASHBOARD---------------------
document.addEventListener("DOMContentLoaded", function() {
    // Fonction pour charger les KPI
    function loadKpiData() {
        fetch('/api/dashboard/kpi')
            .then(response => response.json())
            .then(data => {
                // Mettre à jour les éléments de la page avec les données récupérées
                document.getElementById('total-revenue').textContent = data.totalRevenue.toFixed(2) + ' €';
                document.getElementById('total-benefit').textContent = data.totalBenefit.toFixed(2) + ' €';
                document.getElementById('total-users').textContent = data.totalUsers;
                document.getElementById('total-reservations').textContent = data.totalReservations;
            })
            .catch(error => {
                console.error('Erreur lors de la récupération des KPI:', error);
            });
    }

    // Charger les KPI au chargement de la page
    loadKpiData();

    // Rafraîchir les KPI toutes les 30 secondes
    setInterval(loadKpiData, 30000); // 30 000 millisecondes = 30 secondes

    // Fonction pour afficher et masquer les spinners
    function showSpinner(spinnerId) {
        const spinner = document.getElementById(spinnerId);
        if (spinner) {
            spinner.style.display = 'table-row';
        }
    }

    function hideSpinner(spinnerId) {
        const spinner = document.getElementById(spinnerId);
        if (spinner) {
            spinner.style.display = 'none';
        }
    }

    // Charger les données pour les voitures
    showSpinner('car-spinner'); // Affiche le spinner
    fetch('/api/dashboard/top10-cars')
        .then(response => response.json())
        .then(data => {
            hideSpinner('car-spinner'); // Cache le spinner
            renderTop10CarsTable(data);
        })
        .catch(error => {
            console.error('Error fetching car data:', error);
            hideSpinner('car-spinner'); // Cache le spinner même en cas d'erreur
        });

    // Charger les données pour les utilisateurs
    showSpinner('user-spinner'); // Affiche le spinner
    fetch('/api/dashboard/top10-users')
        .then(response => response.json())
        .then(data => {
            hideSpinner('user-spinner'); // Cache le spinner
            renderTop10UsersTable(data);
        })
        .catch(error => {
            console.error('Error fetching user data:', error);
            hideSpinner('user-spinner'); // Cache le spinner même en cas d'erreur
        });

    // Tableau des mois en français
    const monthNames = [
        'janvier', 'février', 'mars', 'avril', 'mai', 'juin', 'juillet', 'août', 'septembre', 'octobre', 'novembre', 'décembre'
    ];

    // Fetch revenue by month and display in a line chart
    fetch('/api/revenues/revenue-by-month')
        .then(response => response.json())
        .then(data => {
            // Utilisation du tableau des mois pour générer les labels
            const labels = data.map(item => `${monthNames[item[0] - 1]} ${item[1]}`); // item[0] est l'index du mois, item[1] est l'année
            const revenueData = data.map(item => item[2]);

            const chartElement = document.getElementById('revenueByMonthChart');
            if (chartElement) {
                const ctx = chartElement.getContext('2d');
                new Chart(ctx, {
                    type: 'line',
                    data: {
                        labels: labels,
                        datasets: [{
                            label: 'Revenue by Month',
                            data: revenueData,
                            fill: false, // No fill under the line
                            borderColor: 'rgba(75, 192, 192, 1)', // Line color
                            backgroundColor: 'rgba(75, 192, 192, 0.2)', // Point background color
                            tension: 0.1 // Smoothness of the line
                        }]
                    },
                    options: {
                        responsive: true,
                        scales: {
                            y: {
                                beginAtZero: true
                            }
                        },
                        plugins: {
                            title: {
                                display: true,
                                text: 'Revenues par mois', // Titre du graphique
                                font: {
                                    size: 18 // Taille de la police du titre
                                }
                            }
                        }
                    }
                });
            }
        })
        .catch(error => {
            console.error('Error fetching revenue by month:', error);
        });


    // Gestion de la sélection de l'année et du mois pour les réservations
    const currentMonth = new Date().getMonth(); // JavaScript months are 0-based
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

    // Gestion de la sidebar
    const hamBurger = document.querySelector(".toggle-btn");
    const sidebar = document.querySelector("#sidebar");

    if (hamBurger && sidebar) {
        hamBurger.addEventListener("click", function () {
            sidebar.classList.toggle("expand");
        });
    }
});

function renderTop10CarsTable(cars) {
    const tableBody = document.getElementById('top10cars-table-body');
    tableBody.innerHTML = ''; // Efface le contenu précédent

    cars.forEach(car => {
        const row = document.createElement('tr');

        // Photo, Brand and Model
        const photoCell = document.createElement('td');
        const photoContainer = document.createElement('div');
        const photo = document.createElement('img');
        const carDetails = document.createElement('div');

        // Vérifiez que les champs existent avant de les assigner
        if (car.photoUrl) {
            photo.src = '/uploads/photo-car/' + car.photoUrl;  // URL of the first photo
        } else {
            photo.src = 'default-image-path'; // Placeholder image if photoUrl is missing
        }
        photo.alt = car.carModel || 'Unknown Model'; // Default text if carModel is undefined
        photo.classList.add('car-photo');

        carDetails.innerHTML = `<strong>${car.carBrand || 'Unknown Brand'}</strong><br>${car.carModel || 'Unknown Model'}`;

        photoContainer.appendChild(photo);
        photoContainer.appendChild(carDetails);
        photoCell.appendChild(photoContainer);
        row.appendChild(photoCell);

        // Reservation Count
        const reservationCountCell = document.createElement('td');
        reservationCountCell.textContent = car.reservationCount || 0;
        row.appendChild(reservationCountCell);

        // Total Revenue
        const totalRevenueCell = document.createElement('td');
        totalRevenueCell.textContent = car.totalRevenue ? `${car.totalRevenue.toFixed(2)} €` : '0 €'; // Total generated revenue
        row.appendChild(totalRevenueCell);

        // Trend (Up/Down Arrow)
        const trendCell = document.createElement('td');
        const trendIcon = document.createElement('i');
        if (car.trend > 0) {
            trendIcon.classList.add('fas', 'fa-arrow-up', 'text-success');
        } else {
            trendIcon.classList.add('fas', 'fa-arrow-down', 'text-danger');
        }
        trendCell.appendChild(trendIcon);
        row.appendChild(trendCell);

        tableBody.appendChild(row);
    });
}

function renderTop10UsersTable(users) {
    const tableBody = document.getElementById('top10users-table-body');
    tableBody.innerHTML = ''; // Efface le contenu précédent

    users.forEach(user => {
        const row = document.createElement('tr');

        // Photo
        const photoCell = document.createElement('td');
        const photo = document.createElement('img');
        photo.src = user.photoUrl ? '/uploads/profil/' + user.photoUrl : '/uploads/profil/default-photo.png';
        photo.alt = `${user.firstName} ${user.lastName}`;
        photo.classList.add('user-photo');
        photoCell.appendChild(photo);
        row.appendChild(photoCell);

        // Name
        const nameCell = document.createElement('td');
        nameCell.textContent = `${user.firstName} ${user.lastName}`;
        row.appendChild(nameCell);

        // Reservations Count
        const reservationCountCell = document.createElement('td');
        reservationCountCell.textContent = user.reservationCount;
        row.appendChild(reservationCountCell);

        // Trend (Up/Down Arrow)
        const trendCell = document.createElement('td');
        const trendIcon = document.createElement('i');
        if (user.trend > 0) {
            trendIcon.classList.add('fas', 'fa-arrow-up', 'text-success', 'trend-icon');
        } else {
            trendIcon.classList.add('fas', 'fa-arrow-down', 'text-danger', 'trend-icon');
        }
        trendCell.appendChild(trendIcon);
        row.appendChild(trendCell);

        tableBody.appendChild(row);
    });
}




//------------------------------- ONGLET RESERVATION -----------------------------
document.addEventListener("DOMContentLoaded", function() {
    loadAllReservations();

    // Filtrer par statut
    document.getElementById('filter-status').addEventListener('change', function() {
        const selectedStatus = this.value;
        const table = $('#all-reservations-table').DataTable();
        if (selectedStatus) {
            table.column(5).search('^' + selectedStatus + '$', true, false).draw();
        } else {
            table.column(5).search('').draw();
        }
    });

    // Sélectionner/Désélectionner toutes les réservations
    document.getElementById('select-all').addEventListener('click', function() {
        const rows = $('#all-reservations-table').DataTable().rows({ 'search': 'applied' }).nodes();
        document.querySelectorAll('input[type="checkbox"]', rows).forEach(checkbox => {
            checkbox.checked = this.checked;
        });
    });

    // Suppression des réservations sélectionnées
    document.getElementById('delete-selected').addEventListener('click', function() {
        const table = $('#all-reservations-table').DataTable();
        const selectedReservations = [];

        table.$('input[type="checkbox"]:checked').each(function() {
            selectedReservations.push(this.id);
        });

        if (selectedReservations.length > 0) {
            if (confirm("Êtes-vous sûr de vouloir supprimer les réservations sélectionnées ?")) {
                // Simuler la suppression locale (vous pouvez remplacer cela par une requête réelle côté serveur)
                selectedReservations.forEach(id => {
                    table.row(`#${id}`).remove().draw();
                });
            }
        } else {
            alert("Aucune réservation sélectionnée.");
        }
    });
});

// Chargement des données de réservation
async function loadAllReservations() {
    try {
        const response = await fetch('/api/admin/reservations');
        const data = await response.json();

        const table = $('#all-reservations-table').DataTable({
            data: data,
            columns: [
                { data: null, render: function(data) { return `<input type="checkbox" id="${data.id}">`; } },
                { data: 'userName' },
                { data: null, render: function(data) { return `${data.carBrand} ${data.carModel}`; } },
                { data: 'debutLocation' },
                { data: 'finLocation' },
                { data: 'statut' },
                { data: null, render: function(data) {
                    return `<button class="btn btn-sm btn-outline-danger" onclick="deleteReservation(${data.id})"><i class="lni lni-trash-can"></i></button>`;
                }}
            ],
            scrollY: '400px',
            scrollCollapse: true,
            paging: false,
            searching: true
        });

        // Mettre à jour les KPI
        updateKPIReservations(data);

        // Afficher les graphiques
        displayCharts(data);

    } catch (error) {
        console.error("Erreur lors du chargement des réservations:", error);
    }
}

// Mise à jour des KPI
function updateKPIReservations(data) {
    const totalReservations = data.length;
    const confirmedCount = data.filter(res => res.statut === 'CONFIRMED').length;
    const cancelledCount = data.filter(res => res.statut === 'CANCELLED').length;
    const pendingCount = data.filter(res => res.statut === 'PENDING').length;
    const nowCount = data.filter(res => res.statut === 'NOW').length;
    const finishedCount = data.filter(res => res.statut === 'FINISHED').length;

    document.getElementById('kpi-total-reservations').textContent = totalReservations;
    document.getElementById('kpi-confirmed-reservations').textContent = confirmedCount;
    document.getElementById('kpi-cancelled-reservations').textContent = cancelledCount;
    document.getElementById('kpi-pending-reservations').textContent = pendingCount;
    document.getElementById('kpi-now-reservations').textContent = nowCount;
    document.getElementById('kpi-finished-reservations').textContent = finishedCount;
}

// Affichage des graphiques
function displayCharts(data) {
    const statuses = ['CONFIRMED', 'CANCELLED', 'PENDING', 'NOW', 'FINISHED'];
    const statusCounts = statuses.map(status => data.filter(res => res.statut === status).length);

    // Graphique à barres des statuts
    const ctxBar = document.getElementById('reservationsStatusChart').getContext('2d');
    new Chart(ctxBar, {
        type: 'bar',
        data: {
            labels: ['Confirmé', 'Annulé', 'En Attente', 'En Cours', 'Terminé'],
            datasets: [{
                data: statusCounts,
                backgroundColor: ['rgba(75, 192, 192, 0.2)', 'rgba(255, 99, 132, 0.2)', 'rgba(255, 206, 86, 0.2)', 'rgba(54, 162, 235, 0.2)', 'rgba(153, 102, 255, 0.2)'],
                borderColor: ['rgba(75, 192, 192, 1)', 'rgba(255, 99, 132, 1)', 'rgba(255, 206, 86, 1)', 'rgba(54, 162, 235, 1)', 'rgba(153, 102, 255, 1)'],
                borderWidth: 1
            }]
        },
        options: {
            scales: { y: { beginAtZero: true } },
            responsive: true,
            plugins: {
                title: {
                    display: true,
                    text: 'Nombre de Réservations par statut',
                    font: { size: 24 }
                }
            }
        }
    });

    // Graphique en ligne pour les réservations confirmées par mois
    fetch('/api/admin/reservations-confirmed-by-month')
        .then(response => response.json())
        .then(data => {
            const monthNames = ["Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"];
            const months = data.map(item => `${monthNames[item.month - 1]} ${item.year}`);
            const counts = data.map(item => item.count);

            const ctxLine = document.getElementById('reservationsChartReservation').getContext('2d');
            new Chart(ctxLine, {
                type: 'line',
                data: {
                    labels: months,
                    datasets: [{
                        data: counts,
                        borderColor: 'rgba(75, 192, 192, 1)',
                        fill: false,
                        tension: 0.1
                    }]
                },
                options: {
                    responsive: true,
                    plugins: {
                        title: {
                            display: true,
                            text: 'Nombre de Réservations Confirmées par Mois (12 derniers mois)',
                            font: { size: 24 }
                        }
                    },
                    scales: { y: { beginAtZero: true } }
                }
            });
        })
        .catch(error => console.error('Erreur lors de la récupération des données :', error));
}


function deleteReservation(reservationId) {
    if (confirm("Êtes-vous sûr de vouloir supprimer cette réservation ?")) {
        // Implémentez la logique de suppression côté serveur
        alert("Réservation supprimée : " + reservationId);
        const table = $('#all-reservations-table').DataTable();
        table.row(`#${reservationId}`).remove().draw();
    }
}


// ------------------------- ONGLET USER ----------------------
document.addEventListener('DOMContentLoaded', function () {
    fetch('/api/stats/user-kpi')
        .then(response => response.json())
        .then(data => {
            // Mettre à jour les valeurs des KPI
            document.getElementById('totalUsers').textContent = data.totalUsers;
            document.getElementById('newRegistrations').textContent = data.newRegistrations;
            document.getElementById('usersOnline').textContent = data.usersOnline;
            document.getElementById('usersWithCars').textContent = data.usersWithCars;
        })
        .catch(error => console.error('Erreur lors de la récupération des KPI:', error));
});

fetch('/api/stats/geolocation')
    .then(response => response.json())
    .then(data => {
        // data est une liste d'objets contenant des propriétés 'locality' et 'count'
        const labels = data.map(item => item.locality); // Les localités (labels)
        const counts = data.map(item => item.count); // Les nombres d'utilisateurs par localité (counts)

        var ctx = document.getElementById('userLocationChart').getContext('2d');
        var userLocationChart = new Chart(ctx, {
            type: 'bar', // Utilisation du graphique en barres
            data: {
                labels: labels,
                datasets: [{
                    label: 'Répartition géographique des utilisateurs', // Cela ne s'affichera plus
                    data: counts,
                    backgroundColor: [
                        'rgba(255, 99, 132, 0.2)',   // Rouge
                        'rgba(54, 162, 235, 0.2)',   // Bleu
                        'rgba(255, 206, 86, 0.2)',   // Jaune
                        'rgba(75, 192, 192, 0.2)',   // Vert
                        'rgba(153, 102, 255, 0.2)',  // Violet
                        'rgba(255, 159, 64, 0.2)',   // Orange
                        'rgba(199, 199, 199, 0.2)'   // Gris
                    ],
                    borderColor: [
                        'rgba(255, 99, 132, 1)',
                        'rgba(54, 162, 235, 1)',
                        'rgba(255, 206, 86, 1)',
                        'rgba(75, 192, 192, 1)',
                        'rgba(153, 102, 255, 1)',
                        'rgba(255, 159, 64, 1)',
                        'rgba(199, 199, 199, 1)'
                    ],
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                scales: {
                    y: {
                        beginAtZero: true,  // Commencer l'axe Y à 0
                    }
                },
                plugins: {
                    legend: {
                        display: false // Cela enlève la légende
                    },
                    title: {
                        display: true,
                        text: 'Répartition des utilisateurs par localité',
                        font: {
                            size: 18
                        }
                    }
                }
            }
        });
    })
    .catch(error => console.error('Erreur lors de la récupération des données géographiques:', error));


let table;  // Déclare la variable table globalement pour y avoir accès partout

document.addEventListener('DOMContentLoaded', function() {
    console.log("Document loaded");

    // Initialize DataTable
    table = $('#usersTable').DataTable({
        scrollX: true,
        scrollCollapse: true,
        paging: true,  // Enable pagination for better UX
        info: true,
        select: {
            style: 'multi'
        },
        ajax: {
            url: '/api/admin/users',
            type: 'GET',
            dataType: 'json',
            dataSrc: '',  // Adjust this if your API returns data in a specific structure
            error: function(xhr, error, code) {
                console.error("Failed to load data", error, code);
                alert("Failed to load data from the server.");
            }
        },
        columns: [
            {
                data: null,
                render: function() {
                    return '<input type="checkbox" class="user-checkbox" />';
                },
                title: 'Select',
                orderable: false
            },
            {
                data: 'photoUrl',
                render: function(data) {
                    return data ? `<img src="/uploads/profil/${data}" alt="Photo de Profil" width="50" height="50">` : 'No Image';
                },
                title: 'Photo'
            },
            {
                data: 'firstName',
                render: function(data) {
                    return data || 'Non spécifié';
                },
                title: 'Nom'
            },
            {
                data: 'lastName',
                render: function(data) {
                    return data || 'Non spécifié';
                },
                title: 'Prénom'
            },
            {
                data: 'email',
                render: function(data) {
                    return data || 'Non spécifié';
                },
                title: 'Email'
            },
            {
                data: null,
                render: function(data) {
                    return `${data.adresse || 'Adresse inconnue'}, ${data.locality || ''}, ${data.postalCode || ''}`;
                },
                title: 'Adresse'
            },
            {
                data: 'phone',
                render: function(data) {
                    return data || 'Non spécifié';
                },
                title: 'Téléphone'
            },
            {
                data: 'iban',
                render: function(data) {
                    return data || 'Non spécifié';
                },
                title: 'IBAN'
            },
            {
                data: 'bic',
                render: function(data) {
                    return data || 'Non spécifié';
                },
                title: 'BIC'
            },
            {
                data: 'createdAt',
                render: function(data) {
                    return data ? new Date(data).toLocaleDateString() : 'Date inconnue';
                },
                title: 'Date de création'
            },
            {
                data: null,
                render: function(data) {
                    return (data.ownedCars && data.ownedCars.length) || 0;
                },
                title: 'Voitures possédées'
            },
            {
                data: null,
                render: function(data) {
                    return (data.reservations && data.reservations.length) || 0;
                },
                title: 'Réservations'
            },
            {
                data: null,
                render: function(data) {
                    // Vérifiez si l'utilisateur est vérifié ou non, et appliquez les classes CSS appropriées
                    if (data && data.verified) {
                        return '<span class="text-success">Complet</span>';
                    } else {
                        return '<span class="text-danger">Incomplet</span>';
                    }
                },
                title: 'Documents vérifiés'
            },
            {
                data: null,
                render: function(data) {
                    return `
                        <div class="dropdown">
                            <button class="btn btn-secondary dropdown-toggle" type="button" id="actionMenu" data-bs-toggle="dropdown" aria-expanded="false">
                                Actions
                            </button>
                            <ul class="dropdown-menu" aria-labelledby="actionMenu">
                                <li><a class="dropdown-item" href="javascript:void(0)" onclick="verifyDocuments(${data.id})">Vérifier Documents</a></li>
                                <li><a class="dropdown-item" href="javascript:void(0)" onclick="editPermissions(${data.id})">Modifier Permissions</a></li>
                                <li><a class="dropdown-item text-danger" href="javascript:void(0)" onclick="deleteUser(${data.id})">Supprimer Utilisateur</a></li>
                                <li><a class="dropdown-item" href="javascript:void(0)" onclick="sendEmail(${data.id})">Envoyer Message</a></li>

                            </ul>
                        </div>`;
                },
                title: 'Actions',
                orderable: false
            }
        ]
    });

    // Gestion des actions groupées
    $('#groupActionBtn').on('click', function() {
        const action = $('#groupActionSelect').val();
        const selectedUsers = [];

        $('#usersTable tbody input[type="checkbox"]:checked').each(function() {
            const row = table.row($(this).closest('tr')).data();
            selectedUsers.push(row.id);
        });

        if (selectedUsers.length > 0) {
            switch (action) {
                case 'delete':
                    deleteUsers(selectedUsers);
                    break;
                case 'verify-documents':
                    verifyDocuments(selectedUsers);
                    break;
                case 'edit-permissions':
                    editPermissions(selectedUsers);
                    break;
                default:
                    alert('Veuillez sélectionner une action');
            }
        } else {
            alert('Aucun utilisateur sélectionné.');
        }
    });
});
/*function sendEmail(userId) {
    currentUserId = userId;  // Stocker l'ID de l'utilisateur

    // Ouvrir le modal
    const emailModal = new bootstrap.Modal(document.getElementById('sendEmailModal'));
    emailModal.show();

    // Réinitialiser le formulaire
    document.getElementById('sendEmailForm').reset();
    document.getElementById('userId').value = userId; // Mettre à jour l'ID de l'utilisateur
}


document.getElementById('sendEmailButton').addEventListener('click', function () {
    const subject = document.getElementById('emailSubject').value.trim();
    const message = document.getElementById('emailMessage').value.trim();
    const userId = document.getElementById('userId').value;

    // Validation des champs
    if (!subject || !message) {
        alert('Veuillez remplir tous les champs avant d\'envoyer le message.');
        return;
    }

    // Envoyer la requête à l'API pour envoyer l'e-mail
    fetch(`/api/admin/users/${userId}/send-email`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ subject: subject, message: message })
    })
    .then(response => {
        if (response.ok) {
            alert('Message envoyé avec succès');
            // Fermer le modal
            const emailModal = bootstrap.Modal.getInstance(document.getElementById('sendEmailModal'));
            emailModal.hide();
        } else {
            return response.json().then(err => {
                alert(`Erreur lors de l'envoi du message : ${err.message || 'Erreur inconnue'}`);
            });
        }
    })
    .catch(error => {
        console.error('Erreur lors de l\'envoi du message :', error);
        alert('Erreur lors de l\'envoi du message. Veuillez réessayer.');
    });
});*/





// Déclaration globale de la fonction verifyDocuments
function verifyDocuments(userId) {
    currentUserId = userId;  // Stocker l'ID de l'utilisateur

    fetch(`/api/admin/users/${userId}/documents`)
        .then(response => response.json())
        .then(data => {
            const documentModal = new bootstrap.Modal(document.getElementById('documentVerificationModal'));
            documentModal.show();

            const documentList = document.getElementById('documentList');
            documentList.innerHTML = '';  // Clear previous content

            if (data.length > 0) {
                data.forEach(doc => {
                    const listItem = document.createElement('li');
                    listItem.className = 'list-group-item';

                    // Utilisez 'url' au lieu de 'fileName'
                    if (doc.directory && doc.url) {
                        listItem.innerHTML = `
                            ${doc.url}
                            <a href="/uploads/${doc.directory}/${doc.url}" class="btn btn-primary btn-sm float-end" download>
                                Télécharger
                            </a>
                        `;
                    } else {
                        listItem.innerHTML = `
                            ${doc.url || "Nom de fichier indisponible"}
                            <span class="text-danger float-end">Le document est introuvable.</span>
                        `;
                    }

                    documentList.appendChild(listItem);
                });
            } else {
                documentList.innerHTML = '<li class="list-group-item">Aucun document trouvé pour cet utilisateur.</li>';
            }
        })
        .catch(error => console.error('Erreur lors du chargement des documents:', error));
}

document.addEventListener('DOMContentLoaded', function () {

    // Gestionnaire de l'événement pour le bouton "Complet"
    const completeVerificationBtn = document.getElementById('completeVerificationBtn');
    completeVerificationBtn.addEventListener('click', function () {
        // Envoyer une requête pour mettre à jour 'isVerified' à true
        fetch(`/api/admin/users/${currentUserId}/verify`, {
            method: 'PUT',  // Utilisez PUT ou POST selon l'API
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ isVerified: true })  // Envoyer la mise à jour de 'isVerified'
        })
        .then(response => {
            if (response.ok) {
                alert('Les documents ont été vérifiés avec succès.');
                // Fermer le modal après la mise à jour
                const documentModal = bootstrap.Modal.getInstance(document.getElementById('documentVerificationModal'));
                documentModal.hide();

                // Mettre à jour l'interface utilisateur si nécessaire (par exemple, changer "Incomplet" en "Complet")
                 // Mettre à jour la ligne de l'utilisateur courant dans DataTables
               const rowIndex = table.rows().indexes().filter(function(index) {
                   const rowData = table.row(index).data();
                   return rowData && rowData.id === currentUserId;  // Rechercher la ligne avec l'ID correspondant
               });

               if (rowIndex.length > 0) {
               const rowData = table.row(rowIndex[0]).data();
               rowData.verified = true;  // Mettre à jour le statut de vérification
               table.row(rowIndex[0]).data(rowData).draw(false);  // Redessiner la ligne sans recharger la table
                }
            } else {
                alert('Erreur lors de la vérification des documents.');
            }
        })
        .catch(error => {
            console.error('Erreur lors de la vérification des documents:', error);
            alert('Erreur lors de la vérification des documents.');
        });
    });
});


let currentUserId = null;

function editPermissions(userId) {
    currentUserId = userId;  // Stocker l'ID de l'utilisateur pour une utilisation ultérieure
    const permissionsModal = new bootstrap.Modal(document.getElementById('permissionsModal'));
    permissionsModal.show();

    // Effacer les cases à cocher précédentes dans le conteneur des rôles
    const rolesContainer = document.getElementById('rolesContainer');
    rolesContainer.innerHTML = '';

    // Récupérer les rôles disponibles et les rôles actuels de l'utilisateur via une API
    Promise.all([
        fetch(`/api/admin/roles`).then(response => response.json()), // Récupérer tous les rôles disponibles
        fetch(`/api/admin/users/${userId}/permissions`).then(response => response.json()) // Récupérer les rôles actuels de l'utilisateur
    ])
    .then(([availableRoles, userPermissions]) => {
        const roles = userPermissions.roles; // Les rôles actuels de l'utilisateur

        // Ajouter le nom complet de l'utilisateur dans le modal
        document.getElementById('userFullName').textContent = `${userPermissions.firstName} ${userPermissions.lastName}`;
        document.getElementById('userFullName').dataset.userId = userId;  // Stocker l'ID utilisateur dans l'élément

        // Remplir le conteneur avec des checkboxes pour chaque rôle disponible
        availableRoles.forEach(roleObj => {
            const roleName = roleObj.name; // Le nom du rôle
            const checkboxWrapper = document.createElement('div');
            checkboxWrapper.className = 'form-check';

            const checkbox = document.createElement('input');
            checkbox.className = 'form-check-input';
            checkbox.type = 'checkbox';
            checkbox.id = roleName;
            checkbox.value = roleName;
            checkbox.checked = roles.includes(roleName); // Cocher si l'utilisateur a ce rôle

            const label = document.createElement('label');
            label.className = 'form-check-label';
            label.htmlFor = roleName;
            label.textContent = roleName;

            checkboxWrapper.appendChild(checkbox);
            checkboxWrapper.appendChild(label);
            rolesContainer.appendChild(checkboxWrapper);
        });
    })
    .catch(error => console.error('Erreur lors de la récupération des rôles ou des permissions:', error));
}

function savePermissions() {
    const userId = currentUserId;
    const selectedRoles = Array.from(document.querySelectorAll('#rolesContainer input[type="checkbox"]:checked')).map(input => input.value);

    // Envoyer les rôles mis à jour via une requête PUT
    fetch(`/api/admin/users/${userId}/permissions`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ permissions: selectedRoles })
    })
    .then(response => {
        if (response.ok) {
            alert('Permissions mises à jour avec succès');
            // Fermer le modal après la mise à jour
            const permissionsModal = bootstrap.Modal.getInstance(document.getElementById('permissionsModal'));
            permissionsModal.hide();
        } else {
            alert('Erreur lors de la mise à jour des permissions');
        }
    })
    .catch(error => console.error('Erreur lors de la mise à jour des permissions:', error));
}


function deleteUser(userId) {
    if (confirm('Êtes-vous sûr de vouloir supprimer cet utilisateur ?')) {
        console.log(`Suppression de l'utilisateur avec l'ID : ${userId}`);
        fetch(`/api/admin/users/${userId}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            },
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Erreur lors de la suppression de l'utilisateur. Statut : ${response.status}`);
            }
            alert('Utilisateur supprimé avec succès');
            location.reload();
        })
        .catch(error => {
            console.error('Erreur lors de la suppression de l\'utilisateur :', error);
        });
    }
}

function deleteUsers(userIds) {
    if (confirm(`Êtes-vous sûr de vouloir supprimer ces utilisateurs : ${userIds.join(', ')} ?`)) {
        userIds.forEach(userId => deleteUser(userId));
    }
}



document.addEventListener('DOMContentLoaded', function () {
    // Initialisation du modal avec Bootstrap
    var addUserModal = new bootstrap.Modal(document.getElementById('addUserModal'));

    // Ouvrir le modal lorsque le bouton est cliqué
    const openModalBtn = document.getElementById('openModalBtn'); // Assurez-vous que le bouton a cet ID
    openModalBtn.addEventListener('click', function () {
        addUserModal.show(); // Afficher le modal
    });

    // Récupérer le formulaire
    const form = document.getElementById('addUserForm');

    // Gérer la soumission du formulaire
    form.addEventListener('submit', function (e) {
        e.preventDefault(); // Empêche le rechargement de la page

        // Récupérer les données du formulaire
        const lastName = document.getElementById('lastName').value;
        const firstName = document.getElementById('firstName').value;
        const adresse = document.getElementById('adresse').value;
        const postalCode = document.getElementById('postalCode').value;
        const locality = document.getElementById('locality').value;
        const phone = document.getElementById('phone').value;
        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;

        // Créer l'objet utilisateur à envoyer
        const newUser = {
            lastName: lastName,
            firstName: firstName,
            adresse: adresse,
            postalCode: postalCode,
            locality: locality,
            phone: phone,
            email: email,
            password: password
        };

        // Envoyer les données à l'API avec fetch
        fetch('/api/admin/add-user', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(newUser) // Convertir l'objet en JSON
        })
        .then(response => {
            // Vérifier si la réponse est OK (statut 200-299)
            if (!response.ok) {
                // Si la réponse n'est pas OK, vérifier si c'est du texte ou du JSON
                return response.text().then(text => {
                    throw new Error(text); // Lancer une exception avec le texte de l'erreur
                });
            }
            // Si la réponse est OK, la traiter comme JSON
            return response.json();
        })
        .then(data => {
            console.log('Utilisateur ajouté:', data);
            // Fermer le modal après l'ajout réussi
            addUserModal.hide();

            // Réinitialiser le formulaire
            form.reset();

            // Afficher un message de succès ou mettre à jour l'interface utilisateur
            alert('Utilisateur ajouté avec succès!');
        })
        .catch(error => {
            console.error('Erreur lors de l\'ajout de l\'utilisateur:', error);
            alert(`Erreur lors de l'ajout de l'utilisateur: ${error.message}`);
        });
    });
});



//--------------------ONGLET CAR ADMIN----------------------




//-------------------ADMIN SECTION CARS---------------------
$(document).ready(function () {
    // Fetch KPIs
    fetch('/api/admin/cars/stats')
        .then(response => response.json())
        .then(data => {
            console.log("Données reçues :", data);
            $('#totalCars').text(data.totalCars);
            $('#percentageOnline').text(data.percentageOnline.toFixed(2) + '%');
            $('#rentedCars').text(data.rentedCars);
            $('#percentagePending').text(data.percentagePending.toFixed(2) + '%');
        })
        .catch(error => console.error('Erreur lors de la récupération des KPI:', error));

    // Fetch Reservations Data for the Chart
fetch('/api/admin/cars/by-locality')  // Assurez-vous que l'API correspond à celle de votre backend
    .then(response => response.json())
    .then(data => {
        // Map les localités et les nombres de voitures
        const localities = data.map(item => item.locality);  // Les noms des localités
        const carCounts = data.map(item => item.count);  // Le nombre de voitures par localité

        const ctx = document.getElementById('reservationsChart').getContext('2d');
        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: localities,  // Les labels sont les noms des localités
                datasets: [{
                    label: 'Nombre de Voitures par Commune',
                    data: carCounts,
                    backgroundColor: 'rgba(54, 162, 235, 0.2)',
                    borderColor: 'rgba(54, 162, 235, 1)',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    title: {
                       display: true,
                       text: 'Nombre de Voitures par Commune',
                       font: {
                           size: 24  // Augmente la taille du titre
                       },
                       align: 'start',  // Positionne le titre à gauche
                       padding: {
                           top: 10,
                           bottom: 30
                       }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
    })
    .catch(error => console.error('Erreur lors de la récupération des données de localités:', error));



});



$(document).ready(function () {
    const carTable = $('#carsTable').DataTable({
        scrollX: true,
        scrollCollapse: true,
        paging: true,  // Activer la pagination pour une meilleure UX
        info: true,
        select: {
            style: 'multi'
        },
        ajax: {
            url: '/api/admin/cars/all',  // API pour récupérer toutes les voitures
            type: 'GET',
            dataType: 'json',
            dataSrc: '',  // Adapter selon la structure de votre réponse API
            error: function (xhr, error, code) {
                console.error("Erreur lors du chargement des données", error, code);
                alert("Échec du chargement des données depuis le serveur.");
            }
        },
        columns: [
            {
                data: null,
                render: function () {
                    return '<input type="checkbox" class="car-checkbox" />';
                },
                title: 'Select',
                orderable: false
            },
            {
                data: 'url',
                render: function (data) {
                    return data ? `<img src="/uploads/photo-car/${data}" alt="Photo de la voiture" width="50" height="50">` : 'No Image';
                },
                title: 'Photo'
            },
            {
                data: 'brand',
                render: function (data) {
                    return data || 'Non spécifié';
                },
                title: 'Marque'
            },
            {
                data: 'model',
                render: function (data) {
                    return data || 'Non spécifié';
                },
                title: 'Modèle'
            },
            {
                data: 'plaqueImmatriculation',
                render: function (data) {
                    return data || 'Non spécifié';
                },
                title: 'Plaque'
            },
            {
                data: 'locality',
                render: function (data) {
                    return data || 'Non spécifié';
                },
                title: 'Commune'
            },
            {
                data: null,
                render: function (data) {
                    return `${data.user.firstName} ${data.user.lastName}`;
                },
                title: 'Propriétaire'
            },
            {
                data: 'displayPrice',
                render: function (data) {
                    return data ? `${data} €` : 'Non spécifié';
                },
                title: 'Prix par jour'
            },
            {
                data: null,
                render: function (data) {
                    return data.online ? '<span class="text-success fw-bold">En ligne</span>' : '<span class="text-danger fw-bold">En attente</span>';
                },
                title: 'Status'
            },
            {
                data: null,
                render: function (data) {
                    return `
                        <div class="dropdown">
                            <button class="btn btn-secondary dropdown-toggle" type="button" id="actionMenu${data.id}" data-bs-toggle="dropdown" aria-expanded="false">
                                Actions
                            </button>
                            <ul class="dropdown-menu" aria-labelledby="actionMenu${data.id}">
                                ${!data.online ? `
                                    <li><button class="dropdown-item text-success" onclick="approveCar(${data.id})">Accepter</button></li>
                                    <li><button class="dropdown-item text-danger" onclick="rejectCar(${data.id})">Rejeter</button></li>
                                ` : ''}
                                <li><a class="dropdown-item" href="/admin/cars/details?id=${data.id}">Détails</a></li>
                                <li><button class="dropdown-item text-danger" onclick="deleteCar(${data.id})">Supprimer</button></li>
                            </ul>
                        </div>`;
                },
                title: 'Actions',
                orderable: false
            }
        ]
    });

    // Fonction pour approuver une voiture
    window.approveCar = function (id) {
        fetch(`/api/admin/cars/approve/${id}`, { method: 'POST' })
            .then(response => response.json())
            .then(() => {
                alert('La voiture a été approuvée.');
                carTable.ajax.reload(); // Recharger les données du tableau
            })
            .catch(error => console.error('Erreur lors de l\'approbation de la voiture:', error));
    };

    // Fonction pour rejeter une voiture
    window.rejectCar = function (id) {
        fetch(`/api/admin/cars/reject/${id}`, { method: 'POST' })
            .then(response => response.json())
            .then(() => {
                alert('La voiture a été rejetée.');
                carTable.ajax.reload(); // Recharger les données du tableau
            })
            .catch(error => console.error('Erreur lors du rejet de la voiture:', error));
    };

    // Fonction pour supprimer une voiture
    window.deleteCar = function (id) {
        if (confirm("Êtes-vous sûr de vouloir supprimer cette voiture ?")) {
            fetch(`/api/admin/cars/delete/${id}`, { method: 'DELETE' })
                .then(response => response.json())
                .then(() => {
                    alert('La voiture a été supprimée.');
                    carTable.ajax.reload(); // Recharger les données du tableau
                })
                .catch(error => console.error('Erreur lors de la suppression de la voiture:', error));
        }
    };


 // Filtrage des voitures par status (ALL, EN LIGNE, EN ATTENTE)
    $('#carStatusFilter').on('change', function () {
        const filterValue = this.value;
        carTable.column(8).search(filterValue).draw();
    });

    // Appliquer des actions groupées
    $('#applyActionBtn').on('click', function () {
        const selectedAction = $('#groupActionSelect').val();  // Récupérer l'action sélectionnée
        const selectedCars = [];

        $('.car-checkbox:checked').each(function () {
            const rowData = carTable.row($(this).parents('tr')).data();
            selectedCars.push(rowData.id);
        });

        if (selectedCars.length > 0) {
            if (selectedAction === 'approve') {
                // Approuver toutes les voitures sélectionnées
                selectedCars.forEach(id => approveCar(id));
            } else if (selectedAction === 'reject') {
                // Rejeter toutes les voitures sélectionnées
                selectedCars.forEach(id => rejectCar(id));
            } else if (selectedAction === 'delete') {
                // Supprimer toutes les voitures sélectionnées
                selectedCars.forEach(id => deleteCar(id));
            }
        } else {
            alert('Aucune voiture sélectionnée.');
        }
    });

});



             //-----------DETAIL CAR-------------------
document.addEventListener("DOMContentLoaded", function() {
    const urlParams = new URLSearchParams(window.location.search);
    const carId = urlParams.get('id');
    const spinner = document.getElementById("spinner");
    const cardBodyContent = document.querySelector(".card-body-content");
    const cardBodyLoading = document.querySelector(".card-body-loading");

        // Afficher le spinner et masquer le contenu de la carte
            cardBodyLoading.style.display = "flex";
            cardBodyContent.style.display = "none";

    spinner.style.display = "block";

    fetch(`/api/admin/cars/${carId}`)
        .then(response => response.json())
        .then(data => {
            console.log(data);

            cardBodyLoading.style.display = "none";
            cardBodyContent.style.display = "block";

            // Vérification des données pour éviter des erreurs
            document.getElementById("car-brand").textContent = data.brand || 'N/A';
            document.getElementById("car-model").textContent = data.model || 'N/A';
            document.getElementById("car-online").textContent = data.online ? 'Oui' : 'Non';
            document.getElementById("car-first-circulation").textContent = data.firstImmatriculation || 'N/A';
            document.getElementById("car-plate").textContent = data.plaqueImmatriculation || 'N/A';
            document.getElementById("car-mode-reservation").textContent = data.modeReservation || 'N/A';

            // Section Prix
            const pricesContainer = document.getElementById("car-prices");
            if (data.price) {
                const { lowPrice, middlePrice, highPrice, promo1, promo2 } = data.price;

                // Créer des colonnes pour les prix
                const lowPriceHtml = `
                    <div class="col-lg-3">
                        <p><strong>Basse saison</strong></p>
                        <p>${lowPrice ? lowPrice + '€' : 'Non spécifié'}</p>
                    </div>
                `;
                const middlePriceHtml = `
                    <div class="col-lg-3">
                        <p><strong>Moyenne saison</strong></p>
                        <p>${middlePrice ? middlePrice + '€' : 'Non spécifié'}</p>
                    </div>
                `;
                const highPriceHtml = `
                    <div class="col-lg-3">
                        <p><strong>Haute saison</strong></p>
                        <p>${highPrice ? highPrice + '€' : 'Non spécifié'}</p>
                    </div>
                `;

                // Créer des colonnes pour les promotions
                let promoHtml = '';
                if (promo1) {
                    promoHtml += `
                        <div class="col-lg-3">
                            <p><strong>Promotion 1</strong></p>
                            <p>${promo1}%</p>
                        </div>
                    `;
                }
                if (promo2) {
                    promoHtml += `
                        <div class="col-lg-3">
                            <p><strong>Promotion 2</strong></p>
                            <p>${promo2}%</p>
                        </div>
                    `;
                }

                // Injecter les colonnes dans le conteneur des prix
                pricesContainer.innerHTML = lowPriceHtml + middlePriceHtml + highPriceHtml + promoHtml;
            } else {
                pricesContainer.innerHTML = '<p>Aucun prix disponible</p>';
            }


            // Affichage des photos
            const photoContainer = document.getElementById("car-photos");
            if (data.photoUrl && data.photoUrl.length > 0) {
                data.photoUrl.forEach(photoUrl => {
                    const img = document.createElement('img');
                    img.src = `/uploads/photo-car/${photoUrl}`;
                    img.alt = "Photo de voiture";
                    img.classList.add('car-photo', 'img-thumbnail');
                    photoContainer.appendChild(img);
                });
            } else {
                photoContainer.innerHTML = '<p>Aucune photo disponible</p>';
            }

            // Caractéristiques
           const featuresContainer = document.getElementById("car-features");
           if (data.features && data.features.length > 0) {
               data.features.forEach(feature => {
                   const colDiv = document.createElement('div');
                   colDiv.classList.add('col-lg-4', 'car-feature','text-center');
                   colDiv.innerHTML = `
                       <h4>${feature.name}</h4>
                       <p>${feature.description || 'Description non disponible'}</p>
                   `;
                   featuresContainer.appendChild(colDiv);
               });
           } else {
               featuresContainer.innerHTML = '<p>Aucune caractéristique disponible</p>';
           }

           // Équipements
           const equipmentsContainer = document.getElementById("car-equipments");

           if (data.equipments && data.equipments.length > 0) {
               data.equipments.forEach(equipment => {
                   const colDiv = document.createElement('div');
                   colDiv.classList.add('col-lg-4', 'car-equipment','text-center');

                   // Générer l'HTML pour chaque équipement
                   colDiv.innerHTML = `
                       <h4>${equipment.equipment}</h4>
                       <img src="${equipment.icon}" alt="${equipment.equipment}" class="equipment-icon">
                   `;

                   equipmentsContainer.appendChild(colDiv);
               });
           } else {
               equipmentsContainer.innerHTML = '<p>Aucun équipement disponible</p>';
           }


           // Conditions
           const conditionsContainer = document.getElementById("car-conditions");
           if (data.conditionsDTOs && data.conditionsDTOs.length > 0) {
               data.conditionsDTOs.forEach(condition => {
                   const colDiv = document.createElement('div');
                   colDiv.classList.add('col-lg-4', 'car-condition','text-center');
                   colDiv.innerHTML = `
                       <p>${condition.condition || 'Description non disponible'}</p>
                   `;
                   conditionsContainer.appendChild(colDiv);
               });
           } else {
               conditionsContainer.innerHTML = '<p>Aucune condition disponible</p>';
           }

            // Liens de téléchargement des fichiers
            const carteGriseLink = document.getElementById("car-carte-grise");
            if (data.carteGrisePath) {
                carteGriseLink.href = `/uploads/registrationCard/${data.carteGrisePath}`;
                carteGriseLink.target = "_blank";  // Ouvrir dans un nouvel onglet
                carteGriseLink.textContent = "Télécharger la carte grise";
            } else {
                carteGriseLink.textContent = "Carte grise non disponible";
            }

 // Liens de téléchargement des documents d'identité
            const identityCardRectoLink = document.getElementById("car-identity-recto");
            const identityCardVersoLink = document.getElementById("car-identity-verso");

            // Vérification si data.user et ses documents existent
            if (data.user && data.user.documents) {
                console.log('Documents trouvés:', data.user.documents);  // Vérification

                // Trouver le document de type 'identity_recto'
                const identityRectoDoc = data.user.documents.find(doc => doc.documentType === 'identity_recto');
                if (identityRectoDoc && identityRectoDoc.url) {
                    console.log('Identity Recto Document:', identityRectoDoc);  // Vérification
                    identityCardRectoLink.href = `/uploads/identityCard/${identityRectoDoc.url}`;
                    identityCardRectoLink.target = "_blank";  // Ouvrir dans un nouvel onglet
                    identityCardRectoLink.textContent = "Télécharger la carte d'identité (recto)";
                } else {
                    console.log('Identity Recto non disponible');
                    identityCardRectoLink.removeAttribute('href');
                    identityCardRectoLink.style.color = 'gray'; // Pour montrer que le lien est inactif
                    identityCardRectoLink.textContent = "Carte d'identité (recto) non disponible";
                }

                // Trouver le document de type 'identity_verso'
                const identityVersoDoc = data.user.documents.find(doc => doc.documentType === 'identity_verso');
                if (identityVersoDoc && identityVersoDoc.url) {
                    console.log('Identity Verso Document:', identityVersoDoc);  // Vérification
                    identityCardVersoLink.href = `/uploads/identityCard/${identityVersoDoc.url}`;
                    identityCardVersoLink.target = "_blank";  // Ouvrir dans un nouvel onglet
                    identityCardVersoLink.textContent = "Télécharger la carte d'identité (verso)";
                } else {
                    console.log('Identity Verso non disponible');
                    identityCardVersoLink.removeAttribute('href');
                    identityCardVersoLink.style.color = 'gray'; // Pour montrer que le lien est inactif
                    identityCardVersoLink.textContent = "Carte d'identité (verso) non disponible";
                }
            } else {
                console.log('Pas de documents trouvés ou data.user est null');
                identityCardRectoLink.removeAttribute('href');
                identityCardRectoLink.style.color = 'gray';
                identityCardRectoLink.textContent = "Documents d'identité non disponibles";

                identityCardVersoLink.removeAttribute('href');
                identityCardVersoLink.style.color = 'gray';
                identityCardVersoLink.textContent = "Documents d'identité non disponibles";
            }

        })
        .catch(error => {
             console.error('Erreur:', error);
            // Tu peux également gérer les erreurs ici en masquant le spinner
            cardBodyLoading.style.display = "none";
            cardBodyContent.innerHTML = "<p>Erreur lors du chargement des données.</p>";
            cardBodyContent.style.display = "block";
        });
});





        //-------------------------CAR GESTION--------------------

  $(document).ready(function () {
        // Initialisation des DataTables
        const categoriesTable = $('#categoriesTable').DataTable({
          "autoWidth": false,
          "scrollX": 500,
          "responsive": true,
          "scrollY":300,
            "info": false     // Désactive l'affichage des informations sur les entrées
        });

        const equipmentsTable = $('#equipmentsTable').DataTable({
          "autoWidth": false,
          "scrollX": 500,
          "responsive": true,
          "scrollY":300,
            "info": false     // Désactive l'affichage des informations sur les entrées
        });

        const featuresTable = $('#featuresTable').DataTable({
          "autoWidth": false,
          "scrollX": 500,
          "responsive": true,
          "scrollY":300,
            "info": false     // Désactive l'affichage des informations sur les entrées
        });

        loadCategories();
        loadEquipments();
        loadFeatures();

        // Charger les catégories
        async function loadCategories() {
          const response = await fetch('/api/admin/categories');
          const categories = await response.json();
          categoriesTable.clear().draw();
          categories.forEach(category => {
            categoriesTable.row.add([
              category.category,
              `<button class="btn btn-danger" onclick="deleteCategory(${category.id})"><i class="fas fa-trash-alt"></i></button>`
            ]).draw(false);
          });
        }

        // Charger les équipements
        async function loadEquipments() {
          const response = await fetch('/api/admin/equipments');
          const equipments = await response.json();
          equipmentsTable.clear().draw();
          equipments.forEach(equipment => {
            equipmentsTable.row.add([
              equipment.equipment,
              `<img src="${equipment.icon}" width="50">`,
              `<button class="btn btn-danger" onclick="deleteEquipment(${equipment.id})"><i class="fas fa-trash-alt"></i></button>`
            ]).draw(false);
          });
        }

        // Charger les caractéristiques
        async function loadFeatures() {
          const response = await fetch('/api/admin/features');
          const features = await response.json();
          featuresTable.clear().draw();
          features.forEach(feature => {
            featuresTable.row.add([
              feature.name,
              feature.description,
              `<button class="btn btn-danger" onclick="deleteFeature(${feature.id})"><i class="fas fa-trash-alt"></i></button>`
            ]).draw(false);
          });
        }

        // Ajout de catégorie
        $('#addCategoryBtn').on('click', function () {
          const categoryName = prompt("Entrez le nom de la catégorie");
          if (categoryName) {
            fetch('/api/admin/categories', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({ category: categoryName })
            }).then(response => response.ok ? loadCategories() : alert("Erreur lors de l'ajout de la catégorie"));
          }
        });

        // Ajout d'équipement
        $('#addEquipmentBtn').on('click', async function () {
          const equipmentName = prompt("Entrez le nom de l'équipement");
          const iconFile = prompt("Entrez l'URL de l'icône (temporairement, sans téléchargement)");
          if (equipmentName && iconFile) {
            const formData = new FormData();
            formData.append('equipment', new Blob([JSON.stringify({ equipment: equipmentName })], { type: 'application/json' }));
            formData.append('icon', iconFile);

            const response = await fetch('/api/admin/equipments', {
              method: 'POST',
              body: formData
            });
            if (response.ok) loadEquipments();
          }
        });

        // Ajout de caractéristique
        $('#addFeatureBtn').on('click', function () {
          const featureName = prompt("Entrez le nom de la caractéristique");
          const featureDescription = prompt("Entrez la description de la caractéristique");
          if (featureName && featureDescription) {
            fetch('/api/admin/features', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({ name: featureName, description: featureDescription })
            }).then(response => response.ok ? loadFeatures() : alert("Erreur lors de l'ajout de la caractéristique"));
          }
        });

        // Supprimer catégorie
        window.deleteCategory = function (id) {
          fetch(`/api/admin/categories/${id}`, { method: 'DELETE' })
            .then(response => response.ok ? loadCategories() : alert("Erreur lors de la suppression de la catégorie"));
        };

        // Supprimer équipement
        window.deleteEquipment = function (id) {
          fetch(`/api/admin/equipments/${id}`, { method: 'DELETE' })
            .then(response => response.ok ? loadEquipments() : alert("Erreur lors de la suppression de l'équipement"));
        };

        // Supprimer caractéristique
        window.deleteFeature = function (id) {
          fetch(`/api/admin/features/${id}`, { method: 'DELETE' })
            .then(response => response.ok ? loadFeatures() : alert("Erreur lors de la suppression de la caractéristique"));
        };
      });




//-------------ONGLET FINANCES-------------------
document.addEventListener('DOMContentLoaded', async function() {
    console.log("DOM fully loaded and parsed");

    try {
        // Charger les paiements
        const paymentResponse = await fetch('/api/admin/payments');
        const paymentData = await paymentResponse.json();
        console.log('Payments data:', paymentData);

        // Affichage du tableau de paiements
        $('#paymentsTable').DataTable({
            scrollY: '600px',
            scrollX: true,
            scrollCollapse: true,
            searching: false,
            data: paymentData,
            columns: [
                { data: null, render: function(data) {
                    return `
                        <div style="text-align: center;">
                            <img src="${data.photoProfil}" alt="${data.userFirstName} ${data.userLastName}" style="width: 40px; height: 40px; border-radius: 50%;">
                            <div style="display: flex; justify-content: center;">
                                <span style="margin-right: 5px;">${data.userFirstName}</span>
                                <span>${data.userLastName}</span>
                            </div>
                        </div>`;
                }},


                { data: 'prixTotal', render: $.fn.dataTable.render.number(',', '.', 2, '€') },
                { data: 'statut' },
                { data: 'paiementMode' },
                { data: 'createdAt', render: function(data) {
                    return new Date(data).toLocaleDateString('fr-FR');
                }},
                { data: 'prixPourDriveShare', render: $.fn.dataTable.render.number(',', '.', 2, '€') },

                { data: 'gainDTO.montantGain', render: function(data) {
                    return data ? `${data.toFixed(2)} €` : 'Aucun';
                }},

                { data: 'refundDTO.amount', render: function(data) {
                    return data ? `${data.toFixed(2)} €` : 'Aucun';
                }},


                { data: 'dateFinLocation', render: function(data) {
                    return new Date(data).toLocaleDateString('fr-FR');
                }},
                { data: null, render: function(data, type, row) {
                    const hasGain = row.gainDTO && row.gainDTO.montantGain > 0;
                    const hasRefund = row.refundDTO && row.refundDTO.amount > 0;

                    return (hasGain || hasRefund) ? 'Oui' : 'Non';
                }},

                {
                    data: null,
                    render: function(data, type, row) {
                        return `
                            <div class="action-buttons">
                                <button class="btn btn-danger" onclick="deletePayment(${row.id})"><i class="fas fa-trash-alt"></i></button>
                            </div>`;
                    }
                }
            ]
        });

        // Charger les KPI
        const kpiResponse = await fetch('/api/admin/payments/kpi');
        const kpiData = await kpiResponse.json();
        updateKPIPayments(kpiData);

        // Charger les statistiques financières pour le graphique
        const financialResponse = await fetch('/api/admin/payments/financial-stats');
        const financialData = await financialResponse.json();
        displayFinancialStatsChart(financialData.benefitByDay, financialData.refundsByDay, financialData.userGeneratedRevenueByDay);

    } catch (error) {
        console.error('Erreur lors du chargement des données:', error);
    }
});

function updateKPIPayments(data) {
    // Total des Transactions
    const totalTransactions = data.totalTransactions;
    const cancellationPercentage = data.cancellationPercentage.toFixed(2) + '%';
    const totalRefunds = data.totalRefunds.toFixed(2) + '€';
    const userGeneratedRevenue = data.userGeneratedRevenue.toFixed(2) + '€';

    // Mise à jour des éléments DOM
    document.getElementById('kpi-total-transactions').textContent = totalTransactions;
    document.getElementById('kpi-cancellation-percentage').textContent = cancellationPercentage;
    document.getElementById('kpi-total-refunds').textContent = totalRefunds;
    document.getElementById('kpi-user-generated-revenue').textContent = userGeneratedRevenue;
}

function displayFinancialStatsChart(benefitByMonth, refundsByMonth, userGeneratedRevenueByMonth) {
    const ctx = document.getElementById('financialStatsChart').getContext('2d');

    const labels = Object.keys(benefitByMonth).map(month => {
        const date = new Date(month);
        return date.toLocaleString('fr-FR', { year: 'numeric', month: 'long' }); // Format mois-année
    });

    new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Bénéfices',
                    data: Object.values(benefitByMonth),
                    backgroundColor: 'rgba(75, 192, 192, 0.2)',
                    borderColor: 'rgba(75, 192, 192, 1)',
                    fill: false
                },
                {
                    label: 'Remboursements',
                    data: Object.values(refundsByMonth),
                    backgroundColor: 'rgba(255, 99, 132, 0.2)',
                    borderColor: 'rgba(255, 99, 132, 1)',
                    fill: false
                },
                {
                    label: 'Revenus générés par les utilisateurs',
                    data: Object.values(userGeneratedRevenueByMonth),
                    backgroundColor: 'rgba(153, 102, 255, 0.2)',
                    borderColor: 'rgba(153, 102, 255, 1)',
                    fill: false
                }
            ]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
}



function generateReport() {
    fetch('/api/admin/payments/report')
        .then(response => response.blob())
        .then(blob => {
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.style.display = 'none';
            a.href = url;
            a.download = 'report.pdf';
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
        })
        .catch(error => console.error('Erreur lors de la génération du rapport:', error));
}


//--------------------------ONGLET CLAIM----------------
document.addEventListener('DOMContentLoaded', async function() {
    document.getElementById('spinner').style.display = 'block';
    document.getElementById('claims-table').style.display = 'none';

    try {
        await Promise.all([
            loadClaims(),
            loadKpiData(),
            loadMonthlyClaimsChart(),
            loadReservationOptions()
        ]);
    } catch (error) {
        console.error('Erreur lors du chargement des données:', error);
    } finally {
        document.getElementById('spinner').style.display = 'none';
        document.getElementById('claims-table').style.display = 'table';
    }
});


document.getElementById('reservationSelect').addEventListener('change', function() {
    const status = this.value;
    loadClaims(status); // Afficher les réclamations selon le statut sélectionné
});

async function loadClaims(status = 'all') {
    try {
        const response = await fetch('/api/admin/claims');
        if (!response.ok) throw new Error('Erreur lors du chargement des réclamations');
        const claims = await response.json();

        // Filtrer les réclamations selon le statut si ce n'est pas 'all'
        const filteredClaims = status === 'all' ? claims : claims.filter(claim => claim.status === status);

        // Trier les réclamations par date, du plus récent au plus ancien
        filteredClaims.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

        // Masquer le spinner et afficher le tableau des réclamations
        const spinner = document.getElementById('spinner');
        const claimsTable = document.getElementById('claims-table');

        if (spinner && claimsTable) {
            spinner.style.display = 'none';
            claimsTable.style.display = 'table';
        }

        displayClaims(filteredClaims);
    } catch (error) {
        console.error('Erreur lors du chargement des réclamations:', error);
        document.getElementById('spinner').style.display = 'none';
    }
}




function displayClaims(claims) {
    const claimsBody = document.getElementById('claims-body');
    if (!claimsBody) return;

    claimsBody.innerHTML = ''; // Vider le contenu précédent

    // Vérifiez si le tableau des réclamations est vide
    if (claims.length === 0) {
        claimsBody.innerHTML = '<tr><td colspan="8">Aucune réclamation trouvée pour ce statut.</td></tr>';
        return; // Sortir de la fonction si aucune réclamation
    }

    // Si des réclamations existent, ajouter les lignes au tableau
    claims.forEach(claim => {
        const row = document.createElement('tr');

        const resolveButton = claim.status !== 'FINISHED' ? `
            <button class="btn btn-outline-success" onclick="resolveClaim(${claim.id})">
                <i class="fas fa-check"></i>
            </button>
        ` : '';

        const responseButton = claim.status !== 'FINISHED' ? `
            <button class="btn btn-outline-info" onclick="openResponseModal(${claim.id})">
                <i class="fas fa-reply"></i>
            </button>
        ` : '';

        const deleteButton = `
            <button class="btn btn-outline-danger" onclick="deleteClaim(${claim.id})">
                <i class="fas fa-trash-alt"></i>
            </button>
        `;

        row.innerHTML = `
            <td>${new Date(claim.createdAt).toLocaleDateString('fr-FR')}</td>
            <td>${claim.message}</td>
            <td>${claim.reservationId}</td>
            <td>${claim.claimantRole}</td>
            <td>${claim.status}</td>
            <td>${claim.responseAt ? new Date(claim.responseAt).toLocaleDateString('fr-FR') : '<i class="fa-solid fa-xmark" style="color:red;"></i>'}</td>
            <td>${claim.response || '<i class="fa-solid fa-xmark" style="color:red;"></i>'}</td>
            <td>
                ${resolveButton} ${responseButton} ${deleteButton}
            </td>
        `;
        claimsBody.appendChild(row); // Ajouter la ligne au corps du tableau
    });
}





function openResponseModal(claimId) {
    const claimIdField = document.getElementById('claimId');
    if (claimIdField) {
        claimIdField.value = claimId;
        const responseModal = new bootstrap.Modal(document.getElementById('responseModal'));
        responseModal.show();
    }
}

const responseForm = document.getElementById('response-form');
if (responseForm) {
    responseForm.addEventListener('submit', async function (event) {
        event.preventDefault();
        const claimId = document.getElementById('claimId').value;
        const message = document.getElementById('responseMessage').value;

        try {
            const response = await fetch(`/api/admin/claims/response/${claimId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ message })
            });

            if (!response.ok) throw new Error("Erreur lors de l'envoi de la réponse.");

            alert('Réponse envoyée avec succès.');
            const responseModal = bootstrap.Modal.getInstance(document.getElementById('responseModal'));
            responseModal.hide();
            loadClaims(); // Recharger les réclamations
        } catch (error) {
            console.error('Erreur lors de l\'envoi de la réponse:', error);
            alert('Erreur lors de l\'envoi de la réponse.');
        }
    });
}
async function loadReservationOptions() {
    const reservationSelect = document.getElementById('reservationSelect');
    if (!reservationSelect) return;

    reservationSelect.innerHTML = ''; // Vider les options existantes

    const allOption = document.createElement('option');
    allOption.value = 'all';
    allOption.textContent = 'Toutes les Réclamations';
    reservationSelect.appendChild(allOption);

    // Définir les statuts manuellement
    const statuses = [
        { value: 'FINISHED', display: 'Clôturé' },
        { value: 'PENDING', display: 'En attente' },
        { value: 'IN_PROGRESS', display: 'En cours' }
    ];

    // Ajouter les options de statut
    statuses.forEach(status => {
        const option = document.createElement('option');
        option.value = status.value; // Utiliser le statut pour le filtre
        option.textContent = status.display;
        reservationSelect.appendChild(option);
    });
}



async function resolveClaim(claimId) {
    try {
        const response = await fetch(`/api/admin/claims/resolve/${claimId}`, {
            method: 'POST'
        });

        if (!response.ok) throw new Error("Une erreur s'est produite lors de la résolution de la réclamation.");

        alert('Réclamation résolue');
        loadClaims(); // Recharger la liste des réclamations
    } catch (error) {
        console.error('Erreur lors de la résolution de la réclamation:', error);
        alert('Erreur lors de la résolution de la réclamation.');
    }
}

async function loadKpiData() {
    try {
        const response = await fetch('/api/admin/claims/kpi');
        if (!response.ok) throw new Error('Erreur lors du chargement des KPI');
        const kpiData = await response.json();

        // Afficher les KPI
        document.getElementById('totalClaims').textContent = kpiData.totalClaims;

        // Calcul des pourcentages
        const resolvedPercentage = ((kpiData.resolvedClaims / kpiData.totalClaims) * 100) || 0;
        const inProgressPercentage = ((kpiData.inProgressClaims / kpiData.totalClaims) * 100) || 0;
        const pendingPercentage = ((kpiData.pendingClaims / kpiData.totalClaims) * 100) || 0;

        document.getElementById('resolvedClaimsPercentage').textContent = resolvedPercentage.toFixed(2) + '%';
        document.getElementById('inProgressClaimsPercentage').textContent = inProgressPercentage.toFixed(2) + '%';
        document.getElementById('pendingClaimsPercentage').textContent = pendingPercentage.toFixed(2) + '%';

    } catch (error) {
        console.error('Erreur lors du chargement des KPI:', error);
    }
}


// Fonction pour charger les réclamations mensuelles et créer un graphique linéaire
async function loadMonthlyClaimsChart() {
    try {
        const response = await fetch('/api/admin/claims/monthly');
        const claimsPerMonth = await response.json();

        // Obtenez la liste des 12 derniers mois
        const months = getLast12Months();

        // Récupérez les valeurs des réclamations correspondantes aux mois
        const claimCounts = months.map(month => claimsPerMonth[month] || 0);

        // Afficher le graphique linéaire avec Chart.js
        const ctx = document.getElementById('claimsChart').getContext('2d');
        const chart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: months.map(month => getMonthName(month)),
                datasets: [{
                    label: 'Nombre de réclamations',
                    data: claimCounts,
                    backgroundColor: 'rgba(75, 192, 192, 0.2)',
                    borderColor: 'rgba(75, 192, 192, 1)',
                    borderWidth: 2,
                    fill: false
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        display: false // Supprimer la légende
                    },
                    title: {
                        display: true,
                        text: 'Évolution des Réclamations par Mois',
                        font: {
                            size: 16 // Taille de la police
                        },
                        padding: {
                            top: 10,
                            bottom: 10
                        }
                    }
                },
                scales: {
                    x: {
                        title: {
                            display: true,
                            text: 'Mois'
                        }
                    },
                    y: {
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: 'Nombre de réclamations'
                        }
                    }
                }
            }
        });
    } catch (error) {
        console.error('Erreur lors du chargement des données des réclamations mensuelles:', error);
    }
}


  // Fonction utilitaire pour obtenir les 12 derniers mois sous forme de "MM-YYYY"
  function getLast12Months() {
      const months = [];
      const currentDate = new Date();

      for (let i = 0; i < 12; i++) {
          const month = currentDate.getMonth() + 1; // Mois (de 0 à 11, donc +1)
          const year = currentDate.getFullYear();
          months.push(`${String(month).padStart(2, '0')}-${year}`);
          currentDate.setMonth(currentDate.getMonth() - 1);
      }

      return months.reverse(); // Pour avoir la plus ancienne date en premier
  }

  // Fonction utilitaire pour convertir "MM-YYYY" en nom de mois
  function getMonthName(monthYear) {
      const [month, year] = monthYear.split('-');
      const monthNames = ["Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"];
      return `${monthNames[parseInt(month) - 1]} ${year}`;
  }