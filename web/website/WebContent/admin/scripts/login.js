(window.setup = function() {
    $("#pageContents").on("click", "#login", function() {
        sendPostRequest({
            url: "/api/users/login",
            headers: {
                authUser: $("#usernameField").val(),
                authPass: $("#passwordField").val()
            },
            successHandler: function(returnData) {
                loadContentWithJS("overview");
                setupLinks();
            },
            errorHandler: function(jqXHR, textStatus, errorThrown) {
                alert(jqXHR.responseJSON.message);
                console.log(textStatus + " : " + errorThrown);
            }
        });
    });
})();
window.cleanup = function() {
    $("#pageContents").unbind();
};