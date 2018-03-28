
// svg pan-and zoom functionality
// Please note that this will not do anything unless the following script has been included:
// https://cdn.ons.gov.uk/vendor/svg-pan-zoom/3.5.2/svg-pan-zoom.min.js
// This is included from map.handlebars - this prevents the script from being loaded unnecessarily

// invokes svg-pan-and-zoom if available, for all maps on the page
function enableMapPanAndZoom() {
    var activateAllMaps = function() {
        var maps = document.querySelectorAll("div.map > svg");
        for (i = 0; i < maps.length; i++) {
            initialiseMap(maps[i].id)
        }
    };
    if (document.readyState == 'loading') {
        document.addEventListener('DOMContentLoaded', activateAllMaps);
    } else {
        activateAllMaps();
    }
}
enableMapPanAndZoom();

// initialises svg pan and zoom for a specific map
function initialiseMap(mapId) {
    if (svgPanZoom) {
        var svg = document.getElementById(mapId);
        if (svg && svg.clientWidth > 0 && svg.hasAttribute("viewBox")) {
            var viewBox = svg.getAttribute("viewBox").split(" ") // x1 y1 x2 y2
            heightRatio = parseInt(viewBox[3]) / parseInt(viewBox[2])

            var setSvgHeight = function() {
                svg.style.height = Math.round(svg.clientWidth * heightRatio) + "px"
            };
            setSvgHeight();

            var panZoom = window.panZoom = svgPanZoom('#' + mapId, {minZoom: 0.75, maxZoom: 100, zoomScaleSensitivity: 0.4, mouseWheelZoomEnabled: false, controlIconsEnabled: true, fit: true, center: true});

            window.addEventListener('resize',
                delayUntilFinalResizeEvent(function(){
                    setSvgHeight();
                    panZoom.resize();
                    panZoom.fit();
                    panZoom.center();
                }, 300, mapId));
        }
    }
}


// delays running a function for ms milliseconds.
// Used to wait until the window has finished resizing before updating the svg pan and zoom.
function delayUntilFinalResizeEvent(callback, ms, uniqueId) {
    var timers = {};
    return function () {
        if (timers[uniqueId]) {
            clearTimeout (timers[uniqueId]);
        }
        timers[uniqueId] = setTimeout(callback, ms);
    };
}