package com.example.kicp.hmfpda.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.kicp.hmfpda.LoginActivity;
import com.example.kicp.hmfpda.QueryActivity.GodownMQueryActivity;
import com.example.kicp.hmfpda.QueryActivity.OrderQueryActivity;
import com.example.kicp.hmfpda.QueryActivity.ReturnQueryActivity;
import com.example.kicp.hmfpda.R;
import com.example.kicp.hmfpda.ScanActivity.GodownMScanActivity;
import com.example.kicp.hmfpda.ScanActivity.OrderScanActivity;
import com.example.kicp.hmfpda.ScanActivity.ReturnScanActivity;
import com.example.kicp.hmfpda.Utils.ProgersssDialog;

/**
 * 数据管理页面
 */

public class DataFragment extends Fragment implements View.OnClickListener {

    private final int GodownMType = 1;  //组装入库
    private final int OrderType = 2;   //发货
    private final int ReturnType = 3;  //退货
    //单据类型
    private int BillType = 0;
    //数据管理项
    private TextView in_content;
    private TextView order_content;
    private TextView return_content;
    private LinearLayout inBtnView;
    private LinearLayout orderBtnView;
    private LinearLayout returnBtnView;
    //按钮
    private Button gmScanBtn;
    private Button gmQueryBtn;
    private Button orderScanBtn;
    private Button orderQueryBtn;
    private Button returnScanBtn;
    private Button returnQueryBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fg_datamanager, container, false);
        dataFragmentBind(view);

        return view;
    }
    /**
     * 初始化 数据管理 Fragment
     * @param view
     */
    public void dataFragmentBind(View view){
        in_content = (TextView)view.findViewById(R.id.in_channel);
        order_content = (TextView)view.findViewById(R.id.order_channel);
        return_content = (TextView)view.findViewById(R.id.return_channel);

        inBtnView = (LinearLayout)view.findViewById(R.id.in_btn_view);
        orderBtnView = (LinearLayout)view.findViewById(R.id.order_btn_view);
        returnBtnView = (LinearLayout)view.findViewById(R.id.return_btn_view);

        in_content.setOnClickListener(this);
        order_content.setOnClickListener(this);
        return_content.setOnClickListener(this);

        gmScanBtn = (Button)view.findViewById(R.id.godownmScanBtn);
        gmQueryBtn = (Button)view.findViewById(R.id.godownmQueryBtn);
        orderScanBtn = (Button)view.findViewById(R.id.orderScanBtn);
        orderQueryBtn = (Button)view.findViewById(R.id.orderQueryBtn);
        returnScanBtn = (Button)view.findViewById(R.id.returnScanBtn);
        returnQueryBtn = (Button)view.findViewById(R.id.returnQueryBtn);
        gmScanBtn.setOnClickListener(this);
        gmQueryBtn.setOnClickListener(this);
        orderScanBtn.setOnClickListener(this);
        orderQueryBtn.setOnClickListener(this);
        returnScanBtn.setOnClickListener(this);
        returnQueryBtn.setOnClickListener(this);

        MenusReset();
    }

    /**
     * 重置所有订单入库等选中状态
     */
    protected  void MenusReset(){
        in_content.setSelected(false);
        order_content.setSelected(false);
        return_content.setSelected(false);

        inBtnView.setSelected(false);
        orderBtnView.setSelected(false);
        returnBtnView.setSelected(false);

        inBtnView.setVisibility(View.INVISIBLE);
        orderBtnView.setVisibility(View.INVISIBLE);
        returnBtnView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch(v.getId()){
            // 菜单
            case R.id.in_channel:
                MenusReset();
                in_content.setSelected(true);
                inBtnView.setSelected(true);
                inBtnView.setVisibility(View.VISIBLE);
                BillType = 1;
                break;
            case R.id.order_channel:
                MenusReset();
                order_content.setSelected(true);
                orderBtnView.setSelected(true);
                orderBtnView.setVisibility(View.VISIBLE);
                BillType = 2;
                break;
            case R.id.return_channel:
                MenusReset();
                return_content.setSelected(true);
                returnBtnView.setSelected(true);
                returnBtnView.setVisibility(View.VISIBLE);
                BillType = 3;
                break;
            //按钮
            //组装入库采集
            case R.id.godownmScanBtn:
                intent = new Intent(this.getActivity(), GodownMScanActivity.class);
                startActivity(intent);//打开新的activity;
                break;
            //出库采集
            case R.id.orderScanBtn:
                intent = new Intent(this.getActivity(), OrderScanActivity.class);
                startActivity(intent);//打开新的activity;
                break;
            //退货采集
            case R.id.returnScanBtn:
                intent = new Intent(this.getActivity(), ReturnScanActivity.class);
                startActivity(intent);//打开新的activity;
                break;
            //组装入库查询
            case R.id.godownmQueryBtn:
                intent = new Intent(this.getActivity(), GodownMQueryActivity.class);
                startActivity(intent);//打开新的activity;
                break;
            //出库查询
            case R.id.orderQueryBtn:
                intent = new Intent(this.getActivity(), OrderQueryActivity.class);
                startActivity(intent);//打开新的activity;
                break;
            //退货查询
            case R.id.returnQueryBtn:
                intent = new Intent(this.getActivity(), ReturnQueryActivity.class);
                startActivity(intent);//打开新的activity;
                break;
        }
    }
}
