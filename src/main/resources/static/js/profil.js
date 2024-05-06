$(document).ready(function() {
    // Sélectionne tous les liens d'onglet et leur associe un gestionnaire de clic
    $('.nav-profil a').click(function(e) {
        e.preventDefault();  // Empêche le navigateur de suivre le lien

        // Récupère l'ID du contenu de l'onglet à partir de l'attribut data-target du lien cliqué
        var targetId = $(this).attr('data-target');  // Utilisez attr au lieu de data si data ne fonctionne pas

        // Retire la classe 'active' de tous les contenus d'onglets
        $('.tab-content').removeClass('active');

        // Ajoute la classe 'active' au contenu de l'onglet ciblé
        $('#' + targetId).addClass('active');

        // Retire la classe 'active' de tous les liens d'onglet
        $('.nav-profil a').removeClass('active');

        // Ajoute la classe 'active' au lien d'onglet qui a été cliqué
        $(this).addClass('active');
    });
});



/* MODIFIER LA PHOTO DE PROFIL*/
$(document).ready(function() {
    $('#editPictureLink').click(function(e) {
        e.preventDefault();  // Empêche le navigateur de suivre le lien
        $('#pictureInput').click();  // Déclenche le clic sur l'input
    });
});