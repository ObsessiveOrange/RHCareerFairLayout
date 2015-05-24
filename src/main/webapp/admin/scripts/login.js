(window.setup = function() {
    $("#pageContents").on("click", "#login", function() {
        sendPostRequest({
            url: "/api/data?method=login",
            headers: {},
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