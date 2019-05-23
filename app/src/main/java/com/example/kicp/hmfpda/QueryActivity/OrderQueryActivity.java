package com.example.kicp.hmfpda.QueryActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.example.kicp.hmfpda.LoginActivity;
import com.example.kicp.hmfpda.Models.BarcodeEntity;
import com.example.kicp.hmfpda.Models.GodownMScanEntity;
import com.example.kicp.hmfpda.Models.OrderScanEntity;
import com.example.kicp.hmfpda.Models.ReturnScanEntity;
import com.example.kicp.hmfpda.R;
import com.example.kicp.hmfpda.Utils.Adialog;
import com.example.kicp.hmfpda.Utils.ApiHelper;
import com.example.kicp.hmfpda.Utils.Config.Config;
import com.example.kicp.hmfpda.Utils.Models.GodownMScanDeleteResultMsg;
import com.example.kicp.hmfpda.Utils.Models.OrderScanDeleteResultMsg;
import com.example.kicp.hmfpda.Utils.ProgersssDialog;
import com.example.kicp.hmfpda.Utils.Public;
import com.example.kicp.hmfpda.decodeLib.DecodeBaseActivity;
import com.example.kicp.hmfpda.decodeLib.DecodeSampleApplication;
import com.imscs.barcodemanager.BarcodeManager;
import com.imscs.barcodemanager.Constants;
import com.imscs.barcodemanager.ScanTouchManager;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * 订单查询
 */
@ContentView(R.layout.query_order)
public class OrderQueryActivity extends DecodeBaseActivity implements  View.OnClickListener,BarcodeManager.OnEngineStatus {

    @ViewInject(R.id.delBtn)
    private Button btnDel;         //删除
    @ViewInject(R.id.btn_Quit)
    private Button btnQuit;      //退出按钮
    @ViewInject(R.id.tbBillNo)
    private EditText tbBillNo;              //单据号
    @ViewInject(R.id.tbAgent)
    private EditText tbAgent;            //客户名称
    @ViewInject(R.id.tbProduct)
    private EditText tbProduct;             //关联产品
    @ViewInject(R.id.tbLN)
    private EditText tbLN;                  //生产批次
    @ViewInject(R.id.tbBarcode)
    private EditText tbBarcode;               //当前条码
    private Adialog mAdialog;                //提示窗口

    private String billId = "";//单据id
    private String productId = "";//产品id
    private String MainFileName = "";//主单文件
    private String EntryFileName = "";//明细文件
    private String ScanFileName = "";//扫描文件

    private int type = 0;  //0:单件删除，1:整盒删除
    private HashMap<String, OrderScanEntity> SerialDic = new HashMap<String, OrderScanEntity>();// 标码字典
    private List<OrderScanEntity> listScan = new ArrayList();  //关联箱所扫码结果列表

