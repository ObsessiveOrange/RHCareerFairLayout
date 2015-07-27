package cf.obsessiveorange.rhcareerfairlayout.ui.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

import cf.obsessiveorange.rhcareerfairlayout.RHCareerFairLayout;
import cf.obsessiveorange.rhcareerfairlayout.data.DBAdapter;
import cf.obsessiveorange.rhcareerfairlayout.data.models.Term;
import cf.obsessiveorange.rhcareerfairlayout.ui.models.Rectangle;
import cf.obsessiveorange.rhcareerfairlayout.ui.models.Table;
import cf.obsessiveorange.rhcareerfairlayout.ui.models.wrappers.TableMap;

public class MapView extends SurfaceView implements SurfaceHolder.Callback {

    public enum InteractionMode {
        None,
        TouchStarted,
        Tap,
        Pan,
        Zoom,
        AtBounds
    }

    final static String TAG = "Test";
    ScaleGestureDetector mScaleDetector;
    InteractionMode mode = InteractionMode.None;
    Matrix mMatrix = new Matrix();
    float mScaleFactor = 1.f;
    double mTouchX;
    double mTouchY;
    double mTouchBackupX;
    double mTouchBackupY;
    double mTouchDownX;
    double mTouchDownY;
    double mVPSwipeSlope = 1 / Math.sqrt(3);

    double containerWidth;
    double containerHeight;

    double mapWidth;
    double mapHeight;

    Thread companySelectionChangedWatcher;

    private TableMap tableMap;

    public MapView(Context context) {
        super(context);

        // we need to get a call for onSurfaceCreated
        SurfaceHolder sh = this.getHolder();
        sh.addCallback(this);

        // for zooming (scaling) the view with two fingers
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);

