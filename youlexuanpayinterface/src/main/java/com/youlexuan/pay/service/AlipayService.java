package com.youlexuan.pay.service;

import java.util.Map;

public interface AlipayService {

    public Map createNative(String out_trade_no,String total_fee);

    public Map queryPayStatus(String out_trade_no);

    public Map close(String out_trade_no);

}
