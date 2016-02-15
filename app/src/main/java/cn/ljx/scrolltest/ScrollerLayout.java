package cn.ljx.scrolltest;

import android.content.Context;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by liangjinxin on 2016/2/14.
 */
public class ScrollerLayout extends ViewGroup {

	private final String TAG = getClass().getSimpleName();
	/**
	 * 用于完成滚动操作的实例
	 */
	private Scroller mScroller;

	/**
	 * 判定为拖动的最小移动像素数
	 */
	private int mTouchSlop;

	/**
	 * 手机按下时的屏幕坐标
	 */
	private float mXDown;

	/**
	 * 手机当时所处的屏幕坐标
	 */
	private float mXMove;

	/**
	 * 上次触发Action_Move事件的屏幕坐标
	 */
	private float mXLastMove;

	/**
	 * 界面可滚动的左边界
	 */
	private int leftBorder;

	/**
	 * 界面可滚动的右边界
	 */
	private int rightBorder;

	public ScrollerLayout(Context context) {
		super(context);
		Log.i(TAG, "s1");
	}

	public ScrollerLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.i(TAG, "s2");
		//第一步 ，创建Scroller实例
		mScroller = new Scroller(context);
		ViewConfiguration configuration = ViewConfiguration.get(context);
		//获取TouchSlop值  触发移动事件的最短距离
		mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View childView = getChildAt(i);
			//测量子控件大小
			measureChild(childView, widthMeasureSpec, heightMeasureSpec);
		}
	}


	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (changed) {
			int childCount = getChildCount();
			for (int i = 0; i < childCount; i++) {
				View childView = getChildAt(i);
				//为每一个子控件在水平方向上进行布局
				childView.layout(i * childView.getMeasuredWidth(), 0, (i + 1) * childView.getMeasuredWidth(), childView.getMeasuredHeight());
				Log.i(TAG, "left :" + i * childView.getMeasuredWidth());
			}
			//初始化坐右边的边界值
			leftBorder = getChildAt(0).getLeft();
			Log.i(TAG, "leftBorder :" + leftBorder);
			rightBorder = getChildAt(getChildCount() - 1).getRight();
			Log.i(TAG, "rightBorder :" + rightBorder);
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mXDown = ev.getRawX();
				mXLastMove = mXDown;
				break;
			case MotionEvent.ACTION_MOVE:
				mXMove = ev.getRawX();
				float diff = Math.abs(mXMove - mXDown);
				mXLastMove = mXMove;
				//当手指拖动值大于TouchSlop值时，认为应该进行滚动，拦截子控件的事件
				if (diff > mTouchSlop) {
					return true;
				}
				break;
		}
		return super.onInterceptTouchEvent(ev);
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_MOVE:
				mXMove = event.getRawX();
				int scrolledX = (int) (mXLastMove - mXMove);
				//处理边界问题
				if (getScrollX() + scrolledX < leftBorder) {
					//scrollTo 相对于控件原始位置移动某段距离
					scrollTo(leftBorder, 0);
					return true;
				} else if (getScaleX() + getWidth() + scrolledX > rightBorder) {
					scrollTo(rightBorder - getWidth(), 0);
					return true;
				}
				// scrollBy 相对于当前位置滚动某段距离
				scrollBy(scrolledX, 0);
				mXLastMove = mXMove;
				break;
			case MotionEvent.ACTION_UP:
				//当手指抬起时，根据当前的滚动值判定应该滚动那个子控件的界面
				int targetIndex = (getScrollX() + getWidth() / 2) / getWidth();
				int dx = targetIndex * getWidth() - getScrollX();
				Log.e(TAG,"dx :"+dx);
				Log.e(TAG,"dx :"+getWidth()/2);
				// 第二步，调用startScroll()方法来初始化滚动数据并刷新界面
				mScroller.startScroll(getScrollX(), 0, dx, 0);
				invalidate();
				break;
		}
		return super.onTouchEvent(event);
	}

	@Override
	public void computeScroll() {
		//第三步 重写computeScroll方法，并在其内部完成平滑滚动的逻辑
		//判断平移是否完成
		if (mScroller.computeScrollOffset()){
			scrollTo(mScroller.getCurrX(),mScroller.getCurrY());
			//不断重绘
			invalidate();
		}
	}
}
