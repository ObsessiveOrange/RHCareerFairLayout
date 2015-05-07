(window.setup = function() {
    sendGetRequest({
        url: "/api/users/admin?method=listTerms",
        successHandler: function(returnData) {
            //
            //set last fetch time, so we know to refresh beyond a certain validity time
            if (returnData.success === 1) {
                returnData.terms.forEach(function(obj) {
                    $("#selectTermField").append("<option value='" + obj.year + obj.quarter + "'>" + obj.year + obj.quarter + "</option>");
                });
            } else {
                alert("Error: Could not retreive data.");
            }
        },
        errorHandler: function(jqXHR, textStatus, errorThrown) {
            alert("Error: Could not retreive data.");
        }
    });
    for (i = 0; i < 5; i++) {
        $("#uploadDataYearField").append("<option value='" + (new Date().getFullYear() + i) + "'>" + (new Date().getFullYear() + i) + "</option>");
    }
    $("#selectTermSubmit").click(function() {
        $.ajax({
            url: "/api/users/admin?method=setTerm",
            type: "POST",
            error: function(_, textStatus, errorThrown) {
                alert("Error: Could not connect to server.");
            },
            success: function(response, textStatus) {
                if (response.success !== 1) {
                    alert("Error: " + response.message);
                }
                console.log(response, textStatus);
            },
            headers: {
                "quarter": $("#selectTermTermField").val(),
                "year": $("#selectTermYearField").val()
            }
        });
    });
    $("#uploadDataSubmit").click(function() {
        $.ajax({
            url: "/api/users/admin?method=uploadData",
            type: "POST",
            contentType: false,
            processData: false,
            data: function() {
                var data = new FormData();
                data.append("termData", $("#uploadDataFile").get(0).files[0]);
                return data;
                // Or simply return new FormData($("form")[0]);
            }(),
            error: function(_, textStatus, errorThrown) {
                alert("Error: Could not connect to server.");
            },
            success: function(response, textStatus) {
                if (response.success !== 1) {
                    alert("Error: " + response.message);
                }
                console.log(response, textStatus);
            },
            headers: {
                "quarter": $("#uploadDataTermField").val(),
                "year": $("#uploadDataYearField").val()
            }
        });
    });
})();
window.cleanup = function() {
    $("#submit").unbind();
};