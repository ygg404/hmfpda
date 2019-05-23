package com.example.kicp.hmfpda.ScanActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.kicp.hmfpda.LoginActivity;
import com.example.kicp.hmfpda.Models.BarcodeEntity;
import com.example.kicp.hmfpda.Models.GodownMBillingEntity;
import com.example.kicp.hmfpda.Models.OrderBillingEntity;
import com.example.kicp.hmfpda.Models.OrderEntity;
import com.example.kicp.hmfpda.Models.ReturnBillingEntity;
import com.example.kicp.hmfpda.Models.ReturnEntity;
import com.example.kicp.hmfpda.R;
import com.example.kicp.hmfpda.Utils.Adialog;
import com.example.kicp.hmfpda.Utils.ApiHelper;
import com.example.kicp.hmfpda.Utils.Config.Config;
import com.example.kicp.hmfpda.Utils.Models.GodownMScanSaveResultMsg;
import com.example.kicp.hmfpda.Utils.Models.OrderBillingListResultMsg;
import com.example.kicp.hmfpda.Utils.Models.OrderListResultMsg;
import com.example.kicp.hmfpda.Utils.Models.ReturnBillingListResultMsg;
import com.example.kicp.hmfpda.Utils.Models.ReturnListResultMsg;
import com.example.kicp.hmfpda.Utils.Models.ReturnScanSaveResultMsg;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 退货扫描
 */
@ContentView(R.layout.scan_return)
public class ReturnScanActivity extends DecodeBaseActivity implements  View.OnClickListener,BarcodeManager.OnEngineStatus {

    @ViewInject(R.id.num_line)
    private EditText line_plist;              //单据号填写
    @ViewInject(R.id.num_spinner)
    private Spinner cmb_plist;              //单据号选择项
    @ViewInject(R.id.tbWarehouse)
    private EditText tbWarehouse;            //客户名称
    @ViewInject(R.id.tbProduct)
    private EditText tbProduct;             //关联产品
    @ViewInject(R.id.tbBarcode)
    private EditText tbBarcode;               //当前条码
    @ViewInject(R.id.list_view)
    private ListView mListView;
    @ViewInject(R.id.listID)
    private LinearLayout listID;
    @ViewInject(R.id.lbCurPreset)
    private TextView lbCurPreset;
    @ViewInject(R.id.lbBillPreset)
    private TextView lbBillPreset;
    @ViewInject(R.id.lbCurCount)
    private TextView lbCurCount;
    @ViewInject(R.id.lbBillCount)
    private TextView lbBillCount;
    @ViewInject(R.id.btn_Lock)
    private Button btnLock;
    @ViewInject(R.id.btn_DelBill)
    private Button btnDelBill;
    @ViewInject(R.id.btn_Upload)
    private Button btnUpload;
    @ViewInject(R.id.btn_Quit)
    private Button btnQuit;


    private String warehouseId = "";  //客户Id
    private String productId = "";//产品id
    private String billNo = "";//单号
    private String billId = "";//单据id
    private String billingId = ""; //明细单据Id
    private String MainFileName = "";//主单文件
    private String EntryFileName = "";//明细文件
    private String ScanFileName = "";//扫描文件
    private int curPreSet = 0;//当前预设
    private int curCount = 0;//当前数量
    private int billPreset = 0;//本单预设
    private int billCount = 0;//本单数量

    private List<String> barcode_exit = new ArrayList<>(); //已扫描的条码列表
    private List<String> returnNumList = new ArrayList<>();      //发货单据编号列表
    private List<ReturnEntity> returnList = new ArrayList<>();   //主单列表
    private List<ReturnBillingEntity> returnbillingList = new ArrayList<>();  //组合入库单据明细列表
    private List<HashMap<String, Object>> ProductInfo = new ArrayList<HashMap<String,Object>>();   //产品信息

    private Adialog mAdialog;                //提示窗口
    private Context mContext;
    private ProgersssDialog mProgersssDialog;
    private SimpleAdapter digAdapter;
    private ArrayAdapter<String> spinAdapter;       //单据号spinner的适配器


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
        mScanTouchManager.setVisibility(View.INVISIBLE);


