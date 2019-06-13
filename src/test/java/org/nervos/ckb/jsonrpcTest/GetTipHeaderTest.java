package org.nervos.ckb.jsonrpcTest;

import com.alibaba.fastjson.JSONObject;
import org.nervos.ckb.util.HttpUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GetTipHeaderTest extends RPCTestBase {


  // test case detail: ${TCMS}/testcase-view-833-1
  // test case detail: ${TCMS}/testcase-view-834-1
  @Test(dataProvider = "positiveData")
  public void testGetTipHeaderPositive(String positiveData) {
    JSONObject jsonObject = JSONObject
        .parseObject(HttpUtils.sendJson(url, positiveData));

    Assert.assertNotNull(jsonObject.get("result"));

  }

  // test case detail: ${TCMS}/testcase-view-835-1
  @Test(dataProvider = "negativeData")
  public void testGetTipHeaderNegative(String negativeData) {
    JSONObject jsonObject = JSONObject
        .parseObject(HttpUtils.sendJson(url, negativeData));

    Assert.assertNotNull(jsonObject.get("error"));

  }


  @DataProvider(parallel = true)
  public Object[][] positiveData() {
    return new Object[][]{
        {buildJsonrpcRequest("get_tip_header", null)},
        {buildJsonrpcRequest("get_tip_header")},
    };
  }


  @DataProvider
  public Object[][] negativeData() {
    return new Object[][]{
        {buildJsonrpcRequest("get_tip_header", "123")}
    };
  }


}
