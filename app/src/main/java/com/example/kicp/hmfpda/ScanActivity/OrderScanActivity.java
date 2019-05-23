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
import com.example.kicp.hmfpda.Models.GodownMEntity;
import com.example.kicp.hmfpda.Models.OrderBillingEntity;
import com.example.kicp.hmfpda.Models.OrderEntity;
import com.example.kicp.hmfpda.R;
import com.example.kicp.hmfpda.Utils.Adialog;
import com.example.kicp.hmfpda.Utils.ApiHelper;
import com.example.kicp.hmfpda.Utils.Config.Config;
import com.example.kicp.hmfpda.Utils.Models.GodownMBillingListResultMsg;
import com.example.kicp.hmfpda.Utils.Models.GodownMListResultMsg;
import com.example.kicp.hmfpda.Utils.Models.OrderBillingListResultMsg;
import com.example.kicp.hmfpda.Utils.Models.OrderListResultMsg;
import com.example.kicp.hmfpda.Utils.Models.OrderScanSaveResultMsg;
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
 * 订单扫描.
 */
@ContentView(R.layout.scan_order)
public class OrderScanActivity extends DecodeBaseActivity implements  View.OnClickListener,BarcodeManager.OnEngineStatus {

    @ViewInject(R.id.num_line)
    private TextView line_plist;              //单据号选择项
    @ViewInject(R.id.num_spinner)
    private Spinner cmb_plist;              //单据号选择项
    @ViewInject(R.id.tbAgent)
    private EditText tbAgent;            //客户名称
    @ViewInject(R.id.tbProduct)
    private EditText tbProduct;             //关联产品
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

    private String agentId = "";  //客户Id
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
    private boolean lockMode = false;
    private List<String> barcode_exit = new ArrayList<>(); //已扫描的条码列表
    private List<String> orderNumList = new ArrayList<>();      //发货单据编号列表
    private List<OrderEntity> orderList = new ArrayList<>();   //主单列表
    private List<OrderBillingEntity> orderbillingList = new ArrayList<>();  //组合入库单据明细列表
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
        mScanTouchManager.setVisibility(View.VISIBLE);


