package com.example.kicp.hmfpda.QueryActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.kicp.hmfpda.LoginActivity;
import com.example.kicp.hmfpda.Models.BarcodeEntity;
import com.example.kicp.hmfpda.Models.GodownMScanEntity;
import com.example.kicp.hmfpda.R;
import com.example.kicp.hmfpda.ScanActivity.GodownMScanActivity;
import com.example.kicp.hmfpda.Utils.Adialog;
import com.example.kicp.hmfpda.Utils.ApiHelper;
import com.example.kicp.hmfpda.Utils.Config.Config;
import com.example.kicp.hmfpda.Utils.FileHelper.FileHelper;
import com.example.kicp.hmfpda.Utils.Models.GodownMListResultMsg;
import com.example.kicp.hmfpda.Utils.Models.GodownMScanDeleteResultMsg;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * 组装入库查询
 */
@ContentView(R.layout.query_godownm)
public class GodownMQueryActivity extends DecodeBaseActivity implements  View.OnClickListener,BarcodeManager.OnEngineStatus {

    @ViewInject(R.id.delOneBtn)
    private Button btnDelOne;         //删除单件
    @ViewInject(R.id.delAllBtn)
    private Button btnDelAll;         //删除整组
    @ViewInject(R.id.quitBtn)
    private Button btnQuit;      //退出按钮
    @ViewInject(R.id.tbBillNo)
    private EditText tbBillNo;              //单据号
    @ViewInject(R.id.tbWarehouse)
    private EditText tbWarehouse;            //仓库名称
    @ViewInject(R.id.tbProduct)
    private EditText tbProduct;             //关联产品
    @ViewInject(R.id.tbPR)
    private EditText tbPR;                  //生产日期
    @ViewInject(R.id.tbLN)
    private EditText tbLN;                  //生产批次
    @ViewInject(R.id.tbBarcode)
    private EditText tbBarcode;               //当前条码

    private String billId = "";//单据id
    private String productId = "";//产品id
    private String MainFileName = "";//主单文件
    private String EntryFileName = "";//明细文件
    private String ScanFileName = "";//扫描文件

    private int type = 0;  //0:单件删除，1:整盒删除
    private HashMap<String, GodownMScanEntity> SerialDic = new HashMap<String, GodownMScanEntity>();;// 内标码字典
    private HashMap<String, GodownMScanEntity> MSerialDic = new HashMap<String, GodownMScanEntity>();// 外标码字典
    private List<GodownMScanEntity> listScan = new ArrayList();  //关联箱所扫码结果列表

    private Context mContext;
    private ProgersssDialog mProgersssDialog;
    private View layout;
    private PopupWindow popupWindow;
    private Adialog mAdialog;                //提示窗口
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
        tbWarehouse.setText("");
        tbLN.setText("");
        tbPR.setText("");
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
            String path = mContext.getFilesDir().getPath().toString() + "/" + Public.gmPath + "/";

