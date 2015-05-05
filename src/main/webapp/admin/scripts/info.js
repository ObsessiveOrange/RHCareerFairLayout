(window.run = function(){
    sendGetRequest({
        url: "/api/data?method=getActiveTerm",
        successHandler: function(returnData) {
            //
            //set last fetch time, so we know to refresh beyond a certain validity time
            if (returnData.success !== 0) {
                $("#activeQuarter").html(returnData.activeQuarter);
                $("#activeYear").html(returnData.activeYear);
            } else {
                alert("Error: Could not retreive data");
            }
        },
        errorHandler: function(jqXHR, textStatus, errorThrown) {
            console.log(textStatus + " : " + errorThrown);
            $("#contentFrame").load("login.html");
        },
        data: {},
        headers: {}
    });
})();
//# sourceURL=/admin/scripts/info.js