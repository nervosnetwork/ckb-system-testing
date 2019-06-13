package org.nervos.ckb.jsonrpcTest;

import com.alibaba.fastjson.JSONObject;
import org.nervos.ckb.util.HttpUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GetEpochByNumberTest extends RPCTestBase {

  // test case detail: ${TCMS}/testcase-view-1439-1
  @Test(dataProvider = "getEpochByNumPositiveData")
  public void getEpochByNumPositive(String getEpochByNumPositiveData) {
    JSONObject jsonObject = JSONObject
        .parseObject(HttpUtils.sendJson(url, getEpochByNumPositiveData));
    long blockReward = jsonObject
        .getJSONObject("result")
        .getLongValue("block_reward");
    Assert.assertTrue(blockReward <= 5000000000000L,
        "The block reward should be smaller than 5000000000000");
  }

  // test case detail: ${TCMS}/testcase-view-1440-1
  // test case detail: ${TCMS}/testcase-view-1441-2
  @Test(dataProvider = "getEpochByNumNegativeData")
  public void getEpochByNumInvalidNegative(String getEpochByNumNegativeData, String assertContent) {
    JSONObject jsonObject = JSONObject
        .parseObject(HttpUtils.sendJson(url, getEpochByNumNegativeData));
    if ("assertError".equals(assertContent)) {
      String errorCode = jsonObject
          .getJSONObject("error")
          .getString("code");
      Assert.assertEquals(errorCode, "-32602");
    } else {
      JSONObject result = jsonObject.getJSONObject("result");
      Assert.assertNull(result, "should return null result if larger than current epoch num");
    }
  }

  @DataProvider
  public Object[][] getEpochByNumPositiveData() {
    return new Object[][]{
        {buildJsonrpcRequest("get_epoch_by_number", "0")}
    };
  }

  @DataProvider
  public Object[][] getEpochByNumNegativeData() {
    return new Object[][]{
        {buildJsonrpcRequest("get_epoch_by_number", "-1"), "assertError"}, // invalid parameter
        {buildJsonrpcRequest("get_epoch_by_number", "10000"), "assertNull"}, // larger than current epoch num
    };
  }

}
