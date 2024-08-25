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
                        }
                    }
                });
            }
        })
        .catch(error => {
            console.error('Error fetching revenue by month:', error);
        });

    // Gestion de la sélection de l'année et du mois pour les réservations
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
            photo.src = '/uploads/' + car.photoUrl;  // URL of the first photo
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
        photo.src = user.photoUrl || 'default-photo.png'; // URL of the user's photo, or a default if none
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
    $('#filter-status').on('change', function() {
        const selectedStatus = $(this).val();
        const table = $('#all-reservations-table').DataTable();
        if (selectedStatus) {
            table.column(5).search('^' + selectedStatus + '$', true, false).draw();
        } else {
            table.column(5).search('').draw();
        }
    });

    // Sélectionner/Désélectionner toutes les réservations
    $('#select-all').on('click', function() {
        const rows = $('#all-reservations-table').DataTable().rows({ 'search': 'applied' }).nodes();
        $('input[type="checkbox"]', rows).prop('checked', this.checked);
    });

    // Suppression des réservations sélectionnées
    $('#delete-selected').on('click', function() {
        const table = $('#all-reservations-table').DataTable();
        const selectedReservations = [];

        table.$('input[type="checkbox"]:checked').each(function() {
            selectedReservations.push($(this).closest('tr').attr('id'));
        });

        if (selectedReservations.length > 0) {
            if (confirm("Êtes-vous sûr de vouloir supprimer les réservations sélectionnées ?")) {
                // Implémentez ici la suppression côté serveur
                console.log("Suppression des réservations : ", selectedReservations);
                // Simuler la suppression locale (à remplacer par une suppression réelle côté serveur)
                selectedReservations.forEach(id => {
                    table.row(`#${id}`).remove().draw();
                });
            }
        } else {
            alert("Aucune réservation sélectionnée.");
        }
    });
});

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
                    return `
                        <button class="btn btn-sm btn-primary" onclick="editReservation(${data.id})">Modifier</button>
                        <button class="btn btn-sm btn-danger" onclick="deleteReservation(${data.id})">Supprimer</button>
                    `;
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

function updateKPIReservations(data) {
    const totalReservations = data.length;
    const confirmedCount = data.filter(res => res.statut === 'CONFIRMED').length;
    const cancelledCount = data.filter(res => res.statut === 'CANCELLED').length;
    const pendingCount = data.filter(res => res.statut === 'PENDING').length;
    const nowCount = data.filter(res => res.statut === 'NOW').length;
    const finishedCount = data.filter(res => res.statut === 'FINISHED').length;

    // Vérification de l'existence de chaque élément avant de définir textContent
    const totalEl = document.getElementById('kpi-total-reservations');
    if (totalEl) totalEl.textContent = totalReservations;

    const confirmedEl = document.getElementById('kpi-confirmed-reservations');
    if (confirmedEl) confirmedEl.textContent = confirmedCount;

    const cancelledEl = document.getElementById('kpi-cancelled-reservations');
    if (cancelledEl) cancelledEl.textContent = cancelledCount;

    const pendingEl = document.getElementById('kpi-pending-reservations');
    if (pendingEl) pendingEl.textContent = pendingCount;

    const nowEl = document.getElementById('kpi-now-reservations');
    if (nowEl) nowEl.textContent = nowCount;

    const finishedEl = document.getElementById('kpi-finished-reservations');
    if (finishedEl) finishedEl.textContent = finishedCount;
}


