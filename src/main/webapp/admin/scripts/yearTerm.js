for (i = 0; i < 5; i++) {
    $("#yearField").append("<option value='" + (new Date().getFullYear() + i) + "'>" + (new Date().getFullYear() + i) + "</option>");
}
$("#submit").click(function() {
    $.ajax({
        url: "/api/users/admin?method=uploadData",
        type: "POST",
        contentType: false,
        processData: false,
        data: function() {
            var data = new FormData();
            data.append("chosenFile", $("#file").get(0).files[0]);
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
            "year": $("#yearField").val(),
            "term": $("#termField").val()
        }
    });
});
//@ sourceURL=yearTerm.js