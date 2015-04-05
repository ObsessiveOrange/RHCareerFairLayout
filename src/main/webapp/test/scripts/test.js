var careerFairData;
var companyList;

$(document).ready(function() {
    var options = {
        valueNames: ['show', 'company', 'table', 'info']
    };
    var companyList = new List('CompanyList', options);
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
            //      $(".careerFairDescription").html(careerFairData.title);
            $("span.careerFairDescription").html(careerFairData.title);
            updateCompanyList();
        }
    });
}

function updateCompanyList() {
    for (var key in careerFairData.entries) {
        if (careerFairData.entries.hasOwnProperty(key)) {
            var entry = careerFairData.entries[key];
            companyList.add({
                show: "<img src='images/checkboxChecked.png' class='checkbox'/>",
                company: entry.name,
                table: entry.table,
                info: "{i}"
            });
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