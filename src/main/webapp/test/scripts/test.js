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
    $canvas.drawRect({
        fillStyle: 'RGBA(0, 255,0,1)',
        x: 500,
        y: 500,
        width: 150,
        height: 300,
        fromCenter: false
    })
    $canvas.drawInverted({
        x: 500,
        y: 500,
        width: 500,
        height: 500,
        holeX: 250,
        holeY: 250,
        holeRadius: 100,
        mask: true
    });
    // This shape is being masked
    $canvas.drawRect({
        fillStyle: 'rgba(0, 0, 255, 0.5)',
        x: 100,
        y: 100,
        width: 800,
        height: 800,
        fromCenter: false
    })
    $canvas.restoreCanvas();
    $canvas.drawArc({
        fillStyle: 'rgba(0, 0, 153, 0.5)',
        x: 550,
        y: 550,
        radius: 100
    });
    // Restore mask
}