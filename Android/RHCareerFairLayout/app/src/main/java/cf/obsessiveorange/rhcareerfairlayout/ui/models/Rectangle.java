package cf.obsessiveorange.rhcareerfairlayout.ui.models;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;

/**
 * Created by Benedict on 7/20/2015.
 */
public class Rectangle {

    private Rect rect = null;
    private Paint strokePaint = null;
    private Paint fillPaint = null;
    private boolean isTappable = false;

    public static Rectangle RectangleBuilderFromTopLeft (double startX, double startY, double width, double height, @Nullable Integer strokeColor, @Nullable Integer fillColor, boolean tappable){
        return RectangleBuilderFromTopLeft((int)startX, (int)startY, (int)width, (int)height, strokeColor, fillColor, tappable);
    }

    public static Rectangle RectangleBuilderFromTopLeft (int startX, int startY, int width, int height, @Nullable Integer strokeColor, @Nullable Integer fillColor, boolean tappable){

        Rectangle rectangle = new Rectangle();

        rectangle.rect = new Rect(startX, startY, startX + width, startY + height);
        rectangle.setIsTappable(tappable);

        if(strokeColor != null) {
            Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            strokePaint.setColor(strokeColor);
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeWidth(2);
            rectangle.strokePaint = strokePaint;
        }

        if(fillColor != null) {
            Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            fillPaint.setColor(fillColor);
            fillPaint.setStyle(Paint.Style.FILL);
            fillPaint.setStrokeWidth(2);
            rectangle.fillPaint = fillPaint;
        }

        return rectangle;
    }

    public static Rectangle RectangleBuilderFromCenter (double centerX, double centerY, double width, double height, @Nullable Integer strokeColor, @Nullable Integer fillColor, boolean tappable){
        return RectangleBuilderFromCenter((int) centerX, (int) centerY, (int) width, (int) height, strokeColor, fillColor, tappable);
    }

    public static Rectangle RectangleBuilderFromCenter (int centerX, int centerY, int width, int height, @Nullable Integer strokeColor, @Nullable Integer fillColor, boolean tappable){

        Rectangle rectangle = new Rectangle();

        rectangle.rect = new Rect(centerX - width / 2, centerY - height / 2, centerX + width / 2, centerY + height / 2);
        rectangle.setIsTappable(tappable);

        if(strokeColor != null) {
            Paint strokePaint = new Paint();
            strokePaint.setColor(strokeColor);
            strokePaint.setStrokeWidth(5);
            strokePaint.setStyle(Paint.Style.STROKE);
            rectangle.strokePaint = strokePaint;
        }

        if(fillColor != null) {
            Paint fillPaint = new Paint();
            fillPaint.setColor(fillColor);
            fillPaint.setStyle(Paint.Style.FILL);
            rectangle.fillPaint = fillPaint;
        }

        return rectangle;
    }

    public Rect getRect() {
        return rect;
    }

    public Rectangle setRect(Rect rect) {
        this.rect = rect;
        return this;
    }

    public Paint getStrokePaint() {
        return strokePaint;
    }

    public Rectangle setStrokePaint(Paint strokePaint) {
        this.strokePaint = strokePaint;
        return this;
    }

    public Paint getFillPaint() {
        return fillPaint;
    }

    public Rectangle setFillPaint(Paint fillPaint) {
        this.fillPaint = fillPaint;
        return this;
    }

    public boolean isTappable() {
        return isTappable;
    }

    public Rectangle setIsTappable(boolean isTappable) {
        this.isTappable = isTappable;
        return this;
    }

    public void draw(Canvas canvas){

        if(getFillPaint() != null) {
            canvas.drawRect(rect, fillPaint);
        }
        if(getStrokePaint() != null) {
            canvas.drawRect(rect, strokePaint);
        }
    }
}
