(window.run = function(){
    sendGetRequest({
        url: "/api/data?method=getSelectedTerm",
        successHandler: function(returnData) {
            //
            //set last fetch time, so we know to refresh beyond a certain validity time
            if (returnData.success !== 0) {
                $("#selectedQuarter").html(returnData.selectedQuarter);
                $("#selectedYear").html(returnData.selectedYear);
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