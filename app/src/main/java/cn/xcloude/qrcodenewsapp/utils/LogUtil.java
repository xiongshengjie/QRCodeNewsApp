package cn.xcloude.qrcodenewsapp.utils;

import android.util.Log;

/**
 * Created by Administrator on 2018/3/1.
 */

public class LogUtil {

    private static int VERBOSE = 1;
    private static int DEBUG = 2;
    private static int INFO = 3;
    private static int WARN = 4;
    private static int ERROR = 5;
    private static int NOTHING = 6;
    public static int level = DEBUG;

    public static void v(String tag,String msg){
        if(level <= VERBOSE){
            Log.v(tag,msg);
        }
    }

    public static void d(String tag,String msg){
        if(level <= DEBUG){
            Log.d(tag,msg);
        }
    }

    public static void I(String tag,String msg){
        if(level <= INFO){
            Log.i(tag,msg);
        }
    }

    public static void w(String tag,String msg){
        if(level <= WARN){
            Log.w(tag,msg);
        }
    }

    public static void e(String tag,String msg){
        if(level <= ERROR){
            Log.e(tag,msg);
        }
    }
}
