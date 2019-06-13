package org.nervos.ckb.jsonrpcTest;

import com.alibaba.fastjson.JSONObject;
import org.nervos.ckb.util.HttpUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GetTipBlockNumberTest extends RPCTestBase {

  // test case detail: ${TCMS}/testcase-view-858-1
  @Test(dataProvider = "positiveData")
  public void testGetTipBlockNumberPositive(String positiveData) {
    JSONObject jsonObject = JSONObject
        .parseObject(HttpUtils.sendJson(url, positiveData));

    Assert.assertTrue(jsonObject.getIntValue("result") >= 0);

  }

  // test case detail: ${TCMS}/testcase-view-859-1
  @Test(dataProvider = "negativeData")
  public void testGetTipBlockNumberNegative(String negativeData) {
    JSONObject jsonObject = JSONObject
        .parseObject(HttpUtils.sendJson(url, negativeData));

    Assert.assertNotNull(jsonObject.get("error"));
  }

  @DataProvider
  public Object[][] positiveData() {
    return new Object[][]{
        {buildJsonrpcRequest("get_tip_block_number", null)}
    };
  }

  @DataProvider
  public Object[][] negativeData() {
    return new Object[][]{
        {buildJsonrpcRequest("get_tip_block_number", 0)}
    };
  }
}
