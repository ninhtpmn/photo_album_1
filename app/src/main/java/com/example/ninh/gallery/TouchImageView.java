package com.example.ninh.gallery;

/**
 * Created by ninh on 06/02/2015.
 */


        import android.content.Context;
        import android.graphics.Matrix;
        import android.graphics.PointF;
        import android.graphics.RectF;
        import android.graphics.drawable.Drawable;
        import android.util.AttributeSet;
        import android.util.Log;
        import android.view.GestureDetector;
        import android.view.MotionEvent;
        import android.view.ScaleGestureDetector;
        import android.view.View;
        import android.widget.ImageView;

public class TouchImageView extends ImageView {
    Matrix matrix;
    float factor = 1;
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;

    int mode = NONE;

    // Remember some things for zooming
    PointF last = new PointF();
    PointF start = new PointF();
    float minScale = 1/3f;
    float maxScale = 3f;
    float[] m;
    int viewWidth, viewHeight;

    static final int CLICK = 3;

    float saveScale = 1f;

    protected float origWidth, origHeight;

    int oldMeasuredWidth, oldMeasuredHeight;

    ScaleGestureDetector mScaleDetector;
    GestureDetector gestureDetector;


    Context context;

    public TouchImageView(Context context) {
        super(context);
        sharedConstructing(context);
    }

