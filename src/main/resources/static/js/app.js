// app.js

function showLoadingSpinner() {
    if (!document.getElementById('loading-spinner')) {
        const spinner = document.createElement('div');
        spinner.id = 'loading-spinner';
        spinner.innerHTML = `
            <div class="spinner-border" role="status">
                <span class="sr-only">Loading...</span>
            </div>
        `;
        document.body.appendChild(spinner);
    }
    document.getElementById('loading-spinner').style.display = 'block';
}

function hideLoadingSpinner() {
    const spinner = document.getElementById('loading-spinner');
    if (spinner) {
        spinner.style.display = 'none';
    }
}

async function fetchWithSpinner(url, options) {
    showLoadingSpinner();
    try {
        const response = await fetch(url, options);
        return response;
    } catch (error) {
        console.error('Erreur lors du fetch:', error);
        throw error;
    } finally {
        hideLoadingSpinner();
    }
}

document.addEventListener("DOMContentLoaded", function() {
    const spinnerStyle = document.createElement('style');
    spinnerStyle.innerHTML = `
        #loading-spinner {
            position: fixed;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            display: none;
            z-index: 9999;
        }
    `;
    document.head.appendChild(spinnerStyle);
});
