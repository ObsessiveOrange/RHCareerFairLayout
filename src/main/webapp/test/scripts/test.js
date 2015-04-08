$(document).ready(function(){
    alert(PersistentStorage.retreiveString("testKey"));

    PersistentStorage.clear();


});


document.onbeforeunload = exit("document.onBeforeUnload");
document.pagehide = exit("document.pagehide");
window.onbeforeunload = exit("window.onBeforeUnload");
function exit(string){

    alert("Unloading from: " + string);
    alert("storing testKey:testValue");
    PersistentStorage.storeString("testKey", "testValue");
}