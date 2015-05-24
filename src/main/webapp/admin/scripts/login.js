(window.setup = function() {
    $("#pageContents").on("click", "#login", function() {
        sendPostRequest({
            url: "/api/users?method=login",
            headers: {
                authUser: $("#username").val(),
                authPass: $("#password").val()
            },
            successHandler: function(returnData) {
                //
                //set last fetch time, so we know to refresh beyond a certain validity time
                if (returnData.success === 1) {
                    loadContentWithJS("overview");
                    setupLinks();
                } else {
                    loadContentWithJS("login");
                }
            }
        });
    });
})();
window.cleanup = function() {
    $("#pageContents").unbind();
};