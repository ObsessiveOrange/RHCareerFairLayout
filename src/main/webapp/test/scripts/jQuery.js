/**
 * @author: Benedict Wong (ObsessiveOrange)
 */
var companyListPopulated = false;
var filterListPopulated = false;
var careerFairData;
var companiesShown = [];
var highlightedTables = [];
var filters = {};
var currentPage = "#mainPage";
var unitX;
var tableWidth;
var unitY;
var tableHeight;
var tableLocations = [];


$(document).ready(function() {
    //force refresh to go to main page. Causes errors otherwise
    if (window.location.hash.length != 0) {
        window.location.href = "/";
    } else {
        setup();
        getInitialRequest();
    }
    //Set page change triggers-------------------------------------------------------------------
    //Main page
    $(document).on("pagebeforeshow", "#mainPage", function(event) { // When entering main page
        $("#mainPageLoadingScreen").show();

        event.stopPropagation();
    });
    $(document).on("pageshow", "#mainPage", function(event) { // When entering main page
        currentPage = "#mainPage";
        setup();
        drawTables($("#mapCanvasTables"));
        updateCompanyList();
        $("#mainPageLoadingScreen").hide();

        event.stopPropagation();
    });
    //Filter page
    $(document).on("pagebeforeshow", "#filterPage", function(event) { // When entering filter page
        $("#filterPageLoadingScreen").show();

        event.stopPropagation();
    });
    $(document).on("pageshow", "#filterPage", function(event) { // When entering filter page
        currentPage = "#filterPage";
        setup();
        updateFilterList();
        $("#filterPageLoadingScreen").hide();

        event.stopPropagation();
    });
    //force resize and regeneration of tables on resize
    $(window).resize(function() {
        setup();
        generateTableLocations();
        if (currentPage == "#mainPage") {
            drawTables($("#mapCanvasTables"));
            updateSelectedTables();
        }
    });
    //bind action listeners-------------------------------------------------------------------
    //Main Page - checkboxes
    $(document).on("change", "input.showOnMapCheckbox", function(event) {
        updateSelectedTables();

        event.stopPropagation();
    });
    //Main Page - selection buttons
    $(document).on("click", "#companyList_selectAll", function(event) {
        highlightedTables = [];
        companiesShown.forEach(function(itemID) {
            var $currCheckbox = $("#" + itemID + "_checkbox");
            if (!$currCheckbox.prop("checked")) {
                $currCheckbox.prop("checked", true).checkboxradio("refresh");
            }
            addToHighlightedList(itemID);
        });
        highlightTables(highlightedTables, "#00FF00");
    });
    $(document).on("click", "#companyList_invertSelection", function(event) {
        highlightedTables = [];
        companiesShown.forEach(function(itemID) {
            var $currCheckbox = $("#" + itemID + "_checkbox");
            $currCheckbox.prop("checked", !$currCheckbox.prop("checked")).checkboxradio("refresh");
            addToHighlightedList(itemID);
        });
        highlightTables(highlightedTables, "#00FF00");
    });
    $(document).on("click", "#companyList_deselectAll", function(event) {
        highlightedTables = [];
        companiesShown.forEach(function(itemID) {
            var $currCheckbox = $("#" + itemID + "_checkbox");
            if ($currCheckbox.prop("checked")) {
                $currCheckbox.prop("checked", false).checkboxradio("refresh");
            }
        });
        highlightTables(highlightedTables, "#00FF00");
    });
    //Filters page - Buttons
    $(document).on("click", "#filtersList_clearFilters", function(event) {
        // filters = {}; //Don't clear yet - they may still want to cancel by clicking back.
        $("input:checkbox.filterCheckbox:checked").each(function() {
            $(this).prop("checked", false).checkboxradio("refresh");
        });
    });
    $(document).on("click", "#filtersList_applyFilters", function(event) {
        updateSelectedFilters();
    });
    // setup height of content div, canvas sizes
    function setup() {
        var $class_containerElement = $("div.container");
        var $class_contentElement = $("div.content");
        var $class_footerElement = $(currentPage + "_footer");
        $class_contentElement.height($class_containerElement.height() - getVertPadding($class_contentElement) - $class_footerElement.outerHeight() - 1);
        if (currentPage == "#mainPage") {
            var canvasWidth = $("canvas").parent().width();
            var canvasHeight = canvasWidth * 1 / 2;
            $("canvas").prop("width", canvasWidth).prop("height", canvasHeight);
            $("#mapCanvasTables").offset($("#mapCanvasHighlights").offset());
        }
        if (currentPage == "#filterPage") {
            var canvasWidth = $("canvas").parent().width();
            var canvasHeight = canvasWidth * 1 / 2;
            $("canvas").prop("width", canvasWidth).prop("height", canvasHeight);
            $("#mapCanvasTables").offset($("#mapCanvasHighlights").offset());
        }
    }

    function getHrzPadding($object) {
        return $object.outerWidth() - $object.width();
    }

    function getVertPadding($object) {
        return $object.outerHeight() - $object.height();
    }
});
//Get data from server, call first round of updates
function getInitialRequest() {
    sendGetRequest({
        url: "api/data?method=getData",
        successHandler: function(data) {
            careerFairData = $.parseJSON(data);
            //			$(".careerFairDescription").html(careerFairData.title);
            $("span.careerFairDescription").html(careerFairData.title);
            generateTableLocations();
            drawTables($("#mapCanvasTables"));
            updateCompanyList();
            $("#mainPageLoadingScreen").hide();
        }
    });
}
//Update list of companies
function updateCompanyList() {
    // if not populated, do it now.
    companiesShown = [];
    if (!companyListPopulated) {
        for (var key in careerFairData.entries) {
            if (careerFairData.entries.hasOwnProperty(key)) {
                var entry = careerFairData.entries[key];
                addEntryToCompanyList($("#search_list"), entry);
                companiesShown.push(key);
            }
        }
        companyListPopulated = true;
    }
    //Otherwise, show/hide based on filters selected
    else {
        for (var key in careerFairData.entries) {
            if (careerFairData.entries.hasOwnProperty(key)) {
                var entry = careerFairData.entries[key];
                var showElement = true;
                //check if included in filtered selection
                checkFilters: for (var type in filters) {
                    if (entry.categories.hasOwnProperty(type) && filters.hasOwnProperty(type)) {
                        var typeIDs = {};
                        var contains = false;
                        for (var i = 0; i < filters[type].length; i++) {
                            typeIDs[filters[type][i]] = true;
                        }
                        for (var i = 0; i < entry.categories[type].length; i++) {
                            if (typeIDs[entry.categories[type][i]] != null) {
                                continue checkFilters;
                            }
                        }
                        showElement = false;
                        break checkFilters;
                    }
                }
                //show/hide
                if (showElement) {
                    showEntry(entry.id);
                    companiesShown.push(entry.id);
                } else {
                    hideEntry(entry.id);
                }
            }
        }
    }
    //refresh and update map
    $("#search_list").enhanceWithin().controlgroup("refresh");
    updateSelectedTables();
}

