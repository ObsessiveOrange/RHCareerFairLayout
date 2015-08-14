(window.setup = function() {
    sendGetRequest({
        url: "/api/data/all",
        data: {
            year: getSelectedYear(),
            quarter: getSelectedQuarter()
        },
        successHandler: function(careerFairData) {
            var companies = careerFairData.companyMap;
            var categories = buildCategories(careerFairData.categoryMap);
            var companyCategoryMapping = careerFairData.companyCategoryMap;

            var headersHtml = "<tr>";
            headersHtml += "<th>";
            headersHtml += "Name:";
            headersHtml += "</th>";
            headersHtml += "<th>";
            headersHtml += "Website:";
            headersHtml += "</th>";
            headersHtml += "<th>";
            headersHtml += "Description:";
            headersHtml += "</th>";
            Object.keys(categories).forEach(function(categoryType) {
                headersHtml += "<th>";
                headersHtml += categoryType + "s:";
                headersHtml += "</th>";
            });
            headersHtml += "<th>";
            headersHtml += "Address:";
            headersHtml += "</th>";
            headersHtml += "</tr>";
            $("#companiesTable").html(headersHtml);
            Object.keys(companies).forEach(function(companyId) {
                var company = companies[companyId];
                var companyHtml = "<tr>";
                companyHtml += "<td>";
                companyHtml += company.name;
                companyHtml += "</td>";
                companyHtml += "<td>";
                companyHtml += company.description;
                companyHtml += "</td>";
                companyHtml += "<td>";
                companyHtml += company.websiteLink;
                companyHtml += "</td>";
                Object.keys(categories).forEach(function(categoryType) {
                    var first = true;
                    companyHtml += "<td>";
                    _.intersection(_.map(Object.keys(categories[categoryType]), function(str) {
                        return Number(str);
                    }), companyCategoryMapping[companyId].categories).forEach(function(categoryId) {
                        var category = categories[categoryType][categoryId];
                        if (!first) {
                            companyHtml += ", ";
                        }
                        companyHtml += category.name;
                        first = false;
                    });
                    companyHtml += "</td>";
                });
                companyHtml += "<td>";
                companyHtml += company.address;
                companyHtml += "</td>";
                companyHtml += "</tr>";
                $("#companiesTable").append(companyHtml);
            });
        },
        errorHandler: function(jqXHR, textStatus, errorThrown) {
            console.log(textStatus + " : " + errorThrown);
            $("#contentFrame").load("login.html");
        }
    });
})();
window.cleanup = function() {};

function buildCategories(categoryMap) {
    var keys = Object.keys(categoryMap);
    var categories = {};
    for (var i = 0; i < keys.length; i++) {
        var category = categoryMap[keys[i]];
        if (categories[category.type] === null || typeof categories[category.type] === 'undefined') {
            categories[category.type] = {};
        }
        categories[category.type][category.id] = category;
    }
    return categories;
}