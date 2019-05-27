package com.example.kicp.hmfpda.ScanActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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
import com.example.kicp.hmfpda.Models.GodownMEntity;
import com.example.kicp.hmfpda.R;
import com.example.kicp.hmfpda.Utils.Adialog;
import com.example.kicp.hmfpda.Utils.ApiHelper;
import com.example.kicp.hmfpda.Utils.Config.Config;
import com.example.kicp.hmfpda.Utils.Models.GodownMBillingListResultMsg;
import com.example.kicp.hmfpda.Utils.Models.GodownMListResultMsg;
import com.example.kicp.hmfpda.Utils.Models.GodownMScanSaveResultMsg;
import com.example.kicp.hmfpda.Utils.Models.OrderScanDeleteResultMsg;
import com.example.kicp.hmfpda.Utils.ProgersssDialog;
import com.example.kicp.hmfpda.Utils.Public;
import com.example.kicp.hmfpda.decodeLib.DecodeBaseActivity;
import com.example.kicp.hmfpda.decodeLib.DecodeSampleApplication;
import com.imscs.barcodemanager.BarcodeManager;
import com.imscs.barcodemanager.Constants;
import com.imscs.barcodemanager.ScanTouchManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InterruptedIOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

/**
 * 组装入库 采集
 */
@ContentView(R.layout.scan_godownm)
public class GodownMScanActivity extends DecodeBaseActivity implements  View.OnClickListener,BarcodeManager.OnEngineStatus {

    @ViewInject(R.id.num_spinner)
    private Spinner cmb_plist;              //单据号选择项
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
    @ViewInject(R.id.curGroup)
    private TextView lbCurGroup;
    @ViewInject(R.id.btn_Lock)
    private Button btnLock;
    @ViewInject(R.id.btn_DelBill)
    private Button btnDelBill;
    @ViewInject(R.id.btn_Upload)
    private Button btnUpload;
    @ViewInject(R.id.btn_Quit)
    private Button btnQuit;

    private Adialog mAdialog;                //提示窗口
    private Context mContext;
    private ProgersssDialog mProgersssDialog;
    private SimpleAdapter digAdapter;
    private ArrayAdapter<String> spinAdapter;       //单据号spinner的适配器
    private String warehouseId = "";  //仓库Id
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

    private int CurGroupNo = 0; //当前组号
    private int CurProductCount = 0;//当前组添加产品个数
    private int CurGroupXCount = 0;//当期添加的组个数
    private int CurSerailNoAddType = 1; //1:产品，2:箱
    private List<String> CurProductNoArr = new ArrayList<>();   //序号组合
    private List<String> barcode_exit = new ArrayList<>(); //已扫描的条码列表
    private List<String> godownMNumList = new ArrayList<>();      //关联箱单据编号列表
    private List<GodownMEntity> godownMList = new ArrayList<>();   //主单列表
    private List<GodownMBillingEntity> godownMbillingList = new ArrayList<>();  //组合入库单据明细列表
    private List<HashMap<String, Object>> ProductInfo = new ArrayList<HashMap<String,Object>>();   //产品信息

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
        bLockMode = false;
        mContext = this.getApplicationContext();
        mAdialog = new Adialog(this);

        setEditTextReadonly(tbProduct);
        setEditTextReadonly(tbWarehouse);
        setEditTextReadonly(tbPR);
        setEditTextReadonly(tbLN);
        mScanTouchManager.setVisibility(View.VISIBLE);

