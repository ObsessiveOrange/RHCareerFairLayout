/**
 * Javascript to get, process and display dynamic data for Career Fair Layout
 *
 * Creator: Benedict Wong, 2015
 */
var careerFairData;
var companyList;
var tableLocations;
var selectedCompanyIDs
var filteredCompanyIDs = [];
var filters;
var $mapCanvasTables;
var $mapCanvasHighlights;
var scaling = 2;
var clearCacheFlag;
$(document).ready(function() {
    //
    //set size and width of canvas elements and containing div
    $mapCanvasTables = $("#mapCanvasTables");
    $mapCanvasHighlights = $("#mapCanvasHighlights");
    var $container = $("#canvasMapContainer");
    var containerWidth = $container.width() * scaling;
    var containerHeight = $container.width() * (scaling / 2);
    $container.prop("height", containerHeight);
    $mapCanvasTables.prop("width", containerWidth).prop("height", containerHeight);
    $mapCanvasHighlights.prop("width", containerWidth).prop("height", containerHeight);
    //
    //try to retrieve data from persistent storage
    loadAfterPageSwitch();
    //
    //if CareerFairData has not been loaded, or it's date is too long ago, reload it.
    if (!careerFairData || (new Date().getTime() - careerFairData.lastFetchTime) > 30 * 60 * 1000) {
        //
        //if other variables have not been created/set, do it now.
        if (!tableLocations || !selectedCompanyIDs || !filteredCompanyIDs || !filters) {
            tableLocations = [];
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
                    if ($("#showOnMapCheckbox_" + id).html() == "☐") {
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
                    if ($("#showOnMapCheckbox_" + id).html() == "☑") {
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
        valueNames: ['companyListHighlightColumn', 'companyListCompanyColumn', 'companyListTableColumn', 'companyListInfoColumn'],
        page: filteredCompanyIDs.length
    };
    companyList = new List('companyListContainer', options);
    //
    //setup the map for the first time
    generateTableLocations();
    drawTables($mapCanvasTables);
    highlightTables();
    var $tutorial = $("#tutorial");
    $tutorial.css("height", $("body").height());
    $tutorial.prop("width", $tutorial.width());
    $tutorial.prop("height", $tutorial.height());
    drawTutorial();
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
                            //else, make sure the intersection of the filters in the type group and the categories the company is in overlap 
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
        var o1 = careerFairData.companies[a].title.toLowerCase();
        var o2 = careerFairData.companies[b].title.toLowerCase();
        var p1 = Number(careerFairData.companies[a].parameters.table);
        var p2 = Number(careerFairData.companies[b].parameters.table);
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
        companyListBody.append("<tr><td class='center companyListHighlightColumn' onclick='toggleCheckbox(" + company.id + ")' id='showOnMapCheckbox_" + company.id + "'>☐</td><td class='companyListCompanyColumn' onclick='toggleCheckbox(" + company.id + ")'>" + company.title + "</td><td class='center companyListTableColumn'>" + company.parameters.table + "</td><td class='center companyListInfoColumn'>[i]</td></tr>");
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
}
//
//deselect the checkbox for compnay with given id
function markCheckboxUnchecked(id) {
    //change icon to unchecked
    $("#showOnMapCheckbox_" + id).text("☐");
    //remove from set of selected companies
    selectedCompanyIDs.removeFromOrderedSet(id);
}
//
//toggle the checkbox
function toggleCheckbox(id) {
    //
    //toggle based on current text value
    if ($("#showOnMapCheckbox_" + id).html() == "☑") {
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
//draw tutorial page
function drawTutorial() {
    var $canvas = $("#tutorial");
    $canvas.drawInverted({
        x: 0,
        y: 0,
        width: $canvas.width(),
        height: $canvas.height(),
        holeX: $("#filterBtn").offset().left + $("#filterBtn").width() / 2,
        holeY: $("#filterBtn").offset().top + $("#filterBtn").height() / 2,
        holeRadius: 50,
        fromCenter: false,
        mask: true
    });
    // This shape is being masked
    $canvas.drawRect({
        fillStyle: 'rgba(0, 0, 0, 0.75)',
        x: 0,
        y: 0,
        width: $canvas.width(),
        height: $canvas.height(),
        fromCenter: false
    })
    $canvas.restoreCanvas();
    $canvas.drawArc({
        strokeStyle: '#0AF',
        strokeWidth: 5,
        x: $("#filterBtn").offset().left + $("#filterBtn").width() / 2 - 200,
        y: $("#filterBtn").offset().top + $("#filterBtn").height() / 2 + 50,
        radius: 200,
        // start and end angles in degrees
        start: 90,
        end: 135,
    });
    $canvas.drawLine({
        strokeStyle: '#0AF',
        strokeWidth: 5,
        x1: $("#filterBtn").offset().left + $("#filterBtn").width() / 2,
        y1: $("#filterBtn").offset().top + $("#filterBtn").height() / 2 + 50,
        x2: $("#filterBtn").offset().left + $("#filterBtn").width() / 2 - 15,
        y2: $("#filterBtn").offset().top + $("#filterBtn").height() / 2 + 75,
        x3: $("#filterBtn").offset().left + $("#filterBtn").width() / 2 + 15,
        y3: $("#filterBtn").offset().top + $("#filterBtn").height() / 2 + 75,
        closed: true,
    });
    $canvas.drawEllipse({
        strokeStyle: '#0AF',
        strokeWidth: 2,
        x: $("#filterBtn").offset().left + $("#filterBtn").width() / 2,
        y: $("#filterBtn").offset().top + $("#filterBtn").height() / 2,
        width: 100,
        height: 100,
    });
    $canvas.drawText({
        fillStyle: '#0AF',
        x: $("#filterBtn").offset().left + $("#filterBtn").width() / 2 - 200 + 200 / Math.sqrt(2) - 5,
        y: $("#filterBtn").offset().top + $("#filterBtn").height() / 2 + 50 + 200 / Math.sqrt(2),
        align: 'right',
        respectAlign: true,
        text: "This is the filter button;\nClick it to filter companies\nlike you would in CareerLink.",
        fontSize: '20pt',
        fontStyle: 'bold',
        fontFamily: 'Verdana, sans-serif',
    });
}
//
//draw tables and table numbers
function drawRect(tableNumber, x, y, width, height) {
    //
    //draw unfilled rectangle - fill is on bottom "highlights" layer
    $mapCanvasTables.drawLine({
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
        $mapCanvasTables.drawText({
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
    tableLocations = [];
    //
    //count number of vertical and horizontal tables there are
    var hrzCount = careerFairData.layout.section2 + 2;
    var vrtCount = Math.max(careerFairData.layout.section1, careerFairData.layout.section3);
    //
    //calculate width and height of tables based on width of the canvas
    unitX = $mapCanvasTables.prop("width") / 100;
    //20% of space allocated to (vertical) walkways
    tableWidth = unitX * 80 / hrzCount;
    unitY = $mapCanvasTables.prop("width") / 2 / 100;
    //30% of space allocated to registration and rest area.
    tableHeight = unitY * 70 / vrtCount;
    //
    //convenience assignments
    var s1 = careerFairData.layout.section1;
    var s2 = careerFairData.layout.section2;
    var s2Rows = careerFairData.layout.section2Rows;
    var s2PathWidth = careerFairData.layout.section2PathWidth;
    var s3 = careerFairData.layout.section3;
    //
    // section 1
    for (var i = 0; i < s1; i++) {
        tableLocations.push({
            x: 5 * unitX,
            y: 5 * unitY + i * tableHeight
        });
    }
    //
    // section 2
    var pathWidth = (unitY * 70 - s2Rows * tableHeight) / (s2Rows / 2);
    //
    //rows
    for (var i = 0; i < s2Rows; i++) {
        //
        //outer rows have no walkway
        if (i == 0 || i == s2Rows - 1) {
            for (var j = 0; j < s2; j++) {
                tableLocations.push({
                    x: (10 * unitX) + ((1 + j) * tableWidth),
                    y: 5 * unitY + Math.floor((i + 1) / 2) * pathWidth + i * tableHeight
                });
            }
        }
        //
        //inner rows need to have walkway halfway through
        else {
            var leftTables = Math.floor((s2 - s2PathWidth) / 2);
            var rightTables = s2 - s2PathWidth - leftTables;
            for (var j = 0; j < leftTables; j++) {
                tableLocations.push({
                    x: (10 * unitX) + ((1 + j) * tableWidth),
                    y: 5 * unitY + Math.floor((i + 1) / 2) * pathWidth + i * tableHeight
                });
            }
            for (var j = 0; j < rightTables; j++) {
                tableLocations.push({
                    x: (10 * unitX) + ((1 + leftTables + s2PathWidth + j) * tableWidth),
                    y: 5 * unitY + Math.floor((i + 1) / 2) * pathWidth + i * tableHeight
                });
            }
        }
    }
    //
    // section 3
    for (var i = 0; i < s3; i++) {
        tableLocations.push({
            x: (15 * unitX) + ((1 + s2) * tableWidth),
            y: 5 * unitY + i * tableHeight
        });
    }
}
//
//draw actual tables, then draw registration and rest areas
function drawTables($mapCanvasTables) {
    //
    //draw company tables based on generated locations
    for (var i = 0; i < tableLocations.length; i++) {
        var locationX = tableLocations[i].x;
        var locationY = tableLocations[i].y;
        drawRect(i + 1, locationX, locationY, tableWidth, tableHeight);
    }
    //
    // rest & registration areas
    drawRect(0, 40 * unitX, 80 * unitY, 45 * unitX, 15 * unitY);
    $mapCanvasTables.drawText({
        //    layer: true,
        fillStyle: '#000000',
        x: 62.5 * unitX,
        y: 87.5 * unitY,
        fontSize: 20 * scaling,
        fontFamily: 'Verdana, sans-serif',
        text: 'Rest Area'
    });
    drawRect(0, 5 * unitX, 80 * unitY, 30 * unitX, 15 * unitY);
    $mapCanvasTables.drawText({
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
    $mapCanvasHighlights.clearCanvas();
    selectedCompanyIDs.forEach(function(id) {
        highlightTable(id, "#0F0");
    });
}
//
//highlight a specific table (used to minimize redrawing for toggling company selected)
function highlightTable(id, color) {
    //
    //get the actual table we need to highlight, not the  company'sid.
    var table = careerFairData.companies[id].parameters.table
    var x = tableLocations[table - 1].x;
    var y = tableLocations[table - 1].y;
    $mapCanvasHighlights.drawRect({
        fillStyle: color,
        x: x,
        y: y,
        width: tableWidth,
        height: tableHeight,
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