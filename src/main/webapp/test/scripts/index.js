var careerFairData;
var companyList;
var tableLocations;
var highlightedTables
var filteredCompanyIDs = [];
var filters;
var $mapCanvasTables;
var $mapCanvasHighlights;
var scaling = 2;
$(document).ready(function() {
    $mapCanvasTables = $("#mapCanvasTables");
    $mapCanvasHighlights = $("#mapCanvasHighlights");
    var $container = $("#canvasMapContainer");
    var containerWidth = $container.width() * scaling;
    var containerHeight = $container.width() * (scaling / 2);
    $container.prop("height", containerHeight);
    $mapCanvasTables.prop("width", containerWidth).prop("height", containerHeight);
    $mapCanvasHighlights.prop("width", containerWidth).prop("height", containerHeight);
    //try to retrieve data;
    loadAfterPageSwitch();
    //if CareerFairData has not been loaded, or it's date is too long ago, reload it.
    if (!careerFairData || (new Date().getTime() - careerFairData.lastFetchTime) > 30 * 60 * 1000) {
        //if other variables have not been created/set, do it now.
        if (!tableLocations || !highlightTables || !filteredCompanyIDs || !filters) {
            tableLocations = [];
            highlightedTables = [];
            filteredCompanyIDs = [];
            filters = {};
        }
        //get careerFairData - calls setupPage();
        getNewData();
    }
    //else, if careerFairData has been loaded, just setup the page using cached data
    else {
        //if the filters have been modified, reset the highlighted tables.
        if (filters.changed) {
            highlightedTables = [];
        }
        //setup the page
        setupPage();
    }
});
//save data when link out of page clicked.
var clearCache;
window.onbeforeunload = function(event) {
    if (typeof clearCache === 'undefined' || !clearCache) {
        prepareForPageSwitch();
    } else {
        SessionVars.clear();
    }
}

function loadAfterPageSwitch() {
    careerFairData = SessionVars.retrieveObject("careerFairData");
    tableLocations = SessionVars.retrieveObject("tableLocations");
    highlightedTables = SessionVars.retrieveObject("highlightedTables");
    filteredCompanyIDs = SessionVars.retrieveObject("filteredCompanyIDs");
    filters = SessionVars.retrieveObject("filters");
}

function prepareForPageSwitch() {
    SessionVars.storeObject("careerFairData", careerFairData);
    SessionVars.storeObject("tableLocations", tableLocations);
    SessionVars.storeObject("highlightedTables", highlightedTables);
    SessionVars.storeObject("filteredCompanyIDs", filteredCompanyIDs);
    SessionVars.storeObject("filters", filters);
}
//Get data from server, call first round of updates
function getNewData() {
    sendGetRequest({
        url: "/api/data?method=getData",
        successHandler: function(data) {
            careerFairData = $.parseJSON(data);
            careerFairData.lastFetchTime = new Date().getTime();
            setupPage();
        }
    });
}

function setupPage() {
    $("span.careerFairDescription").html(careerFairData.title);
    updateCompanyList();
    var options = {
        valueNames: ['companyListHighlightColumn', 'companyListCompanyColumn', 'companyListTableColumn', 'companyListInfoColumn']
    };
    companyList = new List('companyListContainer', options);
    companyList.sort('companyListCompanyColumn', {
        order: "asc"
    });
    generateTableLocations();
    drawTables($mapCanvasTables);
    highlightTables("#0F0");
}

function updateCompanyList() {
    var companyListBody = $("#companyListBody");
    Object.keys(careerFairData.companies).forEach(function(companyID) {
        var company = careerFairData.companies[key];
        var showCompany = true;
        Object.keys(filters).forEach(function(filterType) {
            if (filters[filterType].length == 0) {
                return true;
            } else if (_.intersection(filters[filterType], company.categories).length == 0) {
                showComapny = false;
            }
        });
        if (showCompany) {
            companyListBody.append("<tr><td class='center companyListHighlightColumn' onclick='toggleCheckbox(" + company.id + ")' id='showOnMapCheckbox_" + company.id + "'></td><td class='companyListCompanyColumn' onclick='toggleCheckbox(" + company.id + ")'>" + company.title + "</td><td class='center companyListTableColumn'>" + company.parameters.table + "</td><td class='center companyListInfoColumn'>[i]</td></tr>");
            markCheckboxChecked(key);
            filteredCompanyIDs.addToOrderedSet(company.id);
        }
    });
    // if(filters)
    // var filteredCompanyIDsByCategory = [];
    // Object.keys(filters).forEach(function(category) {
    //     var categoryID = filteredCompanyIDsByCategory.length;
    //     filteredCompanyIDsByCategory.push([]);
    //     Object.keys(filters[category]).forEach(function(filterID) {
    //         filteredCompanyIDsByCategory[categoryID] = _.union(filteredCompanyIDsByCategory[categoryID], careerFairData.companies);
    //     });
    // });
    // filteredCompanyIDs = filteredCompanyIDsByCategory[0];
    // for (var i = 1; i < filteredCompanyIDsByCategory.length; i++) {
    //     filteredCompanyIDs = _.intersection(filteredCompanyIDs, filteredCompanyIDsByCategory[i]);
    // }
    // filteredCompanyIDs.forEach(function(companyID) {
    //     var company = careerFairData.companies[key];
    //     companyListBody.append("<tr><td class='center companyListHighlightColumn' onclick='toggleCheckbox(" + company.id + ")' id='showOnMapCheckbox_" + company.id + "'></td><td class='companyListCompanyColumn' onclick='toggleCheckbox(" + company.id + ")'>" + company.title + "</td><td class='center companyListTableColumn'>" + company.parameters.table + "</td><td class='center companyListInfoColumn'>[i]</td></tr>");
    //     markCheckboxChecked(key);
    //     filteredCompanyIDs.push(company.id);
    // });
}