    private Context mContext;
    private ProgersssDialog mProgersssDialog;
    private boolean isDelOnline = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        x.view().inject(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        windowManagerParams = ((DecodeSampleApplication) getApplication()).getWindowParams();

        //initialize ScanTouch and set clicklistener
        mScanTouchManager = new ScanTouchManager(getApplicationContext(), windowManagerParams);
        mScanTouchManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                doScanInBackground();
            }
        });
        mScanTouchManager.setVisibility(View.VISIBLE);

        initView();
        mDoDecodeThread = new DoDecodeThread();
        mDoDecodeThread.start();
    }

    //清空控件
    private void SetEditTextNull()
    {
        tbBillNo.setText("");
        tbProduct.setText("");
        tbAgent.setText("");
        tbLN.setText("");
    }

    //设置单据保存文件的路径
    private void SetFilePath(String billNo)
    {
        String dir = mContext.getFilesDir().getPath().toString() + "/" + Public.gmPath + "/";
        MainFileName = dir + billNo + "" + Public.FileType;
        EntryFileName = dir + billNo + "-Billing" + Public.FileType;
        ScanFileName = dir + billNo + "-Scan" + Public.FileType;
    }

    //初始化搜索字典
    public void SerialNoDicInit()
    {
        try {
            String path = mContext.getFilesDir().getPath().toString() + "/" + Public.rdPath + "/";

            File dir = new File(path);
            File[] files = dir.listFiles(); // 该文件目录下文件全部放入数组
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    String fileName = files[i].getName();
                    if (files[i].isFile()) { // 判断是文件还是文件夹
                        String[] lineMember;
                        String line;
                        if (fileName.contains("-Scan")) {
                            FileReader fr = new FileReader(fileName);
                            BufferedReader br = new BufferedReader(fr);
                            while ((line = br.readLine()) != null) {
                                OrderScanEntity mScanEntity = new OrderScanEntity();

                                lineMember = line.split(",");
                                mScanEntity.barcode = lineMember[0];
                                mScanEntity.orderId = lineMember[1];
                                mScanEntity.orderCode = lineMember[2];
                                mScanEntity.agentId = lineMember[3];
                                mScanEntity.agentName = lineMember[4];
                                mScanEntity.productId = lineMember[5];
                                mScanEntity.productName = lineMember[6];
                                mScanEntity.LN = lineMember[7];
                                mScanEntity.scanQty = Integer.parseInt( lineMember[8] );
                                mScanEntity.CreateUserId = lineMember[9];
                                mScanEntity.CreateTime = lineMember[10];
                                SerialDic.put(lineMember[0],mScanEntity);
                            }
                        }
                    }
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    //是否在线删除数据
    private boolean isDelOnline()
    {
        AlertDialog alert = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        // 设置对话框标题
        alert = builder.setIcon(R.mipmap.ic_launcher)
                .setTitle("系统提示：")
                .setMessage("确定删除该条码并同步服务器的扫描数据吗?")
                .setCancelable(false)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isDelOnline = false;
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isDelOnline = true;
                    }
                }).create();             //创建AlertDialog对象
        alert.show();//显示对话框

        return isDelOnline;
    }

    //删除
    public void DelClick()
    {
        try {
            String barcode = this.tbBarcode.getText().toString();
            if (!SerialDic.containsKey(barcode)) {
                mAdialog.failDialog("请选择要删除的条码扫描信息！");
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            // 设置对话框标题
            AlertDialog alert = builder.setIcon(R.mipmap.ic_launcher)
                    .setTitle("系统提示：")
                    .setMessage("确定删除该条码并同步服务器的扫描数据吗?")
                    .setCancelable(false)
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    })
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).create();             //创建AlertDialog对象
            alert.show();//显示对话框

            if (LoginActivity.onlineFlag && isDelOnline()) {
                //上传删除信息
                String exceptionMsg = "";
                BarcodeEntity barcodeEntity = Public.IsBarCodeValid(barcode);
                HashMap<String, String> query = new HashMap<String, String>();

                query.put("godownMId", billId);
                query.put("serialNo", barcode);
                query.put("type", barcodeEntity.grade!= 2 ? "0" : "1");

                OrderScanDeleteResultMsg orderDel = ApiHelper.GetHttp(OrderScanDeleteResultMsg.class,
                        Config.WebApiUrl + "OrderScanDelete?", query, Config.StaffId, Config.AppSecret, true);
                orderDel.setResult();

                if (orderDel.StatusCode != 200) {
                    throw new Exception(orderDel.Info);
                }
                if (orderDel.Result == null || orderDel.Result.isEmpty()) {
                    throw new Exception("无相关删除内容！");
                }

            }

            String text = "";
            //删除盒码字典
            File file = new File(ScanFileName);
            if(file.exists()) {
                String line;
                String[] lineMember;
                FileReader fr = new FileReader(ScanFileName);
                BufferedReader br = new BufferedReader(fr);
                while ((line = br.readLine()) != null) {
                    lineMember = line.split(",");
                    //扫描的序号不在 则保存扫描数据
                    if (lineMember[0] != barcode)
                    {
                        text += line + "\r\n";
                    }
                }
            }
            //删除成功保存文本
            FileWriter fw = new FileWriter(file, false);
            BufferedWriter out = new BufferedWriter(fw);
            out.write( text );
            out.flush(); // 把缓存区内容压入文件
            out.close(); // 关闭文件

            //清空控件
            SetEditTextNull();

            SerialDic.remove(barcode);

        }catch (Exception ex){

        }


    }

    //初始化界面
    public void initView(){

        btnQuit.setOnClickListener(this);
        btnDel.setOnClickListener(this);

        setEditTextReadonly(tbBillNo);
        setEditTextReadonly(tbAgent);
        setEditTextReadonly(tbProduct);
        setEditTextReadonly(tbLN);

        mContext = this.getApplicationContext();
        SerialNoDicInit();
        tbBarcode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //TODO:回车键按下时要执行的操作
                if(keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER || keyCode == KeyEvent.KEYCODE_ENTER){
                    HandleBarcode(tbBarcode.getText().toString());
                    return true;
                }else
                {
                    return false;
                }
            }
        });
    }

    //设置EiditText为只读属性
    public void setEditTextReadonly(EditText et){
        et.setCursorVisible(false);             //设置输入框中的光标不可见
        et.setFocusable(false);                 //无焦点
        et.setFocusableInTouchMode(false);      //触摸时也得不到焦点
    }

    //扫码处理
    public void HandleBarcode(String barCode) {
        //清空控件
        SetEditTextNull();

        BarcodeEntity barcodeEntity = Public.IsBarCodeValid(barCode);
        this.tbBarcode.setText(barCode);
        if(barcodeEntity.errorMessage == null){
            mAdialog.failDialog(barcodeEntity.errorMessage);
            return;
        }

        OrderScanEntity entity =  new OrderScanEntity();
        if(SerialDic.containsKey(barcodeEntity.realBarCode)){
            entity = SerialDic.get(barcodeEntity.realBarCode);
        }else{
            mAdialog.warnDialog("未查找到相关扫描信息！");
            return;
        }
        billId = entity.orderId;
        tbBillNo.setText(entity.orderCode);
        tbAgent.setText(entity.agentName);
        tbProduct.setText(entity.productName);
        SetFilePath(barcodeEntity.realBarCode);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //删除
            case R.id.delBtn:
                DelClick();
                break;
            //退出
            case R.id.quitBtn:
                finish();
                break;
        }
    }

    private Handler ScanResultHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.DecoderReturnCode.RESULT_SCAN_SUCCESS:
                    mScanAccount++;
                    BarcodeManager.ScanResult decodeResult = (BarcodeManager.ScanResult) msg.obj;

                    HandleBarcode(decodeResult.result);

                    if (mBarcodeManager != null) {
                        mBarcodeManager.beepScanSuccess();
                    }
                    break;

                case Constants.DecoderReturnCode.RESULT_SCAN_FAIL: {
                    if (mBarcodeManager != null) {
                        mBarcodeManager.beepScanFail();
                    }
//                mDecodeResultEdit.setText("Scan failed");
                }
                break;
                case Constants.DecoderReturnCode.RESULT_DECODER_READY: {
                    // Enable all sysbology if needed
                    // try {
                    // mDecodeManager.enableSymbology(SymbologyID.SYM_ALL);   //enable all Sym
                    // } catch (RemoteException e) {
                    // // TODO Auto-generated catch block
                    // e.printStackTrace();
                    // }
                }
                break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };

    @Override
    public void onEngineReady() {
        // TODO Auto-generated method stub
        ScanResultHandler.sendEmptyMessage(Constants.DecoderReturnCode.RESULT_DECODER_READY);
    }

    @Override
    public int scanResult(boolean suc,BarcodeManager.ScanResult result) {
        // TODO Auto-generated method stub
        Message m = new Message();
        m.obj = result;
        if (suc){
            // docode successfully
            m.what = Constants.DecoderReturnCode.RESULT_SCAN_SUCCESS;
        }else{
            m.what = Constants.DecoderReturnCode.RESULT_SCAN_FAIL;

        }
        ScanResultHandler.sendMessage(m);
        return 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ( mBarcodeManager == null) {
            // initialize decodemanager
            mBarcodeManager = new BarcodeManager(this ,this);
        }
        mScanTouchManager.createScanTouch();
    }
}
