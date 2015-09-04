function NWayMap(input, headersArray) {
    this.isNWayMap = true;
    if (input.isNWayMap) {
        this.headersArray = input.headersArray;
        this.dataByHeaders = input.dataByHeaders;
        return this;
    }
    this.headersArray = headersArray;
    this.dataByHeaders = {};
    var objArr;
    if (this.type(input) === "object") {
        objArr = _.values(input);
    } else if (this.type(input) === "array") {
        objArr = input;
    }
    for (var i = 0; i < headersArray.length; i++) {
        var header = headersArray[i];
        this.dataByHeaders[header] = {};
    }
    for (var i = 0; i < objArr.length; i++) {
        this.put(objArr[i]);
    }
    return this;
}
NWayMap.prototype.checkHeader = function(header) {
    if (!_.contains(this.headersArray, header)) {
        throw "Error: Invalid header: " + header;
    }
};
NWayMap.prototype.put = function(value) {
    for (var i = 0; i < this.headersArray.length; i++) {
        var header = this.headersArray[i];
        if (this.get(header, value[header]) !== null) {
            throw "Error: Duplicate entry found in header: " + header + " for value " + JSON.stringify(value);
        }
    }
    for (var i = 0; i < this.headersArray.length; i++) {
        var header = this.headersArray[i];
        if (typeof value[header] !== 'undefined' && value[header] !== null) {
            this.dataByHeaders[header][value[header]] = value;
        }
    }
};
NWayMap.prototype.get = function(header, key) {
    this.checkHeader(header);
    return (typeof this.dataByHeaders[header][key] === 'undefined' || this.dataByHeaders[header][key] === null) ? null : this.dataByHeaders[header][key];
};
NWayMap.prototype.remove = function(header, key) {
    var prevValue = this.get(header, key);
    if (prevValue === null) {
        return null;
    }
    for (var i = 0; i < this.headersArray.length; i++) {
        var thisHeader = this.headersArray[i];
        delete this.dataByHeaders[thisHeader][prevValue[thisHeader]];
    }
    return prevValue;
};
NWayMap.prototype.getKeys = function(header) {
    this.checkHeader(header);
    return Object.keys(this.dataByHeaders[header]);
};
NWayMap.prototype.getValues = function(header) {
    this.checkHeader(header);
    var results = [];
    for (var i = 0; i < Object.keys(this.dataByHeaders[header]).length; i++) {
        results.push(this.dataByHeaders[header][Object.keys(this.dataByHeaders[header])[i]]);
    }
    return results;
};
NWayMap.prototype.type = function(a) {
    var t = typeof a;
    if (t === "number" && isNaN(a)) {
        return "NaN";
    } else if (t === "object") {
        return toString.call(a).replace("[object ", "").replace("]", "").toLowerCase();
    } else {
        return t.toLowerCase();
    }
};