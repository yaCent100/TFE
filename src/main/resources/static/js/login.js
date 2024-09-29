
document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('signin-form').addEventListener('submit', async (event) => {
        event.preventDefault(); // Empêche la soumission par défaut du formulaire

        const username = document.getElementById('email-input').value;
        const password = document.getElementById('password-input-connexion').value;

        try {
            const data = await authenticateUser(username, password);
            const { jwt } = data;

            if (jwt) {
                localStorage.setItem('jwtToken', jwt);
                window.location.href = '/home'; // Redirection après la connexion
            } else {
                showAlert('Authentication failed');
            }
        } catch (error) {
            console.error('Error during authentication:', error);
            showAlert('Authentication failed. Please check your credentials and try again.');
        }
    });
});

function showAlert(message) {
    const alertMessage = document.getElementById('alert-message');
    const alertContent = document.getElementById('alert-message-content');
    alertContent.textContent = message;
    alertMessage.classList.remove('hide');
}




