(window.setup = function() {
    sendGetRequest({
        url: "/api/data/category/all",
        successHandler: function(data) {
            var categories = buildCategories(data);
            Object.keys(categories).forEach(function(typeName) {
                var type = categories[typeName];
                $("#categoriesTable").append("<tr><td colspan='2'><h3>" + typeName + "s:" + "</h3></td></tr>");
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
        },
        errorHandler: function(jqXHR, textStatus, errorThrown) {
            alert("Error: Could not retreive category list");
            console.log(textStatus + " : " + errorThrown);
        }
    });
})();
window.cleanup = function() {};

function buildCategories(data) {
    var keys = Object.keys(data.categoryMap);
    var categories = {};
    for (var i = 0; i < keys.length; i++) {
        var category = data.categoryMap[keys[i]];
        if (categories[category.type] === null || typeof categories[category.type] === 'undefined') {
            categories[category.type] = {};
        }
        categories[category.type][category.id] = category;
    }
    return categories;
}