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

const noResultsMessage = 'Oops! No results found in our databse. ' + 
                  'We recommend visiting a doctor for personalized ' +
                  'medical attention.';

const base = 'http://localhost:8080/api/';

class PromptError extends Error {
    constructor(message) {
        super(message);
        this.name = this.constructor.name;
    }
}
async function apiCall(endpoint, params) {
    const paramsString = new URLSearchParams(params);
    return fetch(base + endpoint + '?' + paramsString, {
        method: 'GET',
        headers: {'Content-Type': 'application/json'}
    }).then((response) => {
        console.log(response);
        if (response.status === 400) {
            throw new PromptError();
        }
        else if (!response.ok){
            throw new Error();
        }
        return response.json();
    }).catch((error) =>{
        if (error instanceof PromptError){
            buildErrorPage(`We did not like your input "${params.input}". Is it really describing some symptoms you have?`)
        }
        else{
            buildErrorPage('An error occurred while fetching data. You are requesting too much!')
        }
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
        }).catch(console.log)
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
    const sortSelect = document.querySelector('#sort-select');

    apiCall('explain_symptoms/from_symptoms', {symptoms: symptoms.join(',')}).then((response) => {
        console.log(response);
        textTyping(symptomsExplanation, response['explanation'], 15)
    }).catch((error) => {
        console.log(error)
    })

    apiCall('medicines/for_symptoms', {symptoms: symptoms.join(',')}).then((response) => {
        const medicinesList = document.querySelector('#medicines-list');
        if (!response['medicines'] || response['medicines'].length === 0){
            const preamble = document.querySelector('#preamble');
            preamble.textContent = noResultsMessage;
        }
        else{
            response['medicines'].forEach((medicine) => { 
                medicinesList.appendChild(buildMedicine(medicine));
            })
            submitButton.addEventListener('click', filter);
            console.log('sortSelect', sortSelect)
            sortSelect.addEventListener('change', sort);
            sort()
        }

    }).catch(console.log)

    const backButton = document.querySelector('#back-button');
    console.log("backbutton", backButton);

    backButton.addEventListener('click', () => {
        buildLandingPage();
    })
}

function buildMedicine(info) {
    const medicine = document.createElement('div');
    medicine.classList.add('medicine');

    const medicinePrice = info.products[0].price;
    const priceDescription = medicinePrice ? `Estimated Price: ${medicinePrice}`: 'Price unavailable';

    medicine.innerHTML = `
        <h2>${info.genericName}</h2>
        <h3>${priceDescription}</h3>
        <span>${info.description}</span>
    `;
    medicine.appendChild(buildDrugBankLink(info.genericName));

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
    let otcInfo = info['overTheCounter'] ? 'Over the counter' : 'Prescription only';
    if (!info.dosageType){
        info.dosageType = 'OTHER';
    }
    productCard.innerHTML = `
        <h3>${info.brandName}</h3>
        <p>${otcInfo}</p>
        <p>${info.dosageType}</p>
        <p style="display: none;">${info.price} USD</p>
    `;

    productCard.appendChild(buildAmazonLink(info.brandName));

    return productCard;
}


function buildAmazonLink(brandName){
    amazonLink = document.createElement('a');
    amazonLink.href = `https://www.amazon.com/s?k=${brandName}`;
    amazonLink.target = '_blank';
    amazonIcon = document.createElement('img');
    amazonIcon.src = 'resources/amazon-sm.webp';
    amazonIcon.classList.add('amazon-icon');
    amazonLink.appendChild(amazonIcon);
    return amazonLink;
}

function buildDrugBankLink(genericName){
    drugBankLink = document.createElement('a');
    drugBankLink.href = `https://go.drugbank.com/unearth/q?searcher=drugs&query=${genericName}`;
    drugBankLink.target = '_blank';
    drugBankSpan = document.createElement('span');
    drugBankSpan.textContent = '... Learn more';
    drugBankLink.appendChild(drugBankSpan);
    drugBankLink.classList.add('drugbank-link');
    return drugBankLink;
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

function filter(){
    const name_filter = document.querySelector('#name-filter').value
    const low_price = document.querySelector('#price-min-filter').value
    const high_price = document.querySelector('#price-max-filter').value
    const overTheCounter = document.querySelector('#otc-filter').checked
    const prescription = document.querySelector('#prescription-filter').checked
    const type = document.querySelector('#dosage-type-filter').value

    let productsShow = Array.from(document.querySelectorAll('.product-card'))
    let medicinesShow = Array.from(document.querySelectorAll('.medicine'))

    console.log(productsShow)

    productsShow.forEach((product) => {
        product.style.display = 'none';
    })

    medicinesShow.forEach((medicine) => {
        medicine.style.display = 'none';
    })

    productsShow = productsShow.filter((product) => {
        let productPrice = product.children[3].textContent.split(' ')[0];
        return (low_price === '' || parseFloat(productPrice) >= parseFloat(low_price)) &&
                    (high_price === '' || parseFloat(productPrice) <= parseFloat(high_price))
    })

    productsShow = productsShow.filter((product) => {
        let otcInfo = product.children[1].textContent;
        return ((overTheCounter && otcInfo.includes('Over the counter')) || 
            prescription && otcInfo.includes('Prescription only') || 
        (!overTheCounter && !prescription))
    })

    productsShow = productsShow.filter((product) => {
        let productType = product.children[2].textContent;
        return (productType === type || type === '')
    })

    medicinesShow = medicinesShow.filter((medicine) => {
        let medicineName = medicine.children[0].textContent.toLowerCase();
        return (name_filter !== '' && medicineName.includes(name_filter.toLowerCase()))
    })

    medicinesShow.forEach((medicine) => {
        medicine.style.display = 'block';
        medicine.querySelectorAll('.product-card').forEach((product) => {
            if (productsShow.includes(product)){
                product.style.display = 'inline-block';
            }
        })
    })

    productsShow = productsShow.filter((product) => {
        let productName = product.children[0].textContent.toLowerCase();
        return (name_filter === '' || productName.includes(name_filter.toLowerCase()))     
    })


    productsShow.forEach((product) => {
        product.style.display = 'inline-block';
        product.parentElement.parentElement.style.display = 'block';
    })
}

function sort(){
    console.log("trigger!")
    const sortType = document.querySelector('#sort-select').value;
    const medicinesList = document.querySelector('#medicines-list');
    let medicines = Array.from(document.querySelectorAll('.medicine'));

    medicinesList.innerHTML = '';

    console.log(sortType);

    medicines = medicines.sort((a, b) => {
        let priceA = parseFloat(a.children[1].textContent.split(' ')[2]);
        let priceB = parseFloat(b.children[1].textContent.split(' ')[2]);
        priceA = isNaN(priceA) ? 10000: priceA;
        priceB = isNaN(priceB) ? 10000: priceB;
        console.log("prices compared", priceA, priceB)
        if (sortType === 'asc'){
            return priceA - priceB;
        }
        else{
            return priceB - priceA;
        }
    })

    console.log(medicines);

    medicines.forEach((medicine) => {
        medicinesList.appendChild(medicine);
    })
}

buildLandingPage();