            File dir = new File(path);
            File[] files = dir.listFiles(); // 该文件目录下文件全部放入数组
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    String fileName = files[i].getName();
                    if (files[i].isFile()) { // 判断是文件还是文件夹
                        String[] lineMember;
                        String line;
                        if (fileName.contains("-Scan")) {
                            FileReader fr = new FileReader(path + fileName);
                            BufferedReader br = new BufferedReader(fr);
                            while ((line = br.readLine()) != null) {
                                GodownMScanEntity mScanEntity = new GodownMScanEntity();

                                lineMember = line.split(",");
                                mScanEntity.GodownMId = lineMember[0];
                                mScanEntity.GodownMCode = lineMember[1];
                                mScanEntity.GodownMBillingId = lineMember[2];
                                mScanEntity.WarehouseId = lineMember[3];
                                mScanEntity.WarehouseName = lineMember[4];
                                mScanEntity.ProductId = lineMember[5];
                                mScanEntity.ProductName = lineMember[6];
                                mScanEntity.PR = lineMember[7];
                                mScanEntity.LN = lineMember[8];
                                mScanEntity.serialArr = lineMember[9];
                                mScanEntity.mserialNo = lineMember[10];
                                mScanEntity.CreateUserId = lineMember[12];
                                mScanEntity.CreateTime = lineMember[13];
                                listScan.add(mScanEntity);
                            }
                        }
                    }
                }
            }

            //初始化 内外码字典
            for (GodownMScanEntity mScanEntity : listScan) {
                MSerialDic.put(mScanEntity.mserialNo, mScanEntity);
                String[] serialNoMember = mScanEntity.serialArr.split("\\|");
                for (String serialNo : serialNoMember) {
                    SerialDic.put(serialNo, mScanEntity);
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }


    Runnable DelAllEventThread = new Runnable() {
        @Override
        public void run() {
            Message mess = new Message();
            String barcode = tbBarcode.getText().toString().trim();
            //根据序号 删除内外标字典
            String MserialNo = "";
            String text = "";
            String[] serialArr = new String[]{};  //产品组合码
            String[] serialArrDel = new String[]{};  //要删除的产品组合码
            try {
                File file = new File(ScanFileName);
                if (file.exists()) {
                    String line;
                    String[] lineMember;
                    FileReader fr = new FileReader(ScanFileName);
                    BufferedReader br = new BufferedReader(fr);
                    while ((line = br.readLine()) != null) {
                        lineMember = line.split(",");
                        serialArr = lineMember[9].split("\\|"); //组合码
                        //序号不在内码且不是盒标 则保存扫描数据
                        if ((Arrays.binarySearch(serialArr, barcode) < 0) && lineMember[10] != barcode) {
                            //不存在
                            text += line + "\r\n";
                        } else {
                            MserialNo = lineMember[10];
                            serialArrDel = serialArr;
                            continue;
                        }
                    }
                    //在线删除数据并同步服务器
                    if (LoginActivity.onlineFlag && isDelOnline) {
                        //上传删除信息
                        String exceptionMsg = "";
                        HashMap<String, String> query = new HashMap<String, String>();

                        query.put("godownMId", billId);
                        query.put("serialNo", MserialNo);
                        query.put("type", "1");

                        GodownMScanDeleteResultMsg gmDel = ApiHelper.GetHttp(GodownMScanDeleteResultMsg.class,
                                Config.WebApiUrl + "GodownMScanDelete?", query, Config.StaffId, Config.AppSecret, true);
                        gmDel.setResult();

                        if (gmDel.StatusCode != 200) {
                            throw new Exception(gmDel.Info);
                        }
                        if (gmDel.Result == null || gmDel.Result.isEmpty()) {
                            throw new Exception("无相关删除内容！");
                        }
                    }
                    //删除盒码字典
                    MSerialDic.remove(MserialNo);
                    //删除内码字典
                    for (String num : serialArrDel) {
                        SerialDic.remove(num);
                    }
                    //删除成功保存文本
                    File f = new File(ScanFileName);
                    FileWriter fw = new FileWriter(f, false);
                    BufferedWriter out = new BufferedWriter(fw);
                    out.write(text);
                    out.flush(); // 把缓存区内容压入文件
                    out.close(); // 关闭文件

                    //清空控件
                    mess.what = 2;
                    mess.obj = "删除成功！";
                    eHandler.sendMessage(mess);
                    return;

                }
            } catch (Exception ex) {
                mess.what = 0;
                mess.obj  = ex.getMessage();
                eHandler.sendMessage(mess);
                return;
            }
        }
    };

    //删除整组
    public void DelAllEvent(){
        String barcode = tbBarcode.getText().toString().trim();
        if ((SerialDic.get(barcode).toString().isEmpty() && MSerialDic.get(barcode).toString().isEmpty()) || billId.isEmpty()) {
            mAdialog.warnDialog("请选择要删除的条码扫描信息！");
            return;
        }
        AlertDialog alert = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        // 设置对话框标题
        alert = builder.setIcon(R.mipmap.ic_launcher)
                    .setTitle("系统提示：")
                    .setMessage("确定删除该条码相关扫描数据吗?")
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
                            AlertDialog alert = null;
                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            // 设置对话框标题
                            alert = builder.setIcon(R.mipmap.ic_launcher)
                                    .setTitle("系统提示：")
                                    .setMessage("是否在线同步删除该条码相关扫描数据吗?")
                                    .setCancelable(false)
                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            isDelOnline = false;
                                            mProgersssDialog = new ProgersssDialog(GodownMQueryActivity.this);
                                            mProgersssDialog.setMsg("删除中");
                                            new Thread(DelAllEventThread).start();
                                            return;
                                        }
                                    })
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            isDelOnline = true;
                                            mProgersssDialog = new ProgersssDialog(GodownMQueryActivity.this);
                                            mProgersssDialog.setMsg("删除中");
                                            new Thread(DelAllEventThread).start();
                                            return;
                                        }
                                    }).create();             //创建AlertDialog对象
                            alert.show();//显示对话框;
                        }
                    }).create();             //创建AlertDialog对象
        alert.show();                    //显示对话框

    }

    Runnable DelOneThread = new Runnable() {
        @Override
        public void run() {
            Message mess = new Message();
            String barcode = tbBarcode.getText().toString().trim();
            //在线删除数据同步服务器
            if (LoginActivity.onlineFlag && isDelOnline) {
                try {
                    //上传删除信息
                    String exceptionMsg = "";
                    HashMap<String, String> query = new HashMap<String, String>();

                    query.put("godownMId", billId);
                    query.put("serialNo", barcode);
                    query.put("type", "0");

                    GodownMScanDeleteResultMsg gmDel = ApiHelper.GetHttp(GodownMScanDeleteResultMsg.class,
                            Config.WebApiUrl + "GodownMScanDelete?", query, Config.StaffId, Config.AppSecret, true);
                    gmDel.setResult();

                    if (gmDel.StatusCode != 200) {
                        throw new Exception(gmDel.Info);
                    }
                    if (gmDel.Result == null || gmDel.Result.isEmpty()) {
                        throw new Exception("无相关删除内容！");
                    }

                    //删除成功修改保存scan文件
                    String text = "";
                    File file = new File(ScanFileName);
                    if (file.exists()) {
                        String line;
                        String[] lineMember;
                        String[] serialArr;  //产品组合码
                        FileReader fr = new FileReader(ScanFileName);
                        BufferedReader br = new BufferedReader(fr);
                        while ((line = br.readLine()) != null) {
                            lineMember = line.split(",");
                            serialArr = lineMember[9].split("\\|"); //组合码
                            //组合内标含有要删除的单件
                            if (Arrays.binarySearch(serialArr, barcode) < 0) {
                                //不存在
                                line = line + "\r\n";
                            } else {
                                lineMember[9] = lineMember[9].replace(barcode, "");
                                lineMember[9] = lineMember[9].replace("||", "|");
                                //删除字典数据
                                //删除内标字典
                                GodownMScanEntity entity = SerialDic.get(barcode);
                                String mserial = entity.mserialNo;
                                SerialDic.remove(barcode);
                                //删除箱码的内标条码
                                entity = MSerialDic.get(mserial);
                                entity.serialArr = entity.serialArr.replace(barcode, "");
                                entity.serialArr = entity.serialArr.replace("||", "|");
                                if (lineMember[9].length() != 0) {
                                    if (entity.serialArr.substring(0, 1).equals("|")) {
                                        entity.serialArr = entity.serialArr.substring(1, lineMember[9].length() - 1);
                                    }
                                    if (entity.serialArr.substring(entity.serialArr.length() - 1, entity.serialArr.length()).equals("|")) {
                                        entity.serialArr = entity.serialArr.substring(0, entity.serialArr.length() - 1);
                                    }
                                    MSerialDic.put(mserial, entity);
                                } else {
                                    //内标全被删除，则直接删除外码字典
                                    MSerialDic.remove(mserial);
                                }

                                if (lineMember[9].length() != 0) {
                                    if (lineMember[9].substring(0, 1).equals("|")) {
                                        lineMember[9] = lineMember[9].substring(1, lineMember[9].length() - 1);
                                    }

                                    if (lineMember[9].substring(lineMember[9].length() - 1, lineMember[9].length()).equals("|")) {
                                        lineMember[9] = lineMember[9].substring(0, lineMember[9].length() - 1);
                                    }

                                    line = "";
                                    for (String lmember : lineMember) {
                                        line += lmember + ",";
                                    }
                                    line = line.substring(0, line.length() - 1) + "\r\n";

                                } else {
                                    line = "";
                                }
                            }
                            text += line;
                        }
                        br.close();
                        fr.close();

                        //定义一个写入流，将值写入到里面去
                        File f = new File(ScanFileName);
                        FileWriter fw = new FileWriter(f, false);
                        BufferedWriter out = new BufferedWriter(fw);
                        out.write(text);
                        out.flush(); // 把缓存区内容压入文件
                        out.close(); // 关闭文件

                        mess.what = 2;
                        mess.obj  = "删除成功！";
                        eHandler.sendMessage(mess);
                        return;
                    }
                } catch (Exception ex) {
                    mess.what = 0;
                    mess.obj = ex.getMessage();
                    eHandler.sendMessage(mess);
                    return;
                }
            }

        }
    };

    //单件删除
    public void DelOneEvent(){
        String barcode = tbBarcode.getText().toString().trim();
        if ( (SerialDic.get(barcode).toString().isEmpty() && MSerialDic.get(barcode).toString().isEmpty()) || billId.isEmpty()) {
            mAdialog.warnDialog("请选择要删除的条码扫描信息！");
            return;
        }

        AlertDialog alert = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        // 设置对话框标题
        alert = builder.setIcon(R.mipmap.ic_launcher)
                .setTitle("系统提示：")
                .setMessage("确定删除该条码相关扫描数据吗?")
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
                        AlertDialog alert = null;
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        // 设置对话框标题
                        alert = builder.setIcon(R.mipmap.ic_launcher)
                                .setTitle("系统提示：")
                                .setMessage("是否在线同步删除该条码相关扫描数据吗?")
                                .setCancelable(false)
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        isDelOnline = false;
                                        mProgersssDialog = new ProgersssDialog(GodownMQueryActivity.this);
                                        mProgersssDialog.setMsg("删除中");
                                        new Thread(DelOneThread).start();
                                        return;
                                    }
                                })
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        isDelOnline = true;
                                        mProgersssDialog = new ProgersssDialog(GodownMQueryActivity.this);
                                        mProgersssDialog.setMsg("删除中");
                                        new Thread(DelOneThread).start();
                                        return;
                                    }
                                }).create();             //创建AlertDialog对象
                        alert.show();//显示对话框
                    }
                }).create();             //创建AlertDialog对象
        alert.show();                    //显示对话框

    };

    //初始化界面
    public void initView(){
        btnQuit.setOnClickListener(this);
        btnDelAll.setOnClickListener(this);
        btnDelOne.setOnClickListener(this);

        setEditTextReadonly(tbBillNo);
        setEditTextReadonly(tbWarehouse);
        setEditTextReadonly(tbProduct);
        setEditTextReadonly(tbPR);
        setEditTextReadonly(tbLN);

        mAdialog = new Adialog(this);
        mContext = GodownMQueryActivity.this;
        SerialNoDicInit();
        tbBarcode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction()==KeyEvent.ACTION_DOWN){
                    //TODO:返回键按下时要执行的操作
                    HandleBarcode( tbBarcode.getText().toString().trim());
                    return true;
                }
                return false;
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
    public void HandleBarcode(String barCode)
    {
        //清空控件
        SetEditTextNull();

        BarcodeEntity barcodeEntity = Public.IsBarCodeValid(barCode);
        tbBarcode.setText( barcodeEntity.realBarCode );
        if (!barcodeEntity.errorMessage.isEmpty()){
            mAdialog.warnDialog(barcodeEntity.errorMessage);
            return;
        }

        GodownMScanEntity entity = new GodownMScanEntity();

        if (MSerialDic.containsKey(barcodeEntity.realBarCode))
        {
            entity = MSerialDic.get(barcodeEntity.realBarCode);
            btnDelOne.setEnabled(false);
        }
        else if (SerialDic.containsKey(barcodeEntity.realBarCode))
        {
            entity = SerialDic.get(barcodeEntity.realBarCode);
            btnDelOne.setEnabled(true);
        }
        else
        {
            mAdialog.warnDialog("未查找到相关扫描信息！");
            return;
        }

        billId = entity.GodownMId;
        this.tbBillNo.setText( entity.GodownMCode );
        this.tbWarehouse.setText(  entity.WarehouseName);
        this.tbProduct.setText(  entity.ProductName);
        this.tbLN.setText(  entity.LN);
        this.tbPR.setText( entity.PR);
        SetFilePath(entity.GodownMCode);
    }

    Handler eHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //登录加载dialog关闭
            mProgersssDialog.cancel();
            switch (msg.what) {
                //错误提示
                case 0:
                    //do something,refresh UI;
                    mAdialog.failDialog( msg.obj.toString() );
                    break;
                //成功提示
                case 1:
                    //do something,refresh UI;
                    mAdialog.okDialog( msg.obj.toString() );
                    break;
                //删除成功提示
                case 2:
                    //清空控件
                    SetEditTextNull();
                    mAdialog.okDialog(msg.obj.toString());
                    break;
                default:
                    break;
            }
        }
    };

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
    public void onClick(View v) {
        switch (v.getId()) {
            //删除整组
            case R.id.delAllBtn:
                DelAllEvent();
                break;
            //删除单件
            case R.id.delOneBtn:
                DelOneEvent();
                break;
            //退出
            case R.id.quitBtn:
                finish();
                break;
        }
    }

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
