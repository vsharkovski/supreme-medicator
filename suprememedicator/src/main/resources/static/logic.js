
// Test script to demonstrate how to use API
const predefSymptoms =  [
    "I have a slight headache",
    "My head feels like it's pounding",
    "There's a throbbing sensation in my head",
    "I'm experiencing dizziness",
    "I'm feeling nauseous",
    "My stomach is upset",
    "I'm experiencing fatigue",
    "I'm having trouble breathing",
    "I'm achy all over",
    "I have a persistent cough",
    "I feel feverish",
    "I'm sweating excessively",
    "I have a runny nose",
    "I'm struggling to sleep",
    "I'm feeling anxious",
    "There's tightness in my chest",
    "My vision is blurry",
    "I've lost my appetite",
    "I'm feeling irritable",
    "I have muscle cramps",
    "I've got a sore throat",
    "I'm feeling lightheaded",
    "My joints ache",
    "There's a tingling sensation in my limbs",
    "My heart is racing"
]

const base = 'http://localhost:8080/api/';

async function apiCall(endpoint, params) {
    const paramsString = new URLSearchParams(params);
    return fetch(base + endpoint + '?' + paramsString, {
        method: 'GET',
        headers: {'Content-Type': 'application/json'}
    }).then((response) => {
        if (!response.ok) {
            throw new Error(`Bad response: ${response.status} ${response.statusText}`);
        }
        return response.json();
    }).catch((error) => {
        buildErrorPage(error)
    })
}


function shutAllPages(){
    const pages = document.querySelectorAll('.page');
    pages.forEach((page) => {
        page.classList.add('hidden');
    })
}


function buildLandingPage(){
    shutAllPages();
    const landingPage = document.querySelector('#landing-page');
    landingPage.classList.remove('hidden');
    const userInput = document.querySelector('#user-input');
    const userInputSubmitButton = document.querySelector('#user-input-submit');
    const suggestions = document.querySelector('#suggested-symptoms-list');

    suggestions.innerHTML = '';
    for (let i = 0; i < 3; i++) {
        const suggestion = document.createElement('li');
        const idx = Math.floor(Math.random() * predefSymptoms.length);
        suggestion.textContent = predefSymptoms[idx];
        suggestion.classList.add('suggested-symptoms');
        suggestions.appendChild(suggestion);

        suggestion.addEventListener('click', () => {
            console.log('Suggestion clicked:', suggestion.textContent);
            userInput.value = suggestion.textContent;
        })
    }

    userInputSubmitButton.addEventListener('click', () => {
        apiCall('extract_symptoms/from_input', {input: userInput.value}).then((response) => {
            buildResultsPage(response['symptoms']);
        }).catch((error) => {
            console.log(error);
        })
    })
}

function buildErrorPage(error){
    shutAllPages();
    const errorPage = document.querySelector('#error-page');
    errorPage.classList.remove('hidden');
    const errorParagraph = document.querySelector('#error-message');
    errorParagraph.textContent = error;
    const errorButton = document.querySelector('#error-button');
    errorButton.addEventListener('click', () => {
        buildLandingPage();
    })
}

function buildResultsPage(symptoms){
    shutAllPages();
    const resultsPage = document.querySelector('#results-page');
    resultsPage.classList.remove('hidden');
    const userSymptoms = document.querySelector('#user-symptoms');
    const symptomsExplanation = document.querySelector('#symptoms-explanation');

    userSymptoms.textContent = "You are experiencing: " + symptoms.join(', ');
    
    const submitButton = document.querySelector('#filter-submit');

    apiCall('explain_symptoms/from_symptoms', {symptoms: symptoms.join(',')}).then((response) => {
        console.log(response);
        textTyping(symptomsExplanation, response['explanation'], 15)
    }).catch((error) => {
        console.log(error)
    })

    apiCall('medicines/for_symptoms', {symptoms: symptoms.join(',')}).then((response) => {
        const medicinesList = document.querySelector('#medicines-list');
        response['medicines'].forEach((medicine) => { 
            medicinesList.appendChild(buildMedicine(medicine));
        })

        submitButton.addEventListener('click', () => {
            const name_filter = document.querySelector('#name-filter').value
            const low_price = document.querySelector('#price-min-filter').value
            const high_price = document.querySelector('#price-max-filter').value
            const medicinesWithResult = new Set([]);

            document.querySelectorAll('.product-card').forEach((product) => {
                    let productName, productType, productDosage, productPrice;
                    [productName, productType, productDosage, productPrice] = product.children;  
                    productName = productName.textContent.toLowerCase();
                    productPrice = productPrice.textContent.split(' ')[0];
                    if ((name_filter === '' || productName.includes(name_filter.toLowerCase())) && 
                        (low_price === '' || parseFloat(productPrice) >= parseFloat(low_price)) &&
                        (high_price === '' || parseFloat(productPrice) <= parseFloat(high_price))) {
                        console.log("moryarty", parseFloat(productPrice), parseFloat(low_price), parseFloat(high_price));
                        product.style.display = 'inline-block';
                        product.parentElement.parentElement.style.display = 'block';
                        medicinesWithResult.add(product.parentElement.parentElement);
                    } else {
                        product.style.display = 'none';
                    }
                })
                document.querySelectorAll('.medicine').forEach((medicine) => {
                    const medicineName = medicine.children[0].textContent.toLowerCase();
                    if (!medicinesWithResult.has(medicine)) {
                        medicine.style.display = 'none';
                    }
                    if (name_filter !== '' && medicineName.includes(name_filter.toLowerCase())) {
                        medicine.style.display = 'block';
                        medicine.querySelectorAll('.product-card').forEach((product) => {
                            product.style.display = 'inline-block';
                        })
                    }
                })
            })




        }).catch((error) => {
        console.log(error)
    })

    const backButton = document.querySelector('#back-button');

    backButton.addEventListener('click', () => {
        buildLandingPage();
    })
}

function buildMedicine(info) {
    const medicine = document.createElement('div');
    medicine.classList.add('medicine');
    medicine.innerHTML = `
        <h2>${info.genericName}</h2>
        <p>${info.description}</p>
    `;

    const productsList = document.createElement('div');
    productsList.classList.add('products-list');

    info['products'].forEach((product) => {
        productsList.appendChild(buildProductCard(product))
    });

    medicine.appendChild(productsList);

    return medicine;
}


function buildProductCard(info){
    const productCard = document.createElement('div');
    productCard.classList.add('product-card');
    let otcInfo = '';
    if (info['overTheCounter'] && info['generic']) {
        otcInfo = 'Over the counter and generic';
    }
    otcInfo = info['overTheCounter'] ? 'Over the counter' : 'Prescription only';
    productCard.innerHTML = `
        <h3>${info.brandName}</h3>
        <p>${otcInfo}</p>
        <p>${info.dosageType}</p>
        <p>${info.price} USD</p>
    `;
    return productCard;
}



function textTyping(element, text, delay) {
    element.textContent = '';
    return new Promise((resolve) => {
        let i = 0;
        const interval = setInterval(() => {
            element.textContent += text[i];
            i++;
            if (i >= text.length) {
                clearInterval(interval);
                resolve();
            }
        }, delay);
    });
}

// buildResultsPage(['headache', 'fever']);
buildLandingPage();
