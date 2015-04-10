var $canvas;
var page;
var slideCounter;

function initTutorials(currentPage) {
    $canvas = $("#tutorial");
    page = currentPage;
    slideCounter = 0;
    $canvas.click(function(event) {
        goToNextSlide();
        console.log("Next Slide!");
        event.stopPropagation();
    });
    tutorialObjects["draw" + page + "TutorialSlide" + slideCounter]();
}

function goToNextSlide() {
    slideCounter++;
    $canvas.clearCanvas();
    tutorialObjects["draw" + page + "TutorialSlide" + slideCounter]();
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
            layer: true,
            fillStyle: 'rgba(0, 0, 0, 0.75)',
            x: 0,
            y: 0,
            width: $canvas.width(),
            height: $canvas.height(),
            fromCenter: false
        });
        $canvas.drawText({
            layer: true,
            fillStyle: '#0AF',
            x: $(window).width() / 2,
            y: $(window).height() / 2 - 100,
            text: "Welcome to the Rose Career Fair Layout website.",
            fontSize: '30pt',
            fontStyle: 'bold',
            fontFamily: 'Verdana, sans-serif',
        });
        $canvas.drawText({
            layer: true,
            fillStyle: '#0AF',
            x: $(window).width() / 2,
            y: $(window).height() / 2,
            text: "This 30-second walkthrough will show you\nthis site's features and options.\n\nPress anywhere to continue.",
            fontSize: '20pt',
            fontStyle: 'bold',
            fontFamily: 'Verdana, sans-serif',
        });
        $canvas.drawRect({
            layer: true,
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
            layer: true,
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
        $('html, body').animate({
            scrollTop: $("#canvasMapContainer").offset().top
        }, 1000);
        $canvas.drawInverted({
            x: 0,
            y: 0,
            width: $canvas.width(),
            height: $canvas.height(),
            holeX: $("#filterBtn").offset().left + $("#filterBtn").width() / 2,
            holeY: $("#filterBtn").offset().top + $("#filterBtn").height() / 2,
            holeRadius: 50,
            fromCenter: false,
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
            click: function() {
                $('html, body').animate({
                    scrollTop: $("#canvasMapContainer").offset().top
                }, 1000);
            }
        });
        $canvas.restoreCanvas();
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
        $canvas.drawEllipse({
            strokeStyle: '#0AF',
            strokeWidth: 2,
            x: $("#filterBtn").offset().left + $("#filterBtn").width() / 2,
            y: $("#filterBtn").offset().top + $("#filterBtn").height() / 2,
            width: 100,
            height: 100,
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
    }
}