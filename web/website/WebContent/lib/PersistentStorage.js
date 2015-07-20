//
// Set strict mode on.
"use strict";
(function(window) {
    function define_PersistentStorage() {
        var PersistentStorage = {};
        PersistentStorage.storeObject = function(key, object) {
            this.storeString(key, JSON.stringify(object));
        }
        PersistentStorage.storeString = function(key, value) {
            window.localStorage.setItem(key, value);
        }
        PersistentStorage.retrieveObject = function(key) {
            return window.JSON.parse(this.retreiveString(key));
        }
        PersistentStorage.retreiveString = function(key) {
            return window.localStorage.getItem(key);
        }
        PersistentStorage.removeItem = function(key) {
            return window.localStorage.removeItem(key);
        }
        PersistentStorage.getKey = function(position) {
            return window.localStorage.key(position);
        }
        PersistentStorage.clear = function() {
            return window.localStorage.clear();
        }
        PersistentStorage.length = function() {
            return window.localStorage.length;
        }
        return PersistentStorage;
    }
    var LSsupport = !(typeof window.localStorage == 'undefined');
    var SSsupport = !(typeof window.localStorage == 'undefined');
    //define globally if it doesn't already exist
    if (typeof(PersistentStorage) === 'undefined') {
        window.PersistentStorage = define_PersistentStorage();
    }
})(window);