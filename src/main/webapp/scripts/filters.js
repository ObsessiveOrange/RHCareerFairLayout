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
                }
                prepareForPageSwitch();
                break;
            case "apply":
                prepareForPageSwitch();
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
    var filterGroupID = 0;
    //
    //jQuery object so that it does not have to be created multiple times
    var $filtersListBody = $("#filtersListBody");
    //
    //go through each category, and create filter groups, then populate with the filters
    Object.keys(careerFairData.categories).sort().forEach(function(filterGroup) {
        //
        //add this new group to the array
        filterGroupArray.push(filterGroup);
        //
        //if there is no such element in the persistent filters object, create it.
        if (!filters[filterGroup]) {
            filters[filterGroup] = [];
        }
        //
        //set groupID
        var filterGroupID = filterGroupArray.length;
        //
        //create header row of group
        $filtersListBody.append("<tr class='filtersListGroupRow' id='filtersListGroup" + filterGroupID + "Row' onclick='toggleFilterGroupID(" + filterGroupID + ")'><td class='center filtersListExpandColumn' id='filtersListExpand_" + filterGroupID + "'>▼</td><td class='filtersListFilterColumn'><b>" + filterGroup + "</b></td>");
        //
        //populate filter group
        Object.keys(careerFairData.categories[filterGroup]).forEach(function(filterID) {
            //
            //force filterID to a number - persistent storage will restore the object with these IDs as numbers, while the keys in categories may be as text. 
            //(or maybe it's the other way round? Either way, this is important)
            filterID = Number(filterID);
            //
            //Create filter rows
            $filtersListBody.append("<tr class='filterGroup" + filterGroupID + "Element' onclick='toggleCheckbox(" + '"' + filterGroup + '", ' + filterID + ")'><td class='center filtersListSelectColumn' id='selectFilterCheckbox_" + filterID + "'>☐</td><td class='filtersListFilterColumn'>" + careerFairData.categories[filterGroup][filterID].title + "</td></tr>");
            //
            //if it was previously selected, check it off.
            if (filters[filterGroup].indexOf(filterID) != -1) {
                markCheckboxChecked(filterGroup, filterID);
            }
        });
        //
        //default behavior for new groups is to be hidden.
        hideFilterGroup(filterGroupID);
    });
}
//shows filter groups
function showFilterGroup(groupID) {
    //
    //set the up arrow to show that it can be minimized.
    $("#filtersListExpand_" + groupID).html("▲");
    //
    //hide all items with animation
    $(".filterGroup" + groupID + "Element").show(250);
    //
    //for last category, toggle the rounded corners.
    if (groupID == Object.keys(careerFairData.categories).length) {
        $("#filtersListGroup" + groupID + "Row").removeClass("tableLastRowRoundBottomLeft");
    }
}
//hides filter groups
function hideFilterGroup(groupID) {
    //
    //set the down arrow to show that it can be expanded.
    $("#filtersListExpand_" + groupID).html("▼");
    //
    //show all items with animation
    $(".filterGroup" + groupID + "Element").hide(250);
    //
    //for last category, toggle the rounded corners.
    if (groupID == Object.keys(careerFairData.categories).length) {
        $("#filtersListGroup" + groupID + "Row").addClass("tableLastRowRoundBottomLeft");
    }
}
//toggles the selected group
function toggleFilterGroupID(groupID) {
    //
    //Show/hide based on what the current icon is.
    if ($("#filtersListExpand_" + groupID).html() == "▼") {
        showFilterGroup(groupID);
    } else {
        hideFilterGroup(groupID);
    }
}
//mark checkbox as checked, and add it to the filters set
function markCheckboxChecked(groupName, filterID) {
    //
    //change icon to checked
    $("#selectFilterCheckbox_" + filterID).text("☑");
    //
    //add to filters set.
    filters[groupName].addToOrderedSet(filterID);
}
//mark checkbox as unchecked, and add it to the filters set
function markCheckboxUnchecked(groupName, filterID) {
    //
    //change icon to unchecked
    $("#selectFilterCheckbox_" + filterID).text("☐");
    //
    //remove from filters set.
    filters[groupName].removeFromOrderedSet(filterID);
}
//tooggles the selected checkbox
function toggleCheckbox(groupName, filterID) {
    //
    //check/uncheck based on current icon.
    if ($("#selectFilterCheckbox_" + filterID).html() == "☑") {
        markCheckboxUnchecked(groupName, filterID);
    } else {
        markCheckboxChecked(groupName, filterID);
    }
    filters.changed = true;
}