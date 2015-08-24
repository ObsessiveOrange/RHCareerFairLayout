var careerFairData;
var $canvasMap;
var scaling = 1;
var selectionToolActive = true;
var mergeToolActive = false;
var splitToolActive = false;
var selectedTable1 = null;
var tableLocations = [];
var companyLocations = [];
var mousedOverTable = null;
var clickedColor = "#0BF";
var hoverClickedColor = "#06B";
var usedColor = "#0F0";
var hoverUsedColor = "#0B0";
var unusedColor = "#DDD";
var hoverUnusedColor = "#BBB";
(window.setup = function() {
    sendGetRequest({
        url: "/api/data/all",
        data: {
            year: getSelectedYear(),
            quarter: getSelectedQuarter()
        },
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
window.cleanup = function() {
    $(".toolsItems").off();
    $("#companyListContainer").off();
    $(".submissionControlCancel").off();
    $(".submissionControlSubmit").off();
};

function setupLinks() {
    $(".toolsItem").click(function(event) {
        //
        //cleanup current selections
        setTableColor(selectedTable1);
        redrawTable(selectedTable1);
        selectedTable1 = null;
        //
        //set new tool active status
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
    $(".submissionControlCancel").click(function(event) {
        loadContentWithJS("layout");
    });
    $(".submissionControlSubmit").click(function(event) {
        submitUpdatedMappings();
    });
}

function setupTableMappings() {
    var s1 = Number(careerFairData.term.layout_Section1);
    var s2 = Number(careerFairData.term.layout_Section2);
    var s2Rows = Number(careerFairData.term.layout_Section2_Rows);
    var s2PathWidth = Number(careerFairData.term.layout_Section2_PathWidth);
    var s3 = Number(careerFairData.term.layout_Section3);
    //create temp var for total count
    var totalCount = 0;
    //sum up tables
    totalCount += s1;
    totalCount += s2 * 2;
    totalCount += (s2 - s2PathWidth) * (s2Rows - 2);
    totalCount += s3;
    for (var i = 0; i < totalCount; i++) {
        if ((typeof careerFairData.tableMappingList[i]) != "undefined") {
            if (careerFairData.tableMappingList[i].size > 1) {
                totalCount -= careerFairData.tableMappingList[i].size - 1;
            }
        } else {
            careerFairData.tableMappingList.push({
                //i counts from 
                id: i + 1,
                companyId: null,
                size: 1
            });
        }
    }
    careerFairData.tableMappingList = new NWayMap(careerFairData.tableMappingList, ["id", "companyId"]);
}

function submitUpdatedMappings() {
    var allCompanies = Object.keys(careerFairData.companyMap);
    for (var i = 0; i < allCompanies.length; i++) {
        if (careerFairData.tableMappingList.get("companyId", allCompanies[i]) === null) {
            alert("Aborting - Not all companies have been assigned. Main page will not work.");
            return;
        }
    }
    // alert("Not implemented yet!");
    $.ajax({
        url: "/api/data/table_mapping/all",
        type: "POST",
        contentType: "application/json",
        processData: false,
        data: JSON.stringify({
            updatedMappings: careerFairData.tableMappingList.getValues("id")
        }),
        error: function(_, textStatus, errorThrown) {
            console.log(textStatus + ":" + errorThrown);
        },
        success: function(response, textStatus) {
            alert(response.message);
        }
    });
}

function populateCompanyList() {
    Object.keys(careerFairData.companyMap).forEach(function(companyId) {
        var company = careerFairData.companyMap[companyId];
        var currentTable = company.id;
        $("#companyListContainer").append("<tr class='level2 companyListItem' id='companyListRow_" + companyId + "'><td id='companyListName_" + companyId + "' class='companyListNameColumn'>" + company.name + "</td><td id='companyListTableSelector_" + companyId + "'><input type='text' class='companyListTableInput' id='companyListTableInput_" + companyId + "' value='" + (careerFairData.tableMappingList.get("companyId", companyId) === null ? "" : careerFairData.tableMappingList.get("companyId", companyId).id) + "' size='2' maxlength='4'/></td></tr>");
        if (Number($("#companyListTableInput_" + companyId).val()) === 0) {
            $("#companyListTableInput_" + companyId).parent().parent().css("background", "#F00");
        }
    });
    $("#companyListContainer").on("change", ".companyListTableInput", function(event) {
        var id = Number($(event.currentTarget).val());
        var companyId = Number($(event.currentTarget).prop("id").replace("companyListTableInput_", ""));
        moveCompany(companyId, id);
    });
    //add null value
    // $(".companyListTableDropdown").append("<option value='-1'></option>");
    // for (var i = 1; i <= Object.keys(careerFairData.tableMappingList).length; i++) {
    //     $(".companyListTableDropdown").append("<option value='-1'>" + i + "</option>");
    // }
}

function moveCompany(companyId, newTable) {
    if (newTable > 0 && newTable <= careerFairData.tableMappingList.getKeys("id").length) {
        var newTableMapping = careerFairData.tableMappingList.get("id", newTable);
        var oldTableMapping = careerFairData.tableMappingList.get("companyId", companyId);
        if (oldTableMapping === null || oldTableMapping.id != newTable) {
            careerFairData.tableMappingList.remove("companyId", companyId);
            if (typeof(newTableMapping.companyId) !== "undefined" && newTableMapping.companyId !== null) {
                if (confirm("Overwrite another entry?")) {
                    var orphanedCompanyId = newTableMapping.companyId;
                    careerFairData.tableMappingList.remove("id", newTable);
                    careerFairData.tableMappingList.remove("companyId", companyId);
                    newTableMapping.companyId = companyId;
                    careerFairData.tableMappingList.put(newTableMapping);
                    if (oldTableMapping !== null) {
                        oldTableMapping.companyId = null;
                        careerFairData.tableMappingList.put(oldTableMapping);
                    }
                    $("#companyListTableInput_" + orphanedCompanyId).val("");
                    $("#companyListTableInput_" + orphanedCompanyId).parent().parent().css("background", "#F00");
                } else {
                    return false;
                }
            } else {
                careerFairData.tableMappingList.remove("id", newTable);
                careerFairData.tableMappingList.remove("companyId", companyId);
                newTableMapping.companyId = companyId;
                careerFairData.tableMappingList.put(newTableMapping);
                if (oldTableMapping !== null) {
                    oldTableMapping.companyId = null;
                    careerFairData.tableMappingList.put(oldTableMapping);
                }
            }
            $("#companyListTableInput_" + companyId).parent().parent().css("background", "");
        }
    } else {
        if (newTable !== 0) {
            alert("Invalid table number entered: " + newTable);
            $(event.currentTarget).val("");
            return false;
        }
        //
        //if nothing entered ("") or actually entered 0, remove company from map.
        var oldTableMapping = careerFairData.tableMappingList.remove("companyId", companyId);
        if (oldTableMapping !== null) {
            oldTableMapping.companyId = null;
            careerFairData.tableMappingList.put(oldTableMapping);
        }
        $("#companyListTableInput_" + orphanedCompanyId).parent().parent().css("background", "#F00");
    }
    highlightUsedTables();
    var allCompanies = Object.keys(careerFairData.companyMap);
    var allCompaniesMapped = true;
    for (var i = 0; i < allCompanies.length; i++) {
        if (careerFairData.tableMappingList.get("companyId", allCompanies[i]) === null) {
            allCompaniesMapped = false;
        }
    }
    if (allCompaniesMapped) {
        $("#companyListGroupHeader").css("background", "");
    } else {
        $("#companyListGroupHeader").css("background", "#F00");
    }
    return true;
}

function updateCompanyList() {
    var companyIDs = Object.keys(careerFairData.companyMap);
    var companyListLength = companyIDs.length;
    for (var i = 0; i < companyListLength; i++) {
        var companyId = companyIDs[i];
        var mapping = careerFairData.tableMappingList.get("companyId", companyId);
        if (mapping !== null) {
            $("#companyListTableInput_" + companyId).val(mapping.id);
        } else {
            $("#companyListTableInput_" + companyId).val("");
        }
    }
    //add null value
    // $(".companyListTableDropdown").append("<option value='-1'></option>");
    // for (var i = 1; i <= Object.keys(careerFairData.tableMappingList).length; i++) {
    //     $(".companyListTableDropdown").append("<option value='-1'>" + i + "</option>");
    // }
}
// function bringTableToFront(id) {
//     bringLayerToFront("table" + id + "Box");
//     bringLayerToFront("table" + id + "Text");
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

function redrawTable(id) {
    $canvasMap.drawLayer("table" + id + "Box");
    $canvasMap.drawLayer("table" + id + "Text");
}

function checkOverlap(layer1, layer2) {
    if (layer1.eventX >= layer2.x && layer1.eventX <= (layer2.x + layer2.width) && layer1.eventY >= layer2.y && layer1.eventY <= (layer2.y + layer2.height)) {
        return true;
    }
    return false;
}

function mergeTables(table1, table2) {
    if (Math.abs(table2 - table1) !== 1 || tableLocations[table1].group !== tableLocations[table2].group) {
        if (selectedTable1 == table1) {
            selectedTable1 = null;
            setTableColor(table1);
            redrawTable(table1);
        } else {
            selectedTable1 = null;
            setTableColor(table2);
            redrawTable(table2);
        }
        if (table1 != table2) {
            alert("Tables must be next to each other to be merged.");
        }
        return;
    }
    //if there is a company in table2, warn that it will remove it.
    if ((typeof careerFairData.tableMappingList.get("id", table2).companyId) !== 'undefined' && careerFairData.tableMappingList.get("id", table2).companyId !== null) {
        alert("Warning: The company in the higher numbered table will be evicted.");
        $("#companyListTableInput_" + careerFairData.tableMappingList.get("id", table2).companyId).parent().parent().css("background", "#F00");
        $("#companyListGroupHeader").css("background", "#F00");
    }
    careerFairData.tableMappingList.get("id", table1).size += careerFairData.tableMappingList.get("id", table2).size;
    //remove higher-numbered table, shift everything down by 1 to account for "removed" table that is being merged.
    careerFairData.tableMappingList.remove("id", table2);
    for (var i = table2; i <= careerFairData.tableMappingList.getKeys("id").length; i++) {
        var prev = careerFairData.tableMappingList.remove("id", i + 1);
        prev.id -= 1;
        careerFairData.tableMappingList.put(prev);
    }
    mousedOverTable = table1;
    selectedTable1 = null;
    // delete careerFairData.tableMappingList.get("id", Object.keys(careerFairData.tableMappingList).length];
    $canvasMap.removeLayers();
    generateTableLocations();
    drawTables();
    updateCompanyList();
}

function splitTable(table) {
    if (careerFairData.tableMappingList.get("id", table).size === 1) {
        return;
    }
    var shiftCount = careerFairData.tableMappingList.get("id", table).size - 1;
    careerFairData.tableMappingList.get("id", table).size = 1;
    for (var i = careerFairData.tableMappingList.getKeys("id").length; i > table; i--) {
        var prev = careerFairData.tableMappingList.remove("id", i);
        prev.id += shiftCount;
        careerFairData.tableMappingList.put(prev);
    }
    for (var i = 0; i < shiftCount; i++) {
        careerFairData.tableMappingList.put({
            id: table + 1 + i,
            size: 1
        });
    }
    mousedOverTable = null;
    $canvasMap.removeLayers();
    generateTableLocations();
    drawTables();
    updateCompanyList();
}
// function restoreTableLocation(id) {
//     var location = tableLocations[careerFairData.layoutVars.tableLocationMapping[id].location];
//     $canvasMap.setLayer("table" + id + "Box", {
//         x: location.x,
//         y: location.y
//     });
//     $canvasMap.setLayer("table" + id + "Text", {
//         x: location.x + location.width / 2,
//         y: location.y + location.height / 2
//     });
// }
//
//draw tables and table numbers
function drawRect(tableObj) {
    //
    //draw id in box for easy reading.
    if (tableObj.id !== 0 && tableObj.id <= careerFairData.tableMappingList.getKeys("id").length) {
        $canvasMap.drawRect({
            layer: true,
            name: 'table' + tableObj.id + 'Box',
            strokeStyle: '#000',
            strokeWidth: scaling,
            data: {
                id: tableObj.id
            },
            x: tableObj.x,
            y: tableObj.y,
            width: tableObj.width,
            height: tableObj.height,
            fromCenter: false,
            click: function(layer) {
                if (mergeToolActive || selectionToolActive) {
                    var id = layer.data.id;
                    if (selectedTable1 === null) {
                        if (selectionToolActive && ((typeof careerFairData.tableMappingList.get("id", id).companyId) === 'undefined' || careerFairData.tableMappingList.get("id", id).companyId === null)) {
                            return;
                        }
                        selectedTable1 = id;
                        setTableColor(selectedTable1);
                        redrawTable(selectedTable1);
                    } else {
                        if (mergeToolActive) {
                            var table1 = id > selectedTable1 ? selectedTable1 : id;
                            var table2 = id > selectedTable1 ? id : selectedTable1;
                            mergeTables(table1, table2);
                        }
                        if (selectionToolActive) {
                            var table1 = selectedTable1;
                            var table2 = layer.data.id;
                            if (moveCompany(careerFairData.tableMappingList.get("id", table1).companyId, id)) {
                                selectedTable1 = null;
                                setTableColor(table1);
                                setTableColor(table2);
                                redrawTable(table1);
                                redrawTable(table2);
                            }
                        }
                    }
                } else if (splitToolActive) {
                    splitTable(layer.data.id);
                }
            },
            mouseover: function(layer) {
                var id = layer.data.id;
                mousedOverTable = id;
                setTableColor(id);
                redrawTable(id);
                drawTag(id);
            },
            mouseout: function(layer) {
                var id = layer.data.id;
                mousedOverTable = null;
                setTableColor(id);
                redrawTable(id);
                removeTag();
            }
        });
        $canvasMap.drawText({
            layer: true,
            name: 'table' + tableObj.id + 'Text',
            fillStyle: '#000000',
            data: {
                id: tableObj.id
            },
            x: tableObj.x + tableObj.width / 2,
            y: tableObj.y + tableObj.height / 2,
            fontSize: tableObj.height / tableObj.yScaling / 2,
            text: tableObj.id,
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
                id: tableObj.id
            },
            x: tableObj.x,
            y: tableObj.y,
            width: tableObj.width,
            height: tableObj.height,
            fromCenter: false
        });
    }
}

function removeTag() {
    $canvasMap.removeLayer("tagShape");
    $canvasMap.removeLayer("tagText");
}

function drawTag(id) {
    if ((typeof careerFairData.tableMappingList.get("id", id).companyId) === 'undefined' || careerFairData.tableMappingList.get("id", id).companyId === null) {
        return;
    }
    var s1 = Number(careerFairData.term.layout_Section1);
    var s2 = Number(careerFairData.term.layout_Section2);
    var s2Rows = Number(careerFairData.term.layout_Section2_Rows);
    var s2PathWidth = Number(careerFairData.term.layout_Section2_PathWidth);
    var s3 = Number(careerFairData.term.layout_Section3);
    var hrzCount = s2 + Math.min(s1, 1) + Math.min(s3, 1);
    var vrtCount = Math.max(s1, s3);
    var tableWidth = unitX * (90 - Math.min(s1, 1) * 5 - Math.min(s3, 1) * 5) / hrzCount;
    var tableHeight = unitY * 70 / vrtCount;
    var pathWidth = (unitY * 70 - s2Rows * tableHeight) / (s2Rows / 2);
    var points = {};

    var group = tableLocations[id].group;
    var section = Number(group.charAt(group.indexOf("section") + "section".length));
    switch (section) {
        case 1:
            var startX = tableLocations[id].x + (tableLocations[id].xScaling * tableWidth);
            var startY = tableLocations[id].y + (tableLocations[id].yScaling * tableHeight) / 2;
            points = {
                x1: startX,
                y1: startY,
                x2: startX + tableWidth / 2,
                y2: startY - tableWidth / 2,
                x3: startX + unitY * 25,
                y3: startY - tableWidth / 2,
                x4: startX + unitY * 25,
                y4: startY + tableWidth / 2,
                x5: startX + tableWidth / 2,
                y5: startY + tableWidth / 2,
                textX: startX + tableWidth / 4 + unitY * 12.5,
                textY: startY
            };
            break;
        case 2:
            var startX = tableLocations[id].x + (tableLocations[id].xScaling * tableWidth) / 2;
            var startY = tableLocations[id].y + (tableLocations[id].yScaling * tableHeight);
            points = {
                x1: startX,
                y1: startY,
                x2: startX + tableWidth / 2,
                y2: startY + tableWidth / 2,
                x3: startX + tableWidth / 2,
                y3: startY + unitY * 25,
                x4: startX - tableWidth / 2,
                y4: startY + unitY * 25,
                x5: startX - tableWidth / 2,
                y5: startY + tableWidth / 2,
                textX: startX,
                textY: startY + tableWidth / 4 + unitY * 12.5
            };
            break;
        case 3:
            var startX = tableLocations[id].x;
            var startY = tableLocations[id].y + (tableLocations[id].yScaling * tableHeight) / 2;
            points = {
                x1: startX,
                y1: startY,
                x2: startX - tableWidth / 2,
                y2: startY - tableWidth / 2,
                x3: startX - unitY * 25,
                y3: startY - tableWidth / 2,
                x4: startX - unitY * 25,
                y4: startY + tableWidth / 2,
                x5: startX - tableWidth / 2,
                y5: startY + tableWidth / 2,
                textX: startX - tableWidth / 4 - unitY * 12.5,
                textY: startY
            };
            break;
        default:
            break;
    }
    $canvasMap.drawLine({
        layer: true,
        name: 'tagShape',
        strokeStyle: '#000',
        strokeWidth: 1,
        fillStyle: '#AAF',
        closed: true,
        x1: points.x1,
        y1: points.y1,
        x2: points.x2,
        y2: points.y2,
        x3: points.x3,
        y3: points.y3,
        x4: points.x4,
        y4: points.y4,
        x5: points.x5,
        y5: points.y5,
    });
    var lines = parseInt(tableWidth / $canvasMap.measureText({
        fontSize: tableHeight / 2,
        text: 'text'
    }).height, 10);
    $canvasMap.drawText({
        layer: true,
        name: 'tagText',
        fillStyle: '#000',
        x: points.textX,
        y: points.textY,
        maxWidth: unitY * 25 - tableWidth / 2,
        fontSize: tableHeight / 2,
        text: getWrappedText({
            text: careerFairData.companyMap[careerFairData.tableMappingList.get("id", id).companyId].name,
            maxWidth: pathWidth - tableWidth / 2,
            maxLines: lines,
            fontSize: tableHeight / 2,
            breakWord: true
        }),
        // Rotate the text by 30 degrees
        rotate: section == 2 ? 90 : 0
    });
    $canvasMap.drawLayers();
}
//
//generate positions of all tables.
function generateTableLocations() {
    //
    //reset tableLocations variable - may have changed
    tableLocations = [];
    //
    //convenience assignments
    var s1 = Number(careerFairData.term.layout_Section1);
    var s2 = Number(careerFairData.term.layout_Section2);
    var s2Rows = Number(careerFairData.term.layout_Section2_Rows);
    var s2PathWidth = Number(careerFairData.term.layout_Section2_PathWidth);
    var s3 = Number(careerFairData.term.layout_Section3);
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
    var id = 1;
    var size = 1;
    var offsetX = 5 * unitX;
    //
    // section 1
    if (s1 > 0) {
        for (var i = 0; i < s1;) {
            size = careerFairData.tableMappingList.get("id", id).size;
            tableLocations[id] = {
                id: id,
                x: offsetX,
                y: 5 * unitY + i * tableHeight,
                width: tableWidth,
                height: tableHeight * size,
                xScaling: 1,
                yScaling: size,
                group: "section1"
            };
            i += size;
            id++;
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
                    size = careerFairData.tableMappingList.get("id", id).size;
                    tableLocations[id] = {
                        id: id,
                        x: offsetX + (j * tableWidth),
                        y: 5 * unitY + Math.floor((i + 1) / 2) * pathWidth + i * tableHeight,
                        width: tableWidth * size,
                        height: tableHeight,
                        xScaling: size,
                        yScaling: 1,
                        group: "section2row" + i
                    };
                    j += size;
                    id++;
                }
            }
            //
            //inner rows need to have walkway halfway through
            else {
                var leftTables = Math.floor((s2 - s2PathWidth) / 2);
                var rightTables = s2 - s2PathWidth - leftTables;
                for (var j = 0; j < leftTables;) {
                    size = careerFairData.tableMappingList.get("id", id).size;
                    tableLocations[id] = {
                        id: id,
                        x: offsetX + (j * tableWidth),
                        y: 5 * unitY + Math.floor((i + 1) / 2) * pathWidth + i * tableHeight,
                        width: tableWidth * size,
                        height: tableHeight,
                        xScaling: size,
                        yScaling: 1,
                        group: "section2row" + i + "L"
                    };
                    j += size;
                    id++;
                }
                for (var j = 0; j < rightTables;) {
                    size = careerFairData.tableMappingList.get("id", id).size;
                    tableLocations[id] = {
                        id: id,
                        x: offsetX + ((leftTables + s2PathWidth + j) * tableWidth),
                        y: 5 * unitY + Math.floor((i + 1) / 2) * pathWidth + i * tableHeight,
                        width: tableWidth * size,
                        height: tableHeight,
                        xScaling: size,
                        yScaling: 1,
                        group: "section2row" + i + "R"
                    };
                    j += size;
                    id++;
                }
            }
        }
        offsetX += s2 * tableWidth + 5 * unitX;
    }
    //
    // section 3
    if (s3 > 0) {
        for (var i = 0; i < s3;) {
            size = careerFairData.tableMappingList.get("id", id).size;
            tableLocations[id] = {
                id: id,
                x: offsetX,
                y: 5 * unitY + i * tableHeight,
                width: tableWidth,
                height: tableHeight * size,
                xScaling: 1,
                yScaling: size,
                group: "section3"
            };
            i += size;
            id++;
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
        id: 0,
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
        id: 0,
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

function setTableColor(id) {
    if (id === selectedTable1) {
        if (id === mousedOverTable) {
            $canvasMap.setLayer('table' + id + 'Box', {
                fillStyle: hoverClickedColor
            });
        } else {
            $canvasMap.setLayer('table' + id + 'Box', {
                fillStyle: clickedColor
            });
        }
    } else if (typeof careerFairData.tableMappingList.get("id", id).companyId !== 'undefined' && careerFairData.tableMappingList.get("id", id).companyId !== null) {
        if (id === mousedOverTable) {
            $canvasMap.setLayer('table' + id + 'Box', {
                fillStyle: hoverUsedColor
            });
        } else {
            $canvasMap.setLayer('table' + id + 'Box', {
                fillStyle: usedColor
            });
        }
    } else {
        if (id === mousedOverTable) {
            $canvasMap.setLayer('table' + id + 'Box', {
                fillStyle: hoverUnusedColor
            });
        } else {
            $canvasMap.setLayer('table' + id + 'Box', {
                fillStyle: unusedColor
            });
        }
    }
}

function highlightUsedTables() {
    var mappingObjs = careerFairData.tableMappingList.getValues("id");
    var mappingObjsLength = mappingObjs.length;
    for (var i = 0; i < mappingObjsLength; i++) {
        setTableColor(mappingObjs[i].id);
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
// Wraps a string of text within a defined width
function getWrappedText(params) {
    var defaults = $.jCanvas.defaults;
    Object.keys(params).forEach(function(element, index) {
        defaults[element] = params[element];
    });
    params = defaults;
    var ctx = $canvasMap[0].getContext('2d');
    ctx.textBaseline = params.baseline;
    ctx.textAlign = params.align;
    // Otherwise, use the given font attributes
    if (!isNaN(Number(params.fontSize))) {
        // Give font size units if it doesn't have any
        params.fontSize += 'px';
    }
    // Set font using given font properties
    ctx.font = params.fontStyle + ' ' + params.fontSize + ' ' + params.fontFamily;
    var allText = params.text,
        // Maximum line width ( optional )
        maxWidth = params.maxWidth,
        // Lines created by manual line breaks ( \n )
        manualLines = allText.split('\n'),
        // All lines created manually and by wrapping
        allLines = [],
        // Other variables
        lines, line, l,
        text, words, w;
    // Loop through manually-broken lines
    for (l = 0; l < manualLines.length; l += 1) {
        text = manualLines[l];
        // Split line into list of words
        words = text.split(' ');
        lines = [];
        line = '';
        // If text is short enough initially
        // Or, if the text consists of only one word
        if (words.length === 1 || ctx.measureText(text).width <= maxWidth) {
            // No need to wrap text
            lines = [text];
        } else {
            // Wrap lines
            for (w = 0; w < words.length; w += 1) {
                // If word is too long, and break-word is true, break the word and hyphenate it.
                if (((ctx.measureText(words[w]).width > maxWidth) || (ctx.measureText(line + words[w]).width > maxWidth && (typeof params.maxLines) !== 'undefined' && Number(params.maxLines) === (lines.length + 1))) && (typeof params.breakWord) !== 'undefined' && params.breakWord === true) {
                    // Add words to line until the line is too wide
                    if (ctx.measureText(line + words[w].substr(0, 1) + '-').width <= maxWidth) {
                        while (ctx.measureText(line + words[w].substr(0, 1) + '-').width <= maxWidth) {
                            line += words[w].substr(0, 1);
                            words[w] = words[w].substring(1);
                        }
                        line += '-';
                    }
                    lines.push(line.trim());
                    // Start new line and repeat process
                    line = '';
                    w--;
                } else {
                    // Once line gets too wide, push word to next line
                    if (ctx.measureText(line + words[w]).width > maxWidth) {
                        // This check prevents empty lines from being created
                        if (line !== '') {
                            lines.push(line.trim());
                        }
                        // Start new line and repeat process
                        line = '';
                    }
                    // Add words to line until the line is too wide
                    if (ctx.measureText(words[w]).width <= maxWidth) {
                        line += words[w];
                        // Do not add a space after the last word
                        if (w !== (words.length - 1)) {
                            line += ' ';
                        }
                    } else {
                        w--;
                    }
                }
            }
            // The last word should always be pushed
            lines.push(line.trim());
        }
        //trim down to maxLines, if set.
        if ((typeof params.maxLines) !== 'undefined' && Number(params.maxLines) < lines.length) {
            lines.splice(Number(params.maxLines), lines.length);
            if (ctx.measureText(lines[lines.length - 1] + "...").width <= maxWidth) {
                lines[lines.length - 1] = lines[lines.length - 1].trim() + "...";
            } else if (ctx.measureText(lines[lines.length - 1] + "..").width <= maxWidth) {
                lines[lines.length - 1] = lines[lines.length - 1].substr(0, lines[lines.length - 1].length - 1).trim() + "...";
            } else if (ctx.measureText(lines[lines.length - 1] + ".").width <= maxWidth) {
                lines[lines.length - 1] = lines[lines.length - 1].substr(0, lines[lines.length - 1].length - 2).trim() + "...";
            } else {
                lines[lines.length - 1] = lines[lines.length - 1].substr(0, lines[lines.length - 1].length - 3).trim() + "...";
            }
        }
        // Remove extra space at the end of each line
        allLines = allLines.concat(lines.join('\n'));
    }
    return allLines;
}