    public TouchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedConstructing(context);
    }

    private void stopInterceptEvent()
    {
        getParent().requestDisallowInterceptTouchEvent(true);
    }

    private void startInterceptEvent()
    {
        getParent().requestDisallowInterceptTouchEvent(false);
    }

    private void sharedConstructing(Context context) {

        super.setClickable(true);

        this.context = context;

        gestureDetector = new GestureDetector(context, new GestureListener());
        gestureDetector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {


                if(saveScale < 1 || saveScale > 2 || saveScale == 2)
                {
                    factor = 1/saveScale;
                    saveScale = 1;
                }
                else
                {
                    factor = 2/saveScale;
                    saveScale = 2;
                }

                if(factor>1) {
                        if (saveScale == 1) {
                            matrix.postScale(factor, factor, viewWidth / 2, viewHeight / 2);
                        }

                        else
                            matrix.postScale(factor, factor, e.getX(), e.getY());
                    }
                    else
                    {

                    matrix.postScale(factor, factor);
                        Drawable drawable = getDrawable();
                        int bmWidth = drawable.getIntrinsicWidth();
                        int bmHeight = drawable.getIntrinsicHeight();

                        Log.i("FACTOR1", ""+bmWidth + ";" + bmHeight);
                        RectF drawableRect = new RectF(0, 0, bmWidth, bmHeight);
                        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
                    matrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER);
                    }

             fixTrans();

                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return false;
            }
        });

        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        matrix = new Matrix();

        m = new float[9];

        setImageMatrix(matrix);

        setScaleType(ScaleType.MATRIX);

        setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                mScaleDetector.onTouchEvent(event);
                gestureDetector.onTouchEvent(event);

                PointF curr = new PointF(event.getX(), event.getY());

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:

                        last.set(curr);

                        start.set(last);

                        mode = DRAG;

                        stopInterceptEvent();

                        break;

                    case MotionEvent.ACTION_MOVE:

                        if (mode == DRAG) {

                            float deltaX = curr.x - last.x;
                            float deltaY = curr.y - last.y;

                            float fixTransX = getFixDragTrans(deltaX, viewWidth, origWidth * saveScale);
                            float fixTransY = getFixDragTrans(deltaY, viewHeight, origHeight * saveScale);

                            matrix.postTranslate(fixTransX, fixTransY);

                            float fix = fixTrans();
                            if(saveScale <=1 || fix !=0)
                                startInterceptEvent();
                            else
                                stopInterceptEvent();

                            last.set(curr.x, curr.y);

                        }

                        break;

                    case MotionEvent.ACTION_UP:

                        mode = NONE;

                        int xDiff = (int) Math.abs(curr.x - start.x);

                        int yDiff = (int) Math.abs(curr.y - start.y);

                        if (xDiff < CLICK && yDiff < CLICK)

                            performClick();

                        startInterceptEvent();

                        break;

                    case MotionEvent.ACTION_POINTER_UP:

                        mode = NONE;

                        break;

                }

                setImageMatrix(matrix);

                invalidate();

                return true; // indicate event was handled

            }

        });
    }

    public void setZoom(float min, float max) {

        minScale = min;
        maxScale = max;

    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {


        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {

            mode = ZOOM;

            return true;

        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            float mScaleFactor = detector.getScaleFactor();

            float origScale = saveScale;

            saveScale *= mScaleFactor;

            if (saveScale > maxScale) {

                saveScale = maxScale;

                mScaleFactor = maxScale / origScale;

            } else if (saveScale < minScale) {

                saveScale = minScale;

                mScaleFactor = minScale / origScale;

            }

            if (origWidth * saveScale <= viewWidth || origHeight * saveScale <= viewHeight)

                matrix.postScale(mScaleFactor, mScaleFactor, viewWidth / 2, viewHeight / 2);

            else

                matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());

            fixTrans();

            return true;

        }

    }

    float fixTrans() {

        matrix.getValues(m);

        float transX = m[Matrix.MTRANS_X];

        float transY = m[Matrix.MTRANS_Y];

        float fixTransX = getFixTrans(transX, viewWidth, origWidth * saveScale);

        float fixTransY = getFixTrans(transY, viewHeight, origHeight * saveScale);
        Log.i("fixTrans", ""+fixTransX);

        if (fixTransX != 0 || fixTransY != 0)

            matrix.postTranslate(fixTransX, fixTransY);

        return fixTransX;

    }



    float getFixTrans(float trans, float viewSize, float contentSize) {

        float minTrans, maxTrans;

        if (contentSize <= viewSize) {

            minTrans = 0;

            maxTrans = viewSize - contentSize;

        } else {

            minTrans = viewSize - contentSize;

            maxTrans = 0;

        }

        if (trans < minTrans)

            return -trans + minTrans;

        if (trans > maxTrans)

            return -trans + maxTrans;

        return 0;

    }

    float getFixDragTrans(float delta, float viewSize, float contentSize) {

        if (contentSize <= viewSize) {

            return 0;

        }

        return delta;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        viewWidth = MeasureSpec.getSize(widthMeasureSpec);

        viewHeight = MeasureSpec.getSize(heightMeasureSpec);

        //
        // Rescales image on rotation
        //
        if (oldMeasuredHeight == viewWidth && oldMeasuredHeight == viewHeight

                || viewWidth == 0 || viewHeight == 0)

            return;

        oldMeasuredHeight = viewHeight;

        oldMeasuredWidth = viewWidth;

        if (saveScale == 1) {

            //Fit to screen.

            float scale;

            Drawable drawable = getDrawable();

            if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0)

                return;

            int bmWidth = drawable.getIntrinsicWidth();

            int bmHeight = drawable.getIntrinsicHeight();

            Log.d("bmSize", "bmWidth: " + bmWidth + " bmHeight : " + bmHeight);

            float scaleX = (float) viewWidth / (float) bmWidth;

            float scaleY = (float) viewHeight / (float) bmHeight;

            scale = Math.min(scaleX, scaleY);

            matrix.setScale(scale, scale);

            // Center the image

            float redundantYSpace = (float) viewHeight - (scale * (float) bmHeight);

            float redundantXSpace = (float) viewWidth - (scale * (float) bmWidth);

            redundantYSpace /= (float) 2;

            redundantXSpace /= (float) 2;

            matrix.postTranslate(redundantXSpace, redundantYSpace);

            origWidth = viewWidth - 2 * redundantXSpace;

            origHeight = viewHeight - 2 * redundantYSpace;

            setImageMatrix(matrix);

        }

        fixTrans();

    }

    private class GestureListener implements GestureDetector.OnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }
}