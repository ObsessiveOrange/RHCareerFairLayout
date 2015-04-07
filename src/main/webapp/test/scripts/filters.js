var careerFairData;
var filters = {};
$(document).ready(function() {
    loadAfterPageSwitch();
    createFilterList();
    $("#backBtn").click(function(event) {
        prepareForPageSwitch();
        event.stopPropagation();
    });
});

function loadAfterPageSwitch() {
    careerFairData = SessionVars.retrieveObject("careerFairData");
    filters = SessionVars.retrieveObject("filters");
}

function prepareForPageSwitch() {
    SessionVars.storeObject("careerFairData", careerFairData);
    SessionVars.storeObject("filters", filters);
}
//Get data from server, call first round of updates ///▲▼
function getInitialRequest() {
    sendGetRequest({
        url: "/api/data?method=getData",
        successHandler: function(data) {
            careerFairData = $.parseJSON(data);
            $("span.careerFairDescription").html(careerFairData.title);
            updateCompanyList();
            companyList.sort('companyListCompanyColumn', {
                order: "asc"
            });
            generateTableLocations();
            drawTables($mapCanvasTables);
            highlightTables("#0F0");
        }
    });
}

function createFilterList() {
    var types = [];
    var filterGroupID = 0;
    var $filtersListBody = $("#filtersListBody");
    Object.keys(careerFairData.categories).sort().forEach(function(filterGroup) {
        types.push(filterGroup);
        if (typeof filters[filterGroup] === 'undefined') {
            filters[filterGroup] = [];
        }
        var filterGroupID = types.length;
        $filtersListBody.append("<tr class='filtersListGroupRow' id='filtersListGroup" + filterGroupID + "Row' onclick='toggleFilterGroupID(" + filterGroupID + ")'><td class='center filtersListExpandColumn' id='filtersListExpand_" + filterGroupID + "'>▼</td><td class='filtersListFilterColumn'><b>" + filterGroup + "</b></td>");
        Object.keys(careerFairData.categories[filterGroup]).forEach(function(filterID) {
            $filtersListBody.append("<tr class='filterGroup" + filterGroupID + "Element'><td class='center filtersListSelectColumn' onclick='toggleCheckbox(" + '"' + filterGroup + '", ' + filterID + ")' id='selectFilterCheckbox_" + filterID + "'>☐</td><td class='filtersListFilterColumn' onclick='toggleCheckbox(" + '"' + filterGroup + '", ' + filterID + ")'>" + careerFairData.categories[filterGroup][filterID].title + "</td></tr>");
            if(filters[filterGroup].indexOf(filterID) != -1){
                markCheckboxChecked(filterGroup, filterID);
            }
        });
        hideFilterGroup(filterGroupID);
    });
}

function showFilterGroup(groupID) {
    $("#filtersListExpand_" + groupID).html("▲");
    $(".filterGroup" + groupID + "Element").show();
    if (groupID == Object.keys(careerFairData.categories).length) {
        $("#filtersListGroup" + groupID + "Row").removeClass("tableLastRow");
    }
}

function hideFilterGroup(groupID) {
    $("#filtersListExpand_" + groupID).html("▼");
    $(".filterGroup" + groupID + "Element").hide();
    if (groupID == Object.keys(careerFairData.categories).length) {
        $("#filtersListGroup" + groupID + "Row").addClass("tableLastRow");
    }
}

function toggleFilterGroupID(groupID) {
    if ($("#filtersListExpand_" + groupID).html() == "▼") {
        showFilterGroup(groupID);
    } else {
        hideFilterGroup(groupID);
    }
}

function markCheckboxChecked(groupName, filterID) {
    $("#selectFilterCheckbox_" + filterID).text("☑");
    filters[groupName].addToOrderedSet(filterID.toString());
}

function markCheckboxUnchecked(groupName, filterID) {
    $("#selectFilterCheckbox_" + filterID).text("☐");
    filters[groupName].removeFromOrderedSet(filterID.toString());
}

function toggleCheckbox(groupName, filterID) {
    if ($("#selectFilterCheckbox_" + filterID).html() == "☑") {
        markCheckboxUnchecked(groupName, filterID);
    } else {
        markCheckboxChecked(groupName, filterID);
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