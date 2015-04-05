var careerFairData;
var companyList;
$(document).ready(function() {
    getInitialRequest();
});

function test() {
    console.log("Success");
}
//Get data from server, call first round of updates
function getInitialRequest() {
    sendGetRequest({
        url: "/api/data?method=getData",
        successHandler: function(data) {
            careerFairData = $.parseJSON(data);
            $("span.careerFairDescription").html(careerFairData.title);
            updateCompanyList();
            var options = {
                valueNames: ['show', 'company', 'table', 'info']
            };
            companyList = new List('companyListContainer', options);
            companyList.sort('company', {order:"asc"});
            generateTableLocations();
            drawTables($("#mapCanvasTables"));
        }
    });
}

function updateCompanyList() {
    var companyListBody = $("#companyListBody");
    for (var key in careerFairData.entries) {
        if (careerFairData.entries.hasOwnProperty(key)) {
            var entry = careerFairData.entries[key];
            companyListBody.append("<tr><td class='show center'  onclick='toggleCheckbox(" + entry.id + ")'><img src='images/checkboxChecked.png' class='checkbox' id='showOnMapCheckbox_" + entry.id + "'/></td><td class='company' onclick='toggleCheckbox(" + entry.id + ")'>" + entry.title + "</td><td class='table center'>" + entry.parameters.table + "</td><td class='info center'>[i]</td></tr>");
            // companyList.add({
            //     show: "<img src='images/checkboxChecked.png' class='checkbox'/>",
            //     company: entry.title,
            //     table: entry.parameters.table,
            //     info: "{i}"
            // });
            entry.checked = true;
        }
    }
}

function toggleCheckbox(id) {
    console.log("Toggling checkbox with id: " + id);
    if (careerFairData.entries[id].checked) {
        $("#showOnMapCheckbox_" + id).attr("src", "images/checkboxUnchecked.png");
        careerFairData.entries[id].checked = false;
    }
    else {
        $("#showOnMapCheckbox_" + id).attr("src", "images/checkboxChecked.png");
        careerFairData.entries[id].checked = true;
    }
}

//draw tables and table numbers
function drawRect($canvas, tableNumber, x, y, width, height) {
    $canvas.drawLine({
        //    layer: true,
        strokeStyle: '#000',
        strokeWidth: 1,
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
        $canvas.drawText({
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
    var $container = $("#canvasMapContainer");
    $("#mapCanvasTables").width($container.width);
    $("#mapCanvasTables").height($container.height);
    $("#mapCanvasHighlights").width($container.width);
    $("#mapCanvasHighlights").height($container.height);


    tableLocations = [];
    var hrzCount = careerFairData.layout.section2 + 2;
    var vrtCount = Math.max(careerFairData.layout.section1, careerFairData.layout.section3);
    unitX = $container.width() / 100;
    tableWidth = unitX * 80 / hrzCount;
    unitY = $container.width() / 2 / 100;
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
function drawTables($canvas) {
    for (var i = 0; i < tableLocations.length; i++) {
        var locationX = tableLocations[i].x;
        var locationY = tableLocations[i].y;
        drawRect($canvas, i + 1, locationX, locationY, tableWidth, tableHeight);
    }
    // rest & registration areas
    drawRect($canvas, 0, 40 * unitX, 80 * unitY, 45 * unitX, 15 * unitY);
    $canvas.drawText({
        //    layer: true,
        fillStyle: '#000000',
        x: 62.5 * unitX,
        y: 87.5 * unitY,
        fontSize: 20,
        fontFamily: 'Verdana, sans-serif',
        text: 'Rest Area'
    });
    drawRect($canvas, 0, 5 * unitX, 80 * unitY, 30 * unitX, 15 * unitY);
    $canvas.drawText({
        //    layer: true,
        fillStyle: '#000000',
        x: 20 * unitX,
        y: 87.5 * unitY,
        fontSize: 20,
        fontFamily: 'Verdana, sans-serif',
        text: 'Registration'
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