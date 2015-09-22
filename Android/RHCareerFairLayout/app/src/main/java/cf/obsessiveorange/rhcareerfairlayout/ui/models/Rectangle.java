package cf.obsessiveorange.rhcareerfairlayout.ui.models;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;

/**
 * Created by Benedict on 7/20/2015.
 */
public class Rectangle {

    private float left;
    private float top;
    private float right;
    private float bottom;
    private Paint strokePaint = null;
    private Paint fillPaint = null;
    private boolean isTappable = false;
    private String text;

    public static Rectangle RectangleBuilderFromTopLeft(float startX,
                                                        float startY,
                                                        float width,
                                                        float height,
                                                        @Nullable Paint strokePaint,
                                                        @Nullable Paint fillPaint,
                                                        boolean tappable,
                                                        String text) {

        Rectangle rectangle = new Rectangle();

        rectangle.left = startX;
        rectangle.top = startY;
        rectangle.right = startX + width;
        rectangle.bottom = startY + height;
        rectangle.setIsTappable(tappable);
        rectangle.text = text;
        rectangle.strokePaint = strokePaint;
        rectangle.fillPaint = fillPaint;

        return rectangle;
    }

    public static Rectangle RectangleBuilderFromCenter(float centerX,
                                                       float centerY,
                                                       float width,
                                                       float height,
                                                       @Nullable Paint strokePaint,
                                                       @Nullable Paint fillPaint,
                                                       boolean tappable,
                                                       String text) {

        Rectangle rectangle = new Rectangle();

        rectangle.left = centerX - width/2;
        rectangle.top = centerY - height/2;
        rectangle.right = centerX + width/2;
        rectangle.bottom = centerY + height/2;
        rectangle.setIsTappable(tappable);
        rectangle.text = text;
        rectangle.strokePaint = strokePaint;
        rectangle.fillPaint = fillPaint;

        return rectangle;
    }

    public float getOriginX(){
        return left;
    }
    public float getOriginY(){
        return top;
    }
    public float getRectangleWidth(){
        return right-left;
    }
    public float getRectangleHeight(){
        return bottom-top;
    }
    public float getCenterX(){
        return getOriginX() + getRectangleWidth()/2;
    }
    public float getCenterY(){
        return getOriginY() + getRectangleHeight()/2;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void draw(Canvas canvas, float fontSize) {

        if (getFillPaint() != null) {
            canvas.drawRect(left, top, right, bottom, fillPaint);
        }
        if (getStrokePaint() != null) {
            canvas.drawRect(left, top, right, bottom, strokePaint);
        }
        if (getText() != null) {

            Paint textPaint = new Paint();
            textPaint.setColor(Color.BLACK);
            textPaint.setStyle(Paint.Style.FILL);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTextSize(fontSize);

            canvas.drawText(text, getCenterX(), getCenterY() + textPaint.getTextSize()/2, textPaint);
        }
    }

    public boolean contains (float pointX, float pointY){
        return (pointX > left && pointX < right && pointY > top && pointY < bottom);
    }
}
