$(document).ready(function() {
    var $tutorial = $("#tutorial");
    $tutorial.prop("width", $tutorial.width());
    $tutorial.prop("height", $tutorial.height());
    drawTutorial();
});
//
//draw tutorial page
function drawTutorial() {
    var $canvas = $("#tutorial");
    // This shape is being masked
    $canvas.drawInverted({
        x: 300 ,
        y: 300,
        width: 500,
        height: 500,
        holeX: 250,
        holeY: 250,
        holeRadius: 50,
        fromCenter: false,
        holeFromCenter: false,
        mask: true
    });
    // This shape is being masked
    $canvas.drawRect({
        fillStyle: 'rgb(0, 0, 255)',
        x: 0,
        y: 0,
        width: 1000,
        height: 1000,
        fromCenter: false
    })
    $canvas.restoreCanvas();
    // Restore mask
}