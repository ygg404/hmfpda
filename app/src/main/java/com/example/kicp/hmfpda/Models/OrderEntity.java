package com.example.kicp.hmfpda.Models;

import java.util.Date;

/**
 * 销售出库 主单
 */
public class OrderEntity {
    /// 入主键
    public String OrderId;

    /// 单据编码
    public String OrderCode;

    /// 单据日期
    public Date OrderDate;

    /// 备注
    public String Description;

    /// 创建日期
    public Date CreateDate;

    /// 创建用户
    public String CreateUserId;

    /// 创建用户
    public String CreateUserName;

    /// 状态（0-未审核，1-已审核）
    public int Status;
}
