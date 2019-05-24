package com.example.kicp.hmfpda.Utils.Models;

import com.alibaba.fastjson.JSON;
import com.example.kicp.hmfpda.Utils.Enum.StatusCodeEnum;


import java.io.Serializable;

/**
 * Created by YGG on 2018/5/30.
 */

public class TokenResultMsg extends HttpResponseMsg implements Serializable {
    public Token Result(){
        if(StatusCode == StatusCodeEnum.Success.getValue()){
            return JSON.parseObject(Data.toString(), Token.class);
        }
        else{
            return null;
        }
    }

}