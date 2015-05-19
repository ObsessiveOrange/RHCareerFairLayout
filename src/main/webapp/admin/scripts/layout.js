var careerFairData;
var $canvasMap;
var scaling = 1;
var selectionToolActive = true;
var mergeToolActive = false;
var splitToolActive = false;
var mergeTable1 = null;
(window.setup = function() {
    $canvasMap = $("#canvasMap");
    var $container = $("#mapContainer");
    var containerWidth = $container.width() * scaling;
    var containerHeight = $container.width() * (scaling / 2);
    $container.prop("height", containerHeight);
    $canvasMap.prop("width", containerWidth).prop("height", containerHeight);
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
    setupLinks();
})();
window.cleanup = function() {};

function setupLinks() {
    $(".toolsItem").click(function(event) {
        var sourceID = event.delegateTarget.id;
        selectionToolActive = false;
        mergeToolActive = false;
        splitToolActive = false;
        $("#selectionTool").removeClass("selected");
        $("#mergeTool").removeClass("selected");
        $("#splitTool").removeClass("selected");
        switch (sourceID) {
            case "selectionTool":
                selectionToolActive = true;
                $("#selectionTool").addClass("selected");
                break;
            case "mergeTool":
                mergeToolActive = true;
                $("#mergeTool").addClass("selected");
                break;
            case "splitTool":
                splitToolActive = true;
                $("#splitTool").addClass("selected");
                break;
            default:
                break;
        }
    });
}
// function bringTableToFront(tableID) {
//     bringLayerToFront("table" + tableID + "Box");
//     bringLayerToFront("table" + tableID + "Text");
//     $canvasMap.drawLayers();
// }
function bringLayerToFront(layerName) {
    var layer = $canvasMap.getLayer(layerName);
    // Remove layer from its original position
    $canvasMap.getLayers().splice(layer.index, 1);
    // Bring layer to front
    // push() new one to end
    $canvasMap.getLayers().push(layer);
    // Update all layers indecies.
    for (var i = 0; i < $canvasMap.getLayers().length; i++) {
        $canvasMap.getLayers()[i].index = i;
    }
}

function redrawTable(tableID) {
    $canvasMap.drawLayer("table" + tableID + "Box");
    $canvasMap.drawLayer("table" + tableID + "Text");
}

function checkOverlap(layer1, layer2) {
    if (layer1.eventX >= layer2.x && layer1.eventX <= (layer2.x + layer2.width) && layer1.eventY >= layer2.y && layer1.eventY <= (layer2.y + layer2.height)) {
        return true;
    }
    return false;
}

function mergeTables(table1, table2) {
    if (Math.abs(table2 - table1) !== 1 || tableLocations[table1].group !== tableLocations[table2].group) {
        $canvasMap.setLayer("table" + table1 + "Box", {
            fillStyle: '#DDD'
        });
        $canvasMap.setLayer("table" + table2 + "Box", {
            fillStyle: '#DDD'
        });
        redrawTable(table1);
        redrawTable(table2);
        mergeTable1 = null;
        return;
    }
    careerFairData.termVars.layout.locationTableMapping[table1].tableSize += careerFairData.termVars.layout.locationTableMapping[table2].tableSize;
    for (var i = table2; i < Object.keys(careerFairData.termVars.layout.locationTableMapping).length; i++) {
        careerFairData.termVars.layout.locationTableMapping[i] = careerFairData.termVars.layout.locationTableMapping[i + 1];
    }
    delete careerFairData.termVars.layout.locationTableMapping[Object.keys(careerFairData.termVars.layout.locationTableMapping).length];
    $canvasMap.removeLayers();
    generateTableLocations();
    drawTables();
    mergeTable1 = null;
}

