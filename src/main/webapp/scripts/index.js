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
var selectedCompanyIDs
var filteredCompanyIDs = [];
var filters;
var $mapTables;
var $mapHighlights;
var unitX;
var unitY;
var scaling = 2;
var clearCacheFlag;
$(document).ready(function() {
    //
    //set size and width of canvas elements and containing div
    $mapTables = $("#mapTables");
    $mapHighlights = $("#mapHighlights");
    var $container = $("#mapContainer");
    var containerWidth = $container.width() * scaling;
    var containerHeight = $container.width() * (scaling / 2);
    $container.prop("height", containerHeight);
    $mapTables.prop("width", containerWidth).prop("height", containerHeight);
    $mapHighlights.prop("width", containerWidth).prop("height", containerHeight);
    //
    //try to retrieve data from persistent storage
    loadAfterPageSwitch();
    //
    //if CareerFairData has not been loaded, or it's date is too long ago, reload it.
    if (!careerFairData || (new Date().getTime() - careerFairData.lastFetchTime) > 30 * 60 * 1000) {
        //
        //if other variables have not been created/set, do it now.
        if (!tableLocations || !selectedCompanyIDs || !filteredCompanyIDs || !filters) {
            tableLocations = {};
            selectedCompanyIDs = [];
            filteredCompanyIDs = [];
            filters = {};
        }
        //
        //get careerFairData - calls setupPage();
        getNewData();
    }
    //
    //else, if careerFairData has been loaded, just setup the page using cached data
    else {
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
                filteredCompanyIDs.forEach(function(id) {
                    if (!careerFairData.companies[id].checked) {
                        markCheckboxChecked(id);
                    }
                });
                break;
            case "invert":
                //
                //just call toggleCheckbox; will automatically invert.
                filteredCompanyIDs.forEach(function(id) {
                    toggleCheckbox(id);
                });
                break;
            case "deselect":
                //
                //for performance (?) reasons, only mark it selected if it has not already been marked. Otherwise, would have to iterate through selected array multiple unnecessary times.
                filteredCompanyIDs.forEach(function(id) {
                    if (careerFairData.companies[id].checked) {
                        markCheckboxUnchecked(id);
                    }
                });
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
    selectedCompanyIDs = PersistentStorage.retrieveObject("selectedCompanyIDs");
    filteredCompanyIDs = PersistentStorage.retrieveObject("filteredCompanyIDs");
    filters = PersistentStorage.retrieveObject("filters");
}
//
//save data to persistent storage
function prepareForPageSwitch() {
    PersistentStorage.storeObject("careerFairData", careerFairData);
    PersistentStorage.storeObject("tableLocations", tableLocations);
    PersistentStorage.storeObject("selectedCompanyIDs", selectedCompanyIDs);
    PersistentStorage.storeObject("filteredCompanyIDs", filteredCompanyIDs);
    PersistentStorage.storeObject("filters", filters);
}
//
//Get data from server, setup page
function getNewData() {
    sendGetRequest({
        url: "/api/data?method=getData",
        successHandler: function(data) {
            //
            //parse the data from JSON (may switch to JSONP eventually... how does that affect this?)
            careerFairData = $.parseJSON(data);
            //
            //set last fetch time, so we know to refresh beyond a certain validity time
            careerFairData.lastFetchTime = new Date().getTime();
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
        valueNames: ['companyListHighlight', 'companyListCompanyID', 'companyListCompanyName', 'companyListTable', 'companyListInfo'],
        page: filteredCompanyIDs.length
    };
    companyList = new List('companyListContainer', options);
    //
    //setup the map for the first time
    generateTableLocations();
    drawTables($mapTables);
    highlightTables();
    initTutorials("Main");
}

function updateCompanyList() {
    //
    //cache the element we are appending to, so that it doesn't have to be created multiple times
    var companyListBody = $("#companyListBody");
    //
    //if no filters applied or only has "changed" flag, skip all the checks for performance reasons.
    //will have either 0, 1, or n+1 elements, where n is the number of types of filters.
    if (Object.keys(filters).length < 2) {
        filteredCompanyIDs = Object.keys(careerFairData.companies);
        selectedCompanyIDs = filteredCompanyIDs.slice();
    }
    //
    //otherwise, make sure all companies conform to filters
    else {
        //
        //Only recompute this if filters have changed. Otherwise, just use previous data
        if (filters.changed) {
            //
            //clear filteredCompanyIDs
            filteredCompanyIDs = [];
            //
            //Iterate through all the companies,
            Object.keys(careerFairData.companies).forEach(function(companyID) {
                //
                //get the actual company object,
                var company = careerFairData.companies[companyID];
                var showCompany = true;
                //
                //check if companies are in at least one of the filters in each type
                Object.keys(filters).forEach(function(filterType) {
                    //
                    //ignore the changed flag - only do the ones that are arrays.
                    if (Array.isArray(filters[filterType])) {
                        //
                        //if the filter length is 0, no filters are applied in that type group - automatically true.
                        if (filters[filterType].length == 0) {
                            return true;
                            //
                            //else, make sure the intersection of the filters (categories) selected in the type group and the categories the company is in overlap 
                            //otherwise, set it to false
                        } else if (_.intersection(filters[filterType], company.categories[filterType]).length == 0) {
                            showCompany = false;
                        }
                    }
                });
                //
                //if the company is valid in the context of all the filters, then add it to the filtered company list
                if (showCompany) {
                    filteredCompanyIDs.addToOrderedSet(companyID);
                }
            });
            //
            //reset the changed flag
            filters.changed = false;
            //
            //default behavior on filter change is to select all of the companies.
            selectedCompanyIDs = filteredCompanyIDs.slice();
        }
    }
    //
    //sort filteredCompanyIDs before creating the array
    filteredCompanyIDs.sort(function(a, b) {
        var o1 = careerFairData.companies[a].name.toLowerCase();
        var o2 = careerFairData.companies[b].name.toLowerCase();
        var p1 = Number(careerFairData.companies[a].tableNumber);
        var p2 = Number(careerFairData.companies[b].tableNumber);
        if (o1 < o2) return -1;
        if (o1 > o2) return 1;
        if (p1 < p2) return -1;
        if (p1 > p2) return 1;
        return 0;
    });
    //
    //add each company that is valid in the context of the selected filters to the list
    filteredCompanyIDs.forEach(function(companyID) {
        var company = careerFairData.companies[companyID];
        // Not in use - includes [i], which is currently not supported.
        //companyListBody.append("<tr><td class='center companyListHighlight' onclick='toggleCheckbox(" + company.id + ")' id='showOnMapCheckbox_" + company.id + "'>☐</td><td class='companyListCompanyID'>" + company.id + "</td><td class='companyListCompanyName' onclick='toggleCheckbox(" + company.id + ")'>" + company.name + "</td><td class='center companyListTable'>" + company.tableNumber + "</td><td class='center companyListInfo'>[i]</td></tr>");
        companyListBody.append("<tr><td class='center companyListHighlight' onclick='toggleCheckbox(" + company.id + ")' id='showOnMapCheckbox_" + company.id + "'>☐</td><td class='companyListCompanyID'>" + company.id + "</td><td class='companyListCompanyName' onclick='toggleCheckbox(" + company.id + ")'>" + company.name + "</td><td class='center companyListTable'>" + company.tableNumber + "</td></tr>");
        careerFairData.companies[companyID].checked = false;
    });
    //
    //Check the ones that are in the list - if no filter change, will check previously selected entries only.
    selectedCompanyIDs.forEach(function(companyID) {
        markCheckboxChecked(companyID);
    });
}
//
//select the checkbox for compnay with given id
function markCheckboxChecked(id) {
    //change icon to checked
    $("#showOnMapCheckbox_" + id).text("☑");
    //add to set of selected companies
    selectedCompanyIDs.addToOrderedSet(id);
    careerFairData.companies[id].checked = true;
}
//
//deselect the checkbox for compnay with given id
function markCheckboxUnchecked(id) {
    //change icon to unchecked
    $("#showOnMapCheckbox_" + id).text("☐");
    //remove from set of selected companies
    selectedCompanyIDs.removeFromOrderedSet(id);
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
function drawRect(tableNumber, x, y, width, height) {
    //
    //draw unfilled rectangle - fill is on bottom "highlights" layer
    $mapTables.drawLine({
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
        closed: true,
        //    click : function(layer) {
        //      alert("You clicked an area!");
        //    } //Box and text both need to be a layer for this to work. Redrawing doesn't quite work as expected, which is why this is disabled.
    });
    //
    //draw tablenumber in box for easy reading.
    if (tableNumber != 0) {
        $mapTables.drawText({
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
    var s1 = careerFairData.termVars.layout.section1;
    var s2 = careerFairData.termVars.layout.section2;
    var s2Rows = careerFairData.termVars.layout.section2Rows;
    var s2PathWidth = careerFairData.termVars.layout.section2PathWidth;
    var s3 = careerFairData.termVars.layout.section3;
    //
    //count number of vertical and horizontal tables there are
    var hrzCount = careerFairData.termVars.layout.section2 + Math.min(s1, 1) + Math.min(s3, 1);
    var vrtCount = Math.max(careerFairData.termVars.layout.section1, careerFairData.termVars.layout.section3);
    //
    //calculate width and height of tables based on width of the canvas
    unitX = $mapTables.prop("width") / 100;
    //10 + (number of sections - 1) * 5 % of space allocated to (vertical) walkways
    var tableWidth = unitX * (90 - Math.min(s1, 1) * 5 - Math.min(s3, 1) * 5) / hrzCount;
    unitY = $mapTables.prop("width") / 2 / 100;
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
            if (i == 0 || i == s2Rows - 1) {
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
    }
    offsetX += tableWidth + 5 * unitX;
}
//
//draw actual tables, then draw registration and rest areas
function drawTables($mapTables) {
    //
    //draw company tables based on generated locations
    Object.keys(tableLocations).forEach(function(key) {
        var location = tableLocations[key];
        drawRect(careerFairData.termVars.layout.locationTableMapping[location.locationID].tableNumber, location.x, location.y, location.width, location.height);
    });
    //
    // rest & registration areas
    drawRect(0, 40 * unitX, 80 * unitY, 45 * unitX, 15 * unitY);
    $mapTables.drawText({
        //    layer: true,
        fillStyle: '#000000',
        x: 62.5 * unitX,
        y: 87.5 * unitY,
        fontSize: 20 * scaling,
        fontFamily: 'Verdana, sans-serif',
        text: 'Rest Area'
    });
    drawRect(0, 5 * unitX, 80 * unitY, 30 * unitX, 15 * unitY);
    $mapTables.drawText({
        //    layer: true,
        fillStyle: '#000000',
        x: 20 * unitX,
        y: 87.5 * unitY,
        fontSize: 20 * scaling,
        fontFamily: 'Verdana, sans-serif',
        text: 'Registration'
    });
}
//
//Highlight all tables in selected companies array
function highlightTables() {
    $mapHighlights.clearCanvas();
    selectedCompanyIDs.forEach(function(id) {
        highlightTable(id, "#0F0");
    });
}
//
//highlight a specific table (used to minimize redrawing for toggling company selected)
function highlightTable(id, color) {
    //
    //get the actual table we need to highlight, not the  company'sid.
    var location = tableLocations[careerFairData.termVars.layout.tableLocationMapping[careerFairData.companies[id].tableNumber].location];
    $mapHighlights.drawRect({
        fillStyle: color,
        x: location.x,
        y: location.y,
        width: location.width,
        height: location.height,
        fromCenter: false
    });
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
};
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