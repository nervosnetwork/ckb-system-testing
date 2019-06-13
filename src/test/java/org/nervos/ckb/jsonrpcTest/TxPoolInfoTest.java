package org.nervos.ckb.jsonrpcTest;

import com.alibaba.fastjson.JSONObject;
import org.nervos.ckb.util.HttpUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TxPoolInfoTest extends RPCTestBase {

  // test case detail: ${TCMS}/testcase-view-1425-1
  @Test(dataProvider = "negativeData")
  public void testTxPoolInfoNegative(String negativeData) {
    JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(url, negativeData));
    String errorCode = jsonObject.getJSONObject("error").getString("code");
    Assert.assertEquals(errorCode, "-32602");
  }

  // test case detail: ${TCMS}/testcase-view-1424-2
  @Test(dataProvider = "positiveData", dependsOnMethods = "org.nervos.ckb.jsonrpcTest.SendTransactionTest.testSendTXPositive")
  public void testTxPoolInfoPositive(String positiveData) {
    JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(url, positiveData));
    JSONObject result = jsonObject.getJSONObject("result");
    Assert.assertNotNull(result);
    String time = result.getString("last_txs_updated_at");
    Assert.assertNotEquals(time, "0");
  }

  @DataProvider
  public Object[][] positiveData() {
    return new Object[][]{
        {buildJsonrpcRequest("tx_pool_info")}
    };
  }

  @DataProvider
  public Object[][] negativeData() {
    return new Object[][]{
        {buildJsonrpcRequest("tx_pool_info", "1212")}// wrong params
    };
  }

}
