$(document).ready(function() {
    //
    //create variables that will be overwritten by individual .js files
    window.setup = function() {};
    window.cleanup = function() {};
    //setup minimization stuff
    $(document.body).on("click", ".groupHeader", function(event) {
        var sourceId = event.currentTarget.id;
        var target = sourceId.replace("GroupHeader", "");
        if ($("#" + sourceId + "Arrow").html().trim() == "▼") {
            $("." + target + "Item").hide();
            $("#" + sourceId + "Arrow").html("►");
        } else {
            $("." + target + "Item").show();
            $("#" + sourceId + "Arrow").html("▼");
        }
    });

    sendGetRequest({
        url: "/api/data/all/term?showInactive=true",
        successHandler: function(data) {
            var terms = data.termList;
            for(var i = 0; i < terms.length; i++){
                var selected = (i === terms.length ? "selected" : "");
                $("#termSelectionBox").prepend("<option value='" + terms[i].id + "' " + selected + ">" + terms[i].quarter + " " + terms[i].year + "</option>");
            }
            setupPage();
        },
        errorHandler: function(jqXHR, textStatus, errorThrown) {
            alert("Fatal error: Could not retrieve terms");
        }
    });
});

function setupPage(){
    sendGetRequest({
        url: "/api/users/admin/check_authentication",
        successHandler: function(data) {
            setupLinks();
            loadContentWithJS("overview");
        },
        errorHandler: function(jqXHR, textStatus, errorThrown) {
            loadContentWithJS("login");
        }
    });
}

function setupLinks() {
    $(".menuLink").click(function(event) {
        var sourceId = event.currentTarget.id;
        loadContentWithJS(sourceId);
    });
    $(".menuAction").click(function(event) {
        var sourceId = event.currentTarget.id;
        window[sourceId]();
    });
}

function removeLinks() {
    $(".menuLink").unbind();
    $(".menuAction").unbind();
}
//
//logout handler
function logout() {
    var cookies = $.cookie();
    for (var cookie in cookies) {
        $.removeCookie(cookie);
    }
    sendPostRequest({
        url: "/api/users/logout",
        successHandler: function(data) {
            loadContentWithJS("login");
            removeLinks();
        },
        errorHandler: function(jqXHR, textStatus, errorThrown) {
            loadContentWithJS("login");
            removeLinks();
        }
    });
}
//
//load page
function loadContent(name) {
    cleanup();
    $("#contentFrame").load(name + ".html");
}
//
//load page and corresponding .js file
function loadContentWithJS(name) {
    cleanup();
    //load HTML first, then load scripts once HTML is done loading.
    $("#contentFrame").load(name + ".html", function() {
        loadScript("scripts/" + name + ".js");
    });
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

function getSelectedTermId(){
    return $("#termSelectionBox").val();
}