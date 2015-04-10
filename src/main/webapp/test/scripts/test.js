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
    $canvas.drawInvertedEllipse({
        x: 300,
        y: 300,
        width: 500,
        height: 500,
        holeX: 250,
        holeY: 250,
        holeWidth: 200,
        holeHeight: 150,
        fromCenter: false,
        holeFromCenter: true,
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
}