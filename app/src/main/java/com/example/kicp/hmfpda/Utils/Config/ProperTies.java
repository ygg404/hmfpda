package com.example.kicp.hmfpda.Utils.Config;

import android.content.Context;
import android.util.Log;

import com.example.kicp.hmfpda.Utils.Public;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * appconfig 文件读取类
 */

public class ProperTies {
    //初始化配置文件
    public static void ConfigFileInit(Context context)
    {
        try {
            //传入路径 + 文件名
            File mFile = new File(context.getFilesDir().getPath().toString() + "/" + Public.ConfigFile );
            //判断文件是否存在，不存在则创建
            if (!mFile.exists()) {
                mFile.createNewFile();
                setProperties(context, "WebApiUrl", Config.WebApiUrl );
                setProperties(context, "StaffId", String.valueOf(Config.StaffId) );
                setProperties(context, "AppSecret", Config.AppSecret);
            }
        }catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }

    //读取配置文件
    public static Properties getProperties(Context context){
        Properties urlProps;
        Properties props = new Properties();
        try {
            ConfigFileInit(context);
            props.load(context.openFileInput(Public.ConfigFile));
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        urlProps = props;
        return urlProps;
    }

    //配置文件 设置键值
    public static void setProperties(Context context, String keyName, String keyValue) {
        Properties props = new Properties();
        try {
            props.load(context.openFileInput(Public.ConfigFile));
            props.setProperty(keyName, keyValue == null?"":keyValue);
            //FileOutputStream out = context.getAssets().openFd("appConfig").createOutputStream();
            FileOutputStream out = context.openFileOutput(Public.ConfigFile,Context.MODE_APPEND);
            // FileOutputStream out = new FileOutputStream(configPath);
            props.store(out, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}