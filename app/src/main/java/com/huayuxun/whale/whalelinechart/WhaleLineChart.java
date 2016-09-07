package com.huayuxun.whale.whalelinechart;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jinhui on 2016/8/31.
 */
public class WhaleLineChart extends ViewGroup {
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
    private float widthDistance;                //设置的横向间隔
    private float heightDistance;               //设置的纵向间隔
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


    private Bitmap imgBitmap;                   //卖座小人图片
    private PathMeasure imgPathMeasure;
    private float[] mCurrentPosition = new float[2];
    private boolean isDrawBackGroundFinished = false;
//    private ImageView imageView;                //小人图片
//    private ObjectAnimator imgAnimator;


    {
        textSize = 35f;
        rectWidth = 60f;
        widthDistance = 100f;
        heightDistance = 100f;
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
//        imageView = new ImageView(context);
//        addView(imageView);
//        imageView.setImageResource(R.mipmap.img);
        setWillNotDraw(false);
        initPaint();
//        initLineAnimation();
        initImgAnimation();
//        initRectFAnimation(stage);
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
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getLineChartWidth(widthMeasureSpec), getLineChartHeight(heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        pivotX = getWidth() / 2;
        pivotY = getHeight() / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //将画布的中心移到屏幕的中心
        canvas.translate(0, pivotY * 2);
        if(!isDrawBackGroundFinished) {
            canvasBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            lineChartCanvas = new Canvas(canvasBitmap);
            for (int i = 0; i < rectHeightList.size(); i++, stage++) {
                drawChartRect(canvas, rectWidth, rectHeightList.get(i), widthDistance);
                drawChartText(canvas, rectWidth, textList.get(i), widthDistance);
            }
            drawChartLine(canvas, (int) (rectHeightList.size() * (widthDistance + rectWidth) - widthDistance));
            isDrawBackGroundFinished = true;
            startPathAnimation(2000);
        }
        canvas.drawBitmap();
        canvas.save();
//        saveCanvasBitmap = canvasBitmap;
        canvas.drawBitmap(small(imgBitmap), mCurrentPosition[0], mCurrentPosition[1], rectPaint);
//        canvasBitmap = saveCanvasBitmap;
        canvas.restore();
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
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(1);
        linePaint.setAntiAlias(true);

        //初始化文本内容画笔
        textPaint = new Paint();
        textPaint.setStrokeWidth(3);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.argb(100, 182, 181, 182));
        textPaint.setTextSize(textSize);
        textPaint.setAntiAlias(true);
        mCurrentPosition = new float[2];
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
                rectFAnimatorValue = 0;
                Log.i("ValueAnimator.isRunning", "" + rectFValueAnimator.isRunning());
                initRectFAnimation(++stage);
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
            width = (int) (rectHeightList.size() * rectWidth + (rectHeightList.size() + 1) * widthDistance);
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
        canvas.drawRoundRect(rectF, 50, 50, rectPaint);

    }

    private void drawChartText(Canvas canvas, float rectWidth, String rectHeight, float distance) {
        //判空
        if (textList == null || textList.size() == 0) {
            return;
        }
        canvas.drawText(textList.get(stage), stage * rectWidth + (stage + 1) * distance + rectWidth / 2, 0, textPaint);
        canvas.drawText(textValueList.get(stage), stage * rectWidth + (stage + 1) * distance + rectWidth / 2, -(textSize + 2 * textDistance + rectHeightList.get(stage)), textPaint);
    }


    //利用贝塞尔曲线画曲线以及画小人
    private void drawChartLine(Canvas canvas, int maxWidth) {
        //起点
        float firstPointX = widthDistance;
        float firstPointY = -(heightDistance + 2 * textSize + 2 * textDistance + rectHeightList.get(0));
        //终点
        float lastPointX = maxWidth + widthDistance;
        float lastPointY = -(heightDistance + 2 * textSize + 2 * textDistance + rectHeightList.get(rectHeightList.size() - 1));
        //控制点
        float controlPointX = 3 * widthDistance + rectWidth * 3;
        float controlPointY = -(heightDistance + 2 * textSize + 2 * textDistance + rectHeightList.get(1));

        linePath = new Path();
        linePath.moveTo(firstPointX, firstPointY);
        linePath.quadTo(controlPointX, controlPointY, lastPointX, lastPointY);
        imgPathMeasure = new PathMeasure(linePath,false);

        canvas.drawPath(linePath, linePaint);
    }

    public void startPathAnimation(int duration) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, imgPathMeasure.getLength());
        valueAnimator.setDuration(duration);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                //获取当前点坐标封装到mCurrentPosition中
                imgPathMeasure.getPosTan(value, mCurrentPosition, null);
//                Log.e("value:", "" + value);
//                Log.e("mCurrentPositionX:", "X:" + mCurrentPosition[0]);
//                Log.e("mCurrentPositionY:", "Y:" + mCurrentPosition[1]);
                postInvalidate();
            }
        });
        valueAnimator.start();
    }

    //操作bitmap缩放的方法
    private static Bitmap small(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postScale(0.35f, 0.35f); //长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizeBmp;
    }
}
