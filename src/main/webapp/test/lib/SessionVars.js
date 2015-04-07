(function(window) {
    //I recommend this
    'use strict';

    function define_sessionVars() {
        var SessionVars = {};
        SessionVars.storeObject = function(key, object) {
            this.storeString(key, JSON.stringify(object));
        }
        SessionVars.storeString = function(key, value) {
            localStorage.setItem(key, value);
        }
        SessionVars.retrieveObject = function(key) {
            return JSON.parse(localStorage.getItem(key));
        }
        SessionVars.retreiveString = function(key) {
            return localStorage.getItem(key);
        }
        SessionVars.removeItem = function(key) {
            return localStorage.removeItem(key);
        }
        SessionVars.getKey = function(position) {
            return localStorage.key(position);
        }
        SessionVars.clear = function() {
            return localStorage.clear();
        }
        SessionVars.length = function() {
            return localStorage.length;
        }
        return SessionVars;
    }
    //define globally if it doesn't already exist
    if (typeof(SessionVars) === 'undefined') {
        window.SessionVars = define_sessionVars();
    }
})(window);