(window.setup = function() {
    sendGetRequest({
        url: "/api/data?method=getCategories",
        successHandler: function(returnData) {
            //
            //set last fetch time, so we know to refresh beyond a certain validity time
            if (returnData.success === 1) {
                Object.keys(returnData.categories).forEach(function(typeName) {
                    var type = returnData.categories[typeName];
                    $("#categoriesTable").append("<tr><th colspan='2'><h3>" + typeName + "s:" + "</h3></th></tr>");
                    Object.keys(type).forEach(function(categoryId) {
                        var category = type[categoryId];
                        var categoryRowHtml = "<tr>";
                        categoryRowHtml += "<td>";
                        categoryRowHtml += "&nbsp;</td>";
                        categoryRowHtml += "<td id='category_" + category.name + "'>";
                        categoryRowHtml += category.name;
                        categoryRowHtml += "</td>";
                        categoryRowHtml += "</tr>";
                        $("#categoriesTable").append(categoryRowHtml);
                    });
                    $("#categoriesTable").append("<tr><td><br /><br /></td></tr>");
                });
            } else {
                alert("Error: Could not retreive data");
            }
        },
        errorHandler: function(jqXHR, textStatus, errorThrown) {
            console.log(textStatus + " : " + errorThrown);
            $("#contentFrame").load("login.html");
        }
    });
})();
window.cleanup = function() {};