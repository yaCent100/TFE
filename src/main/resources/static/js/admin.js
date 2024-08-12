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
        row.classList.add('m-3');

        for (let j = 0; j < 2; j++) {
            if (i + j < statuses.length) {
                const status = statuses[i + j];
                const reservations = reservationsByStatus[status];

                // Vérifiez si le statut a des réservations
                if (reservations.length > 0) {
                    const col = document.createElement('div');
                    col.classList.add('col-md-6');
                    col.classList.add('p-3');
                    col.innerHTML = `
                        <div class="status-section">
                            <h3>${getStatusTranslation(status)}</h3>
                            <table class="display" id="${status}-table">
                                <thead>
                                    <tr>
                                        <th></th>
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
                    reservations.forEach(reservation => {
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
                        paging: false,
                        searching: false,
                        language: {
                            info: "",
                            infoEmpty: "",
                            infoFiltered: "",
                            zeroRecords: "",
                            emptyTable: ""
                        }
                    });
                }
            }
        }
        // N'ajoutez la ligne au container que si elle contient des colonnes
        if (row.children.length > 0) {
            reservationContainer.appendChild(row);
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
    container.classList.add('mt-5');
    container.classList.add('mb-5');

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


// ------------------------- ONGLET USER ----------------------
document.addEventListener('DOMContentLoaded', function() {
    console.log("Document loaded");

    // Fetch and initialize users table
    fetch('/api/admin/users')
        .then(response => response.json())
        .then(data => {
            console.log('Users data:', data);
            $('#usersTable').DataTable({
                scrollY: '300px',
                scrollX: true,
                scrollCollapse: true,
                paging: false,
                info: false,
                data: data,
                columns: [
                    { data: 'photoUrl', render: data => `<img src="/uploads/profil/${data}" alt="Photo de Profil" width="50" height="50">` },
                    { data: 'nom' },
                    { data: 'prenom' },
                    { data: 'email' },
                    { data: null, render: data => `${data.adresse}, ${data.locality}, ${data.codePostal}` },
                    { data: 'telephoneNumber' },
                    { data: null, render: data => `<div class="action-buttons"><button class="btn btn-primary" onclick="viewUser(${data.id})"><i class="fas fa-eye"></i></button><button class="btn btn-secondary" onclick="editUser(${data.id})"><i class="fas fa-edit"></i></button><button class="btn btn-danger" onclick="deleteUser(${data.id})"><i class="fas fa-trash-alt"></i></button></div>` }
                ]
            });
        })
        .catch(error => console.error('Erreur lors de la récupération des utilisateurs:', error));

    // Fetch and initialize documents table
    fetch('/api/admin/users')
        .then(response => response.json())
        .then(data => {
            const documentsData = data.map(user => ({
                userId: user.id,
                userName: `${user.nom} ${user.prenom}`,
                photoUrl: user.photoUrl,
                identityRecto: user.documents.find(doc => doc.documentType === 'identity_recto')?.url.replace(/\\/g, '/') || 'Non disponible',
                identityVerso: user.documents.find(doc => doc.documentType === 'identity_verso')?.url.replace(/\\/g, '/') || 'Non disponible',
                licenseRecto: user.documents.find(doc => doc.documentType === 'licence_recto')?.url.replace(/\\/g, '/') || 'Non disponible',
                licenseVerso: user.documents.find(doc => doc.documentType === 'licence_verso')?.url.replace(/\\/g, '/') || 'Non disponible'
            }));
            console.log('Documents data:', documentsData);
            $('#documentsTable').DataTable({
                scrollY: '300px',
                scrollX: true,
                scrollCollapse: true,
                paging: false,
                info: false,
                data: documentsData,
               columns: [
                          { data: 'photoUrl', render: data => `<div><img src="/uploads/profil/${data}" alt="Photo de Profil" width="50" height="50"><p>${data.userName}</p></div>` },
                          { data: 'identityRecto', render: data => data !== 'Non disponible' ? `<a href="javascript:void(0)" onclick="downloadFile('identityCard', '${data}')">${data}</a>` : 'Non disponible' },
                          { data: 'identityVerso', render: data => data !== 'Non disponible' ? `<a href="javascript:void(0)" onclick="downloadFile('identityCard', '${data}')">${data}</a>` : 'Non disponible' },
                          { data: 'licenseRecto', render: data => data !== 'Non disponible' ? `<a href="javascript:void(0)" onclick="downloadFile('licence', '${data}')">${data}</a>` : 'Non disponible' },
                          { data: 'licenseVerso', render: data => data !== 'Non disponible' ? `<a href="javascript:void(0)" onclick="downloadFile('licence', '${data}')">${data}</a>` : 'Non disponible' },
                          { data: null, render: data => `<button class="btn btn-danger" onclick="deleteDocument(${data.userId}, 'identity_recto')"><i class="fas fa-trash-alt"></i></button><button class="btn btn-danger" onclick="deleteDocument(${data.userId}, 'identity_verso')"><i class="fas fa-trash-alt"></i></button><button class="btn btn-danger" onclick="deleteDocument(${data.userId}, 'licence_recto')"><i class="fas fa-trash-alt"></i></button><button class="btn btn-danger" onclick="deleteDocument(${data.userId}, 'licence_verso')"><i class="fas fa-trash-alt"></i></button>` }
                      ]

            });
        })
        .catch(error => console.error('Erreur lors de la récupération des documents:', error));

    // Fetch and initialize permissions table
    fetch('/api/admin/users')
        .then(response => response.json())
        .then(data => {
            console.log('Permissions data:', data);
            $('#permissionsTable').DataTable({
                scrollY: '300px',
                scrollX: true,
                scrollCollapse: true,
                paging: false,
                info: false,
                data: data,
                columns: [
                    { data: 'photoUrl', render: data => `<img src="/uploads/profil/${data}" alt="Photo de Profil" width="50" height="50">` },
                    { data: 'nom' },
                    { data: 'prenom' },
                    { data: 'roles', render: data => data.map(role => role.role).join(', ') },
                    { data: null, render: data => `<button class="btn btn-secondary" onclick="openEditPermissionsModal(${data.id}, ${JSON.stringify(data.roles)})"><i class="fas fa-edit"></i></button>` }
                ]
            });
        })
        .catch(error => console.error('Erreur lors de la récupération des permissions:', error));

    $('#addUserForm').on('submit', function(event) {
        event.preventDefault();
        // Handle form submission for adding a user
    });

    $('#editPermissionsForm').on('submit', function(event) {
        event.preventDefault();
        const userId = $('#editUserId').val();
        const selectedPermissions = Array.from($('#permissionsCheckboxes input:checked')).map(checkbox => checkbox.value);
        updateUserPermissions(userId, selectedPermissions);
    });
});

function downloadFile(directory, fileName) {
    console.log(`Attempting to download file from directory: ${directory}, with fileName: ${fileName}`);
    fetch(`/api/files/${directory}/${fileName}`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`Erreur lors du téléchargement du fichier. Statut: ${response.status}`);
            }
            return response.blob();
        })
        .then(blob => {
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.style.display = 'none';
            a.href = url;
            a.download = fileName; // Récupère le nom du fichier
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
        })
        .catch(error => {
            console.error('Erreur lors du téléchargement du fichier:', error);
            alert('Erreur lors du téléchargement du fichier. Vérifiez la console pour plus de détails.');
        });
}

function viewUser(id) {
    alert('Voir l\'utilisateur avec l\'ID ' + id);
}

function editUser(id) {
    alert('Modifier l\'utilisateur avec l\'ID ' + id);
}

function deleteUser(userId) {
    if (confirm('Êtes-vous sûr de vouloir supprimer cet utilisateur?')) {
        console.log(`Tentative de suppression de l'utilisateur avec l'ID: ${userId}`);
        fetch(`/api/admin/users/${userId}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            },
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Erreur lors de la suppression de l'utilisateur. Statut: ${response.status}`);
            }
            alert('Utilisateur supprimé avec succès');
            location.reload(); // Recharge la page pour mettre à jour la liste des utilisateurs
        })
        .catch(error => {
            console.error('Erreur lors de la suppression de l\'utilisateur:', error);
            alert('Erreur lors de la suppression de l\'utilisateur. Vérifiez la console pour plus de détails.');
        });
    }
}

