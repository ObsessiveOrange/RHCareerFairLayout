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
    $canvas.drawInvertedRectangle({
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
    // Restore mask
    $canvas.drawRect({
        fillStyle: 'rgba(0, 0, 0, 0.8)',
        x: 300,
        y: 300,
        width: 500,
        height: 100,
        cornerRadius: 10
    });
    $canvas.drawText({
        fillStyle: '#0AF',
        x: 300,
        y: 300,
        text: "This is the map of the career fair;\nCompanies that fit the filters and that you\nhave selected will be highlighted here.",
        fontSize: '20pt',
        fontStyle: 'bold',
        fontFamily: 'Verdana, sans-serif',
    });
}