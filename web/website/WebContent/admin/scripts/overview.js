(window.setup = function() {
    sendGetRequest({
        url: "/api/data/" + getSelectedTermId() + "/term",
        successHandler: function(returnData) {
            $("#selectedQuarter").html(returnData.term.quarter);
            $("#selectedYear").html(returnData.term.year);
        },
        errorHandler: function(jqXHR, textStatus, errorThrown) {
            alert("Error: Could not retreive term");
            console.log(textStatus + " : " + errorThrown);
        }
    });
    sendGetRequest({
        url: "/api/data/" + getSelectedTermId() + "/statistics",
        successHandler: function(returnData) {
                $("#companyCount").html(returnData.companyCount);
                $("#totalTableCount").html(returnData.totalTableCount);
                $("#usedTableCount").html(returnData.usedTableCount);
        },
        errorHandler: function(jqXHR, textStatus, errorThrown) {
            alert("Error: Could not retreive statistics");
            console.log(textStatus + " : " + errorThrown);
        }
    });
})();
window.cleanup = function() {};