function addEntryToCompanyList($list, element) {
    var $newCategory = $("<input type='checkbox' id='" + element.id + "_checkbox' class='showOnMapCheckbox'  style='display:hidden' checked/>" + "<label for='" + element.id + "_checkbox' id='" + element.id + "_checkboxLabel' class='showOnMapCompany' style='font-size: 2.25em !important;'>" + "<div class='companyListsDescription' id='" + element.id + "_name'>" + element.title + "</div>" + "<div class='companyListNumber' id='" + element.id + "_table'>" + element.parameters.table + "</div>" + "</label>");
    $list.controlgroup("container").append($newCategory);
}

function updateFilterList() {
    //If filters list not populated, do it now
    if (!filterListPopulated) {
        var types = [];
        var filterGroup = 0;
        for (var key in careerFairData.categories) {
            if (careerFairData.categories.hasOwnProperty(key)) {
                if ($.inArray(careerFairData.categories[key].type, types) == -1) {
                    types.push(careerFairData.categories[key].type);
                    filterGroup = types.length;
                    var $newGroup = $("<fieldset data-role='collapsible'>" + "<legend>" + "<span style='font-size: 2.25em !important;'>" + careerFairData.categories[key].type + "</span>" + "</legend><div id='filters_group" + filterGroup + "' data-role='controlgroup'>" + "</fieldset></fieldset>");
                    $("#filters_list").append($newGroup);
                }
                var category = careerFairData.categories[key];
                addCategoryToFilterList($("#filters_group" + filterGroup), category);
            }
        }
        $("#filters_list").trigger("create");
        filterListPopulated = true;
    }
    //Otherwise, update which checkboxes are checked.
    else {
        $("input:checkbox.filterCheckbox:checked").each(function() {
            $(this).prop("checked", false);
            $(this).checkboxradio("refresh");
        });
        var allSelectedFilters = [];
        for (var key in filters) {
            if (filters.hasOwnProperty(key)) {
                allSelectedFilters = allSelectedFilters.concat(filters[key]);
            }
        }
        allSelectedFilters.forEach(function(filterID) {
            $("#" + filterID + "_checkbox").prop("checked", true);
            $("#" + filterID + "_checkbox").checkboxradio("refresh");
        });
    }
}