        mDoDecodeThread = new DoDecodeThread();
        mDoDecodeThread.start();
        bindView();
    }

    //初始化界面
    public void bindView(){
        bLockMode = true;
        setEditTextReadonly(tbWarehouse);
        setEditTextReadonly(tbProduct);

        mContext = this.getApplicationContext();
        mAdialog = new Adialog(this);
        btnLock.setOnClickListener(this);
        btnDelBill.setOnClickListener(this);
        btnUpload.setOnClickListener(this);
        btnQuit.setOnClickListener(this);
        mScanTouchManager.setVisibility(View.VISIBLE);

        //在线方式 隐藏下拉选框
        if(LoginActivity.onlineFlag){
            cmb_plist.setVisibility(View.INVISIBLE);
            line_plist.setVisibility(View.VISIBLE);
            line_plist.setOnKeyListener(new View.OnKeyListener(){
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    //TODO:回车键按下时要执行的操作
                    if ( (keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER || keyCode == KeyEvent.KEYCODE_ENTER) && event.getAction()==KeyEvent.ACTION_DOWN ){
                        try {
                            billNo = line_plist.getText().toString();
                            mProgersssDialog = new ProgersssDialog(ReturnScanActivity.this);
                            mProgersssDialog.show();
                            new Thread(GetBillingRun).start();
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }
                        return true;
                    }else{
                        return false;
                    }
                }
            });
        }else
        {
            line_plist.setVisibility(View.INVISIBLE);
            cmb_plist.setVisibility(View.VISIBLE);
        }
    }

    //初始化单据 和 列表
    public void ListInit(){
//        //初始化单据选项列表(单据号)
//        godownMNumList = Public.GetGodownMBills(mContext);
//        spinAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, godownMNumList);
//        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        cmb_plist.setAdapter(spinAdapter);
//        cmb_plist.setOnItemSelectedListener(
//                new Spinner.OnItemSelectedListener() {
//                    @Override
//                    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//                        //ViewClear();
//                        billNo = cmb_plist.getSelectedItem().toString().trim();
//                        ChangeData(billNo);
//                    }
//                    @Override
//                    public void onNothingSelected(AdapterView<?> arg0) {
//                    }
//                });


        ProductInfo.clear();
        //创建SimpleAdapter适配器将数据绑定到item显示控件上
        digAdapter = new SimpleAdapter(this, ProductInfo, R.layout.list_item_return,
                new String[]{ "returnBillingId", "warehouseId","warehouseName","productId","productName" },
                new int[]{R.id.returnBillingId, R.id.warehouseId, R.id.warehouseName,R.id.productId,R.id.productName});
        //实现列表的显示
        mListView.setAdapter(digAdapter);
        //条目点击事件
        mListView.setOnItemClickListener(new ItemClickListener());
    }

    /**
     * 获取单据线程
     */
    Runnable GetBillingRun = new Runnable() {
        @Override
        public void run() {
            Message message = new Message();
            try {
                DownLoadBills();
                message.what = 2;
                message.obj = "获取单据成功！";
                //
                eHandler.sendMessage(message);
            }catch ( Exception ex){
                message.what = 0;
                message.obj = ex.getMessage();
                eHandler.sendMessage(message);
            }
        }
    };

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
                    lbBillCount.setText(String.valueOf(billCount));
                    lbBillPreset.setText( String.valueOf(billPreset));
                    lbCurCount.setText( String.valueOf(curCount));
                    lbCurPreset.setText( String.valueOf(curPreSet));
                    if(msg.obj != null) mAdialog.okDialog(msg.obj.toString());
                    break;
                //下载单据后更新UI
                case 2:
                    break;
                default:
                    break;
            }
        }
    };

    //下载单据（覆盖文件）
    private void DownLoadBills()throws Exception{
        //获取主单
        HashMap<String,String> query = new HashMap<String, String>();

        try {
            query.put("returnCode",billNo);
            query.put("beginDate", "");
            query.put("endDate", "");
            query.put("status", "0");

            ReturnListResultMsg Listc = ApiHelper.GetHttp(ReturnListResultMsg.class,
                    Config.WebApiUrl + "GetReturnList?", query, Config.StaffId , Config.AppSecret ,true);
            Listc.setResult();

            if(Listc.StatusCode != 200)
            {
                throw new Exception(Listc.Info);
            }
            if( Listc.Result == null || Listc.Result.isEmpty())
            {
                throw new Exception("无相关主单数据！");
            }

            //保存入库主单
            SaveGoDownDataFile(Listc.Result);

            //获取主单明细，并保存
            for( ReturnEntity reEntity : Listc.Result)
            {
                query.clear();
                query.put("returnId" , reEntity.ReturnId);
                ReturnBillingListResultMsg returnBillListc = ApiHelper.GetHttp(ReturnBillingListResultMsg.class,
                        Config.WebApiUrl + "GetReturnBillingListByReturnId?", query, Config.StaffId , Config.AppSecret ,true);
                returnBillListc.setResult();

                if(returnBillListc.StatusCode != 200)
                {
                    throw new Exception(returnBillListc.Info);
                }
                if( returnBillListc.Result == null || returnBillListc.Result.isEmpty())
                {
                    continue;
                }
                SetFilePath(reEntity.ReturnCode);
                SaveGoDownBillingDataFile(returnBillListc.Result);
            }
        }catch (Exception ex){
            throw new Exception(ex.getMessage());
        }
    }

    //设置单据保存文件的路径
    private void SetFilePath(String billNo)
    {
        String dir = mContext.getFilesDir().getPath().toString() + "/" + Public.gmPath + "/";
        MainFileName = dir + billNo + "" + Public.FileType;
        EntryFileName = dir + billNo + "-Billing" + Public.FileType;
        ScanFileName = dir + billNo + "-Scan" + Public.FileType;
    }

    //初始化dg_Grid列表
    private void dgDataInit()
    {
        ProductInfo.clear();

        for( ReturnBillingEntity entity : returnbillingList){
            if(entity.ReturnId.equals(billId)) {
                HashMap<String, Object> item = new HashMap<String, Object>();
                item.put("returnBillingId", entity.ReturnBillingId);
                item.put( "warehouseId" , entity.WarehouseId);
                item.put( "warehouseName" , entity.WarehouseName);
                item.put( "productId" , entity.ProductId);
                item.put( "productName" , entity.ProductName );
                ProductInfo.add(item);
            }
        }
        digAdapter.notifyDataSetChanged();
        listID.setVisibility(View.VISIBLE);
    }

    //获取点击事件
    private final class ItemClickListener implements AdapterView.OnItemClickListener {

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ListView listView = (ListView) parent;
            HashMap<String, Object> productData = (HashMap<String, Object>) listView.getItemAtPosition(position);

            billingId = productData.get("returnBillingId").toString();
            productId = productData.get("productId").toString();
            warehouseId = productData.get("warehouseId").toString();
            tbWarehouse.setText(productData.get("warehouseName").toString());
            tbProduct.setText( productData.get("productName").toString() );

            curPreSet = 0;   //当前预设
            billPreset = 0;  //本单预设
            for( ReturnBillingEntity billingEntity : returnbillingList)
            {
                billPreset += billingEntity.Qty;
                if (billingEntity.ReturnBillingId.equals(billingId))
                {
                    warehouseId = billingEntity.WarehouseId;
                    productId = billingEntity.ProductId;
                    curPreSet = billingEntity.Qty;
                }
            }

            //更新预设值
            lbBillPreset.setText( String.valueOf(billPreset) );
            lbCurPreset.setText(String.valueOf(curPreSet));

            listID.setVisibility(View.INVISIBLE);
        }
    }

    //单据选择变化
    private void ChangeData(String pno)
    {

        //清空控件
        try {
            //加载主表信息
            billNo = pno;
            SetFilePath(billNo);
            FileReader mfr = new FileReader(MainFileName);
            BufferedReader mbr = new BufferedReader(mfr);
            String[] strArray = mbr.readLine().split(",");
            billId = strArray[0]; //单据id

            //加载明细表信息
            returnbillingList.clear();

            File file = new File(EntryFileName);
            if(!file.exists()){
                mAdialog.warnDialog("单据明细文件不存在！");
                return;
            }
            billPreset = 0;
            FileReader fr = new FileReader(EntryFileName);
            BufferedReader br = new BufferedReader(fr);
            String line = "";
            String[] lineMembers = null;
            while ((line = br.readLine()) != null) {
                lineMembers = line.split(",");
                ReturnBillingEntity entity = new ReturnBillingEntity();
                entity.ReturnBillingId = lineMembers[0];
                entity.ReturnId = lineMembers[1];
                entity.WarehouseId = lineMembers[2];
                entity.WarehouseName = lineMembers[3];
                entity.WarehouseCode = lineMembers[4];
                entity.ProductId = lineMembers[5];
                entity.ProductName = lineMembers[6];
                entity.ProductCode = lineMembers[7];
                entity.Qty = Integer.parseInt(lineMembers[8]);
                entity.SinglePerBox = Integer.parseInt(lineMembers[9]);
                entity.SingleBoxPerBigBox = Integer.parseInt(lineMembers[10]);
                returnbillingList.add(entity);
                billPreset += entity.Qty;
            }
            br.close();
            fr.close();

            curPreSet = 0;
            lbCurPreset.setText( String.valueOf(curPreSet) );
            lbBillPreset.setText( String.valueOf(billPreset) );
            curCount = 0;
            // billCount = 0;
            lbCurCount.setText( String.valueOf(curCount) );
            lbBillCount.setText( String.valueOf(billCount) );

            dgDataInit();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    //保存主单文件
    private void SaveGoDownDataFile(List<ReturnEntity> list)
    {
        try {
            for (ReturnEntity entity : list) {
                SetFilePath(entity.ReturnCode);
                //如果文件存在，则重写内容；如果文件不存在，则创建文件
                File f = new File(MainFileName);
                FileWriter fw = new FileWriter(f, false);
                BufferedWriter out = new BufferedWriter(fw);
                out.write( entity.ReturnId + "," +
                        entity.ReturnCode + "," +
                        ( entity.ReturnDate == null? "" : formatter.format(entity.ReturnDate) )+ "," +
                        entity.Description + "," +
                        (  entity.CreateDate == null? "" : formatter.format(entity.CreateDate) ) + "," +
                        entity.CreateUserId + "," +
                        entity.CreateUserName + "," +
                        entity.Status +
                        "\r\n"); // \r\n即为换行
                out.flush(); // 把缓存区内容压入文件
                out.close(); // 关闭文件
            }
        }catch (Exception ex){
            ;
        }
    }

    // 保存明细文件
    private void SaveGoDownBillingDataFile(List<ReturnBillingEntity> list) throws Exception
    {
        try {
            for (ReturnBillingEntity entity : list) {
                //如果文件存在，则重写内容；如果文件不存在，则创建文件
                File f=new File(EntryFileName);
                FileWriter fw = new FileWriter(f, false);
                BufferedWriter out = new BufferedWriter(fw);
                out.write( entity.ReturnBillingId + "," +
                        entity.ReturnId + "," +
                        entity.WarehouseId + "," +
                        entity.WarehouseName + "," +
                        entity.WarehouseCode + "," +
                        entity.ProductId + "," +
                        entity.ProductName + "," +
                        entity.ProductCode + "," +
                        String.valueOf(entity.Qty) + "," +
                        String.valueOf(entity.SinglePerBox) + "," +
                        String.valueOf(entity.SingleBoxPerBigBox) + "," +
                        entity.CreateUserId + "," +
                        entity.CreateUserName +
                        "\r\n"); // \r\n即为换行
                out.flush(); // 把缓存区内容压入文件
                out.close(); // 最后记得关闭文件
            }
        }catch (Exception ex){
            throw new Exception(ex.getMessage());
        }
    }

    //保存扫描文件
    public void SaveScanFile(int scanQty) throws Exception{
        try {
            //如果文件存在，则重写内容；如果文件不存在，则创建文件
            File f=new File(ScanFileName);
            FileWriter fw = new FileWriter(f, false);
            BufferedWriter out = new BufferedWriter(fw);

            out.write( tbBarcode.getText().toString() + "," +
                    billId + "," +
                    billNo + "," +
                    warehouseId + "," +
                    tbWarehouse.getText().toString() + "," +
                    productId + "," +
                    tbProduct.getText().toString() + "," +
                    String.valueOf(scanQty) + "," +
                    LoginActivity.CreateUserId + "," +
                    df.format(new Date()) +
                    "\r\n"); // \r\n即为换行
            out.flush(); // 把缓存区内容压入文件
            out.close(); // 最后记得关闭文件
        }catch (Exception ex){
            throw new Exception(ex.getMessage());
        }
    }

    //删除单据
    private  void btnDelBillEvent(){
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("是否要删除该单据")
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Public.DelDataFile(MainFileName);
                        Public.DelDataFile(EntryFileName);
                        Public.DelDataFile(ScanFileName);

                        //重置预设值
                        lbBillCount.setText("0");
                        lbBillPreset.setText("0");
                        lbCurCount.setText("0");
                        lbCurPreset.setText("0");
                        mAdialog.okDialog("删除成功");
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {//添加取消
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        return;
                    }
                })
                .create();
        alertDialog.show();
    }

    //设置EiditText为只读属性
    public void setEditTextReadonly(EditText et){
        et.setCursorVisible(false);             //设置输入框中的光标不可见
        et.setFocusable(false);                 //无焦点
        et.setFocusableInTouchMode(false);      //触摸时也得不到焦点
    }

    Runnable PostReturn = new Runnable() {
        @Override
        public void run() {
            try {
                Message mess = new Message();
                int scanQty = 0;
                String barcode = tbBarcode.getText().toString().trim();
                //在线采集
                if (LoginActivity.onlineFlag) {
                    //上传扫描明细
                    String exceptionMsg = "";
                    int staffId = Integer.parseInt(LoginActivity.currentStaffId);
                    HashMap<String, String> parames = new HashMap<>();
                    parames.put("returnId", billId);
                    parames.put("serialNo", barcode);
                    parames.put("warehouseId", warehouseId);
                    parames.put("productId", productId);
                    parames.put("createUserId", LoginActivity.CreateUserId);

                    ReturnScanSaveResultMsg rtScan = ApiHelper.GetHttp(ReturnScanSaveResultMsg.class,
                            Config.WebApiUrl + "PostReturnSerialNo?", parames, Config.StaffId, Config.AppSecret, true);
                    rtScan.setResult();

                    if(!exceptionMsg.isEmpty()){
                        mess.what = 0;
                        mess.obj = exceptionMsg;
                        eHandler.sendMessage(mess);
                        return;
                    }

                    if(rtScan.StatusCode != 200){
                        mess.what = 0;
                        mess.obj = rtScan.Info;
                        eHandler.sendMessage(mess);
                        return;
                    }

                    scanQty = rtScan.Qty;

                    if (scanQty == 0) {
                        mess.what = 0;
                        mess.obj = "异常：退货数量为0！";
                        eHandler.sendMessage(mess);
                        return;
                    }
                    //保存到扫描文件
                    SaveScanFile(scanQty);
                }



                curCount += scanQty;
                billCount += scanQty;

                barcode_exit.add(barcode);//加到内存中

                if (billCount == billPreset)
                {
                    mess.what = 1;
                    mess.obj = "本单已扫描完成!";
                    eHandler.sendMessage(mess);
                    return;
                }

                if (curCount == curPreSet)
                {
                    mess.what = 1;
                    mess.obj = "当前产品扫描完成!";
                    eHandler.sendMessage(mess);
                    return;
                }

                mess.what = 1;
                mess.obj = null;
                eHandler.sendMessage(mess);
                return;
            }catch (Exception ex){
                Message mess = new Message();
                mess.what = 0;
                mess.obj = ex.getMessage();
                eHandler.sendMessage(mess);
                return;
            }

        }
    };

    //扫码处理
    public void HandleBarcode(String barCode)
    {
        if (bLockMode)
        {
            BarcodeEntity barcodeEntity = Public.IsBarCodeValid(barCode);
            tbBarcode.setText(barcodeEntity.realBarCode);
            if (!barcodeEntity.errorMessage.isEmpty()){
                mAdialog.warnDialog(barcodeEntity.errorMessage);
                return;
            }

            //检查条码是否重复
            if (barcode_exit.contains(barcodeEntity.realBarCode))
            {
                mAdialog.warnDialog("条码" + barcodeEntity.realBarCode + "已扫描，请不要重复扫描。");
                return;
            }

            mProgersssDialog = new ProgersssDialog(this.getApplicationContext());
            mProgersssDialog.setMsg("扫码上传中");
            new Thread(PostReturn).start();
        }
        else
        {

        }
    }

    //解除锁定
    private void Unlock(){
        cmb_plist.setEnabled(true);
        tbWarehouse.setEnabled(true);
        tbProduct.setEnabled(true);
        btnDelBill.setEnabled(true);
        btnQuit.setEnabled(true);
        if(!LoginActivity.onlineFlag)btnUpload.setEnabled(true);
        btnLock.setText("锁定");
        bLockMode = false;
        barcode_exit.clear();
    }

    //锁定扫描
    private void Lock(){
        cmb_plist.setEnabled(false);
        tbWarehouse.setEnabled(false);
        tbProduct.setEnabled(false);
        btnQuit.setEnabled(false);
        btnDelBill.setEnabled(false);
        btnUpload.setEnabled(false);
        tbBarcode.setFocusable(true);
        bLockMode = true;
        btnLock.setText("解锁");

    }

    //扫描锁定按钮事件
    private void LockEvent(){
        if(bLockMode){
            Unlock();
        }
        else {
            if (cmb_plist.getSelectedItem() == null )
            {
                mAdialog.warnDialog( "请选择单号！" );
                return;
            }
            if (productId.isEmpty() || tbProduct.getText().toString().isEmpty())
            {
                mAdialog.warnDialog( "出货产品不能为空，请重新选择单据！" );
                return;
            }
            if (warehouseId.isEmpty() || tbWarehouse.getText().toString().isEmpty())
            {
                mAdialog.warnDialog( "出货客户不能为空，请重新选择单据！" );
                return;
            }

            LoadBarcodes();
            if(billPreset == billCount){
                mAdialog.warnDialog("单据扫描完成!");
                return;
            }
            if(curCount == curPreSet){
                mAdialog.warnDialog("当前产品扫描完成!");
                return;
            }
            Lock();
        }
    }

    //加载已扫描条码到内存
    private void LoadBarcodes()
    {
        barcode_exit.clear();

        curCount = 0;
        billCount = 0;
        File file = new File(ScanFileName);
        if(file.exists()){
            try {
                String line;
                String[] lineMember;
                String curBarcode;
                String curProductId;
                String curWarehouseId;
                int ScanQty = 0;
                FileReader fr = new FileReader(ScanFileName);
                BufferedReader br = new BufferedReader(fr);
                while ((line = br.readLine()) != null) {
                    lineMember = line.split(",");
                    curBarcode = lineMember[0];
                    barcode_exit.add(curBarcode);//加入到已有列表
                    curWarehouseId = lineMember[3];
                    curProductId = lineMember[5];
                    ScanQty = Integer.parseInt(lineMember[7]);

                    billCount += ScanQty;
                    if(curWarehouseId == warehouseId && curProductId == productId ){
                        curCount += ScanQty;
                    }

                }
                br.close();
                fr.close();

                lbCurCount.setText( String.valueOf( curCount ) );
                lbBillCount.setText( String.valueOf(billCount) );

            }catch(Exception ex){
                ex.printStackTrace();
            }
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
    public void onClick(View v) {
        switch (v.getId()) {
            //锁定扫描
            case R.id.btn_Lock:
                LockEvent();
                break;
            //整单上传
            case R.id.btn_Upload:
                break;
            //删除单据
            case R.id.btn_DelBill:
                btnDelBillEvent();
                break;
            //退出
            case R.id.btn_Quit:
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
