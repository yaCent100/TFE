
/*FORMULAIRE STEP*/
document.addEventListener("DOMContentLoaded", function() {
  const nextButtons = document.querySelectorAll('.btn-next');
  const prevButtons = document.querySelectorAll('.btn-prev');
  const formSteps = document.querySelectorAll('.form-step');
  const circles = document.querySelectorAll('.circle');
  const lines = document.querySelectorAll('.line');

 if (!nextButtons.length || !formSteps.length || !circles.length) {
    console.error("Some elements are missing, check the HTML structure and Thymeleaf fragments.");
    return; // Stop the function if required elements are missing
  }

  let currentStep = 0;

  function updateProgress() {
    circles.forEach(circle => circle.classList.remove('active'));
    lines.forEach(line => line.classList.remove('active'));
    for (let i = 0; i <= currentStep; i++) {
      circles[i].classList.add('active');
      if (lines[i]) {
        lines[i].classList.add('active');
      }
    }
  }

  function addInnerCircle(step) {
    const currentCircle = circles[step];
    if (!currentCircle.querySelector('.inner-circle')) {
      const innerCircle = document.createElement('div');
      innerCircle.classList.add('inner-circle');
      currentCircle.appendChild(innerCircle);
    }
  }

  function addCheckmark(step) {
    const prevStep = step - 1;
    if (prevStep >= 0) {
      const prevCircle = circles[prevStep];
      const prevInnerCircle = prevCircle.querySelector('.inner-circle');
      if (prevInnerCircle) {
        prevInnerCircle.innerHTML = '✔'; // Ajoutez le symbole de validation
      }
    }
  }

  function removeInnerCircle(step) {
    const currentCircle = circles[step];
    const innerCircle = currentCircle.querySelector('.inner-circle');
    if (innerCircle) {
      innerCircle.remove(); // Supprimez le inner-circle
    }
  }

  function removeCheckmark(step) {
    const prevStep = step + 1;
    if (prevStep < circles.length) {
      const prevCircle = circles[prevStep];
      const prevInnerCircle = prevCircle.querySelector('.inner-circle');
      if (prevInnerCircle) {
        prevInnerCircle.innerHTML = ''; // Supprimez le symbole de validation
      }
    }
  }

  nextButtons.forEach(button => {
    button.addEventListener('click', function() {
      if (currentStep < formSteps.length - 1) {
        removeCheckmark(currentStep); // Supprimez le symbole de validation de l'étape précédente
        formSteps[currentStep].classList.remove('active');
        currentStep++;
        formSteps[currentStep].classList.add('active');
        addCheckmark(currentStep); // Ajoutez le symbole de validation pour l'étape actuelle
        addInnerCircle(currentStep); // Ajoutez le inner-circle pour l'étape suivante
        updateProgress();
      }
    });
  });

  prevButtons.forEach(button => {
    button.addEventListener('click', function() {
      if (currentStep > 0) {
        formSteps[currentStep].classList.remove('active');
        currentStep--;
        formSteps[currentStep].classList.add('active');
        removeCheckmark(currentStep + 1); // Supprimez le symbole de validation de l'étape actuelle
        removeInnerCircle(currentStep + 1); // Supprimez le inner-circle de l'étape actuelle
        updateProgress();
      }
    });
  });

  // Ajoutez d'abord le inner-circle au chargement de la page
  addInnerCircle(currentStep);

  updateProgress();
});




/* AJOUTER UNE VOITURE*/
/* MARQUES */

document.addEventListener('DOMContentLoaded', function() {
    const brandSelect = document.getElementById('marque');
    const modelSelect = document.getElementById('modele');

    // Vérifiez si les éléments existent avant de continuer
    if (!brandSelect || !modelSelect) {
        console.error('Les éléments select de marque ou de modèle ne sont pas trouvés dans le DOM');
        return;
    }

    // Gestionnaire d'événement pour le changement de marque
    brandSelect.onchange = function() {
        fetchModels(brandSelect.value); // Appel pour obtenir les modèles basés sur la marque sélectionnée
    };

    // Fonction pour charger les marques
    fetchBrands();

    // Fonction pour obtenir les marques
    function fetchBrands() {
        const url = '/brands';
        fetch(url)
            .then(response => response.json())
            .then(brands => {
                brands.forEach(brand => {
                    const option = new Option(brand, brand);
                    brandSelect.appendChild(option);
                });
            })
            .catch(error => console.error('Erreur lors de la récupération des marques:', error));
    }

    // Fonction pour obtenir les modèles basés sur la marque sélectionnée
    function fetchModels(brand) {
        const url = `/models?brand=${brand}`;
        fetch(url)
            .then(response => response.json())
            .then(models => {
                modelSelect.innerHTML = ''; // Nettoyer les options existantes
                models.forEach(model => {
                    const option = new Option(model, model);
                    modelSelect.appendChild(option);
                });
            })
            .catch(error => console.error(`Erreur lors de la récupération des modèles pour la marque ${brand}:`, error));
    }
});

