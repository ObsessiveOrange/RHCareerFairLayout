$(document).ready(function(){
    alert("storing testKey:testValue");
    SessionVars.storeString("testKey", "testValue");
    alert(SessionVars.retreiveString("testKey"));

});