function markCheckboxChecked(id) {
    $("#showOnMapCheckbox_" + id).text("☑");
    highlightedTables.addToOrderedSet(careerFairData.companies[id].parameters.table);
}

function markCheckboxUnchecked(id) {
    $("#showOnMapCheckbox_" + id).text("☐");
    highlightedTables.removeFromOrderedSet(careerFairData.companies[id].parameters.table);
}

function toggleCheckbox(id) {
    if ($("#showOnMapCheckbox_" + id).html() == "☑") {
        markCheckboxUnchecked(id);
        highlightTable(careerFairData.companies[id].parameters.table, "#EEE");
    } else {
        markCheckboxChecked(id);
        highlightTable(careerFairData.companies[id].parameters.table, "#0F0");
    }
}
//draw tables and table numbers
function drawRect(tableNumber, x, y, width, height) {
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
        //    } //Box and text both need to be a layer for this to work.
    });
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
//generate positions of all tables.
function generateTableLocations() {
    tableLocations = [];
    var hrzCount = careerFairData.layout.section2 + 2;
    var vrtCount = Math.max(careerFairData.layout.section1, careerFairData.layout.section3);
    unitX = $mapCanvasTables.prop("width") / 100;
    tableWidth = unitX * 80 / hrzCount;
    unitY = $mapCanvasTables.prop("width") / 2 / 100;
    tableHeight = unitY * 70 / vrtCount;
    var s1 = careerFairData.layout.section1;
    var s2 = careerFairData.layout.section2;
    var s2Rows = careerFairData.layout.section2Rows;
    var s2PathWidth = careerFairData.layout.section2PathWidth;
    var s3 = careerFairData.layout.section3;
    // section 1
    for (var i = 0; i < s1; i++) {
        tableLocations.push({
            x: 5 * unitX,
            y: 5 * unitY + i * tableHeight
        });
    }
    // section 2
    var pathWidth = (unitY * 70 - s2Rows * tableHeight) / (s2Rows / 2);
    //rows
    for (var i = 0; i < s2Rows; i++) {
        //outer rows have no walkway
        if (i == 0 || i == s2Rows - 1) {
            for (var j = 0; j < s2; j++) {
                tableLocations.push({
                    x: (10 * unitX) + ((1 + j) * tableWidth),
                    y: 5 * unitY + Math.floor((i + 1) / 2) * pathWidth + i * tableHeight
                });
            }
        }
        //inner rows need to have walkway halfway through
        else {
            var leftTables = Math.floor((s2 - s2PathWidth) / 2);
            var rightTables = s2 - 2 - leftTables;
            for (var j = 0; j < leftTables; j++) {
                tableLocations.push({
                    x: (10 * unitX) + ((1 + j) * tableWidth),
                    y: 5 * unitY + Math.floor((i + 1) / 2) * pathWidth + i * tableHeight
                });
            }
            for (var j = 0; j < rightTables; j++) {
                tableLocations.push({
                    x: (10 * unitX) + ((1 + leftTables + 2 + j) * tableWidth),
                    y: 5 * unitY + Math.floor((i + 1) / 2) * pathWidth + i * tableHeight
                });
            }
        }
    }
    // section 3
    for (var i = 0; i < s3; i++) {
        tableLocations.push({
            x: (15 * unitX) + ((1 + s2) * tableWidth),
            y: 5 * unitY + i * tableHeight
        });
    }
}
//draw actual tables, then draw registration and rest areas
function drawTables($mapCanvasTables) {
    for (var i = 0; i < tableLocations.length; i++) {
        var locationX = tableLocations[i].x;
        var locationY = tableLocations[i].y;
        drawRect(i + 1, locationX, locationY, tableWidth, tableHeight);
    }
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
//Highlight tables in array
function highlightTables(color) {
    $mapCanvasHighlights.clearCanvas();
    highlightedTables.forEach(function(table) {
        highlightTable(table, color);
    });
}

function highlightTable(id, color) {
    var x = tableLocations[id - 1].x;
    var y = tableLocations[id - 1].y;
    $mapCanvasHighlights.drawRect({
        fillStyle: color,
        x: x,
        y: y,
        width: tableWidth,
        height: tableHeight,
        fromCenter: false
    });
}

function sendGetRequest(requestObject) {
    $.ajax({
        url: requestObject.url,
        type: "GET",
        headers: requestObject.headers,
        data: requestObject.data,
        success: requestObject.successHandler
    });
};

function sendPostRequest(requestObject) {
    $.ajax({
        url: requestObject.url,
        type: "POST",
        headers: requestObject.headers,
        data: requestObject.data,
        success: requestObject.successHandler
    });
}