(window.setup = function() {
    for (i = 0; i < 5; i++) {
        $("#uploadDataYearField").append("<option value='" + (new Date().getFullYear() + i) + "'>" + (new Date().getFullYear() + i) + "</option>");
    }
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
                alert(response.message);
            },
            headers: {
                "quarter": $("#uploadDataQuarterField").val(),
                "year": $("#uploadDataYearField").val()
            }
        });
    });
})();
window.cleanup = function() {
    $("#submit").unbind();
};