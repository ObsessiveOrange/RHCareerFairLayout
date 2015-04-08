$(document).ready(function(){
    alert(SessionVars.retreiveString("testKey"));

    SessionVars.clear();


});


document.onbeforeunload = exit("document.onBeforeUnload");
document.pagehide = exit("document.pagehide");
window.onbeforeunload = exit("window.onBeforeUnload");
function exit(string){

    alert("Unloading from: " + string);
    alert("storing testKey:testValue");
    SessionVars.storeString("testKey", "testValue");
}