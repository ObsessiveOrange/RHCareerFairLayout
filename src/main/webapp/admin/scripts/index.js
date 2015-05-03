$(document).ready(function() {
    sendPostRequest({
        url: "/api/users?method=checkAuthentication",
        successHandler: function(data) {
            //
            //parse the data from JSON (may switch to JSONP eventually... how does that affect this?)
            // var returnData = $.parseJSON(data);
            var returnData = data;
            //
            //set last fetch time, so we know to refresh beyond a certain validity time
            if (returnData.success !== 0) {
                $("#contentFrame").load("info.html");
                $.getScript("scripts/test.js");
            } else {
                $("#contentFrame").load("login.html");
            }
        },
        errorHandler: function(jqXHR, textStatus, errorThrown) {
            console.log(textStatus + " : " + errorThrown);
            $("#contentFrame").load("login.html");
        }
    });
});
//
//send get request
function sendGetRequest(requestObject) {
    $.ajax({
        url: requestObject.url,
        type: "GET",
        headers: requestObject.headers,
        data: requestObject.data,
        success: requestObject.successHandler,
        error: requestObject.errorHandler
    });
}
//
//send post request
function sendPostRequest(requestObject) {
    $.ajax({
        url: requestObject.url,
        type: "POST",
        headers: requestObject.headers,
        data: requestObject.data,
        success: requestObject.successHandler,
        error: requestObject.errorHandler
    });
}