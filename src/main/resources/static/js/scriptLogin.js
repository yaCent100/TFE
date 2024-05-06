

/*FORMULAIRE STEP*/
document.addEventListener("DOMContentLoaded", function() {
  const nextButtons = document.querySelectorAll('.btn-next');
  const prevButtons = document.querySelectorAll('.btn-prev');
  const formSteps = document.querySelectorAll('.form-step');
  const circles = document.querySelectorAll('.circle');
  const lines = document.querySelectorAll('.line');

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





