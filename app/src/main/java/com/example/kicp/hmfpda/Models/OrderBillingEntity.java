package com.example.kicp.hmfpda.Models;

import java.util.Date;

/**
 * 销售 明细
 */
public class OrderBillingEntity {

    /// 出库单开单表主键
    public String OrderBillingId;

    /// 出库单主键
    public String OrderId;

    /// 代理主键
    public String AgentId;

    /// 代理名称
    public String AgentName;

    /// 代理编码
    public String AgentCode;

    /// 产品Id
    public String ProductId;

    /// 产品名称
    public String ProductName;

    /// 产品编码
    public String ProductCode;

    /// 数量
    public int Qty;

    /// 实际数量
    public int FatQty;

    /// 批次
    public String LN;

    /// 中盒多少瓶
    public int SinglePerBox;

    /// 大箱多少盒
    public int SingleBoxPerBigBox;

    /// 创建用户
    public String CreateUserId;

    /// 用户名
    public String CreateUserName;
}
