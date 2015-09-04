/**
 * Inverted mask plugin for jCanvas
 *
 * Creator: Benedict Wong, 2015
 *
 */
//
// Set strict mode on.
"use strict";
(function($, Math) {
    var PI = Math.PI;
    $.jCanvas.extend({
        name: 'drawInvertedEllipse',
        type: 'invertedEllispe',
        props: {
            holeX: 0,
            holeY: 0,
            holeWidth: 10,
            holeHeight: 10,
            holeFromCenter: true
        },
        fn: function(ctx, params) {
            // Enable shape transformation
            $.jCanvas.transformShape(this, ctx, params);
            ctx.beginPath();
            // Draw outer circle
            if (!params.fromCenter) {
                params.x = params.x + params.width / 2;
                params.y = params.y + params.height / 2;
            }
            if (!params.holeFromCenter) {
                params.holeX = params.holeX + params.holeWidth / 2;
                params.holeY = params.holeY + params.holeHeight / 2;
            }
            ctx.moveTo(params.x - params.width / 2, params.y - params.height / 2);
            ctx.lineTo(params.x + params.width / 2, params.y - params.height / 2);
            ctx.lineTo(params.x + params.width / 2, params.y + params.height / 2);
            ctx.lineTo(params.x - params.width / 2, params.y + params.height / 2);
            ctx.closePath();
            // If donut has a hole
            if (params.holeWidth > 0 && params.holeHeight > 0) {
                // Draw inner hole
                ctx.moveTo(params.x - params.width / 2 + params.holeX + params.holeWidth / 2, params.y - params.height / 2 + params.holeY);
                ctx.ellipse(params.x - params.width / 2 + params.holeX, params.y - params.height / 2 + params.holeY, params.holeWidth / 2, params.holeHeight / 2, 0, 0, 2 * PI, true);
            }
            // Enable jCanvas events
            $.jCanvas.detectEvents(this, ctx, params);
            $.jCanvas.closePath(this, ctx, params);
        }
    })
}(jQuery, Math));
//
/**
 *
 *
 */
(function($, Math) {
    var PI = Math.PI;
    $.jCanvas.extend({
        name: 'drawInvertedRectangle',
        type: 'invertedRectangle',
        props: {
            cornerRadius: 0,
            holeX: 0,
            holeY: 0,
            holeWidth: 10,
            holeHeight: 10,
            holeFromCenter: true,
            holeCornerRadius: 0
        },
        fn: function(ctx, params) {
            // Enable shape transformation
            $.jCanvas.transformShape(this, ctx, params);
            ctx.beginPath();
            // Draw outer circle
            if (!params.fromCenter) {
                params.x = params.x + params.width / 2;
                params.y = params.y + params.height / 2;
            }
            if (!params.holeFromCenter) {
                params.holeX = params.holeX + params.holeWidth / 2;
                params.holeY = params.holeY + params.holeHeight / 2;
            }
            var outerLeft = params.x - params.width / 2;
            var outerTop = params.y - params.height / 2;
            var outerRight = params.x + params.width / 2;
            var outerBottom = params.y + params.height / 2;
            ctx.moveTo(outerLeft + params.cornerRadius, outerTop);
            ctx.lineTo(outerRight - params.cornerRadius, outerTop);
            ctx.arc(outerRight - params.cornerRadius, outerTop + params.cornerRadius, params.cornerRadius, 1.5 * Math.PI, 2.0 * Math.PI);
            ctx.lineTo(outerRight, outerBottom - params.cornerRadius);
            ctx.arc(outerRight - params.cornerRadius, outerBottom - params.cornerRadius, params.cornerRadius, 0.0 * Math.PI, 0.5 * Math.PI);
            ctx.lineTo(outerLeft + params.cornerRadius, outerBottom);
            ctx.arc(outerLeft + params.cornerRadius, outerBottom - params.cornerRadius, params.cornerRadius, 0.5 * Math.PI, 1 * Math.PI);
            ctx.lineTo(outerLeft, outerTop + params.cornerRadius);
            ctx.arc(outerLeft + params.cornerRadius, outerTop + params.cornerRadius, params.cornerRadius, 1.0 * Math.PI, 1.5 * Math.PI);
            ctx.closePath();
            // If donut has a hole
            if (params.holeWidth > 0 && params.holeHeight > 0) {
                var innerLeft = outerLeft + params.holeX - params.holeWidth / 2;
                var innerTop = outerLeft + params.holeY - params.holeHeight / 2;
                var innerRight = outerLeft + params.holeX + params.holeWidth / 2;
                var innerBottom = outerLeft + params.holeY + params.holeHeight / 2;
                ctx.moveTo(innerLeft, innerTop + params.holeCornerRadius);
                ctx.lineTo(innerLeft, innerBottom - params.holeCornerRadius);
                ctx.arc(innerLeft + params.holeCornerRadius, innerBottom - params.holeCornerRadius, params.holeCornerRadius, 1.0 * Math.PI, 0.5 * Math.PI, true);
                ctx.lineTo(innerRight - params.holeCornerRadius, innerBottom);
                ctx.arc(innerRight - params.holeCornerRadius, innerBottom - params.holeCornerRadius, params.holeCornerRadius, 0.5 * Math.PI, 0.0 * Math.PI, true);
                ctx.lineTo(innerRight, innerTop + params.holeCornerRadius);
                ctx.arc(innerRight - params.holeCornerRadius, innerTop + params.holeCornerRadius, params.holeCornerRadius, 0.0 * Math.PI, 1.5 * Math.PI, true);
                ctx.lineTo(innerLeft + params.holeCornerRadius, innerTop);
                ctx.arc(innerLeft + params.holeCornerRadius, innerTop + params.holeCornerRadius, params.holeCornerRadius, 1.5 * Math.PI, 1.0 * Math.PI, true);
                ctx.closePath();
            }
            // Enable jCanvas events
            $.jCanvas.detectEvents(this, ctx, params);
            $.jCanvas.closePath(this, ctx, params);
        }
    })
}(jQuery, Math));