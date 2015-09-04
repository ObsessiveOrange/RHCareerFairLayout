(window.setup = function() {
    for (i = 0; i < 5; i++) {
        $("#uploadDataYearField").append("<option value='" + (new Date().getFullYear() + i) + "'>" + (new Date().getFullYear() + i) + "</option>");
    }
    $("#uploadDataSubmit").click(function() {
        $.ajax({
            url: "/api/data/new/all",
            type: "POST",
            contentType: false,
            processData: false,
            data: function() {
                var data = new FormData();
                data.append("quarter", $("#uploadDataQuarterField").val());
                data.append("year", $("#uploadDataYearField").val());
                data.append("data", $("#uploadDataFile").get(0).files[0]);
                return data;
                // Or simply return new FormData($("form")[0]);
            }(),
            errorHandler: function(jqXHR, textStatus, errorThrown) {
                console.log(textStatus + " : " + errorThrown);
            },
            success: function(response, textStatus) {
                alert("Successfully uploaded data");
            }
        });
    });
})();
window.cleanup = function() {
    $("#submit").unbind();
};