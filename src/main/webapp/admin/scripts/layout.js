var careerFairData;
var $canvasMap;
var scaling = 1;
var selectionToolActive = true;
var mergeToolActive = false;
var splitToolActive = false;
var mergeTable1 = null;
var tableLocations = [];
var companyLocations = [];
var prevTableColor;
(window.setup = function() {
    sendGetRequest({
        url: "/api/data?method=getData",
        successHandler: function(data) {
            //
            //jQuery auto-parses the json data, since the content type is application/json (may switch to JSONP eventually... how does that affect this?)
            careerFairData = data;
            setupTableMappings();
            populateCompanyList();
            //
            //set last fetch time, so we know to refresh beyond a certain validity time
            careerFairData.lastFetchTime = new Date().getTime();
            //
            //setup canvas sizing
            $canvasMap = $("#canvasMap");
            var $container = $("#mapContainer");
            var containerWidth = $container.width() * scaling;
            var containerHeight = $container.width() * (scaling / 2);
            $container.prop("height", containerHeight);
            $canvasMap.prop("width", containerWidth).prop("height", containerHeight);
            //draw on canvas
            generateTableLocations();
            drawTables();
        }
    });
    setupLinks();
})();
window.cleanup = function() {};

function setupLinks() {
    $(".toolsItem").click(function(event) {
        var sourceId = event.delegateTarget.id;
        selectionToolActive = false;
        mergeToolActive = false;
        splitToolActive = false;
        $("#selectionTool").removeClass("selected");
        $("#mergeTool").removeClass("selected");
        $("#splitTool").removeClass("selected");
        switch (sourceId) {
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

function setupTableMappings() {
    var s1 = Number(careerFairData.termVars.layout.section1);
    var s2 = Number(careerFairData.termVars.layout.section2);
    var s2Rows = Number(careerFairData.termVars.layout.section2_Rows);
    var s2PathWidth = Number(careerFairData.termVars.layout.section2_PathWidth);
    var s3 = Number(careerFairData.termVars.layout.section3);
    //create temp var for total count
    var totalCount = 0;
    //sum up tables
    totalCount += s1;
    totalCount += s2 * 2;
    totalCount += (s2 - s2PathWidth) * (s2Rows - 2);
    totalCount += s3;
    for (var i = 0; i < totalCount; i++) {
        if ((typeof careerFairData.termVars.layout.tableMappings[i]) != "undefined") {
            if (careerFairData.termVars.layout.tableMappings[i].tableSize > 1) {
                totalCount -= careerFairData.termVars.layout.tableMappings[i].tableSize - 1;
            }
        } else {
            // careerFairData.termVars.layout.tableMappings[i] = {
            //     location: i,
            //     tableNumber: i,
            //     tableSize: 1
            // };
            careerFairData.termVars.layout.tableMappings[i] = {
                //i counts from 
                tableNumber: i+1,
                companyId: null,
                tableSize: 1
            };
        }
    }
    careerFairData.termVars.layout.tableMappings = new NWayMap(careerFairData.termVars.layout.tableMappings, ["tableNumber", "companyId"]);
}

function populateCompanyList() {
    Object.keys(careerFairData.companies).forEach(function(companyId) {
        var company = careerFairData.companies[companyId];
        var currentTable = company.tableNumber;
        $("#companyListContainer").append("<tr class='level2 companyListItem' id='companyListRow_" + companyId + "'><td id='companyListName_" + companyId + "' class='companyListNameColumn'>" + company.name + "</td><td id='companyListTableSelector_" + companyId + "'><input type='text' id='companyListTableInput_" + companyId + "' value='" + (careerFairData.termVars.layout.tableMappings.get("companyId", companyId) === null ? "" : careerFairData.termVars.layout.tableMappings.get("companyId", companyId).tableNumber) + "' size='2' maxlength='4'/></td></tr>");
    });
    //add null value
    // $(".companyListTableDropdown").append("<option value='-1'></option>");
    // for (var i = 1; i <= Object.keys(careerFairData.termVars.layout.tableMappings).length; i++) {
    //     $(".companyListTableDropdown").append("<option value='-1'>" + i + "</option>");
    // }
}

function updateCompanyList() {
    var companyIDs = Object.keys(careerFairData.companies);
    var companyListLength = companyIDs.length;
    for (var i = 0; i < companyListLength; i++) {
        var companyId = companyIDs[i];
        var mapping = careerFairData.termVars.layout.tableMappings.get("companyId", companyId);
        if (mapping !== null) {
            $("#companyListTableInput_" + companyId).val(mapping.tableNumber);
        } else {
            $("#companyListTableInput_" + companyId).val("");
        }
    }
    //add null value
    // $(".companyListTableDropdown").append("<option value='-1'></option>");
    // for (var i = 1; i <= Object.keys(careerFairData.termVars.layout.tableMappings).length; i++) {
    //     $(".companyListTableDropdown").append("<option value='-1'>" + i + "</option>");
    // }
}
// function bringTableToFront(tableId) {
//     bringLayerToFront("table" + tableId + "Box");
//     bringLayerToFront("table" + tableId + "Text");
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

function redrawTable(tableId) {
    $canvasMap.drawLayer("table" + tableId + "Box");
    $canvasMap.drawLayer("table" + tableId + "Text");
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
    careerFairData.termVars.layout.tableMappings.get("tableNumber", table1).tableSize += careerFairData.termVars.layout.tableMappings.get("tableNumber", table2).tableSize;
    //remove higher-numbered table, shift everything down by 1 to account for "removed" table that is being merged.
    careerFairData.termVars.layout.tableMappings.remove("tableNumber", table2);
    for (var i = table2; i <= careerFairData.termVars.layout.tableMappings.getKeys("tableNumber").length; i++) {
        var prev = careerFairData.termVars.layout.tableMappings.remove("tableNumber", i + 1);
        prev.tableNumber -= 1;
        careerFairData.termVars.layout.tableMappings.put(prev);
    }
    // delete careerFairData.termVars.layout.tableMappings.get("tableNumber", Object.keys(careerFairData.termVars.layout.tableMappings).length];
    $canvasMap.removeLayers();
    generateTableLocations();
    drawTables();
    updateCompanyList();
    mergeTable1 = null;
}

function splitTable(table) {
    if (careerFairData.termVars.layout.tableMappings.get("tableNumber", table).tableSize === 1) {
        return;
    }
    var shiftCount = careerFairData.termVars.layout.tableMappings.get("tableNumber", table).tableSize - 1;
    careerFairData.termVars.layout.tableMappings.get("tableNumber", table).tableSize = 1;
    for (var i = careerFairData.termVars.layout.tableMappings.getKeys("tableNumber").length; i > table; i--) {
        var prev = careerFairData.termVars.layout.tableMappings.remove("tableNumber", i);
        prev.tableNumber += shiftCount;
        careerFairData.termVars.layout.tableMappings.put(prev);
    }
    for (var i = 0; i < shiftCount; i++) {
        careerFairData.termVars.layout.tableMappings.put({
            tableNumber: table + 1 + i,
            tableSize: 1
        });
    }
    $canvasMap.removeLayers();
    generateTableLocations();
    drawTables();
    updateCompanyList();
}
// function restoreTableLocation(tableId) {
//     var location = tableLocations[careerFairData.termVars.layout.tableLocationMapping[tableId].location];
//     $canvasMap.setLayer("table" + tableId + "Box", {
//         x: location.x,
//         y: location.y
//     });
//     $canvasMap.setLayer("table" + tableId + "Text", {
//         x: location.x + location.width / 2,
//         y: location.y + location.height / 2
//     });
// }
//
//draw tables and table numbers
function drawRect(tableObj) {
    //
    //draw tableId in box for easy reading.
    if (tableObj.tableId !== 0 && tableObj.tableId <= careerFairData.termVars.layout.tableMappings.getKeys("tableNumber").length) {
        $canvasMap.drawRect({
            layer: true,
            name: 'table' + tableObj.tableId + 'Box',
            strokeStyle: '#000',
            strokeWidth: scaling,
            data: {
                tableId: tableObj.tableId
            },
            x: tableObj.x,
            y: tableObj.y,
            width: tableObj.width,
            height: tableObj.height,
            fromCenter: false,
            click: function(layer) {
                if (mergeToolActive) {
                    var tableId = layer.data.tableId;
                    if (mergeTable1 === null) {
                        mergeTable1 = tableId;
                        $canvasMap.setLayer(layer, {
                            fillStyle: '#0BF'
                        });
                        redrawTable(tableId);
                    } else {
                        var table1 = tableId > mergeTable1 ? mergeTable1 : tableId;
                        var table2 = tableId > mergeTable1 ? tableId : mergeTable1;
                        mergeTables(table1, table2);
                    }
                } else if (splitToolActive) {
                    splitTable(layer.data.tableId);
                }
            },
            mouseover: function(layer) {
                prevTableColor = layer.fillStyle;
                $canvasMap.setLayer(layer, {
                    fillStyle: '#BBB'
                });
            },
            mouseout: function(layer) {
                if (layer.fillStyle === '#CCC') {
                    $canvasMap.setLayer(layer, {
                        fillStyle: prevTableColor
                    });
                }
            }
        });
        $canvasMap.drawText({
            layer: true,
            name: 'table' + tableObj.tableId + 'Text',
            fillStyle: '#000000',
            data: {
                tableId: tableObj.tableId
            },
            x: tableObj.x + tableObj.width / 2,
            y: tableObj.y + tableObj.height / 2,
            fontSize: tableObj.height / tableObj.yScaling / 2,
            fontFamily: 'Verdana, sans-serif',
            text: tableObj.tableId,
            intangible: true
        });
    } else {
        //
        //draw unfilled rectangle - fill is on bottom "highlights" layer
        $canvasMap.drawRect({
            layer: true,
            strokeStyle: '#000',
            strokeWidth: scaling,
            data: {
                tableId: tableObj.tableId
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
    companyLocations = [];
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
    var tableId = 1;
    var tableSize = 1;
    var offsetX = 5 * unitX;
    //
    // section 1
    if (s1 > 0) {
        for (var i = 0; i < s1;) {
            tableSize = careerFairData.termVars.layout.tableMappings.get("tableNumber", tableId).tableSize;
            tableLocations[tableId] = {
                tableId: tableId,
                x: offsetX,
                y: 5 * unitY + i * tableHeight,
                width: tableWidth,
                height: tableHeight * tableSize,
                xScaling: 1,
                yScaling: tableSize,
                group: "section1"
            };
            i += tableSize;
            tableId += tableSize;
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
                    tableSize = careerFairData.termVars.layout.tableMappings.get("tableNumber", tableId).tableSize;
                    tableLocations[tableId] = {
                        tableId: tableId,
                        x: offsetX + (j * tableWidth),
                        y: 5 * unitY + Math.floor((i + 1) / 2) * pathWidth + i * tableHeight,
                        width: tableWidth * tableSize,
                        height: tableHeight,
                        xScaling: tableSize,
                        yScaling: 1,
                        group: "section2row" + i
                    };
                    j += tableSize;
                    tableId++;
                }
            }
            //
            //inner rows need to have walkway halfway through
            else {
                var leftTables = Math.floor((s2 - s2PathWidth) / 2);
                var rightTables = s2 - s2PathWidth - leftTables;
                for (var j = 0; j < leftTables;) {
                    tableSize = careerFairData.termVars.layout.tableMappings.get("tableNumber", tableId).tableSize;
                    tableLocations[tableId] = {
                        tableId: tableId,
                        x: offsetX + (j * tableWidth),
                        y: 5 * unitY + Math.floor((i + 1) / 2) * pathWidth + i * tableHeight,
                        width: tableWidth * tableSize,
                        height: tableHeight,
                        xScaling: tableSize,
                        yScaling: 1,
                        group: "section2row" + i + "L"
                    };
                    j += tableSize;
                    tableId++;
                }
                for (var j = 0; j < rightTables;) {
                    tableSize = careerFairData.termVars.layout.tableMappings.get("tableNumber", tableId).tableSize;
                    tableLocations[tableId] = {
                        tableId: tableId,
                        x: offsetX + ((leftTables + s2PathWidth + j) * tableWidth),
                        y: 5 * unitY + Math.floor((i + 1) / 2) * pathWidth + i * tableHeight,
                        width: tableWidth * tableSize,
                        height: tableHeight,
                        xScaling: tableSize,
                        yScaling: 1,
                        group: "section2row" + i + "R"
                    };
                    j += tableSize;
                    tableId++;
                }
            }
        }
        offsetX += s2 * tableWidth + 5 * unitX;
    }
    //
    // section 3
    if (s3 > 0) {
        for (var i = 0; i < s3;) {
            tableSize = careerFairData.termVars.layout.tableMappings.get("tableNumber", tableId).tableSize;
            tableLocations[tableId] = {
                tableId: tableId,
                x: offsetX,
                y: 5 * unitY + i * tableHeight,
                width: tableWidth,
                height: tableHeight * tableSize,
                xScaling: 1,
                yScaling: tableSize,
                group: "section3"
            };
            i += tableSize;
            tableId++;
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
        tableId: 0,
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
        tableId: 0,
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
    highlightUsedTables();
}

function highlightUsedTables() {
    var mappingObjs = careerFairData.termVars.layout.tableMappings.getValues("tableNumber");
    var mappingObjsLength = mappingObjs.length;
    for (var i = 0; i < mappingObjsLength; i++) {
        if (typeof mappingObjs[i].companyId !== 'undefined' && mappingObjs[i].companyId !== null) {
            $canvasMap.setLayer('table' + mappingObjs[i].tableNumber + 'Box', {
                fillStyle: '#0F0'
            });
        }
    }
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