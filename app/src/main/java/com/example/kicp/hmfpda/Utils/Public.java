package com.example.kicp.hmfpda.Utils;

import android.content.Context;

import com.example.kicp.hmfpda.Models.BarcodeEntity;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by YGG on 2018/7/2.
 */


public class Public {
    public static final String ConfigFile = "set.properties";   //配置文件文件名
    //基础资料目录
    public static final String BasePath = "BaseData";
    //装箱入库目录
    public static final String gmPath = "装箱入库" ;
    //发货目录
    public static final String rdPath = "销售出库";
    //退货目录
    public static final String rtPath = "销售退货";

    public static final String FileType = ".dat";

    public static final String GODOWN_MAIN_TABLE = "godownMainBill";    //入库主单
    public static final String ORDER_MAIN_TABLE = "orderMainBill";     //订单主单
    public static final String RETURN_MAIN_TABLE = "returnMainBill";   //退货主单
    public static final String ALLOT_MAIN_TABLE = "allotMainBill";     //调拨主单
    public static final String CHECK_MAIN_TABLE = "checkMainBill";     //盘点主单
    public static final String GodownX_MAIN_TABLE = "godownxMainBill";   //关联箱主单

    public static final String B_INVENTORY_File = "BaseInventory";  //产品资料
    public static final String B_CUSTOMER_File = "BaseCustomer";    //客户资料
    public static final String B_WAREHOUSE_File = "BaseWarehouse"; //仓库资料

    public static final String GodownBillingType = "-PD_Billing";   //入库明细单后缀
    public static final String OrderBillingType  = "-OD_Billing";   //出库明细单后缀
    public static final String ReturnBillingType = "-RD_Billing";   //退货明细单后缀
    public static final String AllotBillingType  = "-AD_Billing";   //调拨明细单后缀
    public static final String GodownXBillingType = "-XD_Billing";   //关联箱明细单后缀

    public static final String GodownScanType = "-PD_Scan";   //入库扫码单后缀
    public static final String OrderScanType  = "-OD_Scan";   //出库扫码单后缀
    public static final String ReturnScanType = "-RD_Scan";   //退货扫码单后缀
    public static final String AllotScanType  = "-AD_Scan";   //调拨扫码单后缀
    public static final String CheckScanType  = "-CD_Scan";   //调拨扫码单后缀
    public static final String GodownXScanType  = "-XD_Scan";   //关联箱扫码单后缀

    /// <summary>
    /// 扫码的码是否合法及返回相应的输出参数供调用的过程使用(根据系统唯一标识进行单独处理)
    /// </summary>
    /// <param name="barCode">条码</param>
    /// <param name="realBarCode">输出参数：实际的号码</param>
    /// <param name="grade">输出参数：1为产品标，2为盒标，3为．．．．．．</param>
    /// <param name="errorMessage">输出参数：错误描述，无效条码时必须不能为空，需要加上说明．如采集数据不能保存，进行相应提示</param>
    /// <returns></returns>
    public static BarcodeEntity IsBarCodeValid(String barCode)
    {
        BarcodeEntity barcodeEntity = new BarcodeEntity();
        String realBarCode = barCode.replace("http://kd315.net/?b=", "").replace("http://kd315.net?b=", "");
        barcodeEntity.errorMessage = "";
        if (realBarCode.length() == 9)//盒标
        {
            barcodeEntity.grade = 2;
        }
        else
        {
            if (realBarCode.length() == 6 || realBarCode.length() == 7 || realBarCode.length() == 8 ||
                    realBarCode.length() == 10 || realBarCode.length() == 12 || realBarCode.length() == 14 || realBarCode.length() == 18)
            {
                barcodeEntity.grade = 1;
            }
            else
            {
                if (realBarCode.length() == 20 || realBarCode.length() == 21)
                {
                    barcodeEntity.grade = 0;
                }
                else
                {
                    barcodeEntity.realBarCode = "";
                    barcodeEntity.errorMessage = "未定义参数";
                }
            }
        }

        barcodeEntity.realBarCode = realBarCode;
        return barcodeEntity;
    }

    //获取 装箱入库 主单单据号
    public static List<String> GetGodownMBills(Context mContext)
    {
        String path = mContext.getFilesDir().getPath().toString() + "/" + Public.gmPath;
        List<String> list = new ArrayList<>();
        File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();
            for (File file2 : files) {
                if(file2.getName().contains(".dat"))
                {
                    if( (file2.getName().indexOf("-Billing") == -1) && (file2.getName().indexOf("-Scan") == -1) )
                    {
                        int index = file2.getName().lastIndexOf(".");
                        String fileName = file2.getName().substring(0, index);
                        list.add(fileName);
                    }
                }
            }
        }
        return list;
    }

    //删除指定路径的文件
    public static void DelDataFile(String filename){
        File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }

    }
}