function splitTable(table) {
    if (careerFairData.termVars.layout.locationTableMapping[table].tableSize === 1) {
        return;
    }
    var shiftCount = careerFairData.termVars.layout.locationTableMapping[table].tableSize;
    careerFairData.termVars.layout.locationTableMapping[table].tableSize = 1;
    for (var i = Object.keys(careerFairData.termVars.layout.locationTableMapping).length; i > table; i--) {
        careerFairData.termVars.layout.locationTableMapping[i+shiftCount] = careerFairData.termVars.layout.locationTableMapping[i];
    }
    for (var i = table+1; i < table + shiftCount + 1; i++) {
        var nextTable = Object.keys(careerFairData.termVars.layout.locationTableMapping).length + 1;
        careerFairData.termVars.layout.locationTableMapping[i] = {
            location: i,
            tableSize: 1
        };
    }
    $canvasMap.removeLayers();
    generateTableLocations();
    drawTables();
}
// function restoreTableLocation(tableID) {
//     var location = tableLocations[careerFairData.termVars.layout.tableLocationMapping[tableID].location];
//     $canvasMap.setLayer("table" + tableID + "Box", {
//         x: location.x,
//         y: location.y
//     });
//     $canvasMap.setLayer("table" + tableID + "Text", {
//         x: location.x + location.width / 2,
//         y: location.y + location.height / 2
//     });
// }
//
//draw tables and table numbers
function drawRect(tableObj) {
    //
    //draw tableID in box for easy reading.
    if (tableObj.tableID !== 0 && tableObj.tableID <= Object.keys(careerFairData.termVars.layout.locationTableMapping).length) {
        $canvasMap.drawRect({
            layer: true,
            name: 'table' + tableObj.tableID + 'Box',
            strokeStyle: '#000',
            strokeWidth: scaling,
            data: {
                tableID: tableObj.tableID
            },
            x: tableObj.x,
            y: tableObj.y,
            width: tableObj.width,
            height: tableObj.height,
            fromCenter: false,
            click: function(layer) {
                if (mergeToolActive) {
                    var tableID = layer.data.tableID;
                    if (mergeTable1 === null) {
                        mergeTable1 = tableID;
                        $canvasMap.setLayer("table" + tableID + "Box", {
                            fillStyle: '#0F0'
                        });
                        redrawTable(tableID);
                    } else {
                        var table1 = tableID > mergeTable1 ? mergeTable1 : tableID;
                        var table2 = tableID > mergeTable1 ? tableID : mergeTable1;
                        console.log("Merge table " + mergeTable1 + " and " + tableID);
                        mergeTables(table1, table2);
                        //     console.log("Merge table " + mergeTable1 + " and " + tableID);
                        //     $canvasMap.setLayer("table" + mergeTable1 + "Box", {
                        //         fillStyle: '#DDD'
                        //     });
                        //     redrawTable(mergeTable1);
                        //     mergeTable1 = null;
                    }
                }
            }
        });
        $canvasMap.drawText({
            layer: true,
            name: 'table' + tableObj.tableID + 'Text',
            fillStyle: '#000000',
            data: {
                tableID: tableObj.tableID
            },
            x: tableObj.x + tableObj.width / 2,
            y: tableObj.y + tableObj.height / 2,
            fontSize: tableObj.height / tableObj.yScaling / 2,
            fontFamily: 'Verdana, sans-serif',
            text: tableObj.tableID,
            click: function(layer) {
                if (mergeToolActive) {
                    var tableID = layer.data.tableID;
                    if (mergeTable1 === null) {
                        mergeTable1 = tableID;
                        $canvasMap.setLayer("table" + tableID + "Box", {
                            fillStyle: '#0F0'
                        });
                        redrawTable(tableID);
                    } else {
                        var table1 = tableID > mergeTable1 ? mergeTable1 : tableID;
                        var table2 = tableID > mergeTable1 ? tableID : mergeTable1;
                        console.log("Merge table " + mergeTable1 + " and " + tableID);
                        mergeTables(table1, table2);
                        // $canvasMap.setLayer("table" + mergeTable1 + "Box", {
                        //     fillStyle: '#DDD'
                        // });
                        // redrawTable(mergeTable1);
                    }
                }
            }
        });
    } else {
        //
        //draw unfilled rectangle - fill is on bottom "highlights" layer
        $canvasMap.drawRect({
            layer: true,
            strokeStyle: '#000',
            strokeWidth: scaling,
            data: {
                tableID: tableObj.tableID
            },
            x: tableObj.x,
            y: tableObj.y,
            width: tableObj.width,
            height: tableObj.height,
            fromCenter: false
        });
    }
}
//
//generate positions of all tables.
function generateTableLocations() {
    //
    //reset tableLocations variable - may have changed
    tableLocations = [];
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
    unitX = $canvasMap.prop("width") / 100;
    //10 + (number of sections - 1) * 5 % of space allocated to (vertical) walkways
    var tableWidth = unitX * (90 - Math.min(s1, 1) * 5 - Math.min(s3, 1) * 5) / hrzCount;
    unitY = $canvasMap.prop("width") / 2 / 100;
    //30% of space allocated to registration and rest area.
    var tableHeight = unitY * 70 / vrtCount;
    //
    //
    var tableID = 1;
    var offsetX = 5 * unitX;
    //
    // section 1
    if (s1 > 0) {
        for (var i = 0; i < s1;) {
            tableLocations[tableID] = {
                tableID: tableID,
                x: offsetX,
                y: 5 * unitY + i * tableHeight,
                width: tableWidth,
                height: tableHeight * careerFairData.termVars.layout.locationTableMapping[tableID].tableSize,
                xScaling: 1,
                yScaling: careerFairData.termVars.layout.locationTableMapping[tableID].tableSize,
                group: "section1"
            };
            i += careerFairData.termVars.layout.locationTableMapping[tableID].tableSize;
            tableID += careerFairData.termVars.layout.locationTableMapping[tableID].tableSize;
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
            //Outer rows have no walkway.
            //Also use this if there is no path inbetween the left and right.
            if (s2PathWidth === 0 || i === 0 || i == s2Rows - 1) {
                for (var j = 0; j < s2;) {
                    tableLocations[tableID] = {
                        tableID: tableID,
                        x: offsetX + (j * tableWidth),
                        y: 5 * unitY + Math.floor((i + 1) / 2) * pathWidth + i * tableHeight,
                        width: tableWidth * careerFairData.termVars.layout.locationTableMapping[tableID].tableSize,
                        height: tableHeight,
                        xScaling: careerFairData.termVars.layout.locationTableMapping[tableID].tableSize,
                        yScaling: 1,
                        group: "section2row" + i
                    };
                    j += careerFairData.termVars.layout.locationTableMapping[tableID].tableSize;
                    tableID++;
                }
            }
            //
            //inner rows need to have walkway halfway through
            else {
                var leftTables = Math.floor((s2 - s2PathWidth) / 2);
                var rightTables = s2 - s2PathWidth - leftTables;
                for (var j = 0; j < leftTables;) {
                    tableLocations[tableID] = {
                        tableID: tableID,
                        x: offsetX + (j * tableWidth),
                        y: 5 * unitY + Math.floor((i + 1) / 2) * pathWidth + i * tableHeight,
                        width: tableWidth * careerFairData.termVars.layout.locationTableMapping[tableID].tableSize,
                        height: tableHeight,
                        xScaling: careerFairData.termVars.layout.locationTableMapping[tableID].tableSize,
                        yScaling: 1,
                        group: "section2row" + i + "L"
                    };
                    j += careerFairData.termVars.layout.locationTableMapping[tableID].tableSize;
                    tableID++;
                }
                for (var j = 0; j < rightTables;) {
                    tableLocations[tableID] = {
                        tableID: tableID,
                        x: offsetX + ((leftTables + s2PathWidth + j) * tableWidth),
                        y: 5 * unitY + Math.floor((i + 1) / 2) * pathWidth + i * tableHeight,
                        width: tableWidth * careerFairData.termVars.layout.locationTableMapping[tableID].tableSize,
                        height: tableHeight,
                        xScaling: careerFairData.termVars.layout.locationTableMapping[tableID].tableSize,
                        yScaling: 1,
                        group: "section2row" + i + "R"
                    };
                    j += careerFairData.termVars.layout.locationTableMapping[tableID].tableSize;
                    tableID++;
                }
            }
        }
        offsetX += s2 * tableWidth + 5 * unitX;
    }
    //
    // section 3
    if (s3 > 0) {
        for (var i = 0; i < s3;) {
            var tableSize = (((typeof careerFairData.termVars.layout.locationTableMapping[tableID]) == "undefined") ? 1 : careerFairData.termVars.layout.locationTableMapping[tableID].tableSize);
            tableLocations[tableID] = {
                tableID: tableID,
                x: offsetX,
                y: 5 * unitY + i * tableHeight,
                width: tableWidth,
                height: tableHeight * tableSize,
                xScaling: 1,
                yScaling: careerFairData.termVars.layout.locationTableMapping[tableID].tableSize,
                group: "section3"
            };
            i += tableSize;
            tableID++;
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
        drawRect(location);
    });
    //
    // rest & registration areas
    drawRect({
        tableID: 0,
        x: 40 * unitX,
        y: 80 * unitY,
        width: 45 * unitX,
        height: 15 * unitY,
        xScaling: 1,
        yScaling: 1
    });
    $canvasMap.drawText({
        layer: true,
        fillStyle: '#000000',
        x: 62.5 * unitX,
        y: 87.5 * unitY,
        fontSize: 20 * scaling,
        fontFamily: 'Verdana, sans-serif',
        text: 'Rest Area'
    });
    drawRect({
        tableID: 0,
        x: 5 * unitX,
        y: 80 * unitY,
        width: 30 * unitX,
        height: 15 * unitY,
        xScaling: 1,
        yScaling: 1
    });
    $canvasMap.drawText({
        layer: true,
        fillStyle: '#000000',
        x: 20 * unitX,
        y: 87.5 * unitY,
        fontSize: 20 * scaling,
        fontFamily: 'Verdana, sans-serif',
        text: 'Registration'
    });
    $canvasMap.drawLayers();
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