const backdrop = document.querySelector('.backdrop');
const sideDrawer = document.querySelector('.mobile-nav');
const menuToggle = document.querySelector('#side-menu-toggle');

function backdropClickHandler() {
    backdrop.style.display = 'none';
    sideDrawer.classList.remove('open');
}

function menuToggleClickHandler() {
    backdrop.style.display = 'block';
    sideDrawer.classList.add('open');
}

backdrop.addEventListener('click', backdropClickHandler);
menuToggle.addEventListener('click', menuToggleClickHandler);

document.addEventListener("DOMContentLoaded", function () {
    function roundToTwoDecimalPlaces(number) {
        return parseFloat(number).toFixed(2);
    }

    var temperatureElements = document.getElementsByClassName("temperature");
    for (var i = 0; i < temperatureElements.length; i++) {
        var temperatureText = temperatureElements[i].textContent;
        var roundedTemperature = roundToTwoDecimalPlaces(temperatureText);
        temperatureElements[i].textContent = roundedTemperature;
    }
});

