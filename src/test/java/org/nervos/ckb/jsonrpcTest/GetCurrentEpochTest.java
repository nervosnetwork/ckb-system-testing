package org.nervos.ckb.jsonrpcTest;

import com.alibaba.fastjson.JSONObject;
import org.nervos.ckb.util.HttpUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GetCurrentEpochTest extends RPCTestBase {

  // test case detail: ${TCMS}/testcase-view-1437-1
  @Test(dataProvider = "getCurrentEpochPositiveData")
  public void testGetCurrentEpochPositive(String getCurrentEpochPstvData) {
    JSONObject jsonObject = JSONObject
        .parseObject(HttpUtils.sendJson(url, getCurrentEpochPstvData));
    long blockReward = jsonObject
        .getJSONObject("result")
        .getLongValue("block_reward");
    Assert.assertTrue(blockReward <= 5000000000000L,
        "The block reward should be smaller than 5000000000000");
  }

  // test case detail: ${TCMS}/testcase-view-1438-1
  @Test(dataProvider = "getCurrentEpochNegativeData")
  public void testGetCurrentEpochNegative(String getCurrentEpochNegtvData) {
    JSONObject jsonObject = JSONObject
        .parseObject(HttpUtils.sendJson(url, getCurrentEpochNegtvData));
    String errorCode = jsonObject
        .getJSONObject("error")
        .getString("code");
    Assert.assertEquals(errorCode, "-32602");
  }

  @DataProvider
  public Object[][] getCurrentEpochPositiveData() {
    return new Object[][]{
        {buildJsonrpcRequest("get_current_epoch")}
    };
  }

  @DataProvider
  public Object[][] getCurrentEpochNegativeData() {
    return new Object[][]{
        {buildJsonrpcRequest("get_current_epoch", 1)}
    };
  }

}
