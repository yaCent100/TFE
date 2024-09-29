var stompClient = null;

async function connect() {
    var socket = new SockJS('/chat-websocket');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);

        // Abonnement aux messages de chat
        subscribeToChatMessages();

        // Abonnement aux notifications
        subscribeToNotifications();
    }, onError);
}

function subscribeToChatMessages() {
    var reservationId = document.getElementById("reservationId").value;

    if (reservationId) {
        stompClient.subscribe('/topic/messages/' + reservationId, onMessageReceived);
        console.log("Subscribed to chat topic: /topic/messages/" + reservationId);
    } else {
        console.error('Reservation ID not found, cannot subscribe to chat messages.');
    }
}

function subscribeToNotifications() {
    var vehicleId = document.getElementById("vehicleId").value;

    if (vehicleId) {
        stompClient.subscribe('/topic/notifications/carId/' + vehicleId, onNotificationReceived);
        console.log("Subscribed to notifications topic: /topic/notifications/carId/" + vehicleId);
    } else {
        console.error('Vehicle ID not found, cannot subscribe to notifications.');
    }
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

        // Effacer le champ de saisie du message sans l'afficher immédiatement
        document.querySelector('#message').value = '';
    } else {
        console.log('Message content is empty or WebSocket connection not established.');
    }
}

async function fetchMessages() {
    var reservationId = document.getElementById("reservationId").value;
    console.log("Fetching messages for reservation ID:", reservationId);

    try {
        const response = await fetch(`/api/messages/reservation/${reservationId}`);
        console.log("Fetch response received:", response);

        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }

        const messages = await response.json();
        console.log("Messages fetched:", messages);
        messages.forEach(message => displayMessage(message));
    } catch (error) {
        console.error('Error fetching messages:', error);
    }
}

function displayMessage(message) {
    if (!message || !message.fromUserNom || !message.content || !message.sentAt || !message.fromUserId) {
        console.error('Message data is incomplete:', message);
        return;
    }

    // ID de l'utilisateur actuel (celui connecté)
    var currentUserId = parseInt(document.querySelector('#fromUserId').value);

    // Vérifier si l'utilisateur actuel est l'expéditeur du message
    var isSelf = (message.fromUserId === currentUserId);

    // Créez un conteneur parent pour chaque message
    var messageContainer = document.createElement('div');
    messageContainer.classList.add('message-container');
    messageContainer.style.display = 'block'; // Forcer chaque message à occuper toute la largeur

    // Ajouter une classe pour styliser si le message provient de l'utilisateur actuel ou non
    if (isSelf) {
        messageContainer.classList.add('message-self-container'); // Messages envoyés par l'utilisateur à droite
    } else {
        messageContainer.classList.add('message-other-container'); // Messages reçus à gauche
    }

    // Créez l'élément pour chaque message
    var messageElement = document.createElement('div');
    messageElement.classList.add('chat-message');

    if (isSelf) {
        messageElement.classList.add('self');
    } else {
        messageElement.classList.add('other');
    }

    // Créez le conteneur de l'image de profil
    var profileElement = document.createElement('div');
    profileElement.classList.add('message-profile');
    var profileImage = document.createElement('img');
    profileImage.src = '/uploads/profil/' + (message.profileImageUrl || 'defaultProfil.png');
    profileElement.appendChild(profileImage);

    // Créez le conteneur de contenu du message
    var contentElement = document.createElement('div');
    contentElement.classList.add('message-content');

    // Ajouter une classe spécifique au contenu du message pour différencier self et other
    if (isSelf) {
        contentElement.classList.add('self-content');
    } else {
        contentElement.classList.add('other-content');
    }

    // Affichez le nom de l'utilisateur si le message vient de quelqu'un d'autre
    if (!isSelf) {
        var userElement = document.createElement('div');
        userElement.classList.add('message-user');
        userElement.textContent = message.fromUserNom;
        contentElement.appendChild(userElement);
    }

    // Créez l'élément texte du message
    var textElement = document.createElement('div');
    textElement.classList.add('message-text');
    textElement.textContent = message.content;
    contentElement.appendChild(textElement);

    // Créez l'élément date
    var dateElement = document.createElement('div');
    dateElement.classList.add('message-date');
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

    // Ajouter la date en premier dans le messageElement
    messageElement.appendChild(dateElement);

    // Ajouter le profil et le contenu dans le conteneur message
    if (isSelf) {
        // Pour les messages self : message à gauche et image à droite
        messageElement.appendChild(contentElement);
        messageElement.appendChild(profileElement);
    } else {
        // Pour les messages other : image à gauche et message à droite
        messageElement.appendChild(profileElement);
        messageElement.appendChild(contentElement);
    }

    // Ajouter le message au conteneur parent
    messageContainer.appendChild(messageElement);

    // Ajouter le conteneur parent au chat
    var chatArea = document.querySelector('.chat-area');
    if (chatArea) {
        chatArea.appendChild(messageContainer);
        chatArea.scrollTop = chatArea.scrollHeight;
    } else {
        console.error('Element with class .chat-area not found');
    }
}







let receivedMessageIds = new Set();

function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);
    console.log("Message reçu via WebSocket:", message); // Ajoutez ce log pour voir le message reçu.

    // Vérifiez que les informations du message sont correctes
    if (!message || !message.content || !message.fromUserId || !message.toUserId) {
        console.error('Message data is incomplete or incorrect:', message);
        return;
    }

    displayMessage(message);
}


function onNotificationReceived(payload) {
    var notification = JSON.parse(payload.body);
    console.log("Notification received: ", notification);
    // Handle the notification here
}

document.addEventListener('DOMContentLoaded', () => {
    console.log("Document loaded. Connecting to WebSocket...");

    connect();  // Initialize WebSocket connection

    var chatForm = document.querySelector('#chat-form');
    if (chatForm) {
        chatForm.addEventListener('submit', sendMessage, true);
    }

    fetchMessages();  // Fetch messages when the document is loaded
});

/*--------------- NOTIFICATIONS ----------*/
document.getElementById('sendNotificationButton').addEventListener('click', function () {
    var message = document.getElementById('notification-message').value;
    var toUserId = document.getElementById('toUserId').value;
    var fromUserId = document.getElementById('fromUserId').value;
    var vehicleId = document.getElementById('vehicleId').value;

    if (message && stompClient) {
        var notification = {
            toUserId: toUserId,
            fromUserId: fromUserId,
            carId: vehicleId,
            message: message,
            type: 'Message'
        };

        stompClient.send("/app/notification/carId/" + vehicleId, {}, JSON.stringify(notification));

        $('#messageModal').modal('hide');
        document.getElementById('notification-form').reset();
    } else {
        console.log('Notification content is empty or WebSocket connection not established.');
    }
});