/**
 * Javascript to get, process and display dynamic data for Career Fair Layout
 *
 * Creator: Benedict Wong, 2015
 */
//
// Set strict mode on.
"use strict";
//
// Initialize variables
var careerFairData;
var companyList;
var tableLocations;
var selectedCompanyIds;
var filteredCompanyIds = [];
var filters;
var $canvasMap;
var unitX;
var unitY;
var scaling = 2;
var clearCacheFlag;
$(document).ready(function() {
    //
    //set size and width of canvas elements and containing div
    $canvasMap = $("#canvasMap");
    var $container = $("#mapContainer");
    var containerWidth = $container.width() * scaling;
    var containerHeight = $container.width() * (scaling / 2);
    $container.prop("height", containerHeight);
    $canvasMap.prop("width", containerWidth).prop("height", containerHeight);
    //
    //try to retrieve data from persistent storage
    loadAfterPageSwitch();
    //
    //if CareerFairData has not been loaded, or it's date is too long ago, reload it. Currently 30 min (1000ms * 60s * 30m)
    if (!careerFairData || (new Date().getTime() - careerFairData.lastFetchTime) > 30 * 60 * 1000) {
        //
        //if other variables have not been created/set, do it now.
        if (!tableLocations || !selectedCompanyIds || !filteredCompanyIds || !filters) {
            tableLocations = {};
            selectedCompanyIds = [];
            filteredCompanyIds = [];
            filters = {};
        }
        //
        //get careerFairData - calls setupPage();
        getNewData();
    }
    //
    //else, if careerFairData has been loaded, just setup the page using cached data
    else {
        careerFairData.termVars.layout.tableMappings = new NWayMap(careerFairData.termVars.layout.tableMappings);
        setupPage();
    }
    $("#companySearchBar").click(function() {
        $('html, body').animate({
            scrollTop: $("#companySearchBar").offset().top
        }, 1000);
    });
    //
    //save data when link out of page clicked.
    $("#filterBtn").on("click", function(event) {
        if (typeof clearCacheFlag === 'undefined' || !clearCacheFlag) {
            prepareForPageSwitch();
        } else {
            PersistentStorage.clear();
        }
        event.stopPropagation();
    });
    //
    //handle bulk-selector buttons
    $("#selectionButtons").on('click', '.button', function(event) {
        switch ($(this).attr('data-btnAction')) {
            case "select":
                //
                //for performance (?) reasons, only mark it selected if it has not already been marked. Otherwise, would have to iterate through selected array multiple unnecessary times.
                filteredCompanyIds.forEach(function(id) {
                    if (!careerFairData.companies[id].checked) {
                        markCheckboxChecked(id);
                    }
                });
                break;
            case "invert":
                //
                //just call toggleCheckbox; will automatically invert.
                filteredCompanyIds.forEach(function(id) {
                    toggleCheckbox(id);
                });
                break;
            case "deselect":
                //
                //for performance (?) reasons, only mark it selected if it has not already been marked. Otherwise, would have to iterate through selected array multiple unnecessary times.
                filteredCompanyIds.forEach(function(id) {
                    if (careerFairData.companies[id].checked) {
                        markCheckboxUnchecked(id);
                    }
                });
                break;
            default:
                break;
        }
        //
        //finally, remember to update the map
        highlightTables();
    });
});
//
//kinda a debug function for use while active development in progress
function clearCache() {
    clearCache = true;
    PersistentStorage.clear();
}
//
//load data from persistent storage
function loadAfterPageSwitch() {
    careerFairData = PersistentStorage.retrieveObject("careerFairData");
    tableLocations = PersistentStorage.retrieveObject("tableLocations");
    selectedCompanyIds = PersistentStorage.retrieveObject("selectedCompanyIds");
    filteredCompanyIds = PersistentStorage.retrieveObject("filteredCompanyIds");
    filters = PersistentStorage.retrieveObject("filters");
}
//
//save data to persistent storage
function prepareForPageSwitch() {
    PersistentStorage.storeObject("careerFairData", careerFairData);
    PersistentStorage.storeObject("tableLocations", tableLocations);
    PersistentStorage.storeObject("selectedCompanyIds", selectedCompanyIds);
    PersistentStorage.storeObject("filteredCompanyIds", filteredCompanyIds);
    PersistentStorage.storeObject("filters", filters);
}
//
//Get data from server, setup page
function getNewData() {
    sendGetRequest({
        url: "/api/data?method=getData",
        successHandler: function(data) {
            //
            //jQuery auto-parses the json data, since the content type is application/json (may switch to JSONP eventually... how does that affect this?)
            careerFairData = data;
            //
            //set last fetch time, so we know to refresh beyond a certain validity time
            careerFairData.lastFetchTime = new Date().getTime();
            setupTableMappings();
            setupPage();
        }
    });
}
// 
//setup the page
function setupPage() {
    //
    //update the title
    $("span#careerFairDescription").html(careerFairData.title);
    //
    //setup company list    
    updateCompanyList();
    //
    //Create options, generate List.js object for searching
    var options = {
        valueNames: ['companyListHighlight', 'companyListCompanyId', 'companyListCompanyName', 'companyListTable', 'companyListInfo'],
        page: filteredCompanyIds.length
    };
    companyList = new List('companyListContainer', options);
    //
    //setup the map for the first time
    generateTableLocations();
    drawTables();
    highlightTables();
    initTutorials("Main");
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
            careerFairData.termVars.layout.tableMappings.push({
                //i counts from 
                tableNumber: i + 1,
                companyId: null,
                tableSize: 1
            });
        }
    }
    careerFairData.termVars.layout.tableMappings = new NWayMap(careerFairData.termVars.layout.tableMappings, ["tableNumber", "companyId"]);
}

