var $canvas;
var page;
var slideCounter;

function initTutorials(currentPage) {
    $canvas = $("#tutorial");
    page = currentPage;
    slideCounter = 0;
    $canvas.click(function(event) {
        goToNextSlide();
        event.stopPropagation();
    });
    tutorialObjects["draw" + page + "TutorialSlide" + slideCounter]();
}

function goToNextSlide() {
    slideCounter++;
    $canvas.clearCanvas();
    if (tutorialObjects.hasOwnProperty("draw" + page + "TutorialSlide" + slideCounter)) {
        tutorialObjects["draw" + page + "TutorialSlide" + slideCounter]();
        console.log("Next Slide!");
    } else {
        endTutorial();
    }
}

function endTutorial() {
    console.log("Done!");
    $canvas.remove();
}
var tutorialObjects = {
    drawMainTutorialSlide0: function() {
        $('html, body').animate({
            scrollTop: $("#navBarContainer").offset().top
        }, 1000);
        $canvas.drawRect({
            fillStyle: 'rgba(0, 0, 0, 0.75)',
            x: 0,
            y: 0,
            width: $canvas.width(),
            height: $canvas.height(),
            fromCenter: false
        });
        alert($canvas.width() + " x " + $canvas.height());
        $canvas.drawText({
            fillStyle: '#0AF',
            x: $(window).width() / 2,
            y: $(window).height() / 2 - 100,
            text: "Welcome to the Rose Career Fair Layout website.",
            fontSize: '30pt',
            fontStyle: 'bold',
            fontFamily: 'Verdana, sans-serif',
        });
        $canvas.drawText({
            fillStyle: '#0AF',
            x: $(window).width() / 2,
            y: $(window).height() / 2,
            text: "This 30-second walkthrough will show you\nthis site's features and options.\n\nPress anywhere to continue.",
            fontSize: '20pt',
            fontStyle: 'bold',
            fontFamily: 'Verdana, sans-serif',
        });
        $canvas.drawRect({
            strokeStyle: "#F55",
            fillStyle: '#F88',
            x: $(window).width() / 2,
            y: $(window).height() / 2 + 150,
            width: 200,
            height: 50,
            click: function(layer) {
                endTutorial();
            }
        });
        $canvas.drawText({
            fillStyle: '#FFF',
            x: $(window).width() / 2,
            y: $(window).height() / 2 + 150,
            text: "Skip",
            fontSize: '20pt',
            fontStyle: 'bold',
            fontFamily: 'Verdana, sans-serif',
            click: function(layer) {
                endTutorial();
            }
        });
    },
    //
    //draw tutorial page
    drawMainTutorialSlide1: function() {
        $canvas.drawInvertedEllipse({
            x: 0,
            y: 0,
            width: $canvas.width(),
            height: $canvas.height(),
            holeX: $("#filterBtn").offset().left + $("#filterBtn").width() / 2,
            holeY: $("#filterBtn").offset().top + $("#filterBtn").height() / 2,
            holeWidth: 100,
            holeHeight: 100,
            fromCenter: false,
            holeFromCenter: true,
            mask: true
        });
        // This shape is being masked
        $canvas.drawRect({
            fillStyle: 'rgba(0, 0, 0, 0.75)',
            x: 0,
            y: 0,
            width: $canvas.width(),
            height: $canvas.height(),
            fromCenter: false,
        });
        $canvas.restoreCanvas();
        $canvas.drawEllipse({
            strokeStyle: '#0AF',
            strokeWidth: 2,
            x: $("#filterBtn").offset().left + $("#filterBtn").width() / 2,
            y: $("#filterBtn").offset().top + $("#filterBtn").height() / 2,
            width: 100,
            height: 100,
        });
        $canvas.drawArc({
            strokeStyle: '#0AF',
            strokeWidth: 5,
            x: $("#filterBtn").offset().left + $("#filterBtn").width() / 2 - 200,
            y: $("#filterBtn").offset().top + $("#filterBtn").height() / 2 + 50,
            radius: 200,
            // start and end angles in degrees
            start: 90,
            end: 135,
        });
        $canvas.drawLine({
            strokeStyle: '#0AF',
            strokeWidth: 5,
            x1: $("#filterBtn").offset().left + $("#filterBtn").width() / 2,
            y1: $("#filterBtn").offset().top + $("#filterBtn").height() / 2 + 50,
            x2: $("#filterBtn").offset().left + $("#filterBtn").width() / 2 - 15,
            y2: $("#filterBtn").offset().top + $("#filterBtn").height() / 2 + 75,
            x3: $("#filterBtn").offset().left + $("#filterBtn").width() / 2 + 15,
            y3: $("#filterBtn").offset().top + $("#filterBtn").height() / 2 + 75,
            closed: true,
        });
        $canvas.drawText({
            fillStyle: '#0AF',
            x: $("#filterBtn").offset().left + $("#filterBtn").width() / 2 - 200 + 200 / Math.sqrt(2) - 5,
            y: $("#filterBtn").offset().top + $("#filterBtn").height() / 2 + 50 + 200 / Math.sqrt(2),
            align: 'right',
            respectAlign: true,
            text: "This is the filter button;\nClick it to filter companies\nlike you would in CareerLink.",
            fontSize: '20pt',
            fontStyle: 'bold',
            fontFamily: 'Verdana, sans-serif',
        });
    },
    //
    //draw tutorial page
    drawMainTutorialSlide2: function() {
        $('html, body').animate({
            scrollTop: $("#canvasMapContainer").offset().top
        }, 1000);
        $canvas.drawInvertedRectangle({
            x: 0,
            y: 0,
            width: $canvas.width(),
            height: $canvas.height(),
            holeX: $("#canvasMapContainer").offset().left,
            holeY: $("#canvasMapContainer").offset().top,
            holeWidth: $("#canvasMapContainer").width(),
            holeHeight: $("#canvasMapContainer").height(),
            fromCenter: false,
            holeFromCenter: false,
            mask: true
        });
        // This shape is being masked
        $canvas.drawRect({
            fillStyle: 'rgba(0, 0, 0, 0.75)',
            x: 0,
            y: 0,
            width: $canvas.width(),
            height: $canvas.height(),
            fromCenter: false,
        });
        $canvas.restoreCanvas();
        $canvas.drawRect({
            strokeStyle: '#0AF',
            strokeWidth: 5,
            x: $("#canvasMapContainer").offset().left,
            y: $("#canvasMapContainer").offset().top,
            width: $("#canvasMapContainer").width(),
            height: $("#canvasMapContainer").height(),
            cornerRadius: 10,
            fromCenter: false,
        });
        $canvas.drawRect({
            fillStyle: 'rgba(0, 0, 0, 0.8)',
            strokeWidth: 5,
            x: $("#canvasMapContainer").offset().left + $("#canvasMapContainer").width() / 2,
            y: $("#canvasMapContainer").offset().top + $("#canvasMapContainer").height() / 2,
            width: 500,
            height: 100,
            cornerRadius: 10
        });
        $canvas.drawText({
            fillStyle: '#0AF',
            x: $("#canvasMapContainer").offset().left + $("#canvasMapContainer").width() / 2,
            y: $("#canvasMapContainer").offset().top + $("#canvasMapContainer").height() / 2,
            text: "This is the map of the career fair;\nCompanies that fit the filters and that you\nhave selected will be highlighted here.",
            fontSize: '20pt',
            fontStyle: 'bold',
            fontFamily: 'Verdana, sans-serif',
        });
    }
}