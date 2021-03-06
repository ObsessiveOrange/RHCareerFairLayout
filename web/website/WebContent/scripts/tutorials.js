/**
 * Dynamic tutorial creator for webpage
 *
 * Creator: Benedict Wong, 2015
 */
//
// Set strict mode on.
"use strict";
//
// Initialize variables
var $canvas;
var page;
var slideCounter;
var top;
var timeoutEvent;
var skipButton = null;
/**
 * Setup canvas and tutorial variables.
 *
 */
function initTutorials(currentPage) {
    //
    //set environment variables required for endTutorial call, if needed.
    page = currentPage;
    $canvas = $("#tutorial");
    //
    //if tutorial has already been shown this session, end the tutorial.
    var tutorialStatus = PersistentStorage.retrieveObject("tutorialStatus");
    if (tutorialStatus && tutorialStatus[page]) {
        endTutorial();
        return;
    }
    //
    //set other environment variables. 
    slideCounter = 0;
    //
    //hide scrollbars
    hideScrollbars();
    //set canvas width and heights.
    setupCanvas();
    //
    //setup bindings for clicks and resizes.
    $canvas.click(function(event) {
        goToNextSlide();
        event.stopPropagation();
    });
    //
    //setup resize bindings
    $(window).resize(function() {
        //
        //reset height and width
        setupCanvas();
        //
        //redraw the same canvas slide.
        $canvas.clearCanvas();
        tutorialObjects["draw" + page + "TutorialSlide" + slideCounter]();
    });
    //
    //call first drawTutorial method
    tutorialObjects["draw" + page + "TutorialSlide" + slideCounter]();
}

function setupCanvas() {
    $canvas.prop("width", $canvas.width());
    $canvas.prop("height", $canvas.height());
}
/**
 * Go to the next slide, if no more slides, end tutorial
 *
 */
function goToNextSlide() {
    slideCounter++;
    $canvas.clearCanvas();
    if (skipButton !== null) {
        skipButton.remove();
        skipButton = null;
    }
    if (tutorialObjects.hasOwnProperty("draw" + page + "TutorialSlide" + slideCounter)) {
        tutorialObjects["draw" + page + "TutorialSlide" + slideCounter]();
    } else {
        endTutorial();
    }
}
/**
 * End tutorial
 *
 */
function endTutorial() {
    if (skipButton !== null) {
        skipButton.remove();
        skipButton = null;
    }
    $(window).unbind('resize');
    $canvas.remove();
    unlockScrolling();
    showScrollbars();
    var tutorialStatus = PersistentStorage.retrieveObject("tutorialStatus");
    if (!tutorialStatus) {
        tutorialStatus = {};
    }
    tutorialStatus[page] = true;
    PersistentStorage.storeObject("tutorialStatus", tutorialStatus);
}

function lockScrolling(top) {
    $(window).on("scroll touchmove mousewheel", function() {
        $(this).scrollTop(top);
    });
}

function hideScrollbars() {
    $('body').css('overflow', 'hidden');
}

function unlockScrolling() {
    clearTimeout(timeoutEvent);
    $(window).unbind('scroll');
    $(window).unbind('touchmove');
    $(window).unbind('mousewheel');
}

