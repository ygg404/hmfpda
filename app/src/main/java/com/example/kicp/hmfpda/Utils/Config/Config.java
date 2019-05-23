package com.example.kicp.hmfpda.Utils.Config;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.kicp.hmfpda.LoginActivity;

import java.util.Properties;

import static android.content.Context.MODE_PRIVATE;

/**
 *  配置文件管理
 */
public class Config {
    public static String WebApiUrl = "http://hmf.uttcare.com:8181/api/WebApi/" ;
    public static int StaffId  = 8000000;
    public static String AppSecret = "5BD387065B4624136F54D9C3653F2D22C7EE9649AE9D71F73A158636E6EB36F3";

    public static void ConfigInit(Context mContext){
        //获取用户配置文件的数据
        Properties proper = ProperTies.getProperties(mContext);
        Config.WebApiUrl  = proper.getProperty("WebApiUrl");
        Config.AppSecret = proper.getProperty("AppSecret");
        Config.StaffId =  Integer.parseInt(proper.getProperty("StaffId"));
    }

    //获取配置文件 键值
    public static String getConfigValue(Context mContext , String key)
    {
        Properties proper = ProperTies.getProperties(mContext);
        return proper.getProperty(key);
    }

    //设置配置文件 键值
    public static void setConfigValue(Context mContext , String key , String value)
    {
        ProperTies.setProperties(mContext , key , value);
    }

}