        mDoDecodeThread = new DoDecodeThread();
        mDoDecodeThread.start();
        bindView();
    }

    //初始化界面
    public void bindView(){
        bLockMode = true;
        setEditTextReadonly(tbAgent);
        setEditTextReadonly(tbProduct);
        setEditTextReadonly(tbLN);

        mContext = this.getApplicationContext();
        mAdialog = new Adialog(this);
        btnLock.setOnClickListener(this);
        btnDelBill.setOnClickListener(this);
        btnUpload.setOnClickListener(this);
        btnQuit.setOnClickListener(this);

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
                            DownLoadBills();
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

        ProductInfo.clear();
        //创建SimpleAdapter适配器将数据绑定到item显示控件上
        digAdapter = new SimpleAdapter(this, ProductInfo, R.layout.list_item_order,
                new String[]{ "orderBillingId", "agnetId","agnetName","productId","productName","ln" },
                new int[]{R.id.orderBillingId, R.id.agnetId, R.id.agnetName,R.id.productId,R.id.productName,R.id.ln});
        //实现列表的显示
        mListView.setAdapter(digAdapter);
        //条目点击事件
        mListView.setOnItemClickListener(new ItemClickListener());
    }

    //获取点击事件
    private final class ItemClickListener implements AdapterView.OnItemClickListener {

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ListView listView = (ListView) parent;
            HashMap<String, Object> productData = (HashMap<String, Object>) listView.getItemAtPosition(position);

            billingId = productData.get("orderBillingId").toString();
            productId = productData.get("productId").toString();
            agentId = productData.get("agentId").toString();
            tbAgent.setText(productData.get("agnetName").toString());
            tbProduct.setText( productData.get("productName").toString() );
            tbLN.setText( productData.get("ln").toString() );

            curPreSet = 0;   //当前预设
            billPreset = 0;  //本单预设
            for( OrderBillingEntity billingEntity : orderbillingList)
            {
                billPreset += billingEntity.Qty;
                if (billingEntity.OrderBillingId.equals(billingId))
                {
                    agentId = billingEntity.AgentId;
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

    //设置单据保存文件的路径
    private void SetFilePath(String billNo)
    {
        String dir = mContext.getFilesDir().getPath().toString() + "/" + Public.gmPath + "/";
        MainFileName = dir + billNo + "" + Public.FileType;
        EntryFileName = dir + billNo + "-Billing" + Public.FileType;
        ScanFileName = dir + billNo + "-Scan" + Public.FileType;
    }

    //保存主单文件
    private void SaveGoDownDataFile(List<OrderEntity> list)
    {
        try {
            for (OrderEntity entity : list) {
                SetFilePath(entity.OrderCode);
                //如果文件存在，则重写内容；如果文件不存在，则创建文件
                File f = new File(MainFileName);
                FileWriter fw = new FileWriter(f, false);
                BufferedWriter out = new BufferedWriter(fw);
                out.write( entity.OrderId + "," +
                        entity.OrderCode + "," +
                        ( entity.OrderDate == null? "" : formatter.format(entity.OrderDate) )+ "," +
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

    //下载单据（覆盖文件）
    private void DownLoadBills()throws Exception{
        //获取主单
        HashMap<String,String> query = new HashMap<String, String>();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        try {
            query.put("orderCode",billNo);
            query.put("beginDate", "");
            query.put("endDate", "");
            query.put("status", "0");

            OrderListResultMsg Listc = ApiHelper.GetHttp(OrderListResultMsg.class,
                    Config.WebApiUrl + "GetOrderList?", query, Config.StaffId , Config.AppSecret ,true);
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
            for( OrderEntity orEntity : Listc.Result)
            {
                query.clear();
                query.put("orderId" , orEntity.OrderId);
                OrderBillingListResultMsg orderBillListc = ApiHelper.GetHttp(OrderBillingListResultMsg.class,
                        Config.WebApiUrl + "GetOrderBillingListByOrderId?", query, Config.StaffId , Config.AppSecret ,true);
                orderBillListc.setResult();

                if(orderBillListc.StatusCode != 200)
                {
                    throw new Exception(orderBillListc.Info);
                }
                if( orderBillListc.Result == null || orderBillListc.Result.isEmpty())
                {
                    continue;
                }
                SetFilePath(orEntity.OrderCode);
                SaveGoDownBillingDataFile(orderBillListc.Result);
            }
        }catch (Exception ex){
            throw new Exception(ex.getMessage());
        }
    }

    // 保存明细文件
    private void SaveGoDownBillingDataFile(List<OrderBillingEntity> list) throws Exception
    {
        try {
            for (OrderBillingEntity entity : list) {
                //如果文件存在，则重写内容；如果文件不存在，则创建文件
                File f=new File(EntryFileName);
                FileWriter fw = new FileWriter(f, false);
                BufferedWriter out = new BufferedWriter(fw);
                out.write( entity.OrderBillingId + "," +
                        entity.OrderId + "," +
                        entity.AgentId + "," +
                        entity.AgentName + "," +
                        entity.AgentCode + "," +
                        entity.ProductId + "," +
                        entity.ProductName + "," +
                        entity.ProductCode + "," +
                        entity.LN + "," +
                        entity.Qty + "," +
                        entity.FatQty + "," +
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
    public void SaveScanFile(int scanQty) throws Exception{
        try {
            //如果文件存在，则重写内容；如果文件不存在，则创建文件
            File f=new File(ScanFileName);
            FileWriter fw = new FileWriter(f, false);
            BufferedWriter out = new BufferedWriter(fw);

            String ln = tbLN.getText().toString();

            out.write( tbBarcode.getText().toString() + "," +
                    billId + "," +
                    billNo + "," +
                    agentId + "," +
                    tbAgent.getText().toString() + "," +
                    productId + "," +
                    tbProduct.getText().toString() + "," +
                    ln + "," +
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
            orderbillingList.clear();

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
                OrderBillingEntity entity = new OrderBillingEntity();
                entity.OrderBillingId = lineMembers[0];
                entity.OrderId = lineMembers[1];
                entity.AgentId = lineMembers[2];
                entity.AgentName = lineMembers[3];
                entity.AgentCode = lineMembers[4];
                entity.ProductId = lineMembers[5];
                entity.ProductName = lineMembers[6];
                entity.ProductCode = lineMembers[7];
                entity.LN = lineMembers[8];
                entity.Qty = Integer.parseInt(lineMembers[9]);
                entity.FatQty = Integer.parseInt(lineMembers[10]);
                entity.SinglePerBox = Integer.parseInt(lineMembers[11]);
                orderbillingList.add(entity);
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
    //初始化dg_Grid列表
    private void dgDataInit()
    {
        ProductInfo.clear();

        for( OrderBillingEntity entity : orderbillingList){
            if(entity.OrderId.equals(billId)) {
                HashMap<String, Object> item = new HashMap<String, Object>();
                item.put("orderBillingId", entity.OrderBillingId);
                item.put( "agendgDataInittId" , entity.AgentId);
                item.put( "agentName" , entity.AgentName);
                item.put( "productId" , entity.ProductId);
                item.put( "productName" , entity.ProductName );
                item.put("ln" , entity.LN);
                ProductInfo.add(item);
            }
        }
        digAdapter.notifyDataSetChanged();
        listID.setVisibility(View.VISIBLE);
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

    //解除锁定
    private void Unlock(){
        cmb_plist.setEnabled(true);
        line_plist.setEnabled(true);
        tbAgent.setEnabled(true);
        tbProduct.setEnabled(true);
        tbLN.setEnabled(true);
        btnDelBill.setEnabled(true);
        btnQuit.setEnabled(true);
        if(!LoginActivity.onlineFlag)btnUpload.setEnabled(true);
        btnLock.setText("锁定");
        lockMode = false;
        barcode_exit.clear();
    }

    //锁定扫描
    private void Lock(){
        cmb_plist.setEnabled(false);
        line_plist.setEnabled(false);
        tbAgent.setEnabled(false);
        tbProduct.setEnabled(false);
        tbLN.setEnabled(false);
        btnQuit.setEnabled(false);
        btnDelBill.setEnabled(false);
        btnUpload.setEnabled(false);
        tbBarcode.setFocusable(true);
        lockMode = true;
        btnLock.setText("解锁");

    }

    //扫描锁定按钮事件
    private void LockEvent(){
        if(lockMode){
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
            if (agentId.isEmpty() || tbAgent.getText().toString().isEmpty())
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
                String curAgentId;
                String curLN;
                int ScanQty = 0;
                FileReader fr = new FileReader(ScanFileName);
                BufferedReader br = new BufferedReader(fr);
                while ((line = br.readLine()) != null) {
                    lineMember = line.split(",");
                    curBarcode = lineMember[0];
                    barcode_exit.add(curBarcode);//加入到已有列表
                    curAgentId = lineMember[3];
                    curProductId = lineMember[5];
                    curLN = lineMember[7];
                    ScanQty = Integer.parseInt(lineMember[8]);

                    billCount += ScanQty;
                    if(curAgentId.equals(agentId) && curProductId.equals(productId) && curLN.equals(tbLN.getText().toString().trim())){
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

    //设置EiditText为只读属性
    public void setEditTextReadonly(EditText et){
        et.setCursorVisible(false);             //设置输入框中的光标不可见
        et.setFocusable(false);                 //无焦点
        et.setFocusableInTouchMode(false);      //触摸时也得不到焦点
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

    //提交扫码
    Runnable PostOrder = new Runnable() {
        @Override
        public void run() {
            try {
                Message mess = new Message();
                int scanQty = 0;
                String barcode = tbBarcode.getText().toString().trim();
                String ln = tbLN.getText().toString().trim();
                //在线采集
                if (LoginActivity.onlineFlag) {
                    //上传扫描明细
                    String exceptionMsg = "";
                    HashMap<String, String> parames = new HashMap<>();
                    parames.put("orderId", billId);
                    parames.put("serialNo", barcode);
                    parames.put("agentId", agentId);
                    parames.put("productId", productId);
                    parames.put("ln", ln);
                    parames.put("createUserId", LoginActivity.CreateUserId);

                    OrderScanSaveResultMsg orScan = ApiHelper.GetHttp(OrderScanSaveResultMsg.class,
                            Config.WebApiUrl + "PostOrderSerialNo?", parames, Config.StaffId, Config.AppSecret, true);
                    orScan.setResult();

                    if(!exceptionMsg.isEmpty()){
                        mess.what = 0;
                        mess.obj = exceptionMsg;
                        eHandler.sendMessage(mess);
                        return;
                    }

                    if(orScan.StatusCode != 200){
                        mess.what = 0;
                        mess.obj = orScan.Info;
                        eHandler.sendMessage(mess);
                        return;
                    }

                    scanQty = orScan.Qty;

                    if (scanQty == 0) {
                        mess.what = 0;
                        mess.obj = "异常：出库数量为0！";
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
            new Thread(PostOrder).start();
        }
        else
        {
            //非锁定方式 扫描 单据号
            line_plist.setText(barCode);

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
