(window.setup = function() {
    sendGetRequest({
        url: "/api/data?method=getCompanies",
        successHandler: function(companiesReturnData) {
            if (companiesReturnData.success === 1) {
                sendGetRequest({
                    url: "/api/data?method=getCategories",
                    successHandler: function(categoriesReturnData) {
                        //
                        //set last fetch time, so we know to refresh beyond a certain validity time
                        if (categoriesReturnData.success === 1) {
                            var companies = companiesReturnData.companies;
                            var categories = categoriesReturnData.categories;
                            var headersHtml = "<tr>";
                            headersHtml += "<th>";
                            headersHtml += "ID:";
                            headersHtml += "</th>";
                            headersHtml += "<th>";
                            headersHtml += "Name:";
                            headersHtml += "</th>";
                            Object.keys(categories).forEach(function(categoryType) {
                                headersHtml += "<th>";
                                headersHtml += categoryType + "s:";
                                headersHtml += "</th>";
                            });
                            headersHtml += "<th>";
                            headersHtml += "Table Number:";
                            headersHtml += "</th>";
                            headersHtml += "</tr>";
                            $("#companiesTable").html(headersHtml);
                            Object.keys(companies).forEach(function(companyId) {
                                var company = companies[companyId];
                                var companyHtml = "<tr>";
                                companyHtml += "<td>";
                                companyHtml += company.id;
                                companyHtml += "</td>";
                                companyHtml += "<td>";
                                companyHtml += company.name;
                                companyHtml += "</td>";
                                Object.keys(categories).forEach(function(categoryType) {
                                    var first = true;
                                    companyHtml += "<td>";
                                    _.intersection(_.map(Object.keys(categories[categoryType]), function(str){return Number(str);}), company.categories).forEach(function(categoryId) {
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
                                companyHtml += company.tableNumber;
                                companyHtml += "</td>";
                                companyHtml += "</tr>";
                                $("#companiesTable").append(companyHtml);
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
            }
        },
        errorHandler: function(jqXHR, textStatus, errorThrown) {
            console.log(textStatus + " : " + errorThrown);
            $("#contentFrame").load("login.html");
        }
    });
})();
window.cleanup = function() {};