package com.example.kicp.hmfpda.Utils.Enum;


/**
 * 获取单据的种类
 */
public enum BillTypeEnum {
    godownType(1),       //入库
    orderType(2),         //发货
    returnType(3),  //退货
    allotType(4),//调拨
    checkType(5), //盘点
    gxType(6); //关联箱

    private final int value;
    //构造方法必须是private或者默认
    private BillTypeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public BillTypeEnum valueOf(int value) {
        switch (value) {
            case 1:
                return BillTypeEnum.godownType;
            case 2:
                return BillTypeEnum.orderType;
            case 3:
                return BillTypeEnum.returnType;
            case 4:
                return BillTypeEnum.allotType;
            case 5:
                return BillTypeEnum.checkType;
            case 6:
                return BillTypeEnum.gxType;
            default:
                return null;
        }
    }

    public String getTypeName(){
        switch (value) {
            case 1:
                return "入库";
            case 2:
                return "发货";
            case 3:
                return "退货";
            case 4:
                return "调拨";
            case 5:
                return "盘点";
            case 6:
                return "生产关联箱";
            default:
                return null;
        }
    }


}