        if (!this.mScaleDetector.isInProgress()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    Log.d(TAG, "Touch up event: " + mode.name());
                    if (mode == InteractionMode.TouchStarted) {
                        mode = InteractionMode.Tap;

                        int[] touchCoordinates = new int[]{(int) event.getX(), (int) event.getY()};


                        //relative distance from centerpoint of diagram
                        double fromCenterX = (event.getX() - mTouchX) / mScaleFactor;
                        double fromCenterY = (event.getY() - mTouchY) / mScaleFactor;

                        //relative point of touch, based on original drawing location.
                        double relativeTapPointX = containerWidth / 2 + fromCenterX;
                        double relativeTapPointY = containerHeight / 2 + fromCenterY;
                        for (Table table : tableMap.values()) {
                            if (table.getRectangle().isTappable() && table.getRectangle().getRect().contains((int) relativeTapPointX, (int) relativeTapPointY)) {
                                Log.d(TAG, "Tapped Rectangle");
                                Toast.makeText(getContext(), "Tapped this rectangle!", Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                    }

                    if (mode == InteractionMode.None || mode == InteractionMode.Zoom) {
                        return false;
                    }

                    // similar to ScaleListener.onScaleEnd (as long as we don't
                    // handle indices of touch events)
                    mode = InteractionMode.None;
                    break;
                case MotionEvent.ACTION_DOWN:
                    Log.d(TAG, "Touch down event: " + mode.name());

                    mTouchDownX = event.getX();
                    mTouchDownY = event.getY();
                    mTouchBackupX = mTouchX;
                    mTouchBackupY = mTouchY;

                    // pan/move started
                    mode = InteractionMode.TouchStarted;
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.d(TAG, "Touch move event: " + mode.name());
                    Log.d(TAG, "X: " + event.getX() + ", Y: " + event.getY());

                    final double diffX = event.getX() - mTouchDownX;
                    final double diffY = event.getY() - mTouchDownY;

                    // If
                    if (diffX > 10 && !canMoveRight() && mode != InteractionMode.Pan &&
                            Math.abs(diffY) / Math.abs(diffX) < mVPSwipeSlope) {
                        mode = InteractionMode.AtBounds;
                    }
                    //
                    else if (diffX < -10 && !canMoveLeft() && mode != InteractionMode.Pan &&
                            Math.abs(diffY) / Math.abs(diffX) < mVPSwipeSlope) {
                        mode = InteractionMode.AtBounds;
                    }
                    // On first movement outide certain bound, change it to pan.
                    else if (mode == InteractionMode.TouchStarted
                            && (Math.abs(diffX) > 10
                            || Math.abs(diffY) > 10)) {
                        mode = InteractionMode.Pan;
                    }
                    // make sure we don't handle the last move event when the first
                    // finger is still down and the second finger is lifted up
                    // already after a zoom/scale interaction. see
                    // ScaleListener.onScaleEnd
                    if (mode == InteractionMode.Pan) {

                        // get distance vector from where the finger touched down to
                        // current location

                        mTouchX = getNewX(mTouchBackupX, diffX);
                        mTouchY = getNewY(mTouchBackupY, diffY);

//                        mTouchY = Math.max(boxHeight / 2 / mScaleFactor, Math.min(containerHeight - boxHeight / 2 / mScaleFactor, mTouchBackupY + diffY));

//                        Log.d("TEST", mTouchX + ", " + mTouchY);

                        CalculateMatrix(true);
                    }
                    //
                    // If interaction mode is AtBounds, means that tabs should now be the target
                    // Thus, unblock parent view from intercepting touch event, and hand off to it.
                    else if (mode == InteractionMode.AtBounds) {
                        getParent().requestDisallowInterceptTouchEvent(false);
                        return false;
                    }
                    break;
            }
        }

        getParent().requestDisallowInterceptTouchEvent(true);

        return true;
    }

    private double getNewX(double referencePoint, double delta) {
        if (mapWidth * mScaleFactor >= containerWidth) {
            return Math.min(mapWidth * (mScaleFactor / 2), Math.max(containerWidth - mapWidth * (mScaleFactor / 2), referencePoint + delta));
        } else {
            return Math.max(mapWidth * (mScaleFactor / 2), Math.min(containerWidth - mapWidth * (mScaleFactor / 2), referencePoint + delta));
        }
    }

    private double getNewY(double referencePoint, double delta) {
        if (mapHeight * mScaleFactor >= containerHeight) {
            return Math.min(mapHeight * (mScaleFactor / 2), Math.max(containerHeight - mapHeight * (mScaleFactor / 2), referencePoint + delta));
        } else {
            return Math.max(mapHeight * (mScaleFactor / 2), Math.min(containerHeight - mapHeight * (mScaleFactor / 2), referencePoint + delta));
        }
    }

    @Override
    public void onDraw(Canvas canvas) {

        int saveCount = canvas.getSaveCount();
        canvas.save();
        canvas.concat(mMatrix);

        canvas.drawColor(Color.GRAY);
        for (Table table : tableMap.values()) {

            table.getRectangle().draw(canvas);
        }

        canvas.restoreToCount(saveCount);
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // otherwise onDraw(Canvas) won't be called
        this.setWillNotDraw(false);

        containerWidth = holder.getSurfaceFrame().width();
        containerHeight = holder.getSurfaceFrame().height();

        generateTableLocations();

        setFocusable(true);

        // initial center/touch point of the view (otherwise the view would jump
        // around on first pan/move touch
        mTouchX = containerWidth / 2;
        mTouchY = containerHeight / 2;

        final Activity activity = (Activity) getContext();

        companySelectionChangedWatcher = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        synchronized (RHCareerFairLayout.companySelectionChanged) {
                            RHCareerFairLayout.companySelectionChanged.wait();
                            if (!RHCareerFairLayout.companySelectionChanged.hasChanged()) {
                                continue;
                            }
                        }
                        generateTableLocations();
                        postInvalidate();
                    }
                    Log.d(RHCareerFairLayout.RH_CFL, "companySelectionChangedWatcher thread stopped.");
                    companySelectionChangedWatcher = null;
                } catch (InterruptedException e) {
                    Log.d(RHCareerFairLayout.RH_CFL, "companySelectionChangedWatcher thread stopped.");
                    companySelectionChangedWatcher = null;
                }
            }
        });
        companySelectionChangedWatcher.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        if (companySelectionChangedWatcher != null) {
            companySelectionChangedWatcher.interrupt();
        }
    }

    void CalculateMatrix(boolean invalidate) {
        float sizeX = this.getWidth() / 2;
        float sizeY = this.getHeight() / 2;

        mMatrix.reset();

        // move the view so that it's center point is located in 0,0
        mMatrix.postTranslate(-sizeX, -sizeY);

        // scale the view
        mMatrix.postScale(mScaleFactor, mScaleFactor);

        // re-move the view to it's desired location
        mMatrix.postTranslate((float) mTouchX, (float) mTouchY);

        if (invalidate)
            invalidate(); // re-draw
    }

    private class ScaleListener extends
            ScaleGestureDetector.SimpleOnScaleGestureListener {

        double mFocusStartX;
        double mFocusStartY;
        double mZoomBackupX;
        double mZoomBackupY;

        public ScaleListener() {
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {

            mode = InteractionMode.Zoom;

            mFocusStartX = detector.getFocusX();
            mFocusStartY = detector.getFocusY();
            mZoomBackupX = mTouchX;
            mZoomBackupY = mTouchY;

            return super.onScaleBegin(detector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

            mode = InteractionMode.None;

            super.onScaleEnd(detector);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            if (mode != InteractionMode.Zoom)
                return true;

            Log.d(TAG, "Touch scale event");

            // get current scale and fix its value
            float scale = detector.getScaleFactor();
            mScaleFactor *= scale;
            mScaleFactor = Math.max(1f, Math.min(mScaleFactor, 5.0f));

            // get current focal point between both fingers (changes due to
            // movement)
            float focusX = detector.getFocusX();
            float focusY = detector.getFocusY();

            // get distance vector from initial event (onScaleBegin) to current
            double diffX = focusX - mFocusStartX;
            double diffY = focusY - mFocusStartY;

            // scale the distance vector accordingly
            diffX *= scale;
            diffY *= scale;

            // set new touch position

            mTouchX = getNewX(mZoomBackupX, diffX);
            mTouchY = getNewY(mZoomBackupY, diffY);

            CalculateMatrix(true);

            return true;
        }

    }


    public boolean canMoveLeft() {
        return (mTouchX - getNewX(mTouchX, -1)) >= 1;
    }

    public boolean canMoveRight() {
        return (getNewX(mTouchX, 1) - mTouchX) >= 1;
    }

    public boolean canMoveUp() {
        return (mTouchY - getNewY(mTouchY, -1)) >= 1;
    }

    public boolean canMoveDown() {
        return (getNewY(mTouchY, 1) - mTouchY) >= 1;
    }

    public void generateTableLocations() {

        //
        // Calculate usable width
        mapWidth = containerWidth >= containerHeight * 2 ? containerHeight * 2 : containerWidth;
        mapHeight = containerWidth >= containerHeight * 2 ? containerHeight : containerWidth / 2;

        tableMap = DBAdapter.getTables();
        Term term = DBAdapter.getTerm();

        //
        //convenience assignments
        int s1 = term.getLayout_Section1();
        int s2 = term.getLayout_Section2();
        int s2Rows = term.getLayout_Section2_Rows();
        int s2PathWidth = term.getLayout_Section2_PathWidth();
        int s3 = term.getLayout_Section3();
        //
        //count number of vertical and horizontal tableMap there are
        int hrzCount = s2 + Math.min(s1, 1) + Math.min(s3, 1);
        int vrtCount = Math.max(s1, s3);
        //
        //calculate width and height of tableMap based on width of the canvas
        double unitX = mapWidth / 100;
        //10 + (number of sections - 1) * 5 % of space allocated to (vertical) walkways
        double tableWidth = unitX * (90 - Math.min(s1, 1) * 5 - Math.min(s3, 1) * 5) / hrzCount;
        double unitY = mapHeight / 100;
        //30% of space allocated to registration and rest area.
        double tableHeight = unitY * 70 / vrtCount;
        //
        //
        long id = 1;
        double offsetX = (containerWidth - mapWidth) / 2;
        double offsetY = (containerHeight - mapHeight) / 2;
        //
        // section 1
        offsetX += 5 * unitX;
        if (s1 > 0) {
            for (int i = 0; i < s1; ) {
                Table table = tableMap.get(id);
                Rectangle rectangle = Rectangle.RectangleBuilderFromTopLeft(
                        offsetX,
                        offsetY + 5 * unitY + i * tableHeight,
                        tableWidth,
                        tableHeight * table.getSize(),
                        Color.BLACK,
                        table.isSelected() ? Color.GREEN : null,
                        table.isSelected()
                );
                id++;
                i += table.getSize();
            }
            offsetX += tableWidth + 5 * unitX;
        }
        //
        // section 2
        double pathWidth = (unitY * 70 - s2Rows * tableHeight) / (s2Rows / 2);
        //
        //rows
        if (s2Rows > 0 && s2 > 0) {
            for (int i = 0; i < s2Rows; i++) {
                //
                //Outer rows have no walkway.
                //Also use this if there is no path inbetween the left and right.
                if (s2PathWidth == 0 || i == 0 || i == s2Rows - 1) {
                    for (int j = 0; j < s2; ) {
                        Table table = tableMap.get(id);
                        Rectangle rectangle = Rectangle.RectangleBuilderFromTopLeft(
                                offsetX + (j * tableWidth),
                                offsetY + 5 * unitY + Math.floor((i + 1) / 2) * pathWidth + i * tableHeight,
                                tableWidth * table.getSize(),
                                tableHeight,
                                Color.BLACK,
                                table.isSelected() ? Color.GREEN : null,
                                table.isSelected()
                        );
                        table.setRectangle(rectangle);

                        id++;
                        j += table.getSize();
                    }
                }
                //
                //inner rows need to have walkway halfway through
                else {
                    int leftTables = ((s2 - s2PathWidth) / 2);
                    int rightTables = s2 - s2PathWidth - leftTables;
                    for (int j = 0; j < leftTables; ) {
                        Table table = tableMap.get(id);
                        Rectangle rectangle = Rectangle.RectangleBuilderFromTopLeft(
                                offsetX + (j * tableWidth),
                                offsetY + 5 * unitY + Math.floor((i + 1) / 2) * pathWidth + i * tableHeight,
                                tableWidth * table.getSize(),
                                tableHeight,
                                Color.BLACK,
                                table.isSelected() ? Color.GREEN : null,
                                table.isSelected()
                        );
                        table.setRectangle(rectangle);

                        id++;
                        j += table.getSize();
                    }
                    for (int j = 0; j < rightTables; ) {
                        Table table = tableMap.get(id);
                        Rectangle rectangle = Rectangle.RectangleBuilderFromTopLeft(
                                offsetX + ((leftTables + s2PathWidth + j) * tableWidth),
                                offsetY + 5 * unitY + Math.floor((i + 1) / 2) * pathWidth + i * tableHeight,
                                tableWidth * table.getSize(),
                                tableHeight,
                                Color.BLACK,
                                table.isSelected() ? Color.GREEN : null,
                                table.isSelected()
                        );
                        table.setRectangle(rectangle);

                        id++;
                        j += table.getSize();
                    }
                }
            }
            offsetX += s2 * tableWidth + 5 * unitX;
        }
        //
        // section 3
        if (s3 > 0) {
            for (int i = 0; i < s3; ) {
                Table table = tableMap.get(id);
                Rectangle rectangle = Rectangle.RectangleBuilderFromTopLeft(
                        offsetX,
                        offsetY + 5 * unitY + i * tableHeight,
                        tableWidth,
                        tableHeight * table.getSize(),
                        Color.BLACK,
                        table.isSelected() ? Color.GREEN : null,
                        table.isSelected()
                );
                table.setRectangle(rectangle);

                id++;
                i += table.getSize();
            }
        }
        offsetX += tableWidth + 5 * unitX;

        //
        // Trim tableMap array, make sure there are no more beyond what layout specifies
        Set<Long> tableIds = new HashSet<Long>(tableMap.keySet());
        for (long tableId : tableIds) {
            if (tableId >= id) {
                tableMap.remove(tableId);
            }
        }
    }
}