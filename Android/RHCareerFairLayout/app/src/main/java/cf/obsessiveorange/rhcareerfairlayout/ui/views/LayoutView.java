package cf.obsessiveorange.rhcareerfairlayout.ui.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.HashSet;
import java.util.Set;

import cf.obsessiveorange.rhcareerfairlayout.RHCareerFairLayout;
import cf.obsessiveorange.rhcareerfairlayout.data.managers.DBManager;
import cf.obsessiveorange.rhcareerfairlayout.data.models.Company;
import cf.obsessiveorange.rhcareerfairlayout.data.models.TableMapping;
import cf.obsessiveorange.rhcareerfairlayout.data.models.Term;
import cf.obsessiveorange.rhcareerfairlayout.ui.activities.DetailActivity;
import cf.obsessiveorange.rhcareerfairlayout.ui.activities.MainActivity;
import cf.obsessiveorange.rhcareerfairlayout.ui.fragments.VPParentFragment;
import cf.obsessiveorange.rhcareerfairlayout.ui.models.Rectangle;
import cf.obsessiveorange.rhcareerfairlayout.ui.models.Table;
import cf.obsessiveorange.rhcareerfairlayout.ui.models.wrappers.TableMap;

public class LayoutView extends SurfaceView implements SurfaceHolder.Callback {

    public enum InteractionMode {
        None,
        TouchStarted,
        Tap,
        Pan,
        Zoom,
        AtBounds
    }

    ScaleGestureDetector mScaleDetector;
    InteractionMode mode = InteractionMode.None;
    Matrix mMatrix = new Matrix();
    float mScaleFactor = 1.f;
    float mTouchX;
    float mTouchY;
    float mTouchBackupX;
    float mTouchBackupY;
    float mTouchDownX;
    float mTouchDownY;
    double mVPSwipeSlope = 1 / Math.sqrt(3);

    float containerWidth;
    float containerHeight;

    Rectangle mapAreaRect;
    Rectangle restAreaRect;
    Rectangle registrationAreaRect;
    float mapWidth;
    float mapHeight;
    private float mFontSize;

    Thread companySelectionChangedWatcher;

    private TableMap mTableMap;
    private final Object mTableMapSynchronizationObject = new Object();

    private volatile Long mSelectedTable = null;
    private Paint fillPaintHighlightTables = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Handler mHighlightTablesHandler = new Handler();

    private Runnable updateGUI = new Runnable() {
        @Override
        public void run() {
            containerWidth = getHolder().getSurfaceFrame().width();
            containerHeight = getHolder().getSurfaceFrame().height();

            generateTableLocations(new Runnable() {
                @Override
                public void run() {

                    CalculateMatrix(true);
                }
            });
        }
    };

