package com.huayuxun.whale.whalelinechart;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jinhui on 2016/8/31.
 */
public class WhaleLineChart extends ViewGroup {
    private Bitmap imgBitmap;                   //卖座小人图片
    private boolean isRectFAnimFinished = false;//矩形动画是否完成
    private float rectFAnimatorValue = 0;       //矩形动画过程中变化的值
    private ValueAnimator rectFValueAnimator;   //矩形动画
//    private ValueAnimator ValueAnimator;      //折线动画

    private Path linePath;                      //曲线的路径
    private float lastLineAnimatorValue = 0;    //上一次折线动画过程中变化的值
    private float lineAnimatorValue = 0;    //折线动画过程中变化的值
    private ValueAnimator lineValueAnimator;    //折线动画

    private RectF rectF;                        //矩形
    private float rectWidth;                    //矩形宽度
    private List<Float> rectHeightList;         //每一列矩形高度
    private float distance;                     //设置的间隔
    private int stage = 0;                      //设置变化的阶段，总共有rectHeightList.size个阶段

    //距离文本的间隔
    private float textDistance = 20;
    private float textSize;                     //文本的字体大小
    private List<String> textList;              //每一列文本的内容
    private List<String> textValueList;         //每一列文本数值的内容


    private float pivotX;                       //屏幕的中心X
    private float pivotY;                       //屏幕的中心Y
    private Paint rectPaint;                    //用于绘制矩形的画笔
    private Paint linePaint;                    //用于绘制折线的画笔
    private Paint textPaint;                    //用于绘制文字的画笔
    private Paint canvasPaint;                  //全局绘制的画笔

    private Rect imgRect;                     //图片的大小
    private Rect imgPositionRectF;             //图片在屏幕的位置


    private Canvas lineChartCanvas;             //绑定bitmap的画布
    private Bitmap canvasBitmap;                //用一个bitmap保存画布
    private Bitmap saveCanvasBitmap;            //保存上一次bitmap状态的画布
    private DisplayMetrics dm = getResources().getDisplayMetrics();

    private ImageView imageView;                //小人图片
    {
        textSize = 35f;
        rectWidth = 60f;
        distance = 100f;
        rectHeightList = new ArrayList<Float>() {
            {
                add(160f);
                add(240f);
                add(320f);
                add(400f);
                add(480f);
            }
        };
        textList = new ArrayList<String>() {
            {
                add("V1");
                add("V2");
                add("V3");
                add("V4");
                add("V5");
            }
        };

        textValueList = new ArrayList<String>() {
            {
                add("100分");
                add("500分");
                add("1000分");
                add("2000分");
                add("5000分");
            }
        };
    }

    public ValueAnimator getRectFValueAnimator() {
        return rectFValueAnimator;
    }

    public ValueAnimator getLineValueAnimator() {
        return lineValueAnimator;
    }

    public WhaleLineChart(Context context) {
        this(context, null);
    }

    public WhaleLineChart(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WhaleLineChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        imageView = new ImageView(context);
        addView(imageView);
        setWillNotDraw(false);
        initPaint();
        initLineAnimation();
        initImgAnimation();
//        initRectFAnimation(stage);
//        TypedArray typedArray = context.obtainStyledAttributes(attrs , R.styleable)
    }

    private void initImgAnimation() {
//        ObjectAnimator objectAnimator = ObjectAnimator.ofObject()
    }

    public WhaleLineChart(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec,heightMeasureSpec);
        setMeasuredDimension(getLineChartWidth(widthMeasureSpec), getLineChartHeight(heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        pivotX = getWidth()  / 2;
        pivotY = getHeight() / 2;
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        lineChartCanvas = new Canvas(canvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //将画布的中心移到屏幕的中心
        lineChartCanvas.save();
        lineChartCanvas.translate(0, pivotY * 2);
        if (!isRectFAnimFinished) {
            for (int i = 0; i < rectHeightList.size(); i++, stage++) {
                drawChartRect(canvas, rectWidth, rectHeightList.get(i), distance);
                drawChartText(canvas, rectWidth, textList.get(i), distance);
            }
            isRectFAnimFinished = true;
        } else {
            drawChartLine(canvas, (int) (rectHeightList.size() * (distance + rectWidth) - distance), lineAnimatorValue);
        }
        lineChartCanvas.restore();
}


    private void initPaint() {
        //初始化矩形画笔
        rectPaint = new Paint();
        rectPaint.setColor(Color.YELLOW);
        rectPaint.setStrokeCap(Paint.Cap.ROUND);
        rectPaint.setStrokeWidth(3);
        rectPaint.setAntiAlias(true);
        rectPaint.setAlpha(80);
        rectPaint.setStyle(Paint.Style.FILL);

        //初始化折线画笔
        linePaint = new Paint();
        linePaint.setColor(Color.CYAN);
        linePaint.setStrokeWidth(10);
        linePaint.setAntiAlias(true);
        linePaint.setAlpha(50);

        //初始化文本内容画笔
        textPaint = new Paint();
        textPaint.setStrokeWidth(3);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.argb(100, 182, 181, 182));
        textPaint.setTextSize(textSize);
        textPaint.setAntiAlias(true);
        linePath = new Path();

        imgBitmap = ((BitmapDrawable) getResources().getDrawable(R.mipmap.img, null)).getBitmap();
    }

    private void initRectFAnimation(int i) {
//        if (rectFValueAnimator != null && rectFValueAnimator.isRunning()) {
//            rectFValueAnimator.cancel();
//            rectFValueAnimator.start();
//        } else {
        rectFValueAnimator = rectFValueAnimator.ofFloat(0, rectHeightList.get(i)).setDuration(500);
        rectFValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        rectFValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                rectFAnimatorValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        });

        rectFValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (stage == rectHeightList.size() - 1) {
                    isRectFAnimFinished = true;
                    initLineAnimation();
                    return;
                } else {
                    rectFAnimatorValue = 0;
                    Log.i("ValueAnimator.isRunning", "" + rectFValueAnimator.isRunning());
                    initRectFAnimation(++stage);
                }
            }
        });
        //开始动画
        rectFValueAnimator.start();
    }

    //    }


    //曲线上升的动画
    private void initLineAnimation() {
        lineValueAnimator = lineValueAnimator.ofFloat(0, 1).setDuration(2000);
        lineValueAnimator.setInterpolator(new AccelerateInterpolator());
        lineValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                lineAnimatorValue = (float) animation.getAnimatedValue();
                Log.e("lineAnimatorValue:", "" + lineAnimatorValue);
                invalidate();
            }
        });
        lineValueAnimator.start();
    }

    public int getLineChartWidth(int widthMeasureSpec) {
        int width = 0;
        int specSize = MeasureSpec.getSize(widthMeasureSpec);
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            width = specSize;
        } else {
            width = (int) (rectHeightList.size() * rectWidth + (rectHeightList.size() + 1) * distance);
            if (specMode == MeasureSpec.AT_MOST) {
                width = Math.min(width, specSize);
            }
        }
        return width;
    }

    public int getLineChartHeight(int heightMeasureSpec) {
        int height = 0;
        int specSize = MeasureSpec.getSize(heightMeasureSpec);
        int specMode = MeasureSpec.getMode(heightMeasureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            height = specSize;
        } else {
            height = dm.heightPixels / 2;
            if (specMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, specSize);
            }
        }
        return height;
    }

    private void drawChartRect(Canvas canvas, float rectWidth, float rectHeight, float distance) {

        //判空
        if (rectHeightList == null || rectHeightList.size() == 0) {
            return;
        }
        rectF = new RectF(stage * rectWidth + (stage + 1) * distance, -(rectHeight) - textSize - textDistance, stage * rectWidth + (stage + 1) * distance + rectWidth, -textSize - textDistance);
        //这里这么写是因为圆角矩形一开始是一条直线
        lineChartCanvas.drawRoundRect(rectF, 50, 50, rectPaint);
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);

    }

    private void drawChartText(Canvas canvas, float rectWidth, String rectHeight, float distance) {
        //判空
        if (textList == null || textList.size() == 0) {
            return;
        }
        lineChartCanvas.drawText(textList.get(stage), stage * rectWidth + (stage + 1) * distance + rectWidth / 2, 0, textPaint);
        lineChartCanvas.drawText(textValueList.get(stage), stage * rectWidth + (stage + 1) * distance + rectWidth / 2, -(textSize + 2 * textDistance + rectHeightList.get(stage)), textPaint);
    }


    //画线以及画小人
    private void drawChartLine(Canvas canvas, int maxWidth, float lineAnimatorValue) {

        float rate = (float) (Math.cbrt(lineAnimatorValue) * lineAnimatorValue);
        float lastRate = (float) (Math.cbrt(lastLineAnimatorValue) * lastLineAnimatorValue);
        if (lineAnimatorValue == 0f) {
            Log.e("distance", "" + distance);
            Log.e("rectHeightList", "" + (float) (-2 * textSize - 2 * textDistance - 200 - rectHeightList.get(0)));
//            linePath.moveTo((float) distance, (float) (-2 * textSize - 2 * textDistance - 200 - rectHeightList.get(0)));
        } else if (lineAnimatorValue == 1) {
//            linePath.quadTo((float) maxWidth + distance, (float) (-2 * textSize - 2 * textDistance - 200 - rectHeightList.get(rectHeightList.size() - 1) / 2), (float) maxWidth + distance, (float) (-2 * textSize - 2 * textDistance - 200 - rate * rectHeightList.get(rectHeightList.size() - 1)));
//            lineChartCanvas.drawPath(linePath, linePaint);
            Log.e("distance", "" + distance);
            Log.e("rectHeightList", "" + (float) (-2 * textSize - 2 * textDistance - 200 - rectHeightList.get(0)));
        }
        //小人移动
        lineChartCanvas.drawLine((float) lastLineAnimatorValue * maxWidth + distance, (float) (-2 * textSize - 2 * textDistance - 200 - lastRate * rectHeightList.get(rectHeightList.size() - 1)), (float) lineAnimatorValue * maxWidth + distance, (float) (-2 * textSize - 2 * textDistance - 200 - rate * rectHeightList.get(rectHeightList.size() - 1)), linePaint);
        lineChartCanvas.drawBitmap(small(imgBitmap), (float) lineAnimatorValue * maxWidth + distance, (float) (-imgBitmap.getHeight() / 2 - 2 * textSize - 2 * textDistance - 200 - rate * rectHeightList.get(rectHeightList.size() - 1)), rectPaint);
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        lastLineAnimatorValue = lineAnimatorValue;
    }

    //操作bitmap缩放的方法
    private static Bitmap small(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postScale(0.35f, 0.35f); //长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizeBmp;
    }


}
