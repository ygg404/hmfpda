package com.example.kicp.hmfpda.Models;

/**
 * 发货订单类 扫码保存
 */

public class OrderScanEntity {

    /// 序列号
    public String barcode;


    /// 出库单主键
    public String orderId;


    /// 单据编码
    public String orderCode;


    /// 客户主键
    public String agentId;


    /// 客户名称
    public String agentName;


    /// 产品主键
    public String productId;


    /// 产品名称
    public String productName;


    /// 批次
    public String LN;


    /// 扫码数量
    public int scanQty;


    /// 创建者ID
    public String CreateUserId;


    /// 创建时间
    public String CreateTime;
}