    public LayoutView(Context context) {
        super(context);

        // we need to get a call for onSurfaceCreated
        SurfaceHolder sh = this.getHolder();
        sh.addCallback(this);

        // for zooming (scaling) the view with two fingers
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        mScaleDetector.onTouchEvent(event);

        if (!this.mScaleDetector.isInProgress()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    Log.d(RHCareerFairLayout.RH_CFL, "Touch up event: " + mode.name());
                    if (mode == InteractionMode.TouchStarted) {
                        mode = InteractionMode.Tap;

                        //relative distance from centerpoint of diagram
                        float fromCenterX = (event.getX() - mTouchX) / mScaleFactor;
                        float fromCenterY = (event.getY() - mTouchY) / mScaleFactor;

                        //relative point of touch, based on original drawing location.
                        float relativeTapPointX = containerWidth / 2 + fromCenterX;
                        float relativeTapPointY = containerHeight / 2 + fromCenterY;

                        synchronized (mTableMapSynchronizationObject) {
                            for (Table table : mTableMap.values()) {
                                if (table.getRectangle().isTappable() && table.getRectangle().contains(relativeTapPointX, relativeTapPointY)) {

                                    Company company = DBManager.getCompanyForTableMapping(table.getId());

                                    if (company == null) {
                                        throw new IllegalArgumentException("Invalid tableId provided - no company found at that index");
                                    }

                                    Log.d(RHCareerFairLayout.RH_CFL, "Tapped table:" + table.getId() + ", companyId:" + company.getId());

                                    Intent detailIntent = new Intent(getContext(), DetailActivity.class);
                                    detailIntent.putExtra(RHCareerFairLayout.INTENT_KEY_SELECTED_COMPANY, company.getId());
                                    ((Activity) getContext()).startActivityForResult(detailIntent, RHCareerFairLayout.REQUEST_CODE_FIND_ON_MAP);

                                    mHighlightTablesHandler.removeCallbacksAndMessages(null);
                                    mSelectedTable = null;
                                    //updateGUI.run();

                                    break;
                                }
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
                    Log.d(RHCareerFairLayout.RH_CFL, "Touch down event: " + mode.name());

                    mTouchDownX = event.getX();
                    mTouchDownY = event.getY();
                    mTouchBackupX = mTouchX;
                    mTouchBackupY = mTouchY;

                    // pan/move started
                    mode = InteractionMode.TouchStarted;
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.d(RHCareerFairLayout.RH_CFL, "Map panned: " + mode.name());

                    final float diffX = event.getX() - mTouchDownX;
                    final float diffY = event.getY() - mTouchDownY;

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

                        mTouchX = mTouchBackupX + diffX;
                        mTouchY = mTouchBackupY + diffY;

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

    private VPParentFragment getParentFragment() {
        return (VPParentFragment) ((MainActivity) getContext()).getSupportFragmentManager().findFragmentByTag(RHCareerFairLayout.PARENT_FRAGMENT_TAG);
    }

    private float getNewX(float referencePoint, float delta) {

        if (mapWidth * mScaleFactor >= containerWidth) {
            return Math.min(mapWidth * (mScaleFactor / 2), Math.max(containerWidth - mapWidth * (mScaleFactor / 2), referencePoint + delta));
        } else {
            return Math.max(mapWidth * (mScaleFactor / 2), Math.min(containerWidth - mapWidth * (mScaleFactor / 2), referencePoint + delta));
        }
    }

    private float getNewY(float referencePoint, float delta) {

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

        canvas.drawColor(Color.WHITE);

        synchronized (mTableMapSynchronizationObject) {

            mapAreaRect.draw(canvas, mFontSize);
            restAreaRect.draw(canvas, mFontSize);
            registrationAreaRect.draw(canvas, mFontSize);

            for (Table table : mTableMap.values()) {

                if (table.getId().equals(mSelectedTable)) {
                    table.getRectangle().setFillPaint(fillPaintHighlightTables);
                    table.getRectangle().draw(canvas, mFontSize);
                } else {
                    table.getRectangle().draw(canvas, mFontSize);
                }
            }
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

        fillPaintHighlightTables.setColor(Color.YELLOW);
        fillPaintHighlightTables.setStyle(Paint.Style.FILL);

        generateTableLocations(new Runnable() {
            @Override
            public void run() {
                post(new Runnable() {
                    @Override
                    public void run() {
                        setFocusable(true);
                    }
                });

                SharedPreferences prefs = getContext().getSharedPreferences(RHCareerFairLayout.RH_CFL, Context.MODE_PRIVATE);
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mTouchX = prefs.getFloat(RHCareerFairLayout.PREF_KEY_MAP_VIEW_FOCUS_X_LAND, containerWidth / 2);
                    mTouchY = prefs.getFloat(RHCareerFairLayout.PREF_KEY_MAP_VIEW_FOCUS_Y_LAND, containerHeight / 2);
                    mScaleFactor = prefs.getFloat(RHCareerFairLayout.PREF_KEY_MAP_VIEW_SCALE_LAND,
                            Math.min(
                                    Math.max(containerWidth / mapWidth, containerHeight / mapHeight),
                                    5.0f
                            ));
                } else {
                    mTouchX = prefs.getFloat(RHCareerFairLayout.PREF_KEY_MAP_VIEW_FOCUS_X_PORT, containerWidth / 2);
                    mTouchY = prefs.getFloat(RHCareerFairLayout.PREF_KEY_MAP_VIEW_FOCUS_Y_PORT, containerHeight / 2);
                    mScaleFactor = prefs.getFloat(RHCareerFairLayout.PREF_KEY_MAP_VIEW_SCALE_PORT,
                            Math.min(
                                    Math.max(containerWidth / mapWidth, containerHeight / mapHeight),
                                    5.0f
                            ));
                }

                CalculateMatrix(true);
            }
        });


        // initial center/touch point of the view (otherwise the view would jump
        // around on first pan/move touch


        companySelectionChangedWatcher = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        synchronized (RHCareerFairLayout.refreshMapNotifier) {
                            if (!RHCareerFairLayout.refreshMapNotifier.hasChanged()) {
                                RHCareerFairLayout.refreshMapNotifier.wait();
                                if (!RHCareerFairLayout.refreshMapNotifier.hasChanged()) {
                                    continue;
                                }
                            }
                        }
                        generateTableLocations(new Runnable() {
                            @Override
                            public void run() {
                                postInvalidate();
                            }
                        });
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

        // make sure x, y are within bounds
        mTouchX = getNewX(mTouchX, 0);
        mTouchY = getNewY(mTouchY, 0);

        // re-move the view to it's desired location
        mMatrix.postTranslate(mTouchX, mTouchY);

        if (invalidate) {
            postInvalidate(); // re-draw
        }


        SharedPreferences prefs = getContext().getSharedPreferences(RHCareerFairLayout.RH_CFL, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            editor.putFloat(RHCareerFairLayout.PREF_KEY_MAP_VIEW_FOCUS_X_LAND, mTouchX);
            editor.putFloat(RHCareerFairLayout.PREF_KEY_MAP_VIEW_FOCUS_Y_LAND, mTouchY);
            editor.putFloat(RHCareerFairLayout.PREF_KEY_MAP_VIEW_SCALE_LAND, mScaleFactor);
        } else {
            editor.putFloat(RHCareerFairLayout.PREF_KEY_MAP_VIEW_FOCUS_X_PORT, mTouchX);
            editor.putFloat(RHCareerFairLayout.PREF_KEY_MAP_VIEW_FOCUS_Y_PORT, mTouchY);
            editor.putFloat(RHCareerFairLayout.PREF_KEY_MAP_VIEW_SCALE_PORT, mScaleFactor);
        }
        editor.apply();
    }

    private class ScaleListener extends
            ScaleGestureDetector.SimpleOnScaleGestureListener {

        float mFocusStartX;
        float mFocusStartY;
        float mZoomBackupX;
        float mZoomBackupY;

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

            if (mScaleFactor == 1) {
                getParentFragment().showToolbar(updateGUI);
            } else {
                getParentFragment().hideToolbar(updateGUI);
            }


            super.onScaleEnd(detector);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            if (mode != InteractionMode.Zoom)
                return true;

            // get current scale and fix its value
            float scale = detector.getScaleFactor();
            mScaleFactor *= scale;
            mScaleFactor = Math.max(1f, Math.min(mScaleFactor, 5.0f));

            // get current focal point between both fingers (changes due to
            // movement)
            float focusX = detector.getFocusX();
            float focusY = detector.getFocusY();

            // get distance vector from initial event (onScaleBegin) to current
            float diffX = focusX - mFocusStartX;
            float diffY = focusY - mFocusStartY;

            // scale the distance vector accordingly
            diffX *= scale;
            diffY *= scale;

            // set new touch position

            mTouchX = mZoomBackupX + diffX;
            mTouchY = mZoomBackupY + diffY;

            CalculateMatrix(true);

            Log.d(RHCareerFairLayout.RH_CFL, "Map scaled to: " + mScaleFactor);

            return true;
        }

    }


    public boolean canMoveLeft() {
        return (mTouchX - getNewX(mTouchX, -1)) >= 1;
    }

    public boolean canMoveRight() {
        return (getNewX(mTouchX, 1) - mTouchX) >= 1;
    }

    public void generateTableLocations(final Runnable... callbacks) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                //
                // Calculate usable width
                mapWidth = containerWidth >= containerHeight * 2 ? containerHeight * 2 : containerWidth;
                mapHeight = containerWidth >= containerHeight * 2 ? containerHeight : containerWidth / 2;


                Paint strokePaintTables = new Paint(Paint.ANTI_ALIAS_FLAG);
                strokePaintTables.setColor(Color.BLACK);
                strokePaintTables.setStyle(Paint.Style.STROKE);
                strokePaintTables.setStrokeWidth(0);

                Paint fillPaintTables = new Paint(Paint.ANTI_ALIAS_FLAG);
                fillPaintTables.setColor(Color.GREEN);
                fillPaintTables.setStyle(Paint.Style.FILL);

                Paint fillPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
                fillPaintText.setColor(Color.BLACK);
                fillPaintText.setStyle(Paint.Style.FILL);

                synchronized (mTableMapSynchronizationObject) {
                    mTableMap = DBManager.getTables();
                    Term term = DBManager.getTerm();

                    if (term == null) {
                        Log.d(RHCareerFairLayout.RH_CFL, "Term null, data invalid.");
                        return;
                    }

                    //convenience assignments
                    int s1 = term.getLayout_Section1();
                    int s2 = term.getLayout_Section2();
                    int s2Rows = term.getLayout_Section2_Rows();
                    int s2PathWidth = term.getLayout_Section2_PathWidth();
                    int s3 = term.getLayout_Section3();

                    Integer numTables = s1 + s2 * s2Rows - (s2 - 2) * s2PathWidth + s3;

                    //count number of vertical and horizontal mTableMap there are
                    int hrzCount = s2 + Math.min(s1, 1) + Math.min(s3, 1);
                    int vrtCount = Math.max(s1, s3);

                    //calculate width and height of mTableMap based on width of the canvas
                    float unitX = mapWidth / 100;

                    //10 + (number of sections - 1) * 5 % of space allocated to (vertical) walkways
                    float tableWidth = unitX * (90 - Math.min(s1, 1) * 5 - Math.min(s3, 1) * 5) / hrzCount;
                    float unitY = mapHeight / 100;

                    //30% of space allocated to registration and rest area.
                    float tableHeight = unitY * 70 / vrtCount;
                    mFontSize = tableHeight * 2 / 3;
                    fillPaintText.setTextSize(mFontSize);

                    float adjustedFontSize = fillPaintText.measureText(numTables.toString(), 0, numTables.toString().length());
                    if(adjustedFontSize > tableWidth){
                        mFontSize = mFontSize * (tableWidth / adjustedFontSize) * 0.75f;
                    }

                    //
                    long id = 1;
                    float offsetX = (containerWidth - mapWidth) / 2;
                    float offsetY = (containerHeight - mapHeight) / 2;


                    // static tables.
                    mapAreaRect = Rectangle.RectangleBuilderFromCenter(
                            containerWidth / 2,
                            containerHeight / 2,
                            mapWidth,
                            mapHeight,
                            strokePaintTables,
                            null,
                            false,
                            null
                    );
                    restAreaRect = Rectangle.RectangleBuilderFromTopLeft(
                            offsetX + 40 * unitX,
                            offsetY + 80 * unitY,
                            45 * unitX,
                            15 * unitY,
                            strokePaintTables,
                            null,
                            false,
                            "Rest Area"
                    );
                    registrationAreaRect = Rectangle.RectangleBuilderFromTopLeft(
                            offsetX + 5 * unitX,
                            offsetY + 80 * unitY,
                            30 * unitX,
                            15 * unitY,
                            strokePaintTables,
                            null,
                            false,
                            "Registration"
                    );


                    //
                    // section 1
                    offsetX += 5 * unitX;
                    if (s1 > 0) {
                        for (int i = 0; i < s1; ) {
                            Table table = mTableMap.get(id);
                            Rectangle rectangle = Rectangle.RectangleBuilderFromTopLeft(
                                    offsetX,
                                    offsetY + 5 * unitY + i * tableHeight,
                                    tableWidth,
                                    tableHeight * table.getSize(),
                                    strokePaintTables,
                                    table.isSelected() ? fillPaintTables : null,
                                    table.isSelected(),
                                    table.getId().toString()
                            );
                            table.setRectangle(rectangle);

                            id++;
                            i += table.getSize();
                        }
                        offsetX += tableWidth + 5 * unitX;
                    }
                    //
                    // section 2
                    float pathWidth = (unitY * 70 - s2Rows * tableHeight) / (s2Rows / 2);
                    //
                    //rows
                    if (s2Rows > 0 && s2 > 0) {
                        for (int i = 0; i < s2Rows; i++) {
                            //
                            //Outer rows have no walkway.
                            //Also use this if there is no path inbetween the left and right.
                            if (s2PathWidth == 0 || i == 0 || i == s2Rows - 1) {
                                for (int j = 0; j < s2; ) {
                                    Table table = mTableMap.get(id);
                                    Rectangle rectangle = Rectangle.RectangleBuilderFromTopLeft(
                                            offsetX + (j * tableWidth),
                                            offsetY + 5 * unitY + (float) Math.floor((i + 1) / 2) * pathWidth + i * tableHeight,
                                            tableWidth * table.getSize(),
                                            tableHeight,
                                            strokePaintTables,
                                            table.isSelected() ? fillPaintTables : null,
                                            table.isSelected(),
                                            table.getId().toString()
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
                                    Table table = mTableMap.get(id);
                                    Rectangle rectangle = Rectangle.RectangleBuilderFromTopLeft(
                                            offsetX + (j * tableWidth),
                                            offsetY + 5 * unitY + (float) Math.floor((i + 1) / 2) * pathWidth + i * tableHeight,
                                            tableWidth * table.getSize(),
                                            tableHeight,
                                            strokePaintTables,
                                            table.isSelected() ? fillPaintTables : null,
                                            table.isSelected(),
                                            table.getId().toString()
                                    );
                                    table.setRectangle(rectangle);

                                    id++;
                                    j += table.getSize();
                                }
                                for (int j = 0; j < rightTables; ) {
                                    Table table = mTableMap.get(id);
                                    Rectangle rectangle = Rectangle.RectangleBuilderFromTopLeft(
                                            offsetX + ((leftTables + s2PathWidth + j) * tableWidth),
                                            offsetY + 5 * unitY + (float) Math.floor((i + 1) / 2) * pathWidth + i * tableHeight,
                                            tableWidth * table.getSize(),
                                            tableHeight,
                                            strokePaintTables,
                                            table.isSelected() ? fillPaintTables : null,
                                            table.isSelected(),
                                            table.getId().toString()
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
                            Table table = mTableMap.get(id);
                            Rectangle rectangle = Rectangle.RectangleBuilderFromTopLeft(
                                    offsetX,
                                    offsetY + 5 * unitY + i * tableHeight,
                                    tableWidth,
                                    tableHeight * table.getSize(),
                                    strokePaintTables,
                                    table.isSelected() ? fillPaintTables : null,
                                    table.isSelected(),

                                    table.getId().toString()
                            );
                            table.setRectangle(rectangle);

                            id++;
                            i += table.getSize();
                        }
                    }

                    //
                    // Trim mTableMap array, make sure there are no more beyond what layout specifies
                    Set<Long> tableIds = new HashSet<>(mTableMap.keySet());
                    for (long tableId : tableIds) {
                        if (tableId >= id) {
                            mTableMap.remove(tableId);
                        }
                    }
                }
                for (Runnable callback : callbacks) {
                    callback.run();
                }
            }
        });

        t.start();
    }

    public void flashTable(long tableId) {

        final TableMapping tableMapping = DBManager.getTableMapping(tableId);

        if (tableMapping == null) {
            throw new IllegalArgumentException("No table mapping found for tableId: " + tableId);
        }

        Runnable focusAndFlash = new Runnable() {
            @Override
            public void run() {
                Table table;
                synchronized (mTableMapSynchronizationObject) {
                    table = mTableMap.get(tableMapping.getId());
                }

                mTouchX = getHolder().getSurfaceFrame().width() / 2 +
                        (getHolder().getSurfaceFrame().width() / 2
                                - table.getRectangle().getCenterX()
                        ) * mScaleFactor;
                mTouchY = getHolder().getSurfaceFrame().height() / 2 +
                        (getHolder().getSurfaceFrame().height() / 2
                                - table.getRectangle().getCenterY()
                        ) * mScaleFactor;

                CalculateMatrix(true);

                for (int i = 0; i < 3; i++) {
                    mHighlightTablesHandler.postDelayed(new Runnable() {
                        public void run() {
                            Log.d(RHCareerFairLayout.RH_CFL, "Highlight off - table " + tableMapping.getId());
                            mSelectedTable = tableMapping.getId();

                            updateGUI.run();
                        }
                    }, i * 2000);
                }
                for (int i = 0; i < 3; i++) {
                    mHighlightTablesHandler.postDelayed(new Runnable() {
                        public void run() {
                            Log.d(RHCareerFairLayout.RH_CFL, "Highlight on - table " + tableMapping.getId());
                            mSelectedTable = null;

                            updateGUI.run();
                        }
                    }, 1000 + i * 2000);
                }
            }
        };

        if (mTableMap == null) {
            generateTableLocations(focusAndFlash);
        } else {
            focusAndFlash.run();
        }
    }

    public void flashCompany(long companyId) {

        final TableMapping tableMapping = DBManager.getTableMappingForCompany(companyId);

        if (tableMapping == null) {
            throw new IllegalArgumentException("No table mapping found for companyId: " + companyId);
        }

        flashTable(tableMapping.getId());

    }

    public float getScaleFactor() {
        return mScaleFactor;
    }
}