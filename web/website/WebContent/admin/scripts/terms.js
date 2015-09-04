(window.setup = function() {
    sendGetRequest({
        url: "/api/data/all/term?showInactive=true",
        successHandler: function(returnData) {
            var terms = returnData.termList;

            var headersHtml = "<tr>";
            headersHtml += "<th>";
            headersHtml += "ID:";
            headersHtml += "</th>";
            headersHtml += "<th>";
            headersHtml += "Year:";
            headersHtml += "</th>";
            headersHtml += "<th>";
            headersHtml += "Quarter:";
            headersHtml += "</th>";
            headersHtml += "<th>";
            headersHtml += "Active:";
            headersHtml += "</th>";
            headersHtml += "<th>";
            headersHtml += "Actions:";
            headersHtml += "</th>";
            headersHtml += "</tr>";
            $("#termsTable").html(headersHtml);
            terms.forEach(function(term) {
                var termHtml = "<tr>";
                termHtml += "<td>";
                termHtml += term.id;
                termHtml += "</td>";
                termHtml += "<td>";
                termHtml += term.year;
                termHtml += "</td>";
                termHtml += "<td>";
                termHtml += term.quarter;
                termHtml += "</td>";
                termHtml += "<td>";
                termHtml += term.active ? "Active" : "Inactive";
                termHtml += "</td>";
                termHtml += "<td>";
                termHtml += term.active ? "" : "<button type='button' id='setActiveBtn_" + term.id + "' onClick='setTermActive(" + term.id + ")'>Set Active</button>";
                termHtml += "</td>";
                termHtml += "</tr>";
                $("#termsTable").append(termHtml);
            });
        },
        errorHandler: function(jqXHR, textStatus, errorThrown) {
            console.log(textStatus + " : " + errorThrown);
            $("#contentFrame").load("login.html");
        }
    });
})();
window.cleanup = function() {};

function setTermActive(termId){
    sendPostRequest({
        url: "/api/data/" + termId + "/term/active?active=true",
        successHandler: function(returnData) {
            loadContentWithJS(terms);
        },
        errorHandler: function(jqXHR, textStatus, errorThrown) {
            console.log(textStatus + " : " + errorThrown);
        }
    });
}