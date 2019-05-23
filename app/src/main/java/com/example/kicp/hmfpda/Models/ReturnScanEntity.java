package com.example.kicp.hmfpda.Models;

/**
 *退货扫码保存
 */
public class ReturnScanEntity {
    /// 序列号
    public String barcode;

    /// 退货单主键
    public String returnId;


    /// 单据编码
    public String returnCode;


    ///仓库主键
    public String warehouseId;


    /// 仓库名称
    public String warehouseName;


    /// 产品主键
    public String productId;


    /// 产品名称
    public String productName;


    /// 扫码数量
    public int scanQty;


    /// 创建者ID
    public String CreateUserId;


    /// 创建时间
    public String CreateTime;
}
