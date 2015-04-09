(function(window) {
    //I recommend this
    'use strict';

    function define_PersistentStorage() {
        var PersistentStorage = {};
        PersistentStorage.storeObject = function(key, object) {
            this.storeString(key, JSON.stringify(object));
        }
        PersistentStorage.storeString = function(key, value) {
            window.sessionStorage.setItem(key, value);
        }
        PersistentStorage.retrieveObject = function(key) {
            return window.JSON.parse(this.retreiveString(key));
        }
        PersistentStorage.retreiveString = function(key) {
            return window.sessionStorage.getItem(key);
        }
        PersistentStorage.removeItem = function(key) {
            return window.sessionStorage.removeItem(key);
        }
        PersistentStorage.getKey = function(position) {
            return window.sessionStorage.key(position);
        }
        PersistentStorage.clear = function() {
            return window.sessionStorage.clear();
        }
        PersistentStorage.length = function() {
            return window.sessionStorage.length;
        }
        return PersistentStorage;
    }
    var LSsupport = !(typeof window.localStorage == 'undefined');
    var SSsupport = !(typeof window.sessionStorage == 'undefined');
    //define globally if it doesn't already exist
    if (typeof(PersistentStorage) === 'undefined') {
        window.PersistentStorage = define_PersistentStorage();
    }
})(window);