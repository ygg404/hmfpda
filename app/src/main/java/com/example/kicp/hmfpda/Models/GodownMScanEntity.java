package com.example.kicp.hmfpda.Models;

/**
 * 组装入库扫码保存类
 */
public class GodownMScanEntity {
    /// 主键
    public String GodownMId;

    /// 单据编码
    public String GodownMCode;

    /// 开单表主键
    public String GodownMBillingId;

    /// 仓库ID
    public String WarehouseId;

    /// 仓库名
    public String WarehouseName;

    /// 产品Id
    public String ProductId;

    /// 产品名称
    public String ProductName;

    /// 生产批次
    public String LN;

    /// 生产日期
    public String PR;

    /// 内码
    public String serialArr;

    /// 箱码
    public String mserialNo;

    /// 创建者ID

    public String CreateUserId;

    /// 创建时间
    public String CreateTime;
}
