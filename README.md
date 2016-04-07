##五子棋
本项目主要是通过观看慕课网hyman的五子棋视频来学习自定义View等只是的Demo，视频地址为：[链接](http://www.imooc.com/learn/641)

#自定义画板
>需要注意的知识点：自定义view时，如果涉及状态保存等，应该要在布局文件中加上id，否则无法对view的状态进行保存。
##总结
通过该Demo的练习，学会了View的测量，并且知道了如何对View的大小进行设置，也学习了View点击事件的拦截，以及对View进行绘制。

###一. 创建自定义View，并实现构造方法

        public WuziqiPanel(Context context, AttributeSet attrs) {
       		super(context, attrs);
        	setBackgroundColor(0x44ff0000);//将背景色设置为红色透明
        	init();
    }

###二. 创建画笔，并初始化画笔

    private void init() {
        mPaint.setColor(0x88000000);//黑色半透明
        mPaint.setAntiAlias(true);//设置抗锯齿
        mPaint.setDither(true);//设置防抖动
        mPaint.setStyle(Paint.Style.STROKE);//设置仅描边
        mWhitePiece = BitmapFactory.decodeResource(getResources(), R.drawable.stone_w2);
        mBlackPiece = BitmapFactory.decodeResource(getResources(), R.drawable.stone_b1);
	}
###三. 测量并设置画布的大小（这里为正方形）
* 通过MeasureSpec类获得布局的长、宽，以及相应的模式
* 将长和宽中最小值设置为画布的宽
* 判断长和宽的模式，如果是未指定的话则以另外的一个为基准
* 通过setMeasuredDimension(width, width);重新设置宽高
>

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //获得宽高的尺寸和模式
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int width = Math.min(widthSize,heightSize);

        if(widthMode == MeasureSpec.UNSPECIFIED){//未指定尺寸模式
            width = heightSize;
        }else if(heightMode == MeasureSpec.UNSPECIFIED){
            width = widthSize;
        }

        setMeasuredDimension(width, width);//设置宽高为相同值
    }

###四. 获得画板的宽度和每个格子的边长
> 在onSizeChanged()中获得画板的长度，以及要绘制的格子的长度,onSizeChanged()会在setMeasuredDimension(width, width);之后被调用

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mPanelWidth = w;
        mLineHeight = mPanelWidth * 1.0f / MAX_LINE;//长度为画板的1/4

        int pieceWidth = (int)(mLineHeight * ratioPieceOfLineHeight);
        mWhitePiece = Bitmap.createScaledBitmap(mWhitePiece, pieceWidth, pieceWidth, false);
        mBlackPiece = Bitmap.createScaledBitmap(mBlackPiece, pieceWidth, pieceWidth, false);
    }

###五. 绘制网格
>重写onDraw方法，并调用自定义的drawBoard()方法，来绘制网格

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBoard(canvas);
    }
>

    private void drawBoard(Canvas canvas) {
        int w = mPanelWidth;
        float lineHeight = mLineHeight;
        for(int i = 0; i < MAX_LINE; i++){
            int startX = (int)(lineHeight / 2);//距离左边0.5个格子的长度
            int endX = (int)(w - lineHeight / 2);//距离右边0.5个格子的长度

            int y = (int)((0.5f + i) * lineHeight);
            canvas.drawLine(startX, y, endX, y, mPaint);//绘制水平方向
            canvas.drawLine( y, startX, y, endX, mPaint);//绘制竖直方向
        }
    }

###六. 绘制棋子
>在init()方法中初始化棋子<br>

        mWhitePiece = BitmapFactory.decodeResource(getResources(), R.drawable.stone_w2);
        mBlackPiece = BitmapFactory.decodeResource(getResources(), R.drawable.stone_b1);

>在onSizeChange()方法中，根据网格的大小将棋子按照相应的比例进行缩放（这里是3/4)<br>

        int pieceWidth = (int) (mLineHeight * ratioPieceOfLineHeight);//棋子的宽度
        mWhitePiece = Bitmap.createScaledBitmap(mWhitePiece, pieceWidth, pieceWidth, false);
        mBlackPiece = Bitmap.createScaledBitmap(mBlackPiece, pieceWidth, pieceWidth, false);
>设置点击网格的点击事件,在onTouchEvent（）中进行点击事件的拦截并处理<br>
>1）判断游戏时候已经结束，结束则直接返回false；对点击事件不做任何处理<br>
>2）对点击事件进行判断，由于MotionEvent.Action_DOWN可能会被其他的View给拦截，所以这里对MotionEvent.Action_UP进行处理。如果点击的位置已经有棋子了则返回false，不做任何处理；<br>
>(3)如果点击的位置没有棋子则将棋子加入相应颜色的棋子列表，并调用invalidate()对界面进行重新绘制，并将下一步要走的棋子的颜色设置为相反的颜色。




	public boolean onTouchEvent(MotionEvent event) {
        if (mIsGameOver)
            return false;
        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP) {
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

###七. 调用onDraw（）方法，在棋谱上绘制相应的棋子

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

###八. view的状态的保存
>由于在选装屏幕或者接收电话等过程中，会重新绘制view，view的数据可能被系统回收，所以必须对View的状态进行保存<br>
>View状态的主要通过onSaveInstanceState()来进行保存,通过onRestoreInstanceState()来进行取出恢复

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

###九. 再来一局

    public void start(){
        mWhiteArray.clear();
        mBlackArray.clear();
        mIsGameOver = false;
        mIsWhite = true;
        invalidate();
    }