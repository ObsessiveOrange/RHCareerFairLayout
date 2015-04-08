$(document).ready(function(){
    alert(SessionVars.retreiveString("testKey"));

    SessionVars.clear();
    alert("storing testKey:testValue");
    SessionVars.storeString("testKey", "testValue");

});