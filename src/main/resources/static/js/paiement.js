document.addEventListener("DOMContentLoaded", function() {
    console.log("Document loaded, initializing Stripe...");
    const stripe = Stripe("pk_test_51PW22EEexTXMsSYZV6f9JMDwcyPFmYyjTm1B4ryyB7dNMUMZ4zsAGWClCvie4ECL7GfSnQvHbNO2VkVEV3Z63PpJ00Tl6Tj5P3");
    let elements;

    async function waitForElements() {
        while (true) {
            const paymentForm = document.querySelector("#payment-form");
            const amountElement = document.querySelector("#amount");
            const reservationIdElement = document.querySelector("#reservation-id");
            const insuranceElement = document.querySelector("#selected-insurance"); // Assurez-vous que l'ID est correct
            const submitBtn = document.querySelector("#submit");

            if (paymentForm && amountElement && reservationIdElement && insuranceElement && submitBtn) {
                return { paymentForm, amountElement, reservationIdElement, insuranceElement, submitBtn };
            }

            console.log("Waiting for elements to load...");
            await new Promise(requestAnimationFrame);
        }
    }

    async function initializeStripe() {
        const { paymentForm, amountElement, reservationIdElement, insuranceElement, submitBtn } = await waitForElements();

        console.log("Elements loaded:", { paymentForm, amountElement, reservationIdElement, insuranceElement, submitBtn });

        if (!paymentForm || !amountElement || !reservationIdElement || !insuranceElement || !submitBtn) {
            console.error("Form or required elements are not found in the DOM.");
            return;
        }

        initialize(amountElement, reservationIdElement, insuranceElement);

        paymentForm.addEventListener("submit", async (event) => handleSubmit(event, amountElement, reservationIdElement, insuranceElement, submitBtn));
    }

    initializeStripe();

    async function initialize(amountElement, reservationIdElement, insuranceElement) {
        try {
            const totalAmount = amountElement.value;
            const reservationId = reservationIdElement.value;

            console.log("Total Amount: ", totalAmount);
            console.log("Reservation ID: ", reservationId);

            if (!totalAmount || !reservationId) {
                throw new Error("Amount or reservation ID is missing.");
            }

            const response = await fetch("/api/payments/create-payment-intent", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ amount: totalAmount, currency: 'eur', reservationId: reservationId }),
            });

            if (!response.ok) {
                const errorText = await response.text();
                console.error("Error response:", errorText);
                throw new Error(`HTTP error! status: ${response.status}, message: ${errorText}`);
            }

            const result = await response.json();
            const clientSecret = result.clientSecret;

            console.log("Payment Intent Client Secret: ", clientSecret);

            const appearance = {
                theme: 'stripe',
                variables: {
                    colorPrimary: '#00BFFF',
                    colorBackground: '#DFE7EC',
                    colorText: '#9EABB3',
                    colorDanger: '#df1b41',
                    fontFamily: 'Ideal Sans, system-ui, sans-serif',
                    spacingUnit: '2px',
                    borderRadius: '4px',
                    // See all possible variables below
                }
            };
            elements = stripe.elements({ appearance, clientSecret });

            const paymentElementOptions = { layout: "tabs" };
            const paymentElement = elements.create("payment", paymentElementOptions);
            paymentElement.mount("#payment-element");
        } catch (error) {
            console.error("Error during initialization:", error);
            showMessage("An error occurred during initialization: " + error.message);
        }
    }

    const handleError = (error) => {
        const messageContainer = document.querySelector('#error-message');
        messageContainer.textContent = error.message;
        const submitBtn = document.querySelector("#submit");
        if (submitBtn) submitBtn.disabled = false;
    }

    async function handleSubmit(event, amountElement, reservationIdElement, insuranceElement, submitBtn) {
        event.preventDefault();

        if (submitBtn.disabled) {
            return;
        }

        submitBtn.disabled = true;

        console.log("Submitting payment...");

        const { error: submitError } = await elements.submit();
        if (submitError) {
            console.error("Submit error:", submitError);
            handleError(submitError);
            return;
        }

        console.log("Fetching client secret for payment confirmation...");
        const totalAmount = amountElement.value;
        const reservationId = reservationIdElement.value;
        const insurance = insuranceElement.value; // Assurez-vous que cette ligne utilise bien `insuranceElement`

        const response = await fetch("/api/payments/create-payment-intent", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ amount: totalAmount, currency: 'eur', reservationId: reservationId }),
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error("Error response while fetching client secret:", errorText);
            handleError(new Error(`HTTP error! status: ${response.status}, message: ${errorText}`));
            return;
        }

        const { clientSecret, paymentIntentId } = await response.json();

        console.log("Confirming payment with Stripe...");
        const returnUrl = new URL('http://localhost:8080/checkout');
        returnUrl.searchParams.set('amount', totalAmount);
        returnUrl.searchParams.set('reservationId', reservationId);
        returnUrl.searchParams.set('paymentIntentId', paymentIntentId);
        returnUrl.searchParams.set('insurance', insurance);

        const { error } = await stripe.confirmPayment({
            elements,
            clientSecret,
            confirmParams: {
                return_url: returnUrl.toString(),
            },
        });

        if (error) {
            console.error("Error during payment confirmation:", error);
            handleError(error);
        }
    }

    function showMessage(messageText) {
        const messageContainer = document.querySelector("#payment-message");
        if (messageContainer) {
            messageContainer.classList.remove("hidden");
            messageContainer.textContent = messageText;

            setTimeout(() => {
                messageContainer.classList.add("hidden");
                messageContainer.textContent = "";
            }, 4000);
        }
    }
});