function displayCharts(data) {
    const statuses = ['CONFIRMED', 'CANCELLED', 'PENDING', 'NOW', 'FINISHED'];
    const statusCounts = statuses.map(status => data.filter(res => res.statut === status).length);

    // Bar Chart
    const ctxBar = document.getElementById('reservationsStatusChart').getContext('2d');
    new Chart(ctxBar, {
        type: 'bar',
        data: {
            labels: ['Confirmé', 'Annulé', 'En Attente', 'En Cours', 'Terminé'],
            datasets: [{
                label: 'Nombre de Réservations',
                data: statusCounts,
                backgroundColor: ['rgba(75, 192, 192, 0.2)', 'rgba(255, 99, 132, 0.2)', 'rgba(255, 206, 86, 0.2)', 'rgba(54, 162, 235, 0.2)', 'rgba(153, 102, 255, 0.2)'],
                borderColor: ['rgba(75, 192, 192, 1)', 'rgba(255, 99, 132, 1)', 'rgba(255, 206, 86, 1)', 'rgba(54, 162, 235, 1)', 'rgba(153, 102, 255, 1)'],
                borderWidth: 1
            }]
        },
        options: {
            scales: {
                y: { beginAtZero: true }
            },
            responsive: true
        }
    });

    // Pie Chart
    const ctxPie = document.getElementById('reservationsPieChart').getContext('2d');
    new Chart(ctxPie, {
        type: 'pie',
        data: {
            labels: ['Confirmé', 'Annulé', 'En Attente', 'En Cours', 'Terminé'],
            datasets: [{
                data: statusCounts,
                backgroundColor: ['rgba(75, 192, 192, 0.2)', 'rgba(255, 99, 132, 0.2)', 'rgba(255, 206, 86, 0.2)', 'rgba(54, 162, 235, 0.2)', 'rgba(153, 102, 255, 0.2)'],
                borderColor: ['rgba(75, 192, 192, 1)', 'rgba(255, 99, 132, 1)', 'rgba(255, 206, 86, 1)', 'rgba(54, 162, 235, 1)', 'rgba(153, 102, 255, 1)'],
                borderWidth: 1
            }]
        },
        options: { responsive: true }
    });
}

