$(document).ready(function(){
    alert(SessionVars.retreiveString("testKey"));

    SessionVars.clear();


});

window.onbeforeunload = function(event) {
    alert("Unloading");
    alert("storing testKey:testValue");
    SessionVars.storeString("testKey", "testValue");
    
}
