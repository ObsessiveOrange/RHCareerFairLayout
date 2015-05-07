(window.setup = function() {
    for (i = 0; i < 5; i++) {
        $("#selectTermYearField").append("<option value='" + (new Date().getFullYear() + i) + "'>" + (new Date().getFullYear() + i) + "</option>");
        $("#uploadDataYearField").append("<option value='" + (new Date().getFullYear() + i) + "'>" + (new Date().getFullYear() + i) + "</option>");
    }
    $("#selectTermSubmit").click(function() {
        $.ajax({
            url: "/api/users/admin?method=setTerm",
            type: "POST",
            error: function(_, textStatus, errorThrown) {
                alert("Error");
                console.log(textStatus, errorThrown);
            },
            success: function(response, textStatus) {
                alert("Success");
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
                alert("Error");
                console.log(textStatus, errorThrown);
            },
            success: function(response, textStatus) {
                alert("Success");
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