function deleteDocument(userId, documentType) {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce document?')) {
        console.log(`Tentative de suppression du document de type: ${documentType} pour l'utilisateur avec l'ID: ${userId}`);
        fetch(`/api/admin/users/${userId}/documents/${documentType}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            },
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Erreur lors de la suppression du document. Statut: ${response.status}`);
            }
            alert('Document supprimé avec succès');
            location.reload(); // Recharge la page pour mettre à jour la liste des documents
        })
        .catch(error => {
            console.error('Erreur lors de la suppression du document:', error);
            alert('Erreur lors de la suppression du document. Vérifiez la console pour plus de détails.');
        });
    }
}

function openEditPermissionsModal(userId, roles) {
    document.getElementById('editUserId').value = userId;
    fetch('/api/admin/roles') // Fetch all available roles
        .then(response => response.json())
        .then(allRoles => {
            const permissionsCheckboxes = document.getElementById('permissionsCheckboxes');
            permissionsCheckboxes.innerHTML = '';
            allRoles.forEach(role => {
                const isChecked = roles.some(userRole => userRole.role === role.role);
                const checkbox = document.createElement('div');
                checkbox.classList.add('form-check');
                checkbox.innerHTML = `
                    <input class="form-check-input" type="checkbox" value="${role.role}" id="role-${role.id}" ${isChecked ? 'checked' : ''}>
                    <label class="form-check-label" for="role-${role.id}">
                        ${role.role}
                    </label>
                `;
                permissionsCheckboxes.appendChild(checkbox);
            });
            const editPermissionsModal = new bootstrap.Modal(document.getElementById('editPermissionsModal'));
            editPermissionsModal.show();
        });
}

