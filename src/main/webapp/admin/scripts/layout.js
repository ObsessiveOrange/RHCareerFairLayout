var careerFairData;
var $mapTablesCanvas;
var $mapHighlightsCanvas;
var scaling = 2;
(window.setup = function() {

    $mapTablesCanvas = $("#mapTables");
    $mapHighlightsCanvas = $("#mapHighlights");
    var $container = $("#mapContainer");
    var containerWidth = $container.width() * scaling;
    var containerHeight = $container.width() * (scaling / 2);
    $container.prop("height", containerHeight);
    $mapTablesCanvas.prop("width", containerWidth).prop("height", containerHeight);
    $mapHighlightsCanvas.prop("width", containerWidth).prop("height", containerHeight);
    
    sendGetRequest({
        url: "/api/data?method=getData",
        successHandler: function(data) {
            //
            //jQuery auto-parses the json data, since the content type is application/json (may switch to JSONP eventually... how does that affect this?)
            careerFairData = data;
            //
            //set last fetch time, so we know to refresh beyond a certain validity time
            careerFairData.lastFetchTime = new Date().getTime();
            generateTableLocations();
            drawTables();
        }
    });
})();
window.cleanup = function() {};
//
//draw tables and table numbers
function drawRect(tableNumber, x, y, width, height) {
    //
    //draw unfilled rectangle - fill is on bottom "highlights" layer
    $mapTablesCanvas.drawLine({
        //    layer: true,
        strokeStyle: '#000',
        strokeWidth: scaling,
        x1: x,
        y1: y,
        x2: x + width,
        y2: y,
        x3: x + width,
        y3: y + height,
        x4: x,
        y4: y + height,
        closed: true
        //    click : function(layer) {
        //      alert("You clicked an area!");
        //    } //Box and text both need to be a layer for this to work. Redrawing doesn't quite work as expected, which is why this is disabled.
    });
    //
    //draw tablenumber in box for easy reading.
    if (Number(tableNumber) !== 0) {
        $mapTablesCanvas.drawText({
            //      layer: true,
            fillStyle: '#000000',
            x: x + width / 2,
            y: y + height / 2,
            fontSize: height / 2,
            fontFamily: 'Verdana, sans-serif',
            text: tableNumber
        });
    }
}
//
//generate positions of all tables.
function generateTableLocations() {
    //
    //reset tableLocations variable - may have changed
    tableLocations = {};
    //
    //convenience assignments
    var s1 = Number(careerFairData.termVars.layout.section1);
    var s2 = Number(careerFairData.termVars.layout.section2);
    var s2Rows = Number(careerFairData.termVars.layout.section2_Rows);
    var s2PathWidth = Number(careerFairData.termVars.layout.section2_PathWidth);
    var s3 = Number(careerFairData.termVars.layout.section3);
    //
    //count number of vertical and horizontal tables there are
    var hrzCount = s2 + Math.min(s1, 1) + Math.min(s3, 1);
    var vrtCount = Math.max(s1, s3);
    //
    //calculate width and height of tables based on width of the canvas
    unitX = $mapTablesCanvas.prop("width") / 100;
    //10 + (number of sections - 1) * 5 % of space allocated to (vertical) walkways
    var tableWidth = unitX * (90 - Math.min(s1, 1) * 5 - Math.min(s3, 1) * 5) / hrzCount;
    unitY = $mapTablesCanvas.prop("width") / 2 / 100;
    //30% of space allocated to registration and rest area.
    var tableHeight = unitY * 70 / vrtCount;
    //
    //
    var locationID = 1;
    var offsetX = 5 * unitX;
    //
    // section 1
    if (s1 > 0) {
        for (var i = 0; i < s1;) {
            tableLocations[locationID] = {
                locationID: locationID,
                x: offsetX,
                y: 5 * unitY + i * tableHeight,
                width: tableWidth,
                height: tableHeight * careerFairData.termVars.layout.locationTableMapping[locationID].tableSize
            };
            i += careerFairData.termVars.layout.locationTableMapping[locationID].tableSize;
            locationID += careerFairData.termVars.layout.locationTableMapping[locationID].tableSize;
        }
        offsetX += tableWidth + 5 * unitX;
    }
    //
    // section 2
    var pathWidth = (unitY * 70 - s2Rows * tableHeight) / (s2Rows / 2);
    //
    //rows
    if (s2Rows > 0 && s2 > 0) {
        for (var i = 0; i < s2Rows; i++) {
            //
            //outer rows have no walkway
            if (i === 0 || i == s2Rows - 1) {
                for (var j = 0; j < s2;) {
                    tableLocations[locationID] = {
                        locationID: locationID,
                        x: offsetX + (j * tableWidth),
                        y: 5 * unitY + Math.floor((i + 1) / 2) * pathWidth + i * tableHeight,
                        width: tableWidth * careerFairData.termVars.layout.locationTableMapping[locationID].tableSize,
                        height: tableHeight
                    };
                    j += careerFairData.termVars.layout.locationTableMapping[locationID].tableSize;
                    locationID += careerFairData.termVars.layout.locationTableMapping[locationID].tableSize;
                }
            }
            //
            //inner rows need to have walkway halfway through
            else {
                var leftTables = Math.floor((s2 - s2PathWidth) / 2);
                var rightTables = s2 - s2PathWidth - leftTables;
                for (var j = 0; j < leftTables;) {
                    tableLocations[locationID] = {
                        locationID: locationID,
                        x: offsetX + (j * tableWidth),
                        y: 5 * unitY + Math.floor((i + 1) / 2) * pathWidth + i * tableHeight,
                        width: tableWidth * careerFairData.termVars.layout.locationTableMapping[locationID].tableSize,
                        height: tableHeight
                    };
                    j += careerFairData.termVars.layout.locationTableMapping[locationID].tableSize;
                    locationID += careerFairData.termVars.layout.locationTableMapping[locationID].tableSize;
                }
                for (var j = 0; j < rightTables;) {
                    tableLocations[locationID] = {
                        locationID: locationID,
                        x: offsetX + ((leftTables + j) * tableWidth),
                        y: 5 * unitY + Math.floor((i + 1) / 2) * pathWidth + i * tableHeight,
                        width: tableWidth * careerFairData.termVars.layout.locationTableMapping[locationID].tableSize,
                        height: tableHeight
                    };
                    j += careerFairData.termVars.layout.locationTableMapping[locationID].tableSize;
                    locationID += careerFairData.termVars.layout.locationTableMapping[locationID].tableSize;
                }
            }
        }
        offsetX += s2 * tableWidth + 5 * unitX;
    }
    //
    // section 3
    if (s3 > 0) {
        for (var i = 0; i < s3;) {
            var tableSize = (((typeof careerFairData.termVars.layout.locationTableMapping[locationID]) == "undefined") ? 1 : careerFairData.termVars.layout.locationTableMapping[locationID].tableSize);
            tableLocations[locationID] = {
                locationID: locationID,
                x: offsetX,
                y: 5 * unitY + i * tableHeight,
                width: tableWidth,
                height: tableHeight * tableSize
            };
            i += tableSize;
            locationID += tableSize;
        }
    }
    offsetX += tableWidth + 5 * unitX;
}
//
//draw actual tables, then draw registration and rest areas
function drawTables() {
    //
    //draw company tables based on generated locations
    Object.keys(tableLocations).forEach(function(key) {
        var location = tableLocations[key];
        var tableNumber = (((typeof careerFairData.termVars.layout.locationTableMapping[location.locationID]) == "undefined") ? "" : careerFairData.termVars.layout.locationTableMapping[location.locationID].tableNumber);
        drawRect(tableNumber, location.x, location.y, location.width, location.height);
    });
    //
    // rest & registration areas
    drawRect(0, 40 * unitX, 80 * unitY, 45 * unitX, 15 * unitY);
    $mapTablesCanvas.drawText({
        //    layer: true,
        fillStyle: '#000000',
        x: 62.5 * unitX,
        y: 87.5 * unitY,
        fontSize: 20 * scaling,
        fontFamily: 'Verdana, sans-serif',
        text: 'Rest Area'
    });
    drawRect(0, 5 * unitX, 80 * unitY, 30 * unitX, 15 * unitY);
    $mapTablesCanvas.drawText({
        //    layer: true,
        fillStyle: '#000000',
        x: 20 * unitX,
        y: 87.5 * unitY,
        fontSize: 20 * scaling,
        fontFamily: 'Verdana, sans-serif',
        text: 'Registration'
    });
}
// Create a rectangle layer
// $('canvas').drawRect({
//     layer: true,
//     draggable: true,
//     bringToFront: true,
//     name: 'myBox',
//     fillStyle: '#585',
//     groups: ['shapes'],
//     dragGroups: ['shapes'],
//     x: 100,
//     y: 100,
//     width: 100,
//     height: 50,
//     click: function(layer) {
//         clicked(layer);
//     }
// });
// $('canvas').drawRect({
//     layer: true,
//     draggable: true,
//     bringToFront: true,
//     name: 'asdf',
//     fillStyle: '#0E0',
//     groups: ['shapes'],
//     dragGroups: ['shapes'],
//     x: 172,
//     y: 135,
//     width: 100,
//     height: 50,
//     click: function(layer) {
//         clicked(layer);
//     }
// });
// $('canvas').clearCanvas();
// $('canvas').drawLayers();
// function clicked(layer) {
//     $('canvas').setLayer(layer, {
//         x: '+=100',
//         y: '-=100'
//     });
//     $('canvas').drawLayers();
// }