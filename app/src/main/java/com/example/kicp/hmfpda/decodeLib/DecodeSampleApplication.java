package com.example.kicp.hmfpda.decodeLib;

import android.app.Application;
import android.view.WindowManager;

/**
 * 扫码基类
 */

public class DecodeSampleApplication  extends Application {
    private WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();

    public WindowManager.LayoutParams getWindowParams() {
        return windowParams;
    }
}
