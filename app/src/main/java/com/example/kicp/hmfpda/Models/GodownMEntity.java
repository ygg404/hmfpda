package com.example.kicp.hmfpda.Models;

import java.util.Date;

/**
 * 组装入库 主单
 */
public class GodownMEntity {
    /// 关联箱单主键
    public String GodownMId;

    /// 单据编码
    public String GodownMCode;

    /// 单据日期
    public Date GodownMDate;

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
