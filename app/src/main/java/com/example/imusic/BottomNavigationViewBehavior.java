package com.example.imusic;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

public class BottomNavigationViewBehavior extends CoordinatorLayout.Behavior<BottomNavigationView>{

    public BottomNavigationViewBehavior() {
    }

    public BottomNavigationViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull BottomNavigationView child, @NonNull View dependency) {
        if (dependency instanceof Snackbar.SnackbarLayout)
        {
            updateSnackbar(child, dependency);
        }
        return super.layoutDependsOn(parent, child, dependency);
    }


    private void updateMiniPlayer(BottomNavigationView child, View miniPlayerView) {
//        if (miniPlayerView.getLayoutParams() instanceof CoordinatorLayout.LayoutParams){
//            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) miniPlayerView.getLayoutParams();
//            params.setAnchorId(child.getId());
//            miniPlayerView.setLayoutParams(params);
//        }
    }

    private void updateSnackbar(BottomNavigationView child, View snackbarLayout) {
        if (snackbarLayout.getLayoutParams() instanceof CoordinatorLayout.LayoutParams)
        {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) snackbarLayout.getLayoutParams();
            params.setAnchorId(child.getId());
            params.gravity = Gravity.TOP;
            snackbarLayout.setLayoutParams(params);
        }
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                       BottomNavigationView child, @NonNull
                                                   View directTargetChild, @NonNull View target,
                                       int axes, int type) {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }


    @Override
    public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull BottomNavigationView child, @NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
//        child.setTranslationY(max(0f, min( (child.getHeight()), child.getTranslationY() + dy)));
    }

}
