var careerFairData;
var filters;
var clearCacheFlag;
$(document).ready(function() {
    loadAfterPageSwitch();

    if(!careerFairData || !filters){
        window.location = "index.html";
        return;
    }

    createFilterList();

    //save data when link out of page clicked.
    $("#backBtn").on("click", function(event) {
        if (typeof clearCacheFlag === 'undefined' || !clearCacheFlag) {
            prepareForPageSwitch();
        } else {
            SessionVars.clear();
        }
        event.stopPropogation();
    });
});

function clearCache() {
    clearCache = true;
    SessionVars.clear();
}


function loadAfterPageSwitch() {
    careerFairData = SessionVars.retrieveObject("careerFairData");
    filters = SessionVars.retrieveObject("filters");
}

function prepareForPageSwitch() {
    SessionVars.storeObject("careerFairData", careerFairData);
    SessionVars.storeObject("filters", filters);
}
//Get data from server, call first round of updates
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
        if (!filters[filterGroup]) {
            filters[filterGroup] = [];
        }
        var filterGroupID = types.length;
        $filtersListBody.append("<tr class='filtersListGroupRow' id='filtersListGroup" + filterGroupID + "Row' onclick='toggleFilterGroupID(" + filterGroupID + ")'><td class='center filtersListExpandColumn' id='filtersListExpand_" + filterGroupID + "'>▼</td><td class='filtersListFilterColumn'><b>" + filterGroup + "</b></td>");
        Object.keys(careerFairData.categories[filterGroup]).forEach(function(filterID) {
            filterID = Number(filterID);
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
    $(".filterGroup" + groupID + "Element").show(250);
    if (groupID == Object.keys(careerFairData.categories).length) {
        $("#filtersListGroup" + groupID + "Row").removeClass("tableLastRow");
    }
}

function hideFilterGroup(groupID) {
    $("#filtersListExpand_" + groupID).html("▼");
    $(".filterGroup" + groupID + "Element").hide(250);
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
    filters[groupName].addToOrderedSet(filterID);
    filters.changed = true;
}

function markCheckboxUnchecked(groupName, filterID) {
    $("#selectFilterCheckbox_" + filterID).text("☐");
    filters[groupName].removeFromOrderedSet(filterID);
    filters.changed = true;
}

function toggleCheckbox(groupName, filterID) {
    if ($("#selectFilterCheckbox_" + filterID).html() == "☑") {
        markCheckboxUnchecked(groupName, filterID);
    } else {
        markCheckboxChecked(groupName, filterID);
    }
}