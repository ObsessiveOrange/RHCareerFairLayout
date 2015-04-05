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
            companyList.sort("company", {order:"asc"});
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