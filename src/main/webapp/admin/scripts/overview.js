(window.setup = function() {
    sendGetRequest({
        url: "/api/data/selected_term",
        successHandler: function(returnData) {
            //
            //set last fetch time, so we know to refresh beyond a certain validity time
            if (returnData.success === 1) {
                $("#selectedQuarter").html(returnData.selectedQuarter);
                $("#selectedYear").html(returnData.selectedYear);
            } else {
                alert("Error: Could not retreive data");
            }
        },
        errorHandler: function(jqXHR, textStatus, errorThrown) {
            console.log(textStatus + " : " + errorThrown);
            $("#contentFrame").load("login.html");
        }
    });
    sendGetRequest({
        url: "/api/data/statistics",
        successHandler: function(returnData) {
            //
            //set last fetch time, so we know to refresh beyond a certain validity time
            if (returnData.success === 1) {
                $("#companyCount").html(returnData.companyCount);
                $("#totalTableCount").html(returnData.totalTableCount);
                $("#usedTableCount").html(returnData.usedTableCount);
            } else {
                alert("Error: Could not retreive data");
            }
        },
        errorHandler: function(jqXHR, textStatus, errorThrown) {
            console.log(textStatus + " : " + errorThrown);
            $("#contentFrame").load("login.html");
        }
    });
})();
window.cleanup = function() {};