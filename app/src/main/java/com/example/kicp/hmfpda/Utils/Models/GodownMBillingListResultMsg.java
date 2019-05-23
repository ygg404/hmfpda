package com.example.kicp.hmfpda.Utils.Models;

import com.alibaba.fastjson.JSON;
import com.example.kicp.hmfpda.Models.GodownMBillingEntity;
import com.example.kicp.hmfpda.Utils.Enum.StatusCodeEnum;


import java.util.List;

/**
 *组装入库明细 请求类
 */
public class GodownMBillingListResultMsg extends HttpResponseMsg{
    public List<GodownMBillingEntity> Result;

    public void setResult(){
        if(StatusCode == StatusCodeEnum.Success.getValue() && !(Data == null || Data=="")){
            Result =  JSON.parseArray(Data.toString(), GodownMBillingEntity.class);
        }
        else{
            Result = null;
        }

    }
}