/* INPUT REQUIRED */
document.addEventListener('DOMContentLoaded', function() {
    const submitButton = document.querySelector('#btn-next-step2');

    submitButton.addEventListener('click', function(event) {
        // Sélectionnez tous les champs requis dans le formulaire actuel
        const form = document.querySelector('.form-step');
        const requiredFields = form.querySelectorAll('[required]');

        // Vérifiez si tous les champs sont remplis et valides
        const allValid = Array.from(requiredFields).every(field => field.checkValidity());

        if (!allValid) {
            event.preventDefault();  // Empêcher la progression si le formulaire n'est pas valide
            alert('Veuillez remplir tous les champs requis pour continuer.');
            // Déclenchez manuellement la validation pour afficher les bulles de validation HTML5
            form.reportValidity();
        }
    });
});




/* ------------------- FORMULAIRE ---------------- */

/* VEHICULE */

$(document).ready(function() {
    $('.equipment-item').click(function() {
        var checkbox = $(this).find('input[type="checkbox"]');
        checkbox.prop('checked', !checkbox.prop('checked'));
        $(this).toggleClass('checked', checkbox.prop('checked'));
    });
});

document.getElementById('addConditionButton').addEventListener('click', function () {
        var textarea = document.getElementById('conditionTextarea');
        var conditionText = textarea.value.trim();
        var conditionsList = document.getElementById('conditionsList');
        var inputRow = document.getElementById('inputRow');

        if (conditionText !== "" && conditionsList.children.length < 4) {
            var conditionCol = document.createElement('div');
            conditionCol.className = 'col-lg-3 col-md-4 col-sm-6 mb-2';

            var conditionItem = document.createElement('div');
            conditionItem.className = 'list-group-item d-flex justify-content-between align-items-center';
            conditionItem.textContent = conditionText;

            var hiddenInput = document.createElement('input');
            hiddenInput.type = 'hidden';
            hiddenInput.name = 'conditions';
            hiddenInput.value = conditionText;

            var removeBtn = document.createElement('button');
            removeBtn.className = 'btn btn-sm btn-danger';
            removeBtn.innerHTML = '&times;';
            removeBtn.addEventListener('click', function () {
                conditionsList.removeChild(conditionCol);
                if (conditionsList.children.length < 4) {
                    inputRow.style.display = 'flex';
                }
            });

            conditionItem.appendChild(removeBtn);
            conditionCol.appendChild(conditionItem);
            conditionCol.appendChild(hiddenInput);
            conditionsList.appendChild(conditionCol);

            textarea.value = "";

            if (conditionsList.children.length >= 4) {
                inputRow.style.display = 'none';
            }
        }
    });

/* PHOTOS */
function previewImage(input, index) {
    if (input.files && input.files[0]) {
        var reader = new FileReader();
        reader.onload = function (e) {
            document.getElementById('img-' + index).src = e.target.result;
            document.getElementById('link-remove-' + index).style.display = 'block';
        };
        reader.readAsDataURL(input.files[0]);
    }
}

function removeImage(index) {
    var img = document.getElementById('img-' + index);
    var fileInput = document.getElementById('file-upload-' + index);
    var removeLink = document.getElementById('link-remove-' + index);

    // Réinitialiser l'image par défaut
    img.src = '/images/picture-icon.png';
    // Masquer le lien Supprimer
    removeLink.style.display = 'none';
    // Réinitialiser l'input file
    fileInput.value = '';
}

function triggerFileUpload(index) {
    console.log('Attempting to trigger file upload for index:', index);
    var fileInput = document.getElementById('file-upload-' + index);
    console.log('Element:', fileInput);
    if (fileInput) {
        fileInput.click();
    } else {
        console.error('No input found for index:', index);
    }
}

/* CARTE GRISE*/

function handleFileUpload(input) {
    if (input.files && input.files[0]) {
        var reader = new FileReader();

        reader.onload = function (e) {
            // On vérifie l'extension du fichier pour décider quoi afficher
            var fileType = input.files[0].type.split('/').pop().toLowerCase();
            var previewImage = document.querySelector('.no-picture-2');
            if (fileType === 'pdf') {
                previewImage.src = '/images/pdf-icon.png'; // Icône pour un PDF
            } else {
                previewImage.src = e.target.result; // Montre l'image uploadée
            }
            // Masquer la zone de téléchargement
            document.getElementById('dropzone-registration-card').style.display = 'none';
            // Afficher le message de succès
            document.getElementById('upload-success-message').style.display = 'block';
        };

        reader.readAsDataURL(input.files[0]);
    }
}