function editReservation(reservationId) {
    alert("Modifier la réservation : " + reservationId);
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

fetch('/api/stats/registrations')
    .then(response => response.json())
    .then(data => {
        const labels = data.map(item => item.period); // Extraire les périodes
        const counts = data.map(item => item.count);  // Extraire les comptes

        var ctx = document.getElementById('userRegistrationChart').getContext('2d');
        var userRegistrationChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Inscriptions par mois',
                    data: counts,
                    backgroundColor: 'rgba(75, 192, 192, 0.2)',
                    borderColor: 'rgba(75, 192, 192, 1)',
                    borderWidth: 1,
                    fill: true,
                    tension: 0.1
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
    })
    .catch(error => console.error('Erreur lors de la récupération des inscriptions:', error));

fetch('/api/stats/geolocation')
    .then(response => response.json())
    .then(data => {
        // data est une liste d'objets contenant des propriétés 'locality' et 'count'
        const labels = data.map(item => item.locality); // Les localités (labels)
        const counts = data.map(item => item.count); // Les nombres d'utilisateurs par localité (counts)

        var ctx = document.getElementById('userLocationChart').getContext('2d');
        var userLocationChart = new Chart(ctx, {
            type: 'pie',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Répartition géographique des utilisateurs',
                    data: counts,
                    backgroundColor: [
                        'rgba(255, 99, 132, 0.2)',
                        'rgba(54, 162, 235, 0.2)',
                        'rgba(255, 206, 86, 0.2)',
                        'rgba(75, 192, 192, 0.2)',
                        'rgba(153, 102, 255, 0.2)',
                        'rgba(255, 159, 64, 0.2)',
                        'rgba(199, 199, 199, 0.2)'
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
            }
        });
    })
    .catch(error => console.error('Erreur lors de la récupération des données géographiques:', error));

document.addEventListener('DOMContentLoaded', function() {
    console.log("Document loaded");

    // Initialize DataTable
    const table = $('#usersTable').DataTable({
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
                    return data.hasIdentityDocuments ? 'Complet' : 'Incomplet';
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

function verifyDocuments(userId) {
    let ids = Array.isArray(userId) ? userId : [userId];

    ids.forEach(id => {
        fetch(`/api/admin/users/${id}/documents`)
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

                        // Vérifiez que directory et fileName sont définis
                        if (doc.directory && doc.fileName) {
                            listItem.innerHTML = `
                                ${doc.fileName}
                                <a href="/api/files/${doc.directory}/${doc.fileName}" class="btn btn-primary btn-sm float-end" download>
                                    Télécharger
                                </a>
                            `;
                        } else {
                            listItem.innerHTML = `
                                ${doc.fileName || "Nom de fichier indisponible"}
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
    });
}

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


//--------------------ONGLET CAR ADMIN----------------------

//-------------------ADMIN SECTION CARS---------------------
$(document).ready(function () {
    // Fetch KPIs
    fetch('/api/admin/cars/stats')
        .then(response => response.json())
        .then(data => {
            $('#totalCars').text(data.totalCars);
            $('#percentageOnline').text(data.percentageOnline.toFixed(2) + '%');
            $('#rentedCars').text(data.rentedCars);
            $('#percentagePending').text(data.percentagePending.toFixed(2) + '%');
        })
        .catch(error => console.error('Erreur lors de la récupération des KPI:', error));

    // Fetch Reservations Data for the Chart
fetch('/api/admin/cars/reservations-by-month')
    .then(response => response.json())
    .then(data => {
        // Tableau pour les noms des mois
        const monthNames = ["Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"];

        // Map les mois et les réservations
        const months = data.map(item => monthNames[item.month - 1]);  // Convertit le numéro du mois en nom de mois
        const counts = data.map(item => item.count);

        const ctx = document.getElementById('reservationsChart').getContext('2d');
        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: months,  // Les labels sont les noms des mois
                datasets: [{
                    label: 'Réservations par Mois',
                    data: counts,
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
                       text: 'Nombre de Réservations par Mois',
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
    .catch(error => console.error('Erreur lors de la récupération des données de réservations:', error));

});

 $(document).ready(function () {

const onlineTable = $('#onlineCarsTable').DataTable({
  "autoWidth": false,
  "scrollX": true,
  "responsive": true,
  "scrollY":600,
    "info": false     // Désactive l'affichage des informations sur les entrées
});

const pendingTable = $('#pendingCarsTable').DataTable({
  "autoWidth": false,
  "scrollX": true,
  "responsive": true,
  "scrollY":600,
    "info": false     // Désactive l'affichage des informations sur les entrées

});

    const onlineSpinner = $('#onlineSpinner');
    const pendingSpinner = $('#pendingSpinner');

    // Fonction pour charger les voitures en ligne
function fetchOnlineCars() {
  onlineSpinner.show();  // Afficher le spinner
  $('#onlineCarsTable').hide();  // Masquer le tableau
  onlineTable.clear().draw();  // Effacer le tableau avant de le recharger
  fetch('/api/admin/cars/online')
    .then(response => response.json())
    .then(data => {
      data.forEach(car => {
        onlineTable.row.add([
          `<img src="/uploads/${car.url}" class="car-photo img-thumbnail">`,
          car.brand,
          car.model,
          car.plaqueImmatriculation,
          `<a href="/admin/cars/details?id=${car.id}" class="btn btn-info" title="Détails">
             <i class="fas fa-info-circle"></i>
           </a>`
        ]).draw(false);
      });
    })
    .catch(error => console.error('Erreur lors du chargement des voitures en ligne:', error))
    .finally(() => {
      onlineSpinner.hide();  // Masquer le spinner
      $('#onlineCarsTable').show();  // Afficher le tableau
    });
}

    // Fonction pour charger les voitures en attente
function fetchPendingCars() {
  pendingSpinner.show();  // Afficher le spinner
  $('#pendingCarsTable').hide();  // Masquer le tableau
  pendingTable.clear().draw();  // Effacer le tableau avant de le recharger
  fetch('/api/admin/cars/pending')
    .then(response => response.json())
    .then(data => {
      data.forEach(car => {
        pendingTable.row.add([
          `<img src="/uploads/${car.url}" class="car-photo img-thumbnail">`,
          car.brand,
          car.model,
          car.plaqueImmatriculation,
          `<div class="action-icons m-2">
             <button class="btn btn-success btn-sm" onclick="approveCar(${car.id})" title="Accepter">
               <i class="fas fa-check"></i>
             </button>
             <button class="btn btn-danger btn-sm" onclick="rejectCar(${car.id})" title="Rejeter">
               <i class="fas fa-times"></i>
             </button>
             <a href="/admin/cars/details?id=${car.id}" class="btn btn-info btn-sm" title="Détails">
               <i class="fas fa-info-circle"></i>
             </a>
           </div>`
        ]).draw(false);
      });
    })
    .catch(error => console.error('Erreur lors du chargement des voitures en attente:', error))
    .finally(() => {
      pendingSpinner.hide();  // Masquer le spinner
      $('#pendingCarsTable').show();  // Afficher le tableau
    });
}


    // Charger les voitures en ligne au démarrage
    fetchOnlineCars();

    // Charger les voitures en attente au démarrage
    fetchPendingCars();

    // Approve Car
    window.approveCar = function (id) {
      fetch(`/api/admin/cars/approve/${id}`, { method: 'POST' })
        .then(response => response.json())
        .then(data => {
          alert('La voiture a été approuvée et est désormais en ligne.');
          fetchPendingCars(); // Recharger les voitures en attente
          fetchOnlineCars();  // Recharger les voitures en ligne
        })
        .catch(error => console.error('Erreur lors de l\'approbation de la voiture:', error));
    }

    // Reject Car
    window.rejectCar = function (id) {
      fetch(`/api/admin/cars/reject/${id}`, { method: 'POST' })
        .then(response => response.json())
        .then(data => {
          alert('La voiture a été rejetée avec succès.');
          fetchPendingCars(); // Recharger les voitures en attente
        })
        .catch(error => console.error('Erreur lors du rejet de la voiture:', error));
    }
  });

             //-----------DETAIL CAR-------------------
document.addEventListener("DOMContentLoaded", function() {
    const urlParams = new URLSearchParams(window.location.search);
    const carId = urlParams.get('id');
    const spinner = document.getElementById("spinner");

    spinner.style.display = "block";

    fetch(`/api/admin/cars/${carId}`)
        .then(response => response.json())
        .then(data => {
            spinner.style.display = "none";

            // Vérification des données pour éviter des erreurs
            document.getElementById("car-brand").textContent = data.brand || 'N/A';
            document.getElementById("car-model").textContent = data.model || 'N/A';
            document.getElementById("car-online").textContent = data.online ? 'Oui' : 'Non';
            document.getElementById("car-first-circulation").textContent = data.firstImmatriculation || 'N/A';
            document.getElementById("car-plate").textContent = data.plaqueImmatriculation || 'N/A';
            document.getElementById("car-mode-reservation").textContent = data.modeReservation || 'N/A';
            document.getElementById("car-price").textContent = data.price?.middlePrice || 'N/A';

            // Affichage des photos
            const photoContainer = document.getElementById("car-photos");
            if (data.photoUrl && data.photoUrl.length > 0) {
                data.photoUrl.forEach(photoUrl => {
                    const img = document.createElement('img');
                    img.src = `/uploads/${photoUrl}`;
                    img.alt = "Photo de voiture";
                    img.classList.add('car-photo', 'img-thumbnail');
                    photoContainer.appendChild(img);
                });
            } else {
                photoContainer.innerHTML = '<p>Aucune photo disponible</p>';
            }

            // Affichage des caractéristiques
            const featuresContainer = document.getElementById("car-features");
            if (data.features && data.features.length > 0) {
                data.features.forEach(feature => {
                    const li = document.createElement('li');
                    li.textContent = `${feature.name}: ${feature.description}`;
                    featuresContainer.appendChild(li);
                });
            } else {
                featuresContainer.innerHTML = '<p>Aucune caractéristique disponible</p>';
            }

            // Affichage des équipements
            const equipmentsContainer = document.getElementById("car-equipments");
            if (data.equipments && data.equipments.length > 0) {
                data.equipments.forEach(equipment => {
                    const li = document.createElement('li');
                    li.innerHTML = `<img src="${equipment.icon}" alt="${equipment.equipment}" class="equipment-icon mr-2"> ${equipment.equipment}`;
                    equipmentsContainer.appendChild(li);
                });
            } else {
                equipmentsContainer.innerHTML = '<p>Aucun équipement disponible</p>';
            }

            // Affichage des conditions du propriétaire
            const conditionsContainer = document.getElementById("car-conditions");
            if (data.conditionsDTOs && data.conditionsDTOs.length > 0) {
                data.conditionsDTOs.forEach(condition => {
                    const li = document.createElement('li');
                    li.textContent = condition.condition;
                    conditionsContainer.appendChild(li);
                });
            } else {
                conditionsContainer.innerHTML = '<p>Aucune condition disponible</p>';
            }

            // Liens de téléchargement des fichiers
            const carteGriseLink = document.getElementById("car-carte-grise");
            if (data.carteGrisePath) {
                carteGriseLink.href = `/uploads/${data.carteGrisePath}`;
                carteGriseLink.textContent = "Télécharger la carte grise";
            } else {
                carteGriseLink.textContent = "Carte grise non disponible";
            }

            const identityCardLink = document.getElementById("identity-card");
            if (data.identityRecto) {
                identityCardLink.href = `/uploads/${data.identityRecto}`;
                identityCardLink.textContent = "Télécharger la carte d'identité";
            } else {
                identityCardLink.textContent = "Carte d'identité non disponible";
            }

        })
        .catch(error => {
            spinner.style.display = "none";
            console.error('Erreur:', error);
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
document.addEventListener('DOMContentLoaded', function() {
    console.log("DOM fully loaded and parsed");

    fetch('/api/admin/payments')
        .then(response => response.json())
        .then(data => {
            console.log('Payments data:', data);

            // Mise à jour des KPI
            updateKPIPayments(data);

            // Affichage du tableau
            $('#paymentsTable').DataTable({
                scrollY: '200px',
                scrollX: true,
                scrollCollapse: true,
                searching: false,
                data: data,
                columns: [
                    { data: null, render: function(data) {
                        return `${data.userFirstName} ${data.userLastName}`;
                    }},  // Colonne pour le nom complet de l'utilisateur
                    { data: 'prixTotal', render: $.fn.dataTable.render.number(',', '.', 2, '€') },  // Prix total pour l'utilisateur
                    { data: 'statut' },
                    { data: 'paiementMode' },
                    { data: 'createdAt', render: function(data) {
                        return new Date(data).toLocaleDateString('fr-FR');
                    }},
                    { data: 'prixPourDriveShare', render: $.fn.dataTable.render.number(',', '.', 2, '€') },  // Prix pour DriveShare
                    { data: 'refund.amount', render: function(data) {  // Montant du remboursement
                                            return data ? `${data} €` : 'Aucun';
                                        }},
                    { data: 'gain.montantGain', render: function(data) {  // Montant du gain
                        return data ? `${data} €` : 'Aucun';
                    }},
                    { data: 'dateFinLocation', render: function(data) {
                        return new Date(data).toLocaleDateString('fr-FR');
                    }},
                    { data: null, render: function(data, type, row) {
                        const dateFinLocation = new Date(row.dateFinLocation);
                        const paymentDueDate = new Date(dateFinLocation);
                        paymentDueDate.setDate(paymentDueDate.getDate() + 3);
                        const isPaid = new Date().getTime() > paymentDueDate.getTime() && row.statut === 'Paid';
                        return isPaid ? 'Oui' : 'Non';
                    }},
                    {
                        data: null,
                        render: function(data, type, row) {
                            return `
                                <div class="action-buttons">
                                    <button class="btn btn-secondary" onclick="viewPayment(${row.id})"><i class="fas fa-eye"></i></button>
                                    <button class="btn btn-primary" onclick="editPayment(${row.id})"><i class="fas fa-edit"></i></button>
                                    <button class="btn btn-danger" onclick="deletePayment(${row.id})"><i class="fas fa-trash-alt"></i></button>
                                </div>`;
                        }
                    }
                ]
            });

            generateDailyPaymentStats(data);
            generateMonthlyPaymentStats(data);
        })
        .catch(error => console.error('Erreur lors de la récupération des paiements:', error));
});

function updateKPIPayments(data) {
    const totalPayments = data.length;
    const successfulPayments = data.filter(payment => payment.statut === 'succeeded').length;
    const pendingPayments = data.filter(payment => payment.statut === 'PENDING').length;
    const failedPayments = data.filter(payment => payment.statut === 'FAILED').length;

    document.getElementById('kpi-total-payments').textContent = totalPayments;
    document.getElementById('kpi-successful-payments').textContent = successfulPayments;
    document.getElementById('kpi-pending-payments').textContent = pendingPayments;
    document.getElementById('kpi-failed-payments').textContent = failedPayments;
}

function viewPayment(id) {
    alert('Voir le paiement avec l\'ID ' + id);
}

function editPayment(id) {
    alert('Modifier le paiement avec l\'ID ' + id);
}

function deletePayment(id) {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce paiement?')) {
        console.log(`Tentative de suppression du paiement avec l'ID: ${id}`);
        fetch(`/api/admin/payments/${id}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            },
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Erreur lors de la suppression du paiement. Statut: ${response.status}`);
            }
            alert('Paiement supprimé avec succès');
            location.reload();
        })
        .catch(error => {
            console.error('Erreur lors de la suppression du paiement:', error);
            alert('Erreur lors de la suppression du paiement. Vérifiez la console pour plus de détails.');
        });
    }
}

function generateDailyPaymentStats(data) {
    const dailyData = {};
    const formatter = new Intl.DateTimeFormat('fr-FR', { month: 'short', day: 'numeric' });

    data.forEach(payment => {
        const date = new Date(payment.createdAt);
        const formattedDate = formatter.format(date);

        if (!dailyData[formattedDate]) {
            dailyData[formattedDate] = 0;
        }
        dailyData[formattedDate] += payment.prixTotal;
    });

    const ctx = document.getElementById('dailyPaymentStatsChart').getContext('2d');
    new Chart(ctx, {
        type: 'line',
        data: {
            labels: Object.keys(dailyData),
            datasets: [{
                label: 'Paiements quotidiens',
                data: Object.values(dailyData),
                backgroundColor: 'rgba(75, 192, 192, 0.2)',
                borderColor: 'rgba(75, 192, 192, 1)',
                borderWidth: 1
            }]
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

function generateMonthlyPaymentStats(data) {
    const monthlyData = {};
    const formatter = new Intl.DateTimeFormat('fr-FR', { month: 'long', year: 'numeric' });

    data.forEach(payment => {
        const date = new Date(payment.createdAt);
        const formattedDate = formatter.format(date);

        if (!monthlyData[formattedDate]) {
            monthlyData[formattedDate] = 0;
        }
        monthlyData[formattedDate] += payment.prixTotal;
    });

    const ctx = document.getElementById('monthlyPaymentStatsChart').getContext('2d');
    new Chart(ctx, {
        type: 'line',
        data: {
            labels: Object.keys(monthlyData),
            datasets: [{
                label: 'Paiements mensuels',
                data: Object.values(monthlyData),
                backgroundColor: 'rgba(153, 102, 255, 0.2)',
                borderColor: 'rgba(153, 102, 255, 1)',
                borderWidth: 1
            }]
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
document.addEventListener('DOMContentLoaded', function() {
    // Afficher le spinner et masquer le tableau avant le chargement
    document.getElementById('spinner').style.display = 'block';
    document.getElementById('claims-table').style.display = 'none';

    // Charger toutes les réclamations et les KPI au démarrage
    loadClaims();
    loadKpiData();
    loadMonthlyClaimsChart();

    // Charger les options de réservation pour le filtre
    loadReservationOptions();

    // Gestion du filtre de réclamations par réservation
    document.getElementById('reservationSelect').addEventListener('change', function() {
        const reservationId = this.value;
        if (reservationId === 'all') {
            loadClaims(); // Afficher toutes les réclamations
        } else {
            loadClaimsByReservation(reservationId); // Filtrer par réservation
        }
    });
});

async function loadClaims() {
    try {
        const response = await fetch('/api/claims');
        if (!response.ok) throw new Error('Erreur lors du chargement des réclamations');
        const claims = await response.json();

        // Masquer le spinner et afficher le tableau des réclamations
        const spinner = document.getElementById('spinner');
        const claimsTable = document.getElementById('claims-table');

        if (spinner && claimsTable) {
            spinner.style.display = 'none';
            claimsTable.style.display = 'table';
        }

        displayClaims(claims);
    } catch (error) {
        console.error('Erreur lors du chargement des réclamations:', error);
        document.getElementById('spinner').style.display = 'none';
    }
}

async function loadClaimsByReservation(reservationId) {
    try {
        const response = await fetch(`/api/claims/reservation/${reservationId}`);
        if (!response.ok) throw new Error('Erreur lors du chargement des réclamations par réservation');
        const claims = await response.json();

        document.getElementById('spinner').style.display = 'none';
        document.getElementById('claims-table').style.display = 'table';

        displayClaims(claims);
    } catch (error) {
        console.error('Erreur lors du chargement des réclamations par réservation:', error);
        document.getElementById('spinner').style.display = 'none';
    }
}

function displayClaims(claims) {
    const claimsBody = document.getElementById('claims-body');
    if (!claimsBody) return;

    claimsBody.innerHTML = ''; // Vider le contenu précédent

    if (claims.length === 0) {
        claimsBody.innerHTML = '<tr><td colspan="7">Aucune réclamation trouvée.</td></tr>';
    } else {
        claims.forEach(claim => {
            const claimRow = `
                <tr>
                    <td>${claim.id}</td>
                    <td>${claim.message}</td>
                    <td>${claim.claimantRole}</td>
                    <td>${claim.status}</td>
                    <td>
                        <button class="btn btn-success" onclick="resolveClaim(${claim.id})">Résoudre</button>
                        <button class="btn btn-primary" onclick="openResponseModal(${claim.id})">Répondre</button>
                    </td>
                </tr>
            `;
            claimsBody.insertAdjacentHTML('beforeend', claimRow);
        });
    }
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
            const response = await fetch(`/api/claims/response/${claimId}`, {
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
    try {
        const response = await fetch('/api/claims');
        if (!response.ok) throw new Error('Erreur lors du chargement des réclamations');
        const claims = await response.json();

        const reservationSelect = document.getElementById('reservationSelect');
        if (!reservationSelect) return;

        reservationSelect.innerHTML = ''; // Vider les options existantes

        const groupedReservations = new Map();
        claims.forEach(claim => {
            if (!groupedReservations.has(claim.reservationId)) {
                groupedReservations.set(claim.reservationId, claim.reservationId);
            }
        });

        const allOption = document.createElement('option');
        allOption.value = 'all';
        allOption.textContent = 'Toutes les Réclamations';
        reservationSelect.appendChild(allOption);

        groupedReservations.forEach((reservationId) => {
            const option = document.createElement('option');
            option.value = reservationId;
            option.textContent = `Réservation #${reservationId}`;
            reservationSelect.appendChild(option);
        });
    } catch (error) {
        console.error('Erreur lors du chargement des options de réservation:', error);
    }
}

async function resolveClaim(claimId) {
    try {
        const response = await fetch(`/api/claims/resolve/${claimId}`, {
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
        const response = await fetch('/api/claims/kpi');
        if (!response.ok) throw new Error('Erreur lors du chargement des KPI');
        const kpiData = await response.json();

        // Afficher les KPI
        document.getElementById('totalClaims').textContent = kpiData.totalClaims;
        document.getElementById('resolvedClaims').textContent = kpiData.resolvedClaims;
        document.getElementById('pendingClaims').textContent = kpiData.pendingClaims;


    } catch (error) {
        console.error('Erreur lors du chargement des KPI:', error);
    }
}

// Fonction pour charger les réclamations mensuelles et créer un graphique linéaire
async function loadMonthlyClaimsChart() {
      try {
          const response = await fetch('/api/claims/monthly');
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