package gjjzx.com.robotclient.diy;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by PC on 2017/10/26.
 */

public class MyRecyclerView extends RecyclerView {


    private View mCurrentCenterChildView;

    public MyRecyclerView(Context context) {
        super(context);
    }

    public MyRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public View findViewAtCenter() {
        if (getLayoutManager().canScrollVertically()) {
            return findViewAt(0, getHeight() / 2);
        }else if (getLayoutManager().canScrollHorizontally()) {
            return findViewAt(getWidth() / 2, 0);
        }
        return null;
    }

    public View findViewAt(int x, int y) {
        final int count = getChildCount();
        for (int i = 0; i < count; ++i) {
            final View v = getChildAt(i);
            final int x0 = v.getLeft();
            final int y0 = v.getTop();
            final int x1 = v.getWidth() + x0;
            final int y1 = v.getHeight() + y0;
            if (x >= x0 && x <= x1 && y >= y0 && y <= y1) {
                return v;
            }
        }
        return null;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);



        mCurrentCenterChildView = findViewAtCenter();
        smoothScrollToView(mCurrentCenterChildView);
    }

    public void smoothScrollToView(View v) {
        int distance = 0;
        if (getLayoutManager() instanceof LinearLayoutManager) {
            if (getLayoutManager().canScrollVertically()) {
                final float y = v.getY() + v.getHeight() * 0.5f;
                final float halfHeight = getHeight() * 0.5f;
                distance = (int) (y - halfHeight);
            } else if (getLayoutManager().canScrollHorizontally()) {
                final float x = v.getX() + v.getWidth() * 0.5f;
                final float halfWidth = getWidth() * 0.5f;
                distance = (int) (x - halfWidth);
            }

        } else
            throw new IllegalArgumentException("CircleRecyclerView just support T extend LinearLayoutManager!");
        smoothScrollBy(distance,distance);
    }



}
