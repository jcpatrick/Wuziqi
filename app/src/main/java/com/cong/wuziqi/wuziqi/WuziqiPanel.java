package com.cong.wuziqi.wuziqi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/4/6.
 */
public class WuziqiPanel extends View {

    private static final int MAX_COUNT_IN_LINE = 5;
    private int mPanelWidth;
    private float mLineHeight;
    private int MAX_LINE = 10;

    private Paint mPaint = new Paint();

    private Bitmap mWhitePiece;
    private Bitmap mBlackPiece;

    boolean mIsWhite = true;
    private ArrayList<Point> mWhiteArray = new ArrayList<>();
    private ArrayList<Point> mBlackArray = new ArrayList<>();

    private float ratioPieceOfLineHeight = 3 * 1.0f / 4;

    private boolean mIsGameOver;
    private boolean mIsWhiteWinner;

    public WuziqiPanel(Context context, AttributeSet attrs) {
        super(context, attrs);

        setBackgroundColor(0x44ff0000);//将背景色设置为红色透明

        init();
    }

    private void init() {
        mPaint.setColor(0x88000000);//黑色半透明
        mPaint.setAntiAlias(true);//设置抗锯齿
        mPaint.setDither(true);//设置防抖动
        mPaint.setStyle(Paint.Style.STROKE);//设置仅描边
        mWhitePiece = BitmapFactory.decodeResource(getResources(), R.drawable.stone_w2);
        mBlackPiece = BitmapFactory.decodeResource(getResources(), R.drawable.stone_b1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //获得宽高的尺寸和模式
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int width = Math.min(widthSize, heightSize);

        if (widthMode == MeasureSpec.UNSPECIFIED) {//未指定尺寸模式
            width = heightSize;
        } else if (heightMode == MeasureSpec.UNSPECIFIED) {
            width = widthSize;
        }

        setMeasuredDimension(width, width);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mPanelWidth = w;
        mLineHeight = mPanelWidth * 1.0f / MAX_LINE;//长度为画板的1/4

        int pieceWidth = (int) (mLineHeight * ratioPieceOfLineHeight);//棋子的宽度
        mWhitePiece = Bitmap.createScaledBitmap(mWhitePiece, pieceWidth, pieceWidth, false);
        mBlackPiece = Bitmap.createScaledBitmap(mBlackPiece, pieceWidth, pieceWidth, false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //如果游戏已经结束，则直接返回
        if (mIsGameOver)
            return false;
        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP) {
            //获得点击的坐标
            int x = (int) event.getX();
            int y = (int) event.getY();
            Point p = getPoint(x, y);
            if (mWhiteArray.contains(p) || mBlackArray.contains(p)) {
                return false;
            }
            if (mIsWhite) {
                mWhiteArray.add(p);
            } else {
                mBlackArray.add(p);
            }

            invalidate();//重绘
            mIsWhite = !mIsWhite;


        }
        return true;
    }

    private Point getPoint(int x, int y) {
        return new Point((int) (x / mLineHeight), (int) (y / mLineHeight));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBoard(canvas);
        drawPiece(canvas);
        checkGameOver();
    }

    private void checkGameOver() {
        boolean whiteWin = checkFiveInLine(mWhiteArray);
        boolean blackWin = checkFiveInLine(mBlackArray);
        if (whiteWin || blackWin) {
            mIsGameOver = true;
            mIsWhiteWinner = whiteWin;
            String text = mIsWhiteWinner ? "白棋胜利" : "黑棋胜利";
            Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkFiveInLine(List<Point> points) {
        if (points.size() >= MAX_COUNT_IN_LINE) {
            Point p = points.get(points.size() - 1);
            boolean win;
            win = checkHorizontal(p.x, p.y, points);
            if (win) return true;
            win = checkVertical(p.x, p.y, points);
            if (win) return true;
            win = checkLHT(p.x, p.y, points);
            if (win) return true;
            win = checkRHT(p.x, p.y, points);
            if (win) return true;

        }
        return false;

    }

    private boolean checkHorizontal(int x, int y, List<Point> points) {
        int count = 1;
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x - i, y))) {
                count++;
            } else {
                break;
            }
        }
        if (count == MAX_COUNT_IN_LINE) {
            return true;
        }
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x + i, y))) {
                count++;
            } else {
                break;
            }
        }
        if (count == MAX_COUNT_IN_LINE) {
            return true;
        }
        return false;
    }

    private boolean checkVertical(int x, int y, List<Point> points) {
        int count = 1;
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x, y - i))) {
                count++;
            } else {
                break;
            }
        }
        if (count == MAX_COUNT_IN_LINE) {
            return true;
        }
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x, y + i))) {
                count++;
            } else {
                break;
            }
        }
        if (count == MAX_COUNT_IN_LINE) {
            return true;
        }
        return false;
    }

    private boolean checkLHT(int x, int y, List<Point> points) {
        int count = 1;
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x - i, y - i))) {
                count++;
            } else {
                break;
            }
        }
        if (count == MAX_COUNT_IN_LINE) {
            return true;
        }
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x + i, y + i))) {
                count++;
            } else {
                break;
            }
        }
        if (count == MAX_COUNT_IN_LINE) {
            return true;
        }
        return false;
    }

    private boolean checkRHT(int x, int y, List<Point> points) {
        int count = 1;
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x - i, y + i))) {
                count++;
            } else {
                break;
            }
        }
        if (count == MAX_COUNT_IN_LINE) {
            return true;
        }
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x + i, y - i))) {
                count++;
            } else {
                break;
            }
        }
        if (count == MAX_COUNT_IN_LINE) {
            return true;
        }
        return false;
    }

    /**
     * 根据每个棋子的坐标，将棋子在相应的每个位置绘制出来
     * 由于每个棋子的宽是ratioPieceOfLineHeight，棋子又在网格点中间，网格起点为1/2个mLightHeight
     * 所以棋子的起点为
     * （1/2 -(ratioPieceOfLineHeight)/2) * mLightHeight
     * = ((1 - ratioPieceOfLineHeight) / 2) * mLightHeight;
     * @param canvas
     */
    private void drawPiece(Canvas canvas) {

        for (int i = 0, n = mWhiteArray.size(); i < n; i++) {
            Point mWhitePoint = mWhiteArray.get(i);
            canvas.drawBitmap(mWhitePiece,
                    (mWhitePoint.x + (1 - ratioPieceOfLineHeight) / 2) * mLineHeight,
                    (mWhitePoint.y + (1 - ratioPieceOfLineHeight) / 2) * mLineHeight,
                    null);
        }
        for (int i = 0, n = mBlackArray.size(); i < n; i++) {
            Point mBlackPoint = mBlackArray.get(i);
            canvas.drawBitmap(mBlackPiece,
                    (mBlackPoint.x + (1 - ratioPieceOfLineHeight) / 2) * mLineHeight,
                    (mBlackPoint.y + (1 - ratioPieceOfLineHeight) / 2) * mLineHeight,
                    null);
        }
    }

    private void drawBoard(Canvas canvas) {
        int w = mPanelWidth;
        float lineHeight = mLineHeight;
        for (int i = 0; i < MAX_LINE; i++) {
            int startX = (int) (lineHeight / 2);//距离左边0.5个格子的长度
            int endX = (int) (w - lineHeight / 2);//距离右边0.5个格子的长度

            int y = (int) ((0.5f + i) * lineHeight);
            canvas.drawLine(startX, y, endX, y, mPaint);//绘制水平方向
            canvas.drawLine(y, startX, y, endX, mPaint);//绘制竖直方向
        }
    }

    private static final String INSTANCE = "instance";
    private static final String INSTANCE_GAME_OVER = "instance_game_over";
    private static final String INSTANCE_IS_WHITE = "instance_is_white";
    private static final String INSTANCE_WHITE_ARRAY = "instance_white_array";
    private static final String INSTANCE_BLACK_ARRAY = "instance_black_array";

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE, super.onSaveInstanceState());
        bundle.putBoolean(INSTANCE_GAME_OVER, mIsGameOver);
        bundle.putBoolean(INSTANCE_IS_WHITE, mIsWhite);
        bundle.putParcelableArrayList(INSTANCE_WHITE_ARRAY, mWhiteArray);
        bundle.putParcelableArrayList(INSTANCE_BLACK_ARRAY, mBlackArray);

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mIsGameOver = bundle.getBoolean(INSTANCE_GAME_OVER);
            mIsWhite = bundle.getBoolean(INSTANCE_IS_WHITE);
            mWhiteArray = bundle.getParcelableArrayList(INSTANCE_WHITE_ARRAY);
            mBlackArray = bundle.getParcelableArrayList(INSTANCE_BLACK_ARRAY);
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE));
        }
    }

    public void start(){
        mWhiteArray.clear();
        mBlackArray.clear();
        mIsGameOver = false;
        mIsWhite = true;
        invalidate();
    }
}
