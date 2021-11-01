package com.sync.imusic;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class MyBottomSheetBehavior<T extends View> extends BottomSheetBehavior<T> {
    private Context context;
    private boolean mDependsOnBottomBar = true;

    public MyBottomSheetBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull T child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        return super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type);
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull T child, @NonNull View dependency) {
        return (dependency instanceof BottomNavigationView) || (dependency instanceof RecyclerView) || super.layoutDependsOn(parent, child, dependency);
    }

    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull T child, @NonNull View dependency) {
        if (dependency instanceof BottomNavigationView) {

            BottomNavigationView bottomBar = (BottomNavigationView) dependency;

            if (mDependsOnBottomBar && getState() != STATE_EXPANDED) {
//                TODO this 4dp margin is actual shadow layout height, which is 4 dp in bottomBar library ver. 2.0.2
                float transitionY = bottomBar.getTranslationY() - bottomBar.getHeight()
                        + (getState() != STATE_EXPANDED ? dpToPx(4) : 0F);
                child.setTranslationY(Math.min(transitionY, 0F));
//                Log.d("bottom", "bottomBar.getTranslationY() = " + bottomBar.getTranslationY());
//                Log.d("bottom", "bottomBar.getHeight() = " + bottomBar.getHeight());
//                Log.d("bottom", "Math.min(transitionY, 0F) = " + Math.min(transitionY, 0F));
            }

            if (bottomBar.getTranslationY() >= bottomBar.getHeight()) {
                Log.d("bottom", "bottomBar.getTranslationY() = " + bottomBar.getTranslationY() + " bottomBar.getHeight() = " +bottomBar.getHeight());
                mDependsOnBottomBar = false;
                bottomBar.setVisibility(View.GONE);
            }
            if (getState() != STATE_EXPANDED) {  //getState method of BottomSheetBehaviour
                mDependsOnBottomBar = true;
                bottomBar.setVisibility(View.VISIBLE);
//                Log.d("bottom", "bottom  bar gone, mDependsOnBottomBar true");
            }

            return false;
        }
        return super.onDependentViewChanged(parent, child, dependency);
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
