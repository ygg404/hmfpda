package com.example.kicp.hmfpda.Models;

import java.util.Date;

/**
 * 退货 明细.
 */
public class ReturnBillingEntity {

    /// 退货单开单表主键
    public String ReturnBillingId;

    /// 退货单主键
    public String ReturnId;

    /// 仓库Id
    public String WarehouseId;

    /// 仓库名称
    public String WarehouseName;

    /// 仓库编码
    public String WarehouseCode;

    /// 产品Id
    public String ProductId;

    /// 产品名称
    public String ProductName;

    /// 产品编码
    public String ProductCode;

    /// 数量
    public int Qty;

    /// 实际数量
    public int QtyFact;

    /// 中盒多少瓶
    public int SinglePerBox;

    /// 大箱多少盒
    public int SingleBoxPerBigBox;

    /// 创建日期
    public Date CreateDate;

    /// 创建用户
    public String CreateUserId;

    /// 用户名
    public String CreateUserName;
}