        btnLock.setOnClickListener(this);
        btnDelBill.setOnClickListener(this);
        btnUpload.setOnClickListener(this);
        btnQuit.setOnClickListener(this);
        tbBarcode.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //TODO:回车键按下时要执行的操作
                if ( (keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER || keyCode == KeyEvent.KEYCODE_ENTER) && event.getAction()==KeyEvent.ACTION_DOWN ){
                    HandleBarcode(tbBarcode.getText().toString());
                    return true;
                }else{
                    return false;
                }
            }
        });

        mProgersssDialog = new ProgersssDialog(GodownMScanActivity.this);
        mProgersssDialog.setMsg("获取单据中");
        new Thread(GetBillingRun).start();

    }

    //初始化单据 和 列表
    public void ListInit(){
        //初始化单据选项列表(单据号)
        godownMNumList = Public.GetGodownMBills(mContext);
        spinAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, godownMNumList);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cmb_plist.setAdapter(spinAdapter);
        cmb_plist.setOnItemSelectedListener(
                new Spinner.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        //ViewClear();
                        billNo = cmb_plist.getSelectedItem().toString().trim();
                        ChangeData(billNo);
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });


        ProductInfo.clear();
        //创建SimpleAdapter适配器将数据绑定到item显示控件上
        digAdapter = new SimpleAdapter(this, ProductInfo, R.layout.list_item_gm,
                new String[]{ "godownMBillingId", "warehouseId","warehouseName","productId","productName","pr","ln" },
                new int[]{R.id.godownMBillingId, R.id.warehouseId, R.id.warehouseName,R.id.productId,R.id.productName,R.id.pr,R.id.ln});
        //实现列表的显示
        mListView.setAdapter(digAdapter);
        //条目点击事件
        mListView.setOnItemClickListener(new ItemClickListener());
    }


    //设置EiditText为只读属性
    public void setEditTextReadonly(EditText et){
        et.setCursorVisible(false);             //设置输入框中的光标不可见
        et.setFocusable(false);                 //无焦点
        et.setFocusableInTouchMode(false);      //触摸时也得不到焦点
    }

    //设置单据保存文件的路径
    private void SetFilePath(String billNo)
    {
        String dir = mContext.getFilesDir().getPath().toString() + "/" + Public.gmPath + "/";
        MainFileName = dir + billNo + "" + Public.FileType;
        EntryFileName = dir + billNo + "-Billing" + Public.FileType;
        ScanFileName = dir + billNo + "-Scan" + Public.FileType;
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
            godownMbillingList.clear();

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
                GodownMBillingEntity entity = new GodownMBillingEntity();
                entity.GodownMBillingId = lineMembers[0];
                entity.GodownMId = lineMembers[1];
                entity.WarehouseId = lineMembers[2];
                entity.WarehouseName = lineMembers[3];
                entity.ProductId = lineMembers[4];
                entity.ProductName = lineMembers[5];
                entity.Qty = Integer.parseInt(lineMembers[6]);
                entity.LN = lineMembers[7];
                entity.PR = lineMembers[8].isEmpty() ? null : formatter.parse(lineMembers[8]);
                entity.SinglePerBox = Integer.parseInt(lineMembers[9]);
                godownMbillingList.add(entity);
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
                String curBarcode;      //X箱码
                String[] curSerialArr;  //产品组合码
                String curProductId;
                String curWarehouseId;
                String curPR;
                String curLN;
                CurGroupNo = 0;
                FileReader fr = new FileReader(ScanFileName);
                BufferedReader br = new BufferedReader(fr);
                while ((line = br.readLine()) != null) {
                    lineMember = line.split(",");
                    curBarcode = lineMember[10];
                    barcode_exit.add(curBarcode);//加入到已有列表
                    curSerialArr = lineMember[9].split("\\|");
                    for (String arr : curSerialArr)
                    {
                        if (!arr.isEmpty())
                            barcode_exit.add(arr);
                    }

                    curWarehouseId = lineMember[3];
                    curProductId = lineMember[5];
                    curPR = lineMember[7];
                    curLN = lineMember[8];

                    billCount += curSerialArr.length;
                    if (curProductId.equals(productId) &&
                            curWarehouseId.equals(warehouseId) &&
                            curPR.equals(tbPR.getText().toString()) &&
                            curLN.equals(tbLN.getText().toString()) )
                    {
                        curCount += curSerialArr.length;
                    }
                    CurGroupNo++; //记录组数
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

    //初始化dg_Grid列表
    private void dgDataInit()
    {
        ProductInfo.clear();

        for( GodownMBillingEntity entity : godownMbillingList){
            if(entity.GodownMId.equals(billId)) {
                HashMap<String, Object> item = new HashMap<String, Object>();
                item.put("godownMBillingId", entity.GodownMBillingId);
                item.put( "warehouseId" , entity.WarehouseId);
                item.put( "warehouseName" , entity.WarehouseName);
                item.put( "productId" , entity.ProductId);
                item.put( "productName" , entity.ProductName );
                item.put("pr" , entity.PR == null? "" : formatter.format(entity.PR) );
                item.put("ln" , entity.LN);
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

            billingId = productData.get("godownMBillingId").toString();
            productId = productData.get("productId").toString();
            warehouseId = productData.get("warehouseId").toString();
            tbWarehouse.setText(productData.get("warehouseName").toString());
            tbProduct.setText( productData.get("productName").toString() );
            tbLN.setText( productData.get("ln").toString() );
            tbPR.setText( productData.get("pr").toString() );

            curPreSet = 0;   //当前预设
            billPreset = 0;  //本单预设
            for( GodownMBillingEntity billingEntity : godownMbillingList)
            {
                billPreset += billingEntity.Qty;
                if (billingEntity.GodownMBillingId.equals(billingId))
                {
                    CurGroupXCount = billingEntity.SinglePerBox;
                    warehouseId = billingEntity.WarehouseId;
                    productId = billingEntity.ProductId;
                    curPreSet = billingEntity.Qty;
                    lbCurGroup.setText( String.valueOf(CurProductCount) + "/" + String.valueOf(CurGroupXCount));
                }
            }

            //更新预设值
            lbBillPreset.setText( String.valueOf(billPreset) );
            lbCurPreset.setText(String.valueOf(curPreSet));

            listID.setVisibility(View.INVISIBLE);
        }
    }

    //下载单据（覆盖文件）
    private void DownLoadBills()throws Exception{
        //获取主单
        HashMap<String,String> query = new HashMap<String, String>();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        try {
            query.put("godownMCode","");
            query.put("beginDate", "");
            query.put("endDate", "");
            query.put("status", "0");

            GodownMListResultMsg gmListc = ApiHelper.GetHttp(GodownMListResultMsg.class,
                    Config.WebApiUrl + "GetGodownMList?", query, Config.StaffId , Config.AppSecret ,true);

            if(gmListc.StatusCode != 200)
            {
                throw new Exception(gmListc.Info);
            }
            if( gmListc.Result() == null || gmListc.Result().isEmpty())
            {
                throw new Exception("无相关主单数据！");
            }

            //保存入库主单
            SaveGoDownDataFile(gmListc.Result());

            //获取主单明细，并保存
            for( GodownMEntity gmEntity : gmListc.Result())
            {
                query.clear();
                query.put("godownMId" , gmEntity.GodownMId);
                GodownMBillingListResultMsg godBillListc = ApiHelper.GetHttp(GodownMBillingListResultMsg.class,
                        Config.WebApiUrl + "GetGodownMBillingListByGodownMId?", query, Config.StaffId , Config.AppSecret ,true);

                if(godBillListc.StatusCode != 200)
                {
                    throw new Exception(godBillListc.Info);
                }
                if( godBillListc.Result() == null || godBillListc.Result().isEmpty())
                {
                    continue;
                }
                SetFilePath(gmEntity.GodownMCode);
                SaveGoDownBillingDataFile(godBillListc.Result());
            }
        }catch (Exception ex){
            throw new Exception(ex.getMessage());
        }
    }

    //保存主单文件
    private void SaveGoDownDataFile(List<GodownMEntity> list)
    {
        try {
            for (GodownMEntity entity : list) {
                SetFilePath(entity.GodownMCode);
                //如果文件存在，则重写内容；如果文件不存在，则创建文件
                File f = new File(MainFileName);
                FileWriter fw = new FileWriter(f, false);
                BufferedWriter out = new BufferedWriter(fw);
                out.write( entity.GodownMId + "," +
                            entity.GodownMCode + "," +
                        ( entity.GodownMDate == null? "" : formatter.format(entity.GodownMDate) )+ "," +
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
    private void SaveGoDownBillingDataFile(List<GodownMBillingEntity> list) throws Exception
    {
        try {
            for (GodownMBillingEntity entity : list) {
                //如果文件存在，则重写内容；如果文件不存在，则创建文件
                File f=new File(EntryFileName);
                FileWriter fw = new FileWriter(f, false);
                BufferedWriter out = new BufferedWriter(fw);
                out.write( entity.GodownMBillingId + "," +
                        entity.GodownMId + "," +
                        entity.WarehouseId + "," +
                        entity.WarehouseName + "," +
                        entity.ProductId + "," +
                        entity.ProductName + "," +
                        entity.Qty + "," +
                        entity.LN + "," +
                        (entity.PR == null? "" : formatter.format(entity.PR)) + "," +
                        entity.SinglePerBox +
                        "\r\n"); // \r\n即为换行
                out.flush(); // 把缓存区内容压入文件
                out.close(); // 最后记得关闭文件
            }
        }catch (Exception ex){
            throw new Exception(ex.getMessage());
        }
    }

    //保存扫描文件
    public void SaveScanFile() throws Exception{
        try {
            CurGroupNo ++;
            //如果文件存在，则添加内容；如果文件不存在，则创建文件
            File f=new File(ScanFileName);
            FileWriter fw = new FileWriter(f, true);
            BufferedWriter out = new BufferedWriter(fw);
            String pr = tbPR.getText().toString();
            String ln = tbLN.getText().toString();

            String curNoArr = "";
            for( String attr : CurProductNoArr){
                curNoArr = curNoArr + attr + "|";
            }
            curNoArr = curNoArr.substring(0, curNoArr.length() - 1 );
            out.write( billId + "," +
                    billNo + "," +
                    billingId + "," +
                    warehouseId + "," +
                    tbWarehouse.getText().toString() + "," +
                    productId + "," +
                    tbProduct.getText().toString() + "," +
                    pr + "," +
                    ln + "," +
                    curNoArr + "," +
                    tbBarcode.getText().toString().trim() + "," +
                    String.format("%05d", CurGroupNo) + "," +
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
                        lbCurGroup.setText("0");

                        ProductInfo.clear();
                        digAdapter.notifyDataSetChanged();
                        ListInit();
                        listID.setVisibility(View.VISIBLE);
                        btnLock.setEnabled(false);

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

    //解除锁定
    private void Unlock(){
        cmb_plist.setEnabled(true);
        tbWarehouse.setEnabled(true);
        tbProduct.setEnabled(true);
        tbPR.setEnabled(true);
        tbLN.setEnabled(true);
        btnDelBill.setEnabled(true);
        btnQuit.setEnabled(true);
        if(!LoginActivity.onlineFlag)btnUpload.setEnabled(true);
        btnLock.setText("锁定");
        bLockMode = false;
        barcode_exit.clear();
        CurGroupNo = 0; //组号重置

    }

    //锁定扫描
    private void Lock(){
        cmb_plist.setEnabled(false);
        tbWarehouse.setEnabled(false);
        tbProduct.setEnabled(false);
        tbPR.setEnabled(false);
        tbLN.setEnabled(false);
        btnQuit.setEnabled(false);
        btnDelBill.setEnabled(false);
        btnUpload.setEnabled(false);
        tbBarcode.setFocusable(true);
        btnLock.setText("解锁");
        bLockMode = true;
    }

    //扫描锁定按钮事件
    private void LockEvent(){
        if(bLockMode){
            if(CurProductCount != 0){

                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle("提示")
                        .setMessage("未完成一组的扫描是否确定要解锁")
                        .setIcon(R.mipmap.ic_launcher)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ;
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

                CurProductCount = 0;
                CurSerailNoAddType = 1;
                CurProductNoArr.clear();
                lbCurGroup.setText( String.valueOf(CurProductCount) + "/" + String.valueOf(CurGroupXCount) );
            }
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
                mAdialog.warnDialog( "请选择产品！" );
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

    /**
     * 提交扫描数据
     */
    Runnable postScanRun = new Runnable() {
        @Override
        public void run() {
            Message message = new Message();
            int qty = 0;
            try{
                //上传扫描明细
                String exceptionMsg = "";
                String realBarcode = tbBarcode.getText().toString();
                HashMap<String, String> parames = new HashMap<String, String>();
                parames.put("godownXId", billId);
                parames.put("warehouseId", warehouseId);
                parames.put("productId", productId);
                parames.put("ln", tbLN.getText().toString());
                parames.put("pr", tbPR.getText().toString());
                parames.put("serialNo", realBarcode );
                parames.put("curSerailNoAddType", String.valueOf(CurSerailNoAddType));
                parames.put("curProductCount", String.valueOf(CurProductCount));

                GodownMScanSaveResultMsg gmScan = ApiHelper.GetHttp(GodownMScanSaveResultMsg.class,
                        Config.WebApiUrl + "CheckGodownMSerialNo?", parames, Config.StaffId, Config.AppSecret, true);


                if(gmScan.StatusCode != 200){
                    if(gmScan.Info.equals("OK1")){
                        CurProductCount ++ ;
                        CurProductNoArr.add(realBarcode);
                        barcode_exit.add(realBarcode);
                        message.what = 1;
                        message.obj = null;
                        eHandler.sendMessage(message);
                        return;
                    }
                    else if(gmScan.Info.equals("OK2")){
                        CurProductCount++;
                        CurSerailNoAddType = 2; //录入盒标
                        CurProductNoArr.add(realBarcode);
                        barcode_exit.add(realBarcode);
                        message.what = 1;
                        message.obj = null;
                        eHandler.sendMessage(message);
                        return;
                    }else if(gmScan.Info.equals("OK3")){
                        String serialNo = "";
                        for( String attr : CurProductNoArr){
                            serialNo = serialNo + attr + ",";
                        }
                        String serialNoX = realBarcode;
                        HashMap<String, String> sparames = new HashMap<>();
                        sparames.put("godownMId", billId);
                        sparames.put("warehouseId", warehouseId);
                        sparames.put("productId", productId);
                        sparames.put("ln", tbLN.getText().toString());
                        sparames.put("pr", tbPR.getText().toString());
                        sparames.put("serialNo", serialNo);
                        sparames.put("mserialNo", serialNoX);
                        sparames.put("createUserId", LoginActivity.CreateUserId);
                        GodownMScanSaveResultMsg sgmScan = ApiHelper.GetHttp(GodownMScanSaveResultMsg.class,
                                Config.WebApiUrl + "PostGodownMSerialNo?", sparames, Config.StaffId, Config.AppSecret, true);

                        if( sgmScan.StatusCode != 200 ){
                            throw new Exception(sgmScan.Info);
                        }

                        barcode_exit.add(realBarcode);
                        CurProductCount = 0;
                        CurSerailNoAddType = 1;
                        qty = sgmScan.Qty();

                        SaveScanFile();

                        curCount += qty;
                        billCount += qty;
                        CurProductNoArr.clear();
                        if (billCount == billPreset)
                        {
                            message.what = 1;
                            message.obj = "本单已扫描完成!";
                            eHandler.sendMessage(message);
                            return;
                        }
                        if (curCount == curPreSet)
                        {
                            message.what = 1;
                            message.obj = "当前产品扫描完成!";
                            eHandler.sendMessage(message);
                            return;
                        }
                        message.what = 1;
                        message.obj = null;
                        eHandler.sendMessage(message);
                    }
                    else{
                        message.what = 0;
                        message.obj = gmScan.Info;
                        eHandler.sendMessage(message);
                        return;
                    }
                }

                if (qty == 0)
                {
                    message.what = 0;
                    message.obj = "异常：关联箱数量为0！";
                    eHandler.sendMessage(message);
                    return;
                }


            }catch (Exception ex){
                message.what = 0;
                message.obj = ex.getMessage();
                eHandler.sendMessage(message);
                return;
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
                    lbCurGroup.setText( String.valueOf(CurProductCount) + "/" + String.valueOf(CurGroupXCount));
                    if(msg.obj != null) mAdialog.okDialog(msg.obj.toString());
                    break;
                //下载单据后更新UI
                case 2:
                    ListInit();
                    break;
                default:
                    break;
            }
        }
    };


    //扫码处理
    public void HandleBarcode(String barCode)
    {
        if (bLockMode)
        {
            try {
                BarcodeEntity barcodeEntity = Public.IsBarCodeValid(barCode);
                tbBarcode.setText(barcodeEntity.realBarCode);
                if (!barcodeEntity.errorMessage.isEmpty()) {
                    mAdialog.warnDialog(barcodeEntity.errorMessage);
                    return;
                }

                //检查条码是否重复
                if (barcode_exit.contains(barcodeEntity.realBarCode)) {
                    mAdialog.warnDialog("条码" + barcodeEntity.realBarCode + "已扫描，请不要重复扫描。");
                    return;
                }
                if(barcodeEntity.grade == 2 && CurGroupXCount > CurProductCount && CurProductCount == 0){
                    mAdialog.warnDialog("请先扫描产品标！");
                    return;
                }
                // 未满组合数扫描箱码
                if (barcodeEntity.grade == 2 && CurGroupXCount > CurProductCount)
                {
                    AlertDialog alertDialog = new AlertDialog.Builder(this)
                            .setTitle("提示")
                            .setMessage("未达到一组的数量是否提交进行组合?")
                            .setIcon(R.mipmap.ic_launcher)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    CurSerailNoAddType = 2; //录入盒标;
                                    mProgersssDialog = new ProgersssDialog(GodownMScanActivity.this);
                                    mProgersssDialog.setMsg("扫码上传中");
                                    new Thread(postScanRun).start();
                                    return;
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

                if (barcodeEntity.grade != 2 && CurGroupXCount == CurProductCount)
                {
                    mAdialog.warnDialog("请录入盒标！");
                    return;
                }

                mProgersssDialog = new ProgersssDialog(GodownMScanActivity.this);
                mProgersssDialog.setMsg("扫码上传中");
                new Thread(postScanRun).start();

            }catch(Exception e){
                    ;
                }
//            new Thread(CheckGroupXRun).start();

        }
        else
        {

        }

        return;
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