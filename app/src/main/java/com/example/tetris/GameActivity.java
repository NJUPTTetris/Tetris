package com.example.tetris;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;
public class GameActivity extends AppCompatActivity {
    int xWidth,xHeight; //游戏区域宽度高度
    View view;//游戏区域控件
    Paint mapPaint; //    地图画笔
    Paint linePaint;//初始化辅助线画笔
    Paint boxPaint;//初始化方块画笔
    boolean[][]maps; //地图
    Point[] boxes; //方块
    Point[] nextboxes; //方块
    int boxSize;//方块大小
    final int TUBE =7;//方块种类
    int boxType;//选择方块类型
    int nextboxType;//选择方块类型
    // 新增一个变量来存储下一个方块的类型
    int imageboxType;
    int nextNextboxType;
    int color;
    int nextColor;

    private int nextNextColor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        initData();//初始化游戏数据
        initView();//初始化视图
        newBoxes();//生成新方块
        initListener();//设置监听器
    }
    //初始化游戏的数据，包括游戏区域的宽度和高度，以及初始化地图数组和方块大小
    public void initData() {
        xWidth=Math.round(160f * getResources().getDisplayMetrics().density);//获取游戏区域宽度
        xHeight=xWidth*3;
        maps=new boolean[10][30];  //初始化地图
        boxSize=xWidth/maps.length;
    }
    //创建了三个画笔对象，分别用于绘制地图、线条和方块
    public void initView() {//初始化视图
        mapPaint=new Paint(  );
        mapPaint.setColor( 0x50000000 );
        mapPaint.setAntiAlias( true );
        linePaint=new Paint();//初始化线条画笔
        linePaint.setColor(0xff666666);
        linePaint.setStrokeWidth(3);
        linePaint.setAntiAlias(true);
        boxPaint=new Paint();//初始化方块画笔
        boxPaint.setColor(0xff000000);
        FrameLayout layoutGame=findViewById(R.id.layoutGame);//得到父容器
        ImageView nextImage = findViewById(R.id.next_image);
        view=new View(this){
            @Override
            protected void onDraw(@NonNull Canvas canvas){  //重写游戏区域绘制，绘制游戏区域的内容，包括地图、方块和辅助线
                super.onDraw(canvas);
                //绘制地图
                for (int x=0;x<maps.length; x++) {
                    for (int y = 0; y < maps[x].length; y++) {
                        if (maps[x][y] == true)
                            canvas.drawRect( x * boxSize, y * boxSize,
                                    x * boxSize + boxSize, y * boxSize + boxSize, mapPaint );
                    }
                }
                for (Point box : boxes) { //方块绘制
                    canvas.drawRect(
                            box.x * boxSize,
                            box.y * boxSize,
                            box.x * boxSize + boxSize,
                            box.y * boxSize + boxSize, boxPaint
                    );
                }
                for(int x=0;x<maps.length;x++)//地图辅助线
                    canvas.drawLine(x*boxSize,0,x*boxSize,view.getHeight(),linePaint);
                for(int y=0;y<maps[0].length;y++)
                    canvas.drawLine(0,y*boxSize,view.getWidth(),y*boxSize,linePaint);
            }
        };
        view.setLayoutParams(new FrameLayout.LayoutParams(xWidth,xHeight));
        layoutGame.addView(view);//添加进父容器
    }
    // 创建一个新的方法来绘制下一个方块
    public void newBoxes() { //生成一个新的随机方块
        if(nextboxes==null){
            nextBoxes();
        }
        boxes = nextboxes;
        boxType = nextboxType;

        // 绘制下一个方块到 Bitmap
        Bitmap nextBlockBitmap = drawNextBlock(nextboxes);
        // 设置 next_image 的图像为绘制的 Bitmap
        ImageView nextBlockView = findViewById(R.id.next_image);
        nextBlockView.setImageBitmap(nextBlockBitmap);
        nextBoxes(); // 更新下一个方块
    }
    public void nextBoxes(){
        Random random=new Random();
        nextboxType=random.nextInt(TUBE);
        switch (nextboxType){
            case 0:
                boxPaint.setColor(getResources().getColor(R.color.red));
                nextboxes= new Point[]{new Point(4,0),new Point(5,0),new Point(4,1),new Point(5,1)};
                break;
            case 1:
                boxPaint.setColor(getResources().getColor(R.color.orange));
                nextboxes= new Point[]{new Point(4,1),new Point(5,0),new Point(3,1),new Point(5,1)};
                break;
            case 2:
                boxPaint.setColor(getResources().getColor(R.color.yellow));
                nextboxes= new Point[]{new Point(4,1),new Point(3,0),new Point(3,1),new Point(5,1)};
                break;
            case 3:
                boxPaint.setColor(getResources().getColor(R.color.green));
                nextboxes= new Point[]{new Point(4,1),new Point(4,0),new Point(3,1),new Point(5,1)};
                break;
            case 4:
                boxPaint.setColor(getResources().getColor(R.color.cyan));
                nextboxes= new Point[]{new Point(4,0),new Point(3,0),new Point(5,0),new Point(6,0)};
                break;
            case 5:
                boxPaint.setColor(getResources().getColor(R.color.blue));
                nextboxes= new Point[]{new Point(4,1),new Point(4,0),new Point(5,1),new Point(5,2)};
                break;
            case 6:
                boxPaint.setColor(getResources().getColor(R.color.purple));
                nextboxes= new Point[]{new Point(5,1),new Point(5,0),new Point(4,1),new Point(4,2)};
                break;
        }
    }
    private Bitmap drawNextBlock(Point[] nextboxes) {
        // 创建一个与ImageView相同大小的Bitmap
        Bitmap bitmap = Bitmap.createBitmap(140, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        // 设置方块颜色
        Paint nextboxPaint = new Paint();
        // 根据 nextboxType 设置颜色
        switch (nextboxType) {
            case 0:
                nextboxPaint.setColor(getResources().getColor(R.color.red));
                break;
            case 1:
                nextboxPaint.setColor(getResources().getColor(R.color.orange));
                break;
            case 2:
                nextboxPaint.setColor(getResources().getColor(R.color.yellow));
                break;
            case 3:
                nextboxPaint.setColor(getResources().getColor(R.color.green));
                break;
            case 4:
                nextboxPaint.setColor(getResources().getColor(R.color.cyan));
                break;
            case 5:
                nextboxPaint.setColor(getResources().getColor(R.color.blue));
                break;
            case 6:
                nextboxPaint.setColor(getResources().getColor(R.color.purple));
                break;
            default:
                nextboxPaint.setColor(getResources().getColor(R.color.black)); // 默认颜色
                break;
        }
        // 计算方块大小
        int boxSize = 18;
        // 在Canvas上绘制方块
        for (Point box : nextboxes) {
            canvas.drawRect(
                    box.x * boxSize,
                    box.y * boxSize,
                    (box.x + 1) * boxSize,
                    (box.y + 1) * boxSize,
                    nextboxPaint
            );
        }
        return bitmap;
    }
    public void initListener() {//初始化监听
        findViewById(R.id.arrow_left).setOnClickListener(v -> move(-1,0));
        findViewById(R.id.arrow_right).setOnClickListener(v -> move(1,0));
        findViewById(R.id.arrow_rotate).setOnClickListener(v -> rotate());
        findViewById(R.id.arrow_down).setOnClickListener(v -> {
            // 使用一个 while 循环来持续调用 moveBottom()，直到返回 false
            while (true) {
                //如果下落失败，则结束循环
                if (!moveBottom())
                    break;
            }
        });
// 重新开始游戏
        findViewById(R.id.btn_restart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recreate();
            }
        });
        //暂停游戏
        findViewById(R.id.btn_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
    public boolean move(int x, int y) {//移动
        for (Point box : boxes) {
            if(checkBoundary(box.x+x,box.y+y))
                return false;
        }
        for (Point box : boxes) { //遍历方块数组
            box.x += x;
            box.y += y;
        }
        view.invalidate();//调用重绘
        return true;
    }
    //首先检查旋转后的方块是否会超出边界或与其他方块重叠，如果没有问题，则更新方块的位置并重绘视图
    public void rotate() { //顺时针旋转90
        if(boxType==0)
            return;
        for (Point box : boxes) {
            int checkX=-box.y+boxes[0].y+boxes[0].x;
            int checkY=box.x-boxes[0].x+boxes[0].y;
            if(checkBoundary(checkX,checkY))
                return;
        }
        for (Point box : boxes) { //遍历方块数组
            int checkX=-box.y+boxes[0].y+boxes[0].x;
            int checkY=box.x-boxes[0].x+boxes[0].y;
            box.x=checkX;
            box.y=checkY;
        }
        view.invalidate();//调用重绘
    }
    //如果方块不能继续下落（即触底或碰到其他方块），则将方块固定到地图上，并生成一个新的方块。
    public boolean moveBottom(){//下落
        if(move(0,1))//移动成功不作处理
            return true;
        for (Point box : boxes)//移动失败堆积处理
            maps[box.x][box.y] = true;
        newBoxes();//生成新的方块
        view.invalidate();//调用重绘
        return false;
    }
    //检查给定的坐标（x, y）是否在游戏区域内，以及是否已经被方块占据
    public boolean checkBoundary(int x,int y){//边界判断
        return (x<0||y<0||x>=maps.length||y>=maps[0].length||maps[x][y]);
    }
}