function updateCompanyList() {
    //
    //cache the element we are appending to, so that it doesn't have to be created multiple times
    var companyListBody = $("#companyListBody");
    //
    //if no filters applied or only has "changed" flag, skip all the checks for performance reasons.
    //will have either 0, 1, or n+1 elements, where n is the number of types of filters.
    if ((typeof filters.changed) == "undefined" || filters.changed == false) {
        filteredCompanyIds = Object.keys(careerFairData.companies);
        selectedCompanyIds = filteredCompanyIds.slice();
    }
    //
    //otherwise, make sure all companies conform to filters
    else {
        //
        //Only recompute this if filters have changed. Otherwise, just use previous data
        if (filters.changed) {
            //
            //clear filteredCompanyIds
            filteredCompanyIds = [];
            //
            //Iterate through all the companies,
            Object.keys(careerFairData.companies).forEach(function(companyId) {
                //
                //get the actual company object,
                var company = careerFairData.companies[companyId];
                var showCompany = true;
                //
                //check if companies are in at least one of the filters in each type
                Object.keys(filters).forEach(function(filterType) {
                    //
                    //ignore the changed flag - only do the ones that are arrays.
                    if (Array.isArray(filters[filterType])) {
                        //
                        //if the filter length is 0, no filters are applied in that type group - automatically true.
                        if (filters[filterType].length === 0) {
                            return;
                            //
                            //else, make sure the intersection of the filters (categories) selected in the type group and the categories the company is in overlap 
                            //otherwise, set it to false
                        } else if (_.intersection(filters[filterType], company.categories).length === 0) {
                            showCompany = false;
                        }
                    }
                });
                //
                //if the company is valid in the context of all the filters, then add it to the filtered company list
                if (showCompany) {
                    filteredCompanyIds.addToOrderedSet(companyId);
                }
            });
            //
            //reset the changed flag
            filters.changed = false;
            //
            //default behavior on filter change is to select all of the companies.
            selectedCompanyIds = filteredCompanyIds.slice();
        }
    }
    //
    //sort filteredCompanyIds before creating the array
    filteredCompanyIds.sort(function(a, b) {
        var o1 = careerFairData.companies[a].name.toLowerCase();
        var o2 = careerFairData.companies[b].name.toLowerCase();
        var p1 = Number(careerFairData.termVars.layout.tableMappings.get("companyId", a) === null ? 0 : careerFairData.termVars.layout.tableMappings.get("companyId", a).tableNumber);
        var p2 = Number(careerFairData.termVars.layout.tableMappings.get("companyId", b) === null ? 0 : careerFairData.termVars.layout.tableMappings.get("companyId", b).tableNumber);
        if (o1 < o2) return -1;
        if (o1 > o2) return 1;
        if (p1 < p2) return -1;
        if (p1 > p2) return 1;
        return 0;
    });
    //
    //add each company that is valid in the context of the selected filters to the list
    filteredCompanyIds.forEach(function(companyId) {
        var company = careerFairData.companies[companyId];
        // Not in use - includes [i], which is currently not supported.
        //companyListBody.append("<tr><td class='center companyListHighlight' onclick='toggleCheckbox(" + company.id + ")' id='showOnMapCheckbox_" + company.id + "'>☐</td><td class='companyListCompanyId'>" + company.id + "</td><td class='companyListCompanyName' onclick='toggleCheckbox(" + company.id + ")'>" + company.name + "</td><td class='center companyListTable'>" + company.tableNumber + "</td><td class='center companyListInfo'>[i]</td></tr>");
        companyListBody.append("<tr><td class='center companyListHighlight' onclick='toggleCheckbox(" + company.id + ")' id='showOnMapCheckbox_" + company.id + "'>☐</td><td class='companyListCompanyId'>" + company.id + "</td><td class='companyListCompanyName' onclick='toggleCheckbox(" + company.id + ")'>" + company.name + "</td><td class='center companyListTable'>" + careerFairData.termVars.layout.tableMappings.get("companyId", company.id).tableNumber + "</td></tr>");
    });
    //
    //Check the ones that are in the list - if no filter change, will check previously selected entries only.
    selectedCompanyIds.forEach(function(companyId) {
        markCheckboxChecked(companyId);
    });
    _.difference(filteredCompanyIds, selectedCompanyIds).forEach(function(companyId) {
        markCheckboxUnchecked(companyId);
    });
}
//
//select the checkbox for compnay with given id
function markCheckboxChecked(id) {
    //change icon to checked
    $("#showOnMapCheckbox_" + id).text("☑");
    //add to set of selected companies
    selectedCompanyIds.addToOrderedSet(id);
    careerFairData.companies[id].checked = true;
}
//
//deselect the checkbox for compnay with given id
function markCheckboxUnchecked(id) {
    //change icon to unchecked
    $("#showOnMapCheckbox_" + id).text("☐");
    //remove from set of selected companies
    selectedCompanyIds.removeFromOrderedSet(id);
    careerFairData.companies[id].checked = false;
}
//
//toggle the checkbox
function toggleCheckbox(id) {
    //
    //toggle based on current text value
    if (careerFairData.companies[id].checked) {
        markCheckboxUnchecked(id);
        //
        //highlight newly checked checkbox
        highlightTable(id, "#EEE");
    } else {
        markCheckboxChecked(id);
        //
        //un-highlight newly checked checkbox
        highlightTable(id, "#0F0");
    }
}
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
            fromCenter: false
            // click: function(layer) {
            //     if (mergeToolActive) {
            //         var tableId = layer.data.tableId;
            //         if (mergeTable1 === null) {
            //             mergeTable1 = tableId;
            //             $canvasMap.setLayer(layer, {
            //                 fillStyle: highlightedColor
            //             });
            //             redrawTable(tableId);
            //         } else {
            //             var table1 = tableId > mergeTable1 ? mergeTable1 : tableId;
            //             var table2 = tableId > mergeTable1 ? tableId : mergeTable1;
            //             mergeTables(table1, table2);
            //         }
            //     } else if (splitToolActive) {
            //         splitTable(layer.data.tableId);
            //     }
            // },
            // mouseover: function(layer) {
            //     prevTableColor = layer.fillStyle;
            //     $canvasMap.setLayer(layer, {
            //         fillStyle: hoverColor
            //     });
            // },
            // mouseout: function(layer) {
            //     if (layer.fillStyle === hoverColor) {
            //         $canvasMap.setLayer(layer, {
            //             fillStyle: prevTableColor
            //         });
            //     }
            // }
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
            tableId++;
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
    highlightTables();
}
//
//Highlight all tables in selected companies array
function highlightTables() {
    selectedCompanyIds.forEach(function(id) {
        $canvasMap.setLayer('table' + careerFairData.termVars.layout.tableMappings.get("companyId", id).tableNumber + 'Box', {
            fillStyle: "#0F0"
        });
    });
    _.difference(filteredCompanyIds, selectedCompanyIds).forEach(function(id) {
        $canvasMap.setLayer('table' + careerFairData.termVars.layout.tableMappings.get("companyId", id).tableNumber + 'Box', {
            fillStyle: "transparent"
        });
    });
    $canvasMap.drawLayers();
}

function redrawTable(tableId) {
    $canvasMap.drawLayer("table" + tableId + "Box");
    $canvasMap.drawLayer("table" + tableId + "Text");
}
//
//highlight a specific table (used to minimize redrawing for toggling company selected)
function highlightTable(id, color) {
    $canvasMap.setLayer('table' + careerFairData.termVars.layout.tableMappings.get("companyId", id).tableNumber + 'Box', {
        fillStyle: color
    });
    redrawTable(careerFairData.termVars.layout.tableMappings.get("companyId", id).tableNumber);
}
//
//send get request
function sendGetRequest(requestObject) {
    $.ajax({
        url: requestObject.url,
        type: "GET",
        headers: requestObject.headers,
        data: requestObject.data,
        success: requestObject.successHandler
    });
}
//
//send post request
function sendPostRequest(requestObject) {
    $.ajax({
        url: requestObject.url,
        type: "POST",
        headers: requestObject.headers,
        data: requestObject.data,
        success: requestObject.successHandler
    });
}