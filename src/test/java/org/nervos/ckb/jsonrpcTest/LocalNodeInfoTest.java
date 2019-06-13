package org.nervos.ckb.jsonrpcTest;

import com.alibaba.fastjson.JSONObject;
import org.nervos.ckb.util.HttpUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LocalNodeInfoTest extends RPCTestBase {

  // test case: ${TCMS}/testcase-view-860-1
  @Test(dataProvider = "positiveData")
  public void testLocalNodeInfoPositive(String positiveData) {
    JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(url, positiveData));
    Assert.assertNull(jsonObject.get("error"));
    Assert.assertNotNull(jsonObject.get("result"));
  }

  // test case: ${TCMS}/testcase-view-861-1
  @Test(dataProvider = "negativeData")
  public void testLocalNodeInfoNegative(String negativeData) {
    JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(url, negativeData));
    Assert.assertNull(jsonObject.get("result"));
    Assert.assertNotNull(jsonObject.get("error"));
  }

  @DataProvider
  public Object[][] positiveData() {
    return new Object[][]{
        {buildJsonrpcRequest("local_node_info")},
    };
  }

  @DataProvider
  public Object[][] negativeData() {
    return new Object[][]{
        {buildJsonrpcRequest("local_node_info", 1)},
    };
  }

}
