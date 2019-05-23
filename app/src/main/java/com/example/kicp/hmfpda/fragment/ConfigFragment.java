package com.example.kicp.hmfpda.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;

import com.example.kicp.hmfpda.LoginActivity;
import com.example.kicp.hmfpda.MainActivity;
import com.example.kicp.hmfpda.R;
import com.example.kicp.hmfpda.Utils.Adialog;
import com.example.kicp.hmfpda.Utils.Config.Config;
import com.example.kicp.hmfpda.Utils.FileHelper.FileHelper;
import com.example.kicp.hmfpda.Utils.Public;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * 系统配置 TAB页面
 */

public class ConfigFragment extends Fragment {
    private TabHost tabHost;

    private Spinner fileSpinner;
    private Button delBtn;
    private Button saveBtn;
    private EditText webApiUrlText;
    private EditText staffIdText;
    private EditText appSecretText;
    private Adialog mAdialog;
    private Context mContext;

    private ArrayAdapter<String> spinAdapter;  //文件选择spinner的适配器

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fg_configmanager, container, false);

        tabHost = (TabHost)view.findViewById(android.R.id.tabhost);
        tabHost.setup();
        inflater.inflate(R.layout.tab_config_del, tabHost.getTabContentView());
        inflater.inflate(R.layout.tab_config_sys, tabHost.getTabContentView());
        // 设置标签1的标题为“删除文件”，且布局为tab_del_linear，下同理
        tabHost.addTab(tabHost.newTabSpec("").setIndicator("删除文件")
                .setContent(R.id.tab_del_linear));
        tabHost.addTab(tabHost.newTabSpec("").setIndicator("系统配置")
                .setContent(R.id.tab_sys_linear));

        mContext = this.getActivity().getApplicationContext();
        mAdialog = new Adialog(this.getActivity());
        //删除业务文件
        FileDelInit(view);
        //系统参数配置
        SysConfigInit(view);

        return view;
    }

    public void FileDelInit(View view){
        delBtn = view.findViewById(R.id.delBtn);
        fileSpinner = view.findViewById(R.id.file_spinner);
        //初始化业务文件选项
        List<String> fileType = new ArrayList<>();
        fileType.add("装箱入库");
        fileType.add("出库采集");
        fileType.add("退货采集");
        spinAdapter = new ArrayAdapter<String>(this.getActivity(),android.R.layout.simple_spinner_item, fileType);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fileSpinner.setAdapter(spinAdapter);
        final Context context = this.getActivity();
        //删除文件
        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog alert = null;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                // 设置对话框标题
                alert = builder.setIcon(R.mipmap.ic_launcher)
                        .setTitle("系统提示：")
                        .setMessage("确定要删除" + fileSpinner.getSelectedItem()  + "吗？")
                        .setCancelable(false)
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ;
                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //删除装箱入库文件
                                if(fileSpinner.getSelectedItemId() == 0){
                                    FileHelper.delAllFile(mContext, Public.gmPath);
                                }
                                //删除发货文件
                                if(fileSpinner.getSelectedItemId() == 1){
                                    FileHelper.delAllFile(mContext, Public.rdPath);
                                }
                                //删除退货文件
                                if(fileSpinner.getSelectedItemId() == 2){
                                    FileHelper.delAllFile(mContext, Public.gmPath);
                                }
                                mAdialog.okDialog("删除成功！");
                            }
                        }).create();             //创建AlertDialog对象
                alert.show();                    //显示对话框

            }
        });
    }

    public void SysConfigInit(View view){

        saveBtn = view.findViewById(R.id.saveBtn);
        webApiUrlText = view.findViewById(R.id.webApiUrl);
        staffIdText = view.findViewById(R.id.StaffId);
        appSecretText = view.findViewById(R.id.AppSecret);
        //编辑输入框初始化
        webApiUrlText.setText(Config.getConfigValue(mContext,"WebApiUrl"));
        staffIdText.setText(Config.getConfigValue(mContext,"StaffId"));
        appSecretText.setText(Config.getConfigValue(mContext,"AppSecret"));
        //保存按钮初始化
        saveBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Config.setConfigValue(mContext, "WebApiUrl", webApiUrlText.getText().toString() );
                Config.setConfigValue(mContext, "StaffId", staffIdText.getText().toString() );
                Config.setConfigValue(mContext, "AppSecret", appSecretText.getText().toString() );
                mAdialog.okDialog("保存成功！");
            }
        });
    }
}
