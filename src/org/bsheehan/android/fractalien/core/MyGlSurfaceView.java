package org.bsheehan.android.fractalien.core;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

public class MyGlSurfaceView extends GLSurfaceView {
    //private static final String TAG = "WallpaperGLSurfaceView";
 
    public MyGlSurfaceView(Context context) {
        super(context);

    }
 
    @Override
    public SurfaceHolder getHolder() {
        return super.getHolder();
    }
 
    public void onDestroy() {
        super.onDetachedFromWindow();
    }
}