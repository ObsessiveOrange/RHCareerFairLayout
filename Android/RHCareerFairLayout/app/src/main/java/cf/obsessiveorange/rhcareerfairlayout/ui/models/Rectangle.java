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

    private Rect rect = null;
    private Paint strokePaint = null;
    private Paint fillPaint = null;
    private boolean isTappable = false;
    private String text;

    public static Rectangle RectangleBuilderFromTopLeft(double startX,
                                                        double startY,
                                                        double width,
                                                        double height,
                                                        @Nullable Paint strokePaint,
                                                        @Nullable Paint fillPaint,
                                                        boolean tappable,
                                                        String text) {
        return RectangleBuilderFromTopLeft((int) startX,
                (int) startY,
                (int) width,
                (int) height,
                strokePaint,
                fillPaint,
                tappable,
                text);
    }

    public static Rectangle RectangleBuilderFromTopLeft(int startX,
                                                        int startY,
                                                        int width,
                                                        int height,
                                                        @Nullable Paint strokePaint,
                                                        @Nullable Paint fillPaint,
                                                        boolean tappable,
                                                        String text) {

        Rectangle rectangle = new Rectangle();

        rectangle.rect = new Rect(startX, startY, startX + width, startY + height);
        rectangle.setIsTappable(tappable);
        rectangle.text = text;
        rectangle.strokePaint = strokePaint;
        rectangle.fillPaint = fillPaint;

        return rectangle;
    }

    public static Rectangle RectangleBuilderFromCenter(double centerX,
                                                       double centerY,
                                                       double width,
                                                       double height,
                                                       @Nullable Paint strokePaint,
                                                       @Nullable Paint fillPaint,
                                                       boolean tappable,
                                                       String text) {
        return RectangleBuilderFromCenter((int) centerX,
                (int) centerY,
                (int) width,
                (int) height,
                strokePaint,
                fillPaint,
                tappable,
                text);
    }

    public static Rectangle RectangleBuilderFromCenter(int centerX,
                                                       int centerY,
                                                       int width,
                                                       int height,
                                                       @Nullable Paint strokePaint,
                                                       @Nullable Paint fillPaint,
                                                       boolean tappable,
                                                       String text) {

        Rectangle rectangle = new Rectangle();

        rectangle.rect = new Rect(centerX - width / 2, centerY - height / 2, centerX + width / 2, centerY + height / 2);
        rectangle.setIsTappable(tappable);
        rectangle.text = text;
        rectangle.strokePaint = strokePaint;
        rectangle.fillPaint = fillPaint;

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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void draw(Canvas canvas, float fontSize) {

        if (getFillPaint() != null) {
            canvas.drawRect(rect, fillPaint);
        }
        if (getStrokePaint() != null) {
            canvas.drawRect(rect, strokePaint);
        }
        if (getText() != null) {

            Paint textPaint = new Paint();
            textPaint.setColor(Color.BLACK);
            textPaint.setStyle(Paint.Style.FILL);
            textPaint.setTextAlign(Paint.Align.CENTER);

            float testTextSize = fontSize;
            textPaint.setTextSize(testTextSize);

            Rect textBounds = new Rect();
            textPaint.getTextBounds(text, 0, text.length(), textBounds);

            textPaint.setTextSize(Math.min(fontSize, fontSize * rect.width() * 0.9f / textBounds.width()));
            textPaint.getTextBounds(text, 0, text.length(), textBounds);

            canvas.drawText(text, rect.exactCenterX(), rect.exactCenterY() - textBounds.exactCenterY(), textPaint);
//            canvas.drawText(text, rect.exactCenterX(), rect.exactCenterY() + rect.height() / 4, textPaint);
        }
    }
}
