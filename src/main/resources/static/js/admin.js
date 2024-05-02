document.addEventListener("DOMContentLoaded", function() {
    const hamBurger = document.querySelector(".toggle-btn");

    hamBurger.addEventListener("click", function () {
        document.querySelector("#sidebar").classList.toggle("expand");
    });
});


 document.addEventListener("DOMContentLoaded", function() {
      const ctx = document.getElementById('revenueByCommuneChart').getContext('2d');
      const revenueByCommuneChart = new Chart(ctx, {
          type: 'bar',
          data: {
              labels: ['Commune A', 'Commune B', 'Commune C'], // Remplacez par les noms réels des communes
              datasets: [{
                  label: 'Rentrées d\'argent par commune',
                  data: [10000, 15000, 20000], // Remplacez par les montants réels des rentrées d'argent par commune
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
    });