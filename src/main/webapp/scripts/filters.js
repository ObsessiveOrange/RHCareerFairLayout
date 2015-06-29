/**
 * Javascript to display, set and modify filters for Career Fairs
 *
 * Creator: Benedict Wong, 2015
 */
//
// Set strict mode on.
"use strict";
//
// Initialize variables
var careerFairData;
var filters;
var clearCacheFlag;
$(document).ready(function() {
    //
    //load data saved from index page
    loadAfterPageSwitch();
    //
    //if data is not present, go back to index page to load the data.
    if (!careerFairData || !filters) {
        window.location = "/";
        return;
    }
    //
    //update name in navigation bar
    $("span#careerFairDescription").html(careerFairData.title + " - Filters");
    //
    //create the filters list
    createFilterList();
    //
    //Add handler to handle the button presses
    $("#filterButtons").on('click', '.button', function(event) {
        switch ($(this).attr('data-btnAction')) {
            case "cancel":
                break;
            case "clear":
                filters = {
                    changed: true
                };
                prepareForPageSwitch();
                break;
            case "apply":
                prepareForPageSwitch();
                break;
            default:
                break;
        }
        window.location = "/";
    });
    initTutorials("Filters");
});
//load data saved in persistent storage
function loadAfterPageSwitch() {
    careerFairData = PersistentStorage.retrieveObject("careerFairData");
    filters = PersistentStorage.retrieveObject("filters");
}
//load data saved in persistent storage
function prepareForPageSwitch() {
    PersistentStorage.storeObject("careerFairData", careerFairData);
    PersistentStorage.storeObject("filters", filters);
}
//creates filters list
function createFilterList() {
    //
    //array of filter groups
    var filterGroupArray = [];
    //
    //id (changes in loops), of current filter group
    var filterGroupId = 0;
    //
    //jQuery object so that it does not have to be created multiple times
    var $filtersListBody = $("#filtersListBody");
    //
    //go through each category, and create filter groups, then populate with the filters
    Object.keys(careerFairData.categoryList).sort().forEach(function(filterGroup) {
        //
        //add this new group to the array
        filterGroupArray.push(filterGroup);
        //
        //if there is no such element in the persistent filters object, create it.
        if (!filters[filterGroup]) {
            filters[filterGroup] = [];
        }
        //
        //set groupId
        var filterGroupId = filterGroupArray.length;
        //
        //create header row of group
        $filtersListBody.append("<tr class='filtersListGroupRow' id='filtersListGroup" + filterGroupId + "Row' onclick='toggleFilterGroupId(" + filterGroupId + ")'><td class='center filtersListExpandColumn' id='filtersListExpand_" + filterGroupId + "'>►</td><td class='filtersListFilterColumn'><b>" + filterGroup + "</b></td>");
        //
        //populate filter group
        Object.keys(careerFairData.categoryList[filterGroup]).forEach(function(filterId) {
            //
            //force filterId to a number - persistent storage will restore the object with these Ids as numbers, while the keys in categories may be as text. 
            //(or maybe it's the other way round? Either way, this is important)
            filterId = Number(filterId);
            //
            //Create filter rows
            $filtersListBody.append("<tr class='filterGroup" + filterGroupId + "Element' onclick='toggleCheckbox(" + '"' + filterGroup + '", ' + filterId + ")'><td class='center filtersListSelectColumn' id='selectFilterCheckbox_" + filterId + "'>☐</td><td class='filtersListFilterColumn'>" + careerFairData.categoryList[filterGroup][filterId].name + "</td></tr>");
            //
            //if it was previously selected, check it off.
            if (filters[filterGroup].indexOf(filterId) != -1) {
                markCheckboxChecked(filterGroup, filterId);
            }
        });
        //
        //default behavior for new groups is to be hidden.
        hideFilterGroup(filterGroupId);
    });
}
//shows filter groups
function showFilterGroup(groupId) {
    //
    //set the up arrow to show that it can be minimized.
    $("#filtersListExpand_" + groupId).html("►");
    //
    //hide all items with animation
    $(".filterGroup" + groupId + "Element").show(250);
    //
    //for last category, toggle the rounded corners.
    if (groupId == Object.keys(careerFairData.categoryList).length) {
        $("#filtersListGroup" + groupId + "Row").removeClass("tableLastRowRoundBottomLeft");
    }
}
//hides filter groups
function hideFilterGroup(groupId) {
    //
    //set the down arrow to show that it can be expanded.
    $("#filtersListExpand_" + groupId).html("▼");
    //
    //show all items with animation
    $(".filterGroup" + groupId + "Element").hide(250);
    //
    //for last category, toggle the rounded corners.
    if (groupId == Object.keys(careerFairData.categoryList).length) {
        $("#filtersListGroup" + groupId + "Row").addClass("tableLastRowRoundBottomLeft");
    }
}
//toggles the selected group
function toggleFilterGroupId(groupId) {
    //
    //Show/hide based on what the current icon is.
    if ($("#filtersListExpand_" + groupId).html() == "▼") {
        showFilterGroup(groupId);
    } else {
        hideFilterGroup(groupId);
    }
}
//mark checkbox as checked, and add it to the filters set
function markCheckboxChecked(groupName, filterId) {
    //
    //change icon to checked
    $("#selectFilterCheckbox_" + filterId).text("☑");
    //
    //add to filters set.
    filters[groupName].addToOrderedSet(filterId);
}
//mark checkbox as unchecked, and add it to the filters set
function markCheckboxUnchecked(groupName, filterId) {
    //
    //change icon to unchecked
    $("#selectFilterCheckbox_" + filterId).text("☐");
    //
    //remove from filters set.
    filters[groupName].removeFromOrderedSet(filterId);
}
//tooggles the selected checkbox
function toggleCheckbox(groupName, filterId) {
    //
    //check/uncheck based on current icon.
    if ($("#selectFilterCheckbox_" + filterId).html() == "☑") {
        markCheckboxUnchecked(groupName, filterId);
    } else {
        markCheckboxChecked(groupName, filterId);
    }
    filters.changed = true;
}