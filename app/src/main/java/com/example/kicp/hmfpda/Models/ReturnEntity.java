package com.example.kicp.hmfpda.Models;

import java.util.Date;

/**
 * 退货 主单
 */

public class ReturnEntity {

    /// 退货单主键
    public String ReturnId;

    /// 单据编码
    public String ReturnCode;

    /// 单据日期
    public Date ReturnDate;

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