function updateUserPermissions(userId, roles) {
    fetch(`/api/admin/users/${userId}/permissions`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ permissions: roles })
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`Erreur lors de la mise à jour des permissions. Statut: ${response.status}`);
        }
        alert('Permissions mises à jour avec succès');
        location.reload(); // Recharge la page pour mettre à jour la liste des permissions
    })
    .catch(error => {
        console.error('Erreur lors de la mise à jour des permissions:', error);
        alert('Erreur lors de la mise à jour des permissions. Vérifiez la console pour plus de détails.');
    });
}




//--------------------ONGLET CAR ADMIN----------------------



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
            document.getElementById("car-brand").textContent = data.brand;
            document.getElementById("car-model").textContent = data.model;
            document.getElementById("car-online").textContent = data.online ? 'Oui' : 'Non';
            document.getElementById("car-first-circulation").textContent = data.firstImmatriculation ? data.firstImmatriculation : 'N/A';
            document.getElementById("car-plate").textContent = data.plaqueImmatriculation;
            document.getElementById("car-mode-reservation").textContent = data.modeReservation ? data.modeReservation : 'N/A';
            document.getElementById("car-price").textContent = data.price ? data.price.middlePrice : 'N/A';

            const photoContainer = document.getElementById("car-photos");
            data.photoUrl.forEach(photoUrl => {
                const img = document.createElement('img');
                img.src = `/uploads/${encodeURIComponent(photoUrl)}`;
                img.alt = "Photo de voiture";
                img.classList.add('car-photo', 'img-thumbnail');
                photoContainer.appendChild(img);
            });

            console.log("Carte Grise Path:", data.carteGrisePath);
            console.log("Identity Recto:", data.identityRecto);


            const carteGriseLink = document.getElementById("car-carte-grise");
            carteGriseLink.href = "javascript:void(0)";
            carteGriseLink.onclick = () => downloadFile('registrationCard', carteGriseFileName);
            carteGriseLink.textContent = "Télécharger la carte grise";

            const identityCardLink = document.getElementById("identity-card");
            identityCardLink.href = "javascript:void(0)";
            identityCardLink.onclick = () => downloadFile('identityCard', identityRectoFileName);
            identityCardLink.textContent = "Télécharger la carte d'identité";

            const featuresContainer = document.getElementById("car-features");
            data.features.forEach(feature => {
                const li = document.createElement('li');
                li.textContent = `${feature.name}: ${feature.description}`;
                featuresContainer.appendChild(li);
            });

            const equipmentsContainer = document.getElementById("car-equipments");
            data.equipments.forEach(equipment => {
                const li = document.createElement('li');
                li.innerHTML = `<img src="${equipment.icon}" alt="${equipment.equipment}" class="equipment-icon mr-2"> ${equipment.equipment}`;
                equipmentsContainer.appendChild(li);
            });

            const conditionsContainer = document.getElementById("car-conditions");
            data.conditionsDTOs.forEach(condition => {
                const li = document.createElement('li');
                li.textContent = condition.condition;
                conditionsContainer.appendChild(li);
            });
        })
        .catch(error => {
            spinner.style.display = "none";
            console.error('Erreur:', error);
        });
});






//-------------ONGLET FINANCES-------------------

  console.log("Script loaded");

document.addEventListener('DOMContentLoaded', function() {
    console.log("DOM fully loaded and parsed");

    fetch('/api/admin/payments')
        .then(response => response.json())
        .then(data => {
            console.log('Payments data:', data);
            $('#paymentsTable').DataTable({
                scrollY: '200px',
                scrollX: true,
                scrollCollapse: true,
                paging: false,
                searching: false,
                info: false,
                data: data,
                columns: [
                    { data: 'id' },
                    { data: 'prixTotal' },
                    { data: 'statut' },
                    { data: 'paiementMode' },
                    { data: 'createdAt' },
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
    const chart = new Chart(ctx, {
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
    const chart = new Chart(ctx, {
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
