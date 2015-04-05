var careerFairData;
var companyList;
$(document).ready(function() {
    var options = {
        valueNames: ['show', 'company', 'table', 'info']
    };
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
            companyList = new List('CompanyList', options);
        }
    });
}

function updateCompanyList() {
    var companyListBody = $("#companyListBody");
    for (var key in careerFairData.entries) {
        if (careerFairData.entries.hasOwnProperty(key)) {
            var entry = careerFairData.entries[key];
            companyListBody.append("<tr>");
            companyListBody.append("<td class='show center'  onclick='toggleCheckbox(" + entry.id + ")'><img src='images/checkboxChecked.png' class='checkbox' id='showOnMapCheckbox_" + entry.id + "/></td>");
            companyListBody.append("<td class='company' onclick='toggleCheckbox(" + entry.id + ")'>" + entry.title + "</td>");
            companyListBody.append("<td class='table center'>" + entry.parameters.table + "</td>");
            companyListBody.append("<td class='info center'>[i]</td>");
            companyListBody.append("</tr>");
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