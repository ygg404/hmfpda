package com.example.kicp.hmfpda.Utils.FileHelper;

import android.content.Context;

import com.example.kicp.hmfpda.Utils.Public;

import java.io.File;

/**
 * 文件
 */
public class FileHelper {
    //初始化文件夹的目录
    public static void FileInit(Context mContext)throws Exception{
        try {
            File path = new File(mContext.getFilesDir().getPath().toString() + "/" + Public.BasePath);
            if (!path.exists()) {
                path.mkdir();
            }
            path = new File(mContext.getFilesDir().getPath().toString() + "/" + Public.gmPath);
            if (!path.exists()) {
                path.mkdir();
            }
            path = new File(mContext.getFilesDir().getPath().toString() + "/" + Public.rdPath);
            if (!path.exists()) {
                path.mkdir();
            }
            path = new File(mContext.getFilesDir().getPath().toString() + "/" + Public.rtPath);
            if (!path.exists()) {
                path.mkdir();
            }
        }catch (Exception ex){
            throw new Exception(ex);
        }
    }

    //删除目录下所有文件
    public static void delAllFile(Context mContext, String path)
    {
        File dir = new File(mContext.getFilesDir().getPath().toString() + "/" + path);
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
//            else if (file.isDirectory())
//                deleteDirWihtFile(file); // 递规的方式删除文件夹
        }

    }

}
