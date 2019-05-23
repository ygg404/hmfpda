package com.example.kicp.hmfpda.Utils.Models;

import com.alibaba.fastjson.JSON;
import com.example.kicp.hmfpda.Models.OrderBillingEntity;
import com.example.kicp.hmfpda.Utils.Enum.StatusCodeEnum;

import java.util.List;

/**
 * 订单明细获取类
 */
public class OrderBillingListResultMsg extends HttpResponseMsg{
    public List<OrderBillingEntity> Result;

    public void setResult(){
        if(StatusCode == StatusCodeEnum.Success.getValue() && !(Data == null || Data=="")){
            Result =  JSON.parseArray(Data.toString(), OrderBillingEntity.class);
        }
        else{
            Result = null;
        }

    }
}