/* IDENTITY */
function previewIdentity(input, side) {
    if (input.files && input.files[0]) {
        var reader = new FileReader();
        reader.onload = function (e) {
            document.getElementById('img-' + side).src = e.target.result;
            // Masquer la zone de téléchargement après le chargement de l'image
            input.parentElement.style.display = 'none';

            // Mise à jour des messages en fonction du côté
            if (side === 'recto') {
                document.getElementById('upload-success-' + side).style.display = 'block';
                document.getElementById('upload-success-' + side).textContent = 'Ok, en attente du verso!';
            } else if (side === 'verso') {
                document.getElementById('upload-success-verso').style.display = 'block';
                document.getElementById('upload-success-verso').textContent = 'Document complet!';

                // Afficher le message général pour toute la section d'identité une fois le verso chargé
                updateDocumentIdentitySection();
            }
        };
        reader.readAsDataURL(input.files[0]);
    }
}

function updateDocumentIdentitySection() {
    // Cacher les zones individuelles de téléchargement
    document.getElementById('id-document-recto-container').style.display = 'none';
    document.getElementById('id-document-verso-container').style.display = 'none';

    // Afficher un message général pour toute la section
    var container = document.querySelector('.document-identity');
    container.innerHTML = '<div class="alert alert-success fs16 text-center" style="width: 100%;">Document envoyé!</div>';
}







 const brusselsData = {
        "1000": "Bruxelles",
        "1020": "Laeken",
        "1030": "Schaerbeek",
        "1040": "Etterbeek",
        "1050": "Ixelles",
        "1060": "Saint-Gilles",
        "1070": "Anderlecht",
        "1080": "Molenbeek-Saint-Jean",
        "1081": "Koekelberg",
        "1082": "Berchem-Sainte-Agathe",
        "1083": "Ganshoren",
        "1090": "Jette",
        "1120": "Neder-Over-Heembeek",
        "1130": "Haren",
        "1140": "Evere",
        "1150": "Woluwe-Saint-Pierre",
        "1160": "Auderghem",
        "1170": "Watermael-Boitsfort",
        "1180": "Uccle",
        "1190": "Forest",
        "1200": "Woluwe-Saint-Lambert",
        "1210": "Saint-Josse-ten-Noode"
    };

    document.addEventListener("DOMContentLoaded", function() {
        const codePostalSelect = document.getElementById("codePostalSelect");
        const communeSelect = document.getElementById("communeSelect");

        for (const [code, commune] of Object.entries(brusselsData)) {
            const optionCode = document.createElement("option");
            optionCode.value = code;
            optionCode.text = code;
            codePostalSelect.appendChild(optionCode);

            const optionCommune = document.createElement("option");
            optionCommune.value = commune;
            optionCommune.text = commune;
            communeSelect.appendChild(optionCommune);
        }

        codePostalSelect.addEventListener("change", function() {
            const selectedCode = codePostalSelect.value;
            communeSelect.value = brusselsData[selectedCode] || "";
        });

        communeSelect.addEventListener("change", function() {
            const selectedCommune = communeSelect.value;
            for (const [code, commune] of Object.entries(brusselsData)) {
                if (commune === selectedCommune) {
                    codePostalSelect.value = code;
                    break;
                }
            }
        });
    });




document.getElementById("monBouton").addEventListener("click", function() {
    document.getElementById("carForm").submit(); // Assurez-vous que l'ID est correct
});


/* FORMULAIRE MODE RESERVATION*/
$(document).ready(function() {
    // Lorsque l'utilisateur souhaite examiner chaque demande
    $("#show-manual-booking-condition").click(function() {
        $("#reservation-auto").hide(); // Cache la première div
        $("#reservation-confirmation").show(); // Montre la deuxième div pour la confirmation
    });

    // Lorsque l'utilisateur confirme les conditions
    $("#next-button").click(function() {
        // Assurez-vous que toutes les cases sont cochées
        if ($("input[name='check_1']").is(":checked") && $("input[name='check_2']").is(":checked")) {
            $("#reservation-confirmation").hide(); // Cache la deuxième div
            $("#reservation-manu").show(); // Montre la troisième div, le texte pour continuer le processus
        } else {
            alert("Veuillez cocher toutes les conditions avant de continuer.");
        }
    });

    // Si l'utilisateur souhaite activer la réservation instantanée
    $("#choose-automatic-booking").click(function() {
        $("#reservation-manu").hide(); // Cache la troisième div
        $("#reservation-auto").show(); // Montre la première div
    });
});


function submitForm(mode) {
    document.getElementById('modeReservation').value = mode; // Met à jour la valeur du mode
}


/* FORM PRIX */

