/**
 * Array Extensions to allow for sets
 *
 * Creator: Benedict Wong, 2015
 */
//
// Set strict mode on.
"use strict";
Array.prototype.insertAtIndex = function(item, index) {
    this.splice(index, 0, item);
};
//
//add to array - might be able to be optimized?
Array.prototype.addToOrderedSet = function(item) {
    var insertIndex;
    for (insertIndex = 0; insertIndex < this.length; insertIndex++) {
        if (item == this[insertIndex]) {
            return;
        } else if (item < this[insertIndex]) {
            break;
        }
    }
    this.insertAtIndex(item, insertIndex);
}
Array.prototype.addArrayToOrderedSet = function(array) {
    array.forEach(this.addToOrderedSet(element));
}
//
//Use a binary search to find the element.
Array.prototype.findInOrderedSet = function(item) {
    var low = 0;
    var high = this.length - 1;
    var mid = Math.floor(this.length / 2);
    if (this.length == 0) {
        return -1;
    }
    while (low >= 0 && high < this.length && low <= high) {
        if (item == this[mid]) {
            return mid;
        } else if (low == high) {
            return -1;
        } else if (item < this[mid]) {
            high = mid - 1;
            mid = Math.floor((low + high) / 2);
        } else {
            low = mid + 1
            mid = Math.floor((low + high) / 2);
        }
    }
    return -1;
}
//
//remove from array.
Array.prototype.removeFromOrderedSet = function(item) {
    var index = this.findInOrderedSet(item);
    if (index > -1) {
        return this.splice(index, 1);
    }
    return [];
}