function addCategoryToFilterList($list, element) {
    var $newCategory = $("<input type='checkbox' id='" + element.id + "_checkbox' class='filterCheckbox' >" + "<label for='" + element.id + "_checkbox' id='" + element.id + "_checkboxLabel' class='filterCheckboxLabel' style='font-size: 2.0em !important;'>" + element.title + "</label>");
    $list.append($newCategory);
}

function showEntry(id) {
    $("#" + id + "_checkbox").prop('checked', true);
    $("#" + id + "_checkbox").checkboxradio("refresh");
    $("#" + id + "_checkboxLabel").show();
}

function hideEntry(id) {
    $("#" + id + "_checkbox").prop('checked', false);
    $("#" + id + "_checkbox").checkboxradio("refresh");
    $("#" + id + "_checkbox").hide();
    $("#" + id + "_checkboxLabel").hide();
}
//Update filters selected - only called on applyFilters button press
function updateSelectedFilters() {
    filters = {};
    $("input:checkbox.filterCheckbox:checked").each(function() {
        var itemID = $(this).prop("id").replace("_checkbox", "");
        if (this.checked) {
            if (filters[careerFairData.categories[itemID].type] == null) {
                filters[careerFairData.categories[itemID].type] = [];
            }
            filters[careerFairData.categories[itemID].type].push(itemID);
        }
    });
}
//Update highlighted tables - called every time checkbox is checked/unchecked
function updateSelectedTables() {
    highlightedTables = [];
    companiesShown.forEach(function(itemID) {
        addToHighlightedList(itemID);
    });
    highlightTables(highlightedTables, "#00FF00");
}

function addToHighlightedList(itemID) {
    if ($("#" + itemID + "_checkbox").prop("checked")) {
        highlightedTables.push(careerFairData.entries[itemID].parameters.table);
    }
}
//draw tables and table numbers
function drawRect($canvas, tableNumber, x, y, width, height) {
    $canvas.drawLine({
        //		layer: true,
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
        //		click : function(layer) {
        //			alert("You clicked an area!");
        //		} //Box and text both need to be a layer for this to work.
    });
    if (tableNumber != 0) {
        $canvas.drawText({
            //			layer: true,
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
    unitX = $(currentPage + "Content").width() / 100;
    tableWidth = unitX * 80 / hrzCount;
    unitY = $(currentPage + "Content").width() / 2 / 100;
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
        //		layer: true,
        fillStyle: '#000000',
        x: 62.5 * unitX,
        y: 87.5 * unitY,
        fontSize: 20,
        fontFamily: 'Verdana, sans-serif',
        text: 'Rest Area'
    });
    drawRect($canvas, 0, 5 * unitX, 80 * unitY, 30 * unitX, 15 * unitY);
    $canvas.drawText({
        //		layer: true,
        fillStyle: '#000000',
        x: 20 * unitX,
        y: 87.5 * unitY,
        fontSize: 20,
        fontFamily: 'Verdana, sans-serif',
        text: 'Registration'
    });
}
//Highligh tables in array
function highlightTables(tables, color) {
    var $canvas = $("#mapCanvasHighlights");
    $canvas.clearCanvas();
    $canvas.drawRect({
        fillStyle: '#FFF',
        x: 0,
        y: 0,
        width: $(currentPage + "Content").width(),
        height: $(currentPage + "Content").width() / 2,
        cornerRadius: 10,
        fromCenter: false
    });
    tables.forEach(function(table) {
        var x = tableLocations[table - 1].x;
        var y = tableLocations[table - 1].y;
        $canvas.drawRect({
            fillStyle: color,
            x: x,
            y: y,
            width: tableWidth,
            height: tableHeight,
            fromCenter: false
        });
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
/*       Sample POST/GET request code       */
// var callbackTest = function(data) {
// $obj_map.text(data);
// };
//
// var requestObj = {
// url : "data",
// headers : {
// cartID : 123456,
// itemID : 10101,
// itemQty : 3
// },
// data : {
// method : "setCartItem"
// },
// successHandler : callbackTest
// };
// sendPostRequest(requestObj);
// callbackTest = function(data) {
// $obj_companyList.text(toString(jQuery.parseJSON(data).Cart));
// };
// requestObj = {
// url : "data",
// headers : {
// cartID : 123456
// },
// data : {
// method : "getCartItems"
// },
// successHandler : callbackTest
// };
// sendGetRequest(requestObj);
// Event Handlers
// }