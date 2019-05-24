package com.example.kicp.hmfpda.Utils.Models;

import com.alibaba.fastjson.JSON;
import com.example.kicp.hmfpda.Models.OrderEntity;
import com.example.kicp.hmfpda.Utils.Enum.StatusCodeEnum;

import java.util.List;

/**
 * 订单主单获取类
 */
public class OrderListResultMsg extends HttpResponseMsg {
    public List<OrderEntity> Result(){
        if(StatusCode == StatusCodeEnum.Success.getValue() && !(Data == null || Data=="")){
            return JSON.parseArray(Data.toString(), OrderEntity.class);
        }
        else{
            return null;
        }
    }
}
