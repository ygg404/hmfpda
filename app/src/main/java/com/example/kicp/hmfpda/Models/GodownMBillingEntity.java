package com.example.kicp.hmfpda.Models;

import java.util.Date;

/**
 * 组装入库 明细
 */
public class GodownMBillingEntity {
    /// 主键
    public String GodownMBillingId;

    /// 单主键
    public String GodownMId;

    /// 仓库ID
    public String WarehouseId;

    /// 仓库名
    public String WarehouseName;

    /// 产品Id
    public String ProductId;

    ///产品名称
    public String ProductName;

    /// 生产批次
    public String LN;

    /// 生产日期
    public Date PR;

    /// 数量
    public int Qty;

    /// 中盒多少瓶
    public int SinglePerBox;

}
