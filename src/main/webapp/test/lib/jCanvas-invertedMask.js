/**
 */
(function($, Math) {
    var PI = Math.PI;
    $.jCanvas.extend({
        name: 'drawInverted',
        type: 'invertedMask',
        props: {
            holeX: 0,
            holeY: 0,
            holeRadius: 10
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
                params.holeX = params.holeX + params.holeRadius;
                params.holeY = params.holeY + params.holeRadius;
            }
            ctx.moveTo(params.x - params.width / 2, params.y - params.height / 2);
            ctx.lineTo(params.x + params.width / 2, params.y - params.height / 2);
            ctx.lineTo(params.x + params.width / 2, params.y + params.height / 2);
            ctx.lineTo(params.x - params.width / 2, params.y + params.height / 2);
            ctx.closePath();
            // If donut has a hole
            if (params.holeRadius > 0) {
                // Draw inner hole
                ctx.moveTo(params.x - params.width / 2 + params.holeX + params.holeRadius, params.y - params.height / 2 + params.holeY);
                ctx.arc(params.x - params.width / 2 + params.holeX, params.y - params.height / 2 + params.holeY, params.holeRadius, 0, 2 * PI, true);
            }
            // Enable jCanvas events
            $.jCanvas.detectEvents(this, ctx, params);
            $.jCanvas.closePath(this, ctx, params);
        }
    })
}(jQuery, Math));