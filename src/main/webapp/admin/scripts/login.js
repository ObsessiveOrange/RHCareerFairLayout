(window.setup = function() {
    $("#pageContents").on("click", "#login", function() {
        sendPostRequest({
            url: "/api/data?method=getStatistics",
            successHandler: function(returnData) {
                //
                //set last fetch time, so we know to refresh beyond a certain validity time
            if (returnData.success === 1) {
                loadContentWithJS("overview");
                setupLinks();
            } else {
                loadContentWithJS("login");
            }
            },
            errorHandler: function(jqXHR, textStatus, errorThrown) {
                console.log(textStatus + " : " + errorThrown);
                $("#contentFrame").load("login.html");
            }
        });
    });
})();
window.cleanup = function() {};