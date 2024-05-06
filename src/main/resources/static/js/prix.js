function afficherJaugePrix(categorie) {
  let prixMin, prixMax;

  // Déterminer les plages de prix en fonction de la catégorie sélectionnée
  switch (categorie) {
    case 'citadine':
      prixMin = 45;
      prixMax = 60;
      break;
    case 'berline':
      prixMin = 60;
      prixMax = 80;
      break;
    case 'break':
      prixMin = 80;
      prixMax = 95;
      break;
    case 'monospace':
      prixMin = 95;
      prixMax = 120;
      break;
    case 'suv':
      prixMin = 120;
      prixMax = 150; // Juste un exemple, ajustez selon vos besoins
      break;
    default:
      // Par défaut, mettre les prix entre 0 et 200 (à adapter selon vos besoins)
      prixMin = 0;
      prixMax = 200;
      break;
  }

  // Mettre à jour la valeur minimale et maximale de la jauge
  document.getElementById('myRange').setAttribute('min', prixMin);
  document.getElementById('myRange').setAttribute('max', prixMax);

  // Réinitialiser la valeur de la jauge à la valeur minimale
  document.getElementById('myRange').value = prixMin;

  // Mettre à jour l'affichage des plages de prix
  document.getElementById('plage-prix').innerText = prixMin + ' - ' + prixMax;

  // Mettre à jour la valeur actuelle de la jauge
  document.getElementById('valeur').innerText = prixMin;
}

// Écouter les changements de la jauge de prix
document.getElementById('myRange').addEventListener('input', function() {
  // Mettre à jour la valeur affichée
  document.getElementById('valeur').innerText = this.value;
});
