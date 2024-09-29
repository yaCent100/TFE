
  // Gestion des dates de réservation
    var reservationStartDate = document.getElementById("reservationStartDate").value;
    var reservationEndDate = document.getElementById("reservationEndDate").value;

// Fonction pour formater une date au format ISO (yyyy-MM-dd)
function formatDateToISO(dateStr) {
    const months = {
        'Jan.': '01', 'Fév.': '02', 'Mar.': '03', 'Avr.': '04', 'Mai': '05', 'Juin': '06',
        'Juil.': '07', 'Aoû.': '08', 'Sep.': '09', 'Oct.': '10', 'Nov.': '11', 'Déc.': '12'
    };

    // Nettoyage des espaces et découpage de la chaîne de date
    const dateParts = dateStr.replace(/\s+/g, ' ').trim().split(' ');

    if (dateParts.length < 4) {
        console.error("Date invalide :", dateStr);
        return null;
    }

    const day = dateParts[1];
    const month = months[dateParts[2]];
    const year = dateParts[3];

    if (!month) {
        console.error("Mois invalide :", dateParts[2]);
        return null;
    }

    // Retourner la date au format ISO 'yyyy-MM-dd'
    return `${year}-${month}-${day.padStart(2, '0')}`;
}

// Exemple d'utilisation avant la redirection vers le paiement
function redirectToPayment() {
    const reservationId = document.getElementById('reservationId').value;
    let reservationStartDate = document.getElementById('reservationStartDate').value;
    let reservationEndDate = document.getElementById('reservationEndDate').value;

    // Convertir les dates au format ISO 'yyyy-MM-dd' (déjà fait côté JS)
    reservationStartDate = formatDateToISO(reservationStartDate);
    reservationEndDate = formatDateToISO(reservationEndDate);

    // Vérifier que les dates sont valides
    if (!reservationStartDate || !reservationEndDate) {
        alert('Les dates de début et de fin sont incorrectes.');
        return;
    }

    // Redirection avec les dates correctement formatées
    const url = `/reservation/payment/${reservationId}?dateDebut=${encodeURIComponent(reservationStartDate)}&dateFin=${encodeURIComponent(reservationEndDate)}`;
    window.location.href = url;
}






    // Fonction pour annuler une réservation
    function cancelReservation(event, reservationId) {
        event.preventDefault();

        var confirmation = confirm("Êtes-vous sûr de vouloir annuler cette réservation ?");
        if (confirmation) {
            fetch(`/api/cancelReservation/${reservationId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                }
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert("La réservation a été annulée avec succès.");
                    document.getElementById('cancel-button').style.display = 'none';
                } else {
                    alert(data.message);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Une erreur s\'est produite lors de l\'annulation de la réservation.');
            });
        }
    }


    // Ouvrir le modal d'évaluation
    function openReviewModal() {
        var reviewModal = new bootstrap.Modal(document.getElementById('reviewModal'));
        reviewModal.show();
    }

        // Ouvrir le modal de réclamation

    function openClaimModal(isOwner) {
        var reservationId = getReservationIdFromUrl();
        var claimantRole = isOwner ? 'PROPRIETAIRE' : 'LOCATAIRE';
        document.getElementById('claimReservationId').value = reservationId;
        document.getElementById('claimantRole').value = claimantRole;
        var claimModal = new bootstrap.Modal(document.getElementById('claimModal'));
        claimModal.show();
    }


    // Obtenir l'ID de réservation à partir de l'URL
    function getReservationIdFromUrl() {
        var urlParts = window.location.pathname.split('/');
        return urlParts[urlParts.length - 1];
    }


document.addEventListener('DOMContentLoaded', function() {
    // Soumettre une réclamation
document.getElementById('claim-form').addEventListener('submit', function(event) {
    event.preventDefault();

    var reservationId = document.getElementById('claimReservationId').value;
    var claimantRole = document.getElementById('claimantRole').value;
    var message = document.getElementById('claimMessage').value;

    if (!message || !reservationId || !claimantRole) {
        alert("Veuillez remplir tous les champs.");
        return;
    }

    var claim = {
        message: message,
        reservationId: reservationId,
        claimantRole: claimantRole
    };

    fetch('/api/claims/submit', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(claim)
    })
    .then(response => response.json())
    .then(data => {
        alert('Votre réclamation a été envoyée avec succès.');

        // Cacher le bouton de réclamation après soumission
        var claimModal = bootstrap.Modal.getInstance(document.getElementById('claimModal'));
        claimModal.hide();

        // Cacher le bouton de soumission de réclamation
        document.getElementById('claim-button').style.display = 'none';
    })
    .catch(error => {
        console.error('Erreur:', error);
        alert('Erreur lors de l\'envoi de votre réclamation : ' + error.message);
    });
});

    // Laisser une évaluation
    document.getElementById('review-form').addEventListener('submit', function(event) {
        event.preventDefault();
        var rating = document.querySelector('input[name="rating"]:checked')?.value;
        var comment = document.getElementById('comment').value;
        const reservationId = document.getElementById('reservationId').value;

        if (!rating || !comment) {
            alert("Veuillez remplir tous les champs.");
            return;
        }

        var review = {
            note: rating,
            avis: comment,
            reservationId: reservationId
        };

        fetch('/api/review', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(review)
        })
        .then(response => response.json())
        .then(data => {
            alert('Votre évaluation a été envoyée avec succès.');
            var reviewModal = bootstrap.Modal.getInstance(document.getElementById('reviewModal'));
            reviewModal.hide();
        })
        .catch(error => {
            console.error('Erreur:', error);
            alert('Une erreur s\'est produite lors de l\'envoi de votre évaluation.');
        });
    });



});
