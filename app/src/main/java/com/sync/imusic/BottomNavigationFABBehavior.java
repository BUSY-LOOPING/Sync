package com.sync.imusic;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class BottomNavigationFABBehavior extends FloatingActionButton.Behavior{

    public BottomNavigationFABBehavior() {
    }

    public BottomNavigationFABBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    private boolean updateButton(View child, View dependency) {
        if (dependency instanceof Snackbar.SnackbarLayout){
            float oldTranslation = child.getTranslationY();
            Log.d("custom", "oldTranslation = " + oldTranslation);
            float height = dependency.getHeight();
            Log.d("custom", "height = " + height);
            float newTransition = dependency.getTranslationY() - height;
            Log.d("custom", "newTransition = " + newTransition);
            child.setTranslationY(newTransition);
            return oldTranslation != newTransition;
        }
        return false;
    }
}
