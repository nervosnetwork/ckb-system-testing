package org.nervos.ckb.jsonrpcTest;

import com.alibaba.fastjson.JSONObject;
import org.nervos.ckb.util.HttpUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GetTransactionTest extends RPCTestBase {

  // test case detail: ${TCMS}/testcase-view-844-1
  // test case detail: ${TCMS}/testcase-view-846-1
  @Test(dataProvider = "positiveData")
  public void testGetTransactionPositive(String positiveData) {
    JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(url, positiveData));

    Assert.assertNull(jsonObject.get("error"));
  }

  // test case detail: ${TCMS}/testcase-view-845-1
  @Test(dataProvider = "negativeData")
  public void testGetTransactionNegative(String negativeData) {
    JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(url, negativeData));

    Assert.assertNotNull(jsonObject.get("error"));
  }


  @DataProvider
  public Object[][] positiveData() throws Exception {

    waitForBlockHeight(1, 120, 1);

    String get_block_hash = buildJsonrpcRequest("get_block_hash", "1");
    JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(url, get_block_hash));
    String blockHash = jsonObject.getString("result");

    String get_block = buildJsonrpcRequest("get_block", blockHash);
    JSONObject jsonObject2 = JSONObject.parseObject(HttpUtils.sendJson(url, get_block));
    String transactionHash = ((JSONObject) jsonObject2.getJSONObject("result")
        .getJSONArray("transactions").get(0)).getString("hash");
    System.out.println("the transactionHash is " + transactionHash);

    return new Object[][]{
        {buildJsonrpcRequest("get_transaction", transactionHash)},
        {buildJsonrpcRequest("get_transaction",
            "0x1234567890123456789012345678901234567890123456789012345678901234")}

    };

  }

  @DataProvider
  public Object[][] negativeData() {
    return new Object[][]{
        {buildJsonrpcRequest("get_transaction", "0x1234567")}
    };
  }


}
