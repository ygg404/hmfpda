package com.example.kicp.hmfpda.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.example.kicp.hmfpda.R;

/**
 * 提示窗口
 */
public class Adialog{
    AlertDialog alert = null;
    AlertDialog.Builder builder;

    public Adialog(Context mContext){
        builder = new AlertDialog.Builder(mContext);
    }

    public void deleteOkDialog(){
        alert = builder.setIcon(R.mipmap.success)
                .setTitle("系统提示：")
                .setMessage("删除成功！")
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ;
                    }
                }).create();             //创建AlertDialog对象
        alert.show();                  //显示对话框
    }

    public void deleteFailDialog(){
        alert = builder.setIcon(R.mipmap.fail)
                .setTitle("系统提示：")
                .setMessage("删除失败！")
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ;
                    }
                }).create();             //创建AlertDialog对象
        alert.show();                    //显示对话框
    }

    //警告提示语
    public void warnDialog(String warnStr){
        alert = builder.setIcon(R.mipmap.warn)
                .setTitle("警告提示：")
                .setMessage(warnStr)
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ;
                    }
                }).create();             //创建AlertDialog对象
        alert.show();                    //显示对话框
    }

    //成功提示语
    public void okDialog(String okStr){
        alert = builder.setIcon(R.mipmap.success)
                .setTitle("提示：")
                .setMessage(okStr)
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ;
                    }
                }).create();             //创建AlertDialog对象
        alert.show();                    //显示对话框
    }

    //失败提示语
    public void failDialog(String failStr){
        alert = builder.setIcon(R.mipmap.fail)
                .setTitle("错误提示：")
                .setMessage(failStr)
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ;
                    }
                }).create();             //创建AlertDialog对象
        alert.show();                    //显示对话框
    }
}