function showScrollbars() {
    $('body').css('overflow', 'auto');
}
var tutorialObjects = {
    drawMainTutorialSlide0: function() {
        unlockScrolling();
        $('html, body').animate({
            scrollTop: $("#navBarContainer").offset().top
        }, 1000);
        timeoutEvent = setTimeout(function() {
            lockScrolling($("#navBarContainer").offset().top);
        }, 1000);
        $canvas.drawRect({
            fillStyle: 'rgba(0, 0, 0, 0.80)',
            x: 0,
            y: 0,
            width: $canvas.width(),
            height: $canvas.height(),
            fromCenter: false
        });
        $canvas.drawText({
            fillStyle: '#0AF',
            x: $(window).width() / 2,
            y: $(window).height() / 2 - 100,
            text: "Welcome to the Rose Career Fair Layout website.",
            fontSize: '30pt',
            fontStyle: 'bold',
            fontFamily: 'Verdana, sans-serif'
        });
        $canvas.drawText({
            fillStyle: '#0AF',
            x: $(window).width() / 2,
            y: $(window).height() / 2,
            text: "This 30-second walkthrough will show you\nthis site's features and options.\n\nPress anywhere to continue.",
            fontSize: '20pt',
            fontStyle: 'bold',
            fontFamily: 'Verdana, sans-serif'
        });
        if (skipButton === null) {
            $("body").append("<div id='tutSkipButton' class='roundTopLeft roundTopRight roundBottomLeft roundBottomRight' onClick='endTutorial()'><span id='tutSkipText'>Skip</span></div>");
            skipButton = $("#tutSkipButton");
        }
    },
    //
    //draw tutorial page
    drawMainTutorialSlide1: function() {
        $canvas.drawInvertedRectangle({
            x: 0,
            y: 0,
            width: $canvas.width(),
            height: $canvas.height(),
            holeX: $("#filterBtn").offset().left + $("#filterBtn").width() / 2,
            holeY: $("#filterBtn").position().top + $("#filterBtn").height() / 2,
            holeWidth: 100,
            holeHeight: 100,
            cornerRadius: 10,
            holeCornerRadius: 10,
            fromCenter: false,
            holeFromCenter: true,
            mask: true
        });
        // This shape is being masked
        $canvas.drawRect({
            fillStyle: 'rgba(0, 0, 0, 0.80)',
            x: 0,
            y: 0,
            width: $canvas.width(),
            height: $canvas.height(),
            fromCenter: false
        });
        $canvas.restoreCanvas();
        $canvas.drawRect({
            strokeStyle: '#0AF',
            strokeWidth: 5,
            x: $("#filterBtn").offset().left + $("#filterBtn").width() / 2,
            y: $("#filterBtn").position().top + $("#filterBtn").height() / 2,
            width: 100,
            height: 100,
            cornerRadius: 10
        });
        $canvas.drawArc({
            strokeStyle: '#0AF',
            strokeWidth: 5,
            x: $("#filterBtn").offset().left + $("#filterBtn").width() / 2 - 200,
            y: $("#filterBtn").position().top + $("#filterBtn").height() / 2 + 55,
            radius: 200,
            // start and end angles in degrees
            start: 90,
            end: 135
        });
        $canvas.drawLine({
            strokeStyle: '#0AF',
            strokeWidth: 5,
            x1: $("#filterBtn").offset().left + $("#filterBtn").width() / 2,
            y1: $("#filterBtn").position().top + $("#filterBtn").height() / 2 + 55,
            x2: $("#filterBtn").offset().left + $("#filterBtn").width() / 2 - 15,
            y2: $("#filterBtn").position().top + $("#filterBtn").height() / 2 + 80,
            x3: $("#filterBtn").offset().left + $("#filterBtn").width() / 2 + 15,
            y3: $("#filterBtn").position().top + $("#filterBtn").height() / 2 + 80,
            closed: true
        });
        $canvas.drawText({
            fillStyle: '#0AF',
            x: $("#filterBtn").offset().left + $("#filterBtn").width() / 2 - 200 + 200 / Math.sqrt(2) - 5,
            y: $("#filterBtn").position().top + $("#filterBtn").height() / 2 + 50 + 200 / Math.sqrt(2) + 5,
            align: 'right',
            respectAlign: true,
            text: "This is the filter button;\nClick it to filter companies\nlike you would in CareerLink.",
            fontSize: '20pt',
            fontStyle: 'bold',
            fontFamily: 'Verdana, sans-serif'
        });
    },
    //
    //draw tutorial page
    drawMainTutorialSlide2: function() {
        unlockScrolling();
        $('html, body').animate({
            scrollTop: $("#mapContainer").offset().top
        }, 1000);
        timeoutEvent = setTimeout(function() {
            lockScrolling($("#mapContainer").offset().top);
        }, 1000);
        $canvas.drawInvertedRectangle({
            x: 0,
            y: 0,
            width: $canvas.width(),
            height: $canvas.height(),
            holeX: $("#canvasMap").offset().left,
            holeY: $("#canvasMap").position().top,
            holeWidth: $("#canvasMap").width(),
            holeHeight: $("#canvasMap").height(),
            cornerRadius: 10,
            holeCornerRadius: 10,
            fromCenter: false,
            holeFromCenter: false,
            mask: true
        });
        // This shape is being masked
        $canvas.drawRect({
            fillStyle: 'rgba(0, 0, 0, 0.80)',
            x: 0,
            y: 0,
            width: $canvas.width(),
            height: $canvas.height(),
            fromCenter: false
        });
        $canvas.restoreCanvas();
        $canvas.drawRect({
            strokeStyle: '#0AF',
            strokeWidth: 5,
            x: $("#canvasMap").offset().left,
            y: $("#canvasMap").position().top,
            width: $("#canvasMap").width(),
            height: $("#canvasMap").height(),
            cornerRadius: 10,
            fromCenter: false
        });
        $canvas.drawRect({
            fillStyle: 'rgba(0, 0, 0, 0.8)',
            strokeWidth: 5,
            x: $("#canvasMap").offset().left + $("#canvasMap").width() / 2,
            y: Math.min($("#canvasMap").position().top + $("#canvasMap").height() / 2, $(window).height() / 2),
            width: 500,
            height: 100,
            cornerRadius: 10
        });
        $canvas.drawText({
            fillStyle: '#0AF',
            x: $("#canvasMap").offset().left + $("#canvasMap").width() / 2,
            y: Math.min($("#canvasMap").position().top + $("#canvasMap").height() / 2, $(window).height() / 2),
            text: "This is the map of the career fair;\nCompanies that fit the filters and that you\nhave selected will be highlighted here.",
            fontSize: '20pt',
            fontStyle: 'bold',
            fontFamily: 'Verdana, sans-serif'
        });
    },
    //
    //draw tutorial page
    drawMainTutorialSlide3: function() {
        unlockScrolling();
        $('html, body').animate({
            scrollTop: $("#companySearchBar").offset().top
        }, 1000);
        timeoutEvent = setTimeout(function() {
            lockScrolling($("#companySearchBar").offset().top);
        }, 1000);
        $canvas.drawInvertedRectangle({
            x: 0,
            y: 0,
            width: $canvas.width(),
            height: $canvas.height(),
            holeX: $("#companySearchBar").offset().left,
            holeY: $("#companySearchBar").position().top,
            holeWidth: $("#companySearchBar").outerWidth(),
            holeHeight: $("#companySearchBar").outerHeight(),
            cornerRadius: 10,
            holeCornerRadius: 10,
            fromCenter: false,
            holeFromCenter: false,
            mask: true
        });
        // This shape is being masked
        $canvas.drawRect({
            fillStyle: 'rgba(0, 0, 0, 0.80)',
            x: 0,
            y: 0,
            width: $canvas.width(),
            height: $canvas.height(),
            fromCenter: false
        });
        $canvas.restoreCanvas();
        $canvas.drawRect({
            strokeStyle: '#0AF',
            strokeWidth: 5,
            x: $("#companySearchBar").offset().left,
            y: $("#companySearchBar").position().top,
            width: $("#companySearchBar").outerWidth(),
            height: $("#companySearchBar").outerHeight(),
            cornerRadius: 10,
            fromCenter: false
        });
        $canvas.drawRect({
            fillStyle: 'rgba(0, 0, 0, 0.8)',
            strokeWidth: 5,
            x: $("#companySearchBar").offset().left + $("#companySearchBar").outerWidth() / 2,
            y: $("#companySearchBar").position().top + $("#companySearchBar").outerHeight() / 2,
            width: 400,
            height: 80,
            cornerRadius: 10
        });
        $canvas.drawText({
            fillStyle: '#0AF',
            x: $("#companySearchBar").offset().left + $("#companySearchBar").outerWidth() / 2,
            y: $("#companySearchBar").position().top + $("#companySearchBar").outerHeight() / 2,
            text: "This is the search bar.\nType a company name to search.",
            fontSize: '20pt',
            fontStyle: 'bold',
            fontFamily: 'Verdana, sans-serif'
        });
    },
    //
    //draw tutorial page
    drawMainTutorialSlide4: function() {
        $canvas.drawInvertedRectangle({
            x: 0,
            y: 0,
            width: $canvas.width(),
            height: $canvas.height(),
            holeX: $("#selectionButtons").offset().left,
            holeY: $("#selectionButtons").position().top,
            holeWidth: $("#selectionButtons").outerWidth(),
            holeHeight: $("#selectionButtons").outerHeight(),
            cornerRadius: 10,
            holeCornerRadius: 10,
            fromCenter: false,
            holeFromCenter: false,
            mask: true
        });
        // This shape is being masked
        $canvas.drawRect({
            fillStyle: 'rgba(0, 0, 0, 0.80)',
            x: 0,
            y: 0,
            width: $canvas.width(),
            height: $canvas.height(),
            fromCenter: false
        });
        $canvas.restoreCanvas();
        $canvas.drawRect({
            strokeStyle: '#0AF',
            strokeWidth: 5,
            x: $("#selectionButtons").offset().left,
            y: $("#selectionButtons").position().top,
            width: $("#selectionButtons").outerWidth(),
            height: $("#selectionButtons").outerHeight(),
            cornerRadius: 10,
            fromCenter: false
        });
        $canvas.drawArc({
            strokeStyle: '#0AF',
            strokeWidth: 5,
            x: $("#selectionButtons").offset().left + $("#selectionButtons").width() / 2 - 200,
            y: $("#selectionButtons").position().top + $("#selectionButtons").height() + 5,
            radius: 200,
            // start and end angles in degrees
            start: 90,
            end: 135
        });
        $canvas.drawLine({
            strokeStyle: '#0AF',
            strokeWidth: 5,
            x1: $("#selectionButtons").offset().left + $("#selectionButtons").width() / 2,
            y1: $("#selectionButtons").position().top + $("#selectionButtons").height() + 5,
            x2: $("#selectionButtons").offset().left + $("#selectionButtons").width() / 2 - 15,
            y2: $("#selectionButtons").position().top + $("#selectionButtons").height() + 30,
            x3: $("#selectionButtons").offset().left + $("#selectionButtons").width() / 2 + 15,
            y3: $("#selectionButtons").position().top + $("#selectionButtons").height() + 30,
            closed: true
        });
        $canvas.drawText({
            fillStyle: '#0AF',
            x: $("#selectionButtons").offset().left + $("#selectionButtons").width() / 2 - 200 + 200 / Math.sqrt(2) - 5,
            y: $("#selectionButtons").position().top + $("#selectionButtons").height() + 200 / Math.sqrt(2) + 5,
            align: 'right',
            respectAlign: true,
            text: "Use these buttons\nto do bulk selections.",
            fontSize: '20pt',
            fontStyle: 'bold',
            fontFamily: 'Verdana, sans-serif'
        });
    },
    //
    //draw tutorial page
    drawMainTutorialSlide5: function() {
        $canvas.drawInvertedRectangle({
            x: 0,
            y: 0,
            width: $canvas.width(),
            height: $canvas.height(),
            holeX: $("#companyListTable").offset().left,
            holeY: $("#companyListTable").position().top,
            holeWidth: $("#companyListTable").outerWidth(),
            holeHeight: $("#companyListTable").outerHeight(),
            cornerRadius: 10,
            holeCornerRadius: 10,
            fromCenter: false,
            holeFromCenter: false,
            mask: true
        });
        // This shape is being masked
        $canvas.drawRect({
            fillStyle: 'rgba(0, 0, 0, 0.80)',
            x: 0,
            y: 0,
            width: $canvas.width(),
            height: $canvas.height(),
            fromCenter: false
        });
        $canvas.restoreCanvas();
        $canvas.drawRect({
            strokeStyle: '#0AF',
            strokeWidth: 5,
            x: $("#companyListTable").offset().left,
            y: $("#companyListTable").position().top,
            width: $("#companyListTable").outerWidth(),
            height: $("#companyListTable").outerHeight(),
            cornerRadius: 10,
            fromCenter: false
        });
        $canvas.drawRect({
            fillStyle: 'rgba(0, 0, 0, 0.8)',
            strokeWidth: 5,
            x: $("#companyListTable").offset().left + $("#companyListTable").outerWidth() / 2,
            y: $("#companyListTable").position().top + ($(window).height() - $("#companyListTable").position().top) / 2,
            width: 600,
            height: 80,
            cornerRadius: 10
        });
        $canvas.drawText({
            fillStyle: '#0AF',
            x: $("#companyListTable").offset().left + $("#companyListTable").outerWidth() / 2,
            y: $("#companyListTable").position().top + ($(window).height() - $("#companyListTable").position().top) / 2,
            text: "Companies are listed here.\nClick on the checkbox or company name\nto toggle the highlight on the map",
            fontSize: '20pt',
            fontStyle: 'bold',
            fontFamily: 'Verdana, sans-serif'
        });
    },
    //
    //draw tutorial page
    drawMainTutorialSlide6: function() {
        $canvas.drawRect({
            fillStyle: 'rgba(0, 0, 0, 0.80)',
            x: 0,
            y: 0,
            width: $canvas.width(),
            height: $canvas.height(),
            fromCenter: false
        });
        $canvas.drawText({
            fillStyle: '#0AF',
            x: $canvas.width() / 2,
            y: $canvas.height() / 2,
            text: "That's it for this page!\n\nGood luck in the career fair!",
            fontSize: '30pt',
            fontStyle: 'bold',
            fontFamily: 'Verdana, sans-serif'
        });
        $canvas.drawText({
            fillStyle: '#0AF',
            x: $canvas.width() / 2,
            y: $canvas.height() - 25,
            text: "RHCareerFairLayout, by Benedict Wong",
            fontSize: '15pt',
            fontStyle: 'bold',
            fontFamily: 'Verdana, sans-serif'
        });
    },
    drawFiltersTutorialSlide0: function() {
        unlockScrolling();
        $('html, body').animate({
            scrollTop: $("#navBarContainer").offset().top
        }, 1000);
        timeoutEvent = setTimeout(function() {
            lockScrolling($("#navBarContainer").offset().top);
        }, 1000);
        $canvas.drawRect({
            fillStyle: 'rgba(0, 0, 0, 0.80)',
            x: 0,
            y: 0,
            width: $canvas.width(),
            height: $canvas.height(),
            fromCenter: false
        });
        $canvas.drawText({
            fillStyle: '#0AF',
            x: $(window).width() / 2,
            y: $(window).height() / 2 - 100,
            text: "Welcome to the Rose Career Fair Layout website.",
            fontSize: '30pt',
            fontStyle: 'bold',
            fontFamily: 'Verdana, sans-serif'
        });
        $canvas.drawText({
            fillStyle: '#0AF',
            x: $(window).width() / 2,
            y: $(window).height() / 2,
            text: "This 30-second walkthrough will show you\nthis site's features and options.\n\nPress anywhere to continue.",
            fontSize: '20pt',
            fontStyle: 'bold',
            fontFamily: 'Verdana, sans-serif'
        });
        if (skipButton === null) {
            $("body").append("<div id='tutSkipButton' class='roundTopLeft roundTopRight roundBottomLeft roundBottomRight' onClick='endTutorial()'><span id='tutSkipText'>Skip</span></div>");
            skipButton = $("#tutSkipButton");
        }
    },
    drawFiltersTutorialSlide1: function() {
        $canvas.drawRect({
            fillStyle: 'rgba(0, 0, 0, 0.80)',
            x: 0,
            y: 0,
            width: $canvas.width(),
            height: $canvas.height(),
            fromCenter: false
        });
        $canvas.drawText({
            fillStyle: '#0AF',
            x: $(window).width() / 2,
            y: $(window).height() / 2 - 100,
            text: "This is the filters page.",
            fontSize: '30pt',
            fontStyle: 'bold',
            fontFamily: 'Verdana, sans-serif'
        });
        $canvas.drawText({
            fillStyle: '#0AF',
            x: $(window).width() / 2,
            y: $(window).height() / 2,
            text: "Selecting filters here will allow you to see companies based on\nwhich major, positions or work authorizations they accept.",
            fontSize: '20pt',
            fontStyle: 'bold',
            fontFamily: 'Verdana, sans-serif'
        });
    },
    //
    //draw tutorial page
    drawFiltersTutorialSlide2: function() {
        $canvas.drawInvertedRectangle({
            x: 0,
            y: 0,
            width: $canvas.width(),
            height: $canvas.height(),
            holeX: $("#backBtn").offset().left + $("#backBtn").width() / 2,
            holeY: $("#backBtn").position().top + $("#backBtn").height() / 2,
            holeWidth: 50,
            holeHeight: 100,
            cornerRadius: 10,
            holeCornerRadius: 10,
            fromCenter: false,
            holeFromCenter: true,
            mask: true
        });
        // This shape is being masked
        $canvas.drawRect({
            fillStyle: 'rgba(0, 0, 0, 0.80)',
            x: 0,
            y: 0,
            width: $canvas.width(),
            height: $canvas.height(),
            fromCenter: false
        });
        $canvas.restoreCanvas();
        $canvas.drawRect({
            strokeStyle: '#0AF',
            strokeWidth: 5,
            x: $("#backBtn").offset().left + $("#backBtn").width() / 2,
            y: $("#backBtn").position().top + $("#backBtn").height() / 2,
            width: 100,
            height: 100,
            cornerRadius: 10
        });
        $canvas.drawArc({
            strokeStyle: '#0AF',
            strokeWidth: 5,
            x: $("#backBtn").offset().left + $("#backBtn").width() / 2 + 200,
            y: $("#backBtn").position().top + $("#backBtn").height() / 2 + 55,
            radius: 200,
            // start and end angles in degrees
            start: 225,
            end: 270
        });
        $canvas.drawLine({
            strokeStyle: '#0AF',
            strokeWidth: 5,
            x1: $("#backBtn").offset().left + $("#backBtn").width() / 2,
            y1: $("#backBtn").position().top + $("#backBtn").height() / 2 + 55,
            x2: $("#backBtn").offset().left + $("#backBtn").width() / 2 - 15,
            y2: $("#backBtn").position().top + $("#backBtn").height() / 2 + 80,
            x3: $("#backBtn").offset().left + $("#backBtn").width() / 2 + 15,
            y3: $("#backBtn").position().top + $("#backBtn").height() / 2 + 80,
            closed: true
        });
        $canvas.drawText({
            fillStyle: '#0AF',
            x: $("#backBtn").offset().left + $("#backBtn").width() / 2 + 200 - 200 / Math.sqrt(2) + 5,
            y: $("#backBtn").position().top + $("#backBtn").height() / 2 + 50 + 200 / Math.sqrt(2) + 5,
            align: 'left',
            respectAlign: true,
            text: "This is the back button;\nIt will bring you back to the main page,\n discarding all filter changes",
            fontSize: '20pt',
            fontStyle: 'bold',
            fontFamily: 'Verdana, sans-serif'
        });
    },
    //
    //draw tutorial page
    drawFiltersTutorialSlide3: function() {
        $canvas.drawInvertedRectangle({
            x: 0,
            y: 0,
            width: $canvas.width(),
            height: $canvas.height(),
            holeX: $("#filtersListTable").offset().left,
            holeY: $("#filtersListTable").offset().top,
            holeWidth: $("#filtersListTable").outerWidth(),
            holeHeight: $("#filtersListTable").outerHeight(),
            cornerRadius: 10,
            holeCornerRadius: 10,
            fromCenter: false,
            holeFromCenter: false,
            mask: true
        });
        // This shape is being masked
        $canvas.drawRect({
            fillStyle: 'rgba(0, 0, 0, 0.80)',
            x: 0,
            y: 0,
            width: $canvas.width(),
            height: $canvas.height(),
            fromCenter: false
        });
        $canvas.restoreCanvas();
        $canvas.drawRect({
            strokeStyle: '#0AF',
            strokeWidth: 5,
            x: $("#filtersListTable").offset().left,
            y: $("#filtersListTable").offset().top,
            width: $("#filtersListTable").outerWidth(),
            height: $("#filtersListTable").outerHeight(),
            cornerRadius: 10,
            fromCenter: false
        });
        $canvas.drawRect({
            fillStyle: 'rgba(0, 0, 0, 0.8)',
            strokeWidth: 5,
            x: $("#filtersListTable").offset().left + $("#filtersListTable").outerWidth() / 2,
            y: $("#filtersListTable").offset().top + $("#filtersListTable").outerHeight() / 2,
            width: 600,
            height: 120,
            cornerRadius: 10
        });
        $canvas.drawText({
            fillStyle: '#0AF',
            x: $("#filtersListTable").offset().left + $("#filtersListTable").outerWidth() / 2,
            y: $("#filtersListTable").offset().top + $("#filtersListTable").outerHeight() / 2,
            text: "All the filters are here, grouped by type.\nThis should be similar to what what\nyou may have used on CareerLink.\n\nClick on each group to show/hide it's subfilters.",
            fontSize: '20pt',
            fontStyle: 'bold',
            fontFamily: 'Verdana, sans-serif'
        });
    },
    //
    //draw tutorial page
    drawFiltersTutorialSlide4: function() {
        $canvas.drawInvertedRectangle({
            x: 0,
            y: 0,
            width: $canvas.width(),
            height: $canvas.height(),
            holeX: $("#filterButtons").offset().left + $("#filterButtons").width() / 2,
            holeY: $("#filterButtons").offset().top + $("#filterButtons").height() / 2,
            holeWidth: $("#filterButtons").width(),
            holeHeight: $("#filterButtons").height(),
            cornerRadius: 10,
            holeCornerRadius: 10,
            fromCenter: false,
            holeFromCenter: true,
            mask: true
        });
        // This shape is being masked
        $canvas.drawRect({
            fillStyle: 'rgba(0, 0, 0, 0.80)',
            x: 0,
            y: 0,
            width: $canvas.width(),
            height: $canvas.height(),
            fromCenter: false
        });
        $canvas.restoreCanvas();
        $canvas.drawRect({
            strokeStyle: '#0AF',
            strokeWidth: 5,
            x: $("#filterButtons").offset().left + $("#filterButtons").width() / 2,
            y: $("#filterButtons").offset().top + $("#filterButtons").height() / 2,
            width: $("#filterButtons").width(),
            height: $("#filterButtons").height(),
            cornerRadius: 10
        });
        $canvas.drawArc({
            strokeStyle: '#0AF',
            strokeWidth: 5,
            x: $("#filterButtons").offset().left + $("#filterButtons").width() / 2 - 200,
            y: $("#filterButtons").offset().top - 5,
            radius: 200,
            // start and end angles in degrees
            start: 45,
            end: 90
        });
        $canvas.drawLine({
            strokeStyle: '#0AF',
            strokeWidth: 5,
            x1: $("#filterButtons").offset().left + $("#filterButtons").width() / 2,
            y1: $("#filterButtons").offset().top - 5,
            x2: $("#filterButtons").offset().left + $("#filterButtons").width() / 2 - 15,
            y2: $("#filterButtons").offset().top - 30,
            x3: $("#filterButtons").offset().left + $("#filterButtons").width() / 2 + 15,
            y3: $("#filterButtons").offset().top - 30,
            closed: true
        });
        $canvas.drawText({
            fillStyle: '#0AF',
            x: $("#filterButtons").offset().left + $("#filterButtons").width() / 2 - 200 + 200 / Math.sqrt(2) - 5,
            y: $("#filterButtons").offset().top - 200 / Math.sqrt(2) - 5,
            align: 'right',
            respectAlign: true,
            text: "These buttons allow you to\ncancel your changes, clear all filters,\n and apply the selected filters, respectively.",
            fontSize: '20pt',
            fontStyle: 'bold',
            fontFamily: 'Verdana, sans-serif'
        });
    },
    //
    //draw tutorial page
    drawFiltersTutorialSlide5: function() {
        $canvas.drawRect({
            fillStyle: 'rgba(0, 0, 0, 0.80)',
            x: 0,
            y: 0,
            width: $canvas.width(),
            height: $canvas.height(),
            fromCenter: false
        });
        $canvas.drawText({
            fillStyle: '#0AF',
            x: $canvas.width() / 2,
            y: $canvas.height() / 2,
            text: "That's it for this page!\n\nGood luck in the career fair!",
            fontSize: '30pt',
            fontStyle: 'bold',
            fontFamily: 'Verdana, sans-serif'
        });
        $canvas.drawText({
            fillStyle: '#0AF',
            x: $canvas.width() / 2,
            y: $canvas.height() - 25,
            text: "RHCareerFairLayout, by Benedict Wong",
            fontSize: '15pt',
            fontStyle: 'bold',
            fontFamily: 'Verdana, sans-serif'
        });
    }
};