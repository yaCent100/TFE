var stompClient = null;
var reservationId = 186; // Assurez-vous que cette valeur est définie correctement

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
    var messageContent = document.querySelector('#message').value.trim();
    var fromUserId = document.querySelector('#fromUserId').value;
    var toUserId = document.querySelector('#toUserId').value;

    console.log("Sending message. fromUserId: ", fromUserId);
    console.log("Sending message. toUserId: ", toUserId);

    if (messageContent && stompClient) {
        var chatMessage = {
            content: messageContent,
            fromUserId: fromUserId,  // Assurez-vous d'utiliser fromUserId et toUserId
            toUserId: toUserId,
            reservationId: reservationId
        };

        stompClient.send("/app/chat/" + reservationId, {}, JSON.stringify(chatMessage));
        console.log("Message sent: ", chatMessage);
        document.querySelector('#message').value = '';
    }
    event.preventDefault();
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
    var messageElement = document.createElement('p');
    messageElement.classList.add('chat-message');
    var textElement = document.createElement('span');
    var messageText = document.createTextNode(message.fromUserNom + ": " + message.content);
    textElement.appendChild(messageText);
    messageElement.appendChild(textElement);
    var chatArea = document.querySelector('.chat-area');
    chatArea.appendChild(messageElement);
    chatArea.scrollTop = chatArea.scrollHeight;
}

function onMessageReceived(payload) {
    console.log("Message received: ", payload);
    var message = JSON.parse(payload.body);
    console.log("Parsed message: ", message);

    if (!message.fromUserNom) {
        console.error("fromUserNom is undefined", message);
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
