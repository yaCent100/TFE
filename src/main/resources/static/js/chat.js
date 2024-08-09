var stompClient = null;


async function connect() {
    var socket = new SockJS('/chat-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, onConnected, onError);
}

function onConnected() {
    stompClient.subscribe('/topic/messages/' + reservationId, onMessageReceived);
    console.log("Connected to WebSocket server. Subscription to topic/messages/" + reservationId);
}

function onError(error) {
    console.log('Could not connect to WebSocket server. Please refresh this page to try again!', error);
}

function sendMessage(event) {
    event.preventDefault(); // Empêche le rechargement de la page

    var messageContent = document.querySelector('#message').value.trim();
    var fromUserId = document.querySelector('#fromUserId').value;
    var toUserId = document.querySelector('#toUserId').value;
    var reservationId = document.getElementById("reservationId").value;

    if (messageContent && stompClient) {
        var chatMessage = {
            content: messageContent,
            fromUserId: fromUserId,
            toUserId: toUserId,
            reservationId: reservationId
        };

        stompClient.send("/app/chat/" + reservationId, {}, JSON.stringify(chatMessage));
        console.log("Message sent: ", chatMessage);

        // Afficher immédiatement le message dans l'UI
        displayMessage({
            fromUserNom: 'Vous',
            content: messageContent,
            sentAt: new Date().toISOString()
        });

        document.querySelector('#message').value = ''; // Efface le champ de saisie du message
    } else {
        console.log('Message content is empty or WebSocket connection not established.');
    }
}

async function fetchMessages() {
    try {
        const response = await fetch(`/api/messages/reservation/${reservationId}`);
        const messages = await response.json();
        messages.forEach(message => displayMessage(message));
    } catch (error) {
        console.error('Error fetching messages:', error);
    }
}
function displayMessage(message) {
    // Vérifiez si le message contient les informations nécessaires
    if (!message || !message.fromUserNom || !message.content || !message.sentAt) {
        console.error('Message data is incomplete:', message);
        return;
    }

    // Créez l'élément du message
    var messageElement = document.createElement('div');
    messageElement.classList.add('chat-message');

    // Créez et ajoutez l'image de profil
    var profileElement = document.createElement('div');
    profileElement.classList.add('message-profile');
    var profileImage = document.createElement('img');
    profileImage.src = message.profileImageUrl || 'uploads/profil/defaultPhoto.png'; // Utilisez une URL d'image par défaut si aucune image n'est fournie
    profileElement.appendChild(profileImage);

    // Créez et ajoutez le contenu du message
    var contentElement = document.createElement('div');
    contentElement.classList.add('message-content');

    var userElement = document.createElement('div');
    userElement.classList.add('message-user');
    userElement.textContent = message.fromUserNom || 'Unknown'; // Affichez 'Unknown' si le nom de l'utilisateur est manquant

    var textElement = document.createElement('div');
    textElement.classList.add('message-text');
    textElement.textContent = message.content || 'No content'; // Affichez 'No content' si le contenu du message est manquant

    var dateElement = document.createElement('div');
    dateElement.classList.add('message-date');

    // Formatez la date du message
    var messageDate = new Date(message.sentAt);
    if (isNaN(messageDate.getTime())) {
        console.error('Invalid Date:', message.sentAt);
        dateElement.textContent = 'Invalid Date';
    } else {
        var formattedDate = messageDate.toLocaleString('fr-FR', {
            weekday: 'long', day: '2-digit', month: 'long', year: 'numeric', hour: '2-digit', minute: '2-digit'
        });
        dateElement.textContent = formattedDate;
    }

    // Ajoutez les éléments au message
    contentElement.appendChild(userElement);
    contentElement.appendChild(textElement);

    messageElement.appendChild(profileElement);
    messageElement.appendChild(contentElement);
    messageElement.appendChild(dateElement);

    // Ajoutez le message à la zone de chat
    var chatArea = document.querySelector('.chat-area');
    if (chatArea) {
        chatArea.appendChild(messageElement);
        chatArea.scrollTop = chatArea.scrollHeight; // Faites défiler vers le bas
    } else {
        console.error('Element with class .chat-area not found');
    }
}


function onMessageReceived(payload) {
    console.log("Message received: ", payload);
    var message = JSON.parse(payload.body);
    console.log("Parsed message: ", message);

    if (!message.fromUserNom || !message.content || !message.sentAt) {
        console.error("Missing required message fields:", message);
        return;
    }

    displayMessage(message);
}

document.addEventListener('DOMContentLoaded', (event) => {
    console.log("Document loaded. Connecting to WebSocket...");
    connect();
    document.querySelector('#chat-form').addEventListener('submit', sendMessage, true);
    fetchMessages();  // Fetch messages when the document is loaded
});


/*--------------- NOTIFICATIONS ----------*/
// Establish WebSocket connection
var socket = new SockJS('/chat-websocket');
var stompClient = Stomp.over(socket);

stompClient.connect({}, function (frame) {
    console.log('Connected: ' + frame);

    document.getElementById('sendNotificationButton').addEventListener('click', function () {
        var message = document.getElementById('notification-message').value;
        var toUserId = document.getElementById('toUserId').value;
        var fromUserId = document.getElementById('fromUserId').value;
        var vehicleId = document.getElementById('vehicleId').value;

        var notification = {
            toUserId: toUserId,
            fromUserId: fromUserId,
            carId: vehicleId,
            message: message,
            type: 'Message'
        };

        stompClient.send("/app/notification/carId/" + vehicleId, {}, JSON.stringify(notification));

        // Fermer le modal après l'envoi en utilisant jQuery
        $('#messageModal').modal('hide');

        // Réinitialiser le formulaire
        document.getElementById('notification-form').reset();
    });
});






