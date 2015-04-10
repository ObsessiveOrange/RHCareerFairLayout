/**
 */
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
                ctx.moveTo(params.x - params.width / 2 + params.holeX - params.holeWidth / 2, params.y - params.height / 2 + params.holeY - params.holeHeight / 2);
                ctx.lineTo(params.x - params.width / 2 + params.holeX - params.holeWidth / 2, params.y - params.height / 2 + params.holeY + params.holeHeight / 2);
                ctx.lineTo(params.x - params.width / 2 + params.holeX + params.holeWidth / 2, params.y - params.height / 2 + params.holeY + params.holeHeight / 2);
                ctx.lineTo(params.x - params.width / 2 + params.holeX + params.holeWidth / 2, params.y - params.height / 2 + params.holeY - params.holeHeight / 2);
                ctx.closePath();
            }
            // Enable jCanvas events
            $.jCanvas.detectEvents(this, ctx, params);
            $.jCanvas.closePath(this, ctx, params);
        }
    })
}(jQuery, Math));