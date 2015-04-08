(function(window) {
    //I recommend this
    'use strict';

    function define_sessionVars() {
        var SessionVars = {};
        SessionVars.storeObject = function(key, object) {
            this.storeString(key, JSON.stringify(object));
        }
        SessionVars.storeString = function(key, value) {
            window.localStorage.setItem(key, value);
        }
        SessionVars.retrieveObject = function(key) {
            return window.JSON.parse(this.retreiveString(key));
        }
        SessionVars.retreiveString = function(key) {
            return window.localStorage.getItem(key);
        }
        SessionVars.removeItem = function(key) {
            return window.localStorage.removeItem(key);
        }
        SessionVars.getKey = function(position) {
            return window.localStorage.key(position);
        }
        SessionVars.clear = function() {
            return window.localStorage.clear();
        }
        SessionVars.length = function() {
            return window.localStorage.length;
        }
        return SessionVars;
    }
    var LSsupport = !(typeof window.localStorage == 'undefined');
    var SSsupport = !(typeof window.sessionStorage == 'undefined');
    //define globally if it doesn't already exist
    if (typeof(SessionVars) === 'undefined') {
        window.SessionVars = define_sessionVars();
    }
})(window);