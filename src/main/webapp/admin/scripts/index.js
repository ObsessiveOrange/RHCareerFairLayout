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
                loadContentWithJS("info");
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
//load page
function loadContent(name) {
    $("#contentFrame").load(name + ".html");
}
//
//load page and corresponding .js file
function loadContentWithJS(name) {
    $("#contentFrame").load(name + ".html");
    loadScript("scripts/" + name + ".js");
}
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
// Helps with IE debugging.
function loadScript(url, callback) {
    var head = document.getElementsByTagName("head")[0];
    var script = document.createElement("script");
    var done = false; // Handle Script loading
    script.src = url;
    script.onload = script.onreadystatechange = function() { // Attach handlers for all browsers
        if (!done && (!this.readyState || this.readyState === "loaded" || this.readyState === "complete")) {
            done = true;
            if (callback) {
                callback();
            }
            script.onload = script.onreadystatechange = null; // Handle memory leak in IE
        }
    };
    head.appendChild(script);
    return undefined; // We handle everything using the script element injection
}