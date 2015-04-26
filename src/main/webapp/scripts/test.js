$(document).ready(function() {
    $("#submitFile").click(function() {
        $.ajax({
            url: "/api/users/admin?method=test",
            type: "POST",
            contentType: false,
            processData: false,
            data: function() {
                var data = new FormData();
                data.append("fileDescription", $("#desc").val());
                data.append("chosenFile", $("#chosenFile").get(0).files[0]);
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
                "authUser": "bennydictwong",
            },
        });
    });
});