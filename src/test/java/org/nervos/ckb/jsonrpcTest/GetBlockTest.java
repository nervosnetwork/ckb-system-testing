package org.nervos.ckb.jsonrpcTest;

import com.alibaba.fastjson.JSONObject;
import org.nervos.ckb.util.HttpUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GetBlockTest extends RPCTestBase {

  // test case detail: ${TCMS}/testcase-view-828-1
  // test case detail: ${TCMS}/testcase-view-831-1
  // test case detail: ${TCMS}/testcase-view-827-1
  @Test(dataProvider = "positiveData")
  public void testGetBlockPositive(String positiveData) {

    JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(url, positiveData));
    Object error = jsonObject.get("error");

    Assert.assertNull(error);
  }

  // test case detail: ${TCMS}/testcase-view-829-1
  // test case detail: ${TCMS}/testcase-view-830-1
  // test case detail: ${TCMS}/testcase-view-832-1
  @Test(dataProvider = "negativeData")
  public void testGetBlockNegative(String negativeData) {
    JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(url, negativeData));

    Assert.assertNotNull(jsonObject.get("error"));
  }

  @DataProvider
  public Object[][] negativeData() {
    return new Object[][]{
        {buildJsonrpcRequest("get_block", "0x509230e5eadd48fb2ab1cfd6237")},
        {buildJsonrpcRequest("get_block", 1)},
        {buildJsonrpcRequest("get_block")}

    };

  }


  @DataProvider
  public Object[][] positiveData() throws Exception {
    waitForBlockHeight(1, 60, 1);

    String get_block_hash_blockZero = buildJsonrpcRequest("get_block_hash", "0");
    JSONObject blockZeroResponse = JSONObject
        .parseObject(HttpUtils.sendJson(url, get_block_hash_blockZero));
    String blockHashOfZero = blockZeroResponse.getString("result");

    String get_block_hash_blockOne = buildJsonrpcRequest("get_block_hash", "1");
    JSONObject blockOneResponse = JSONObject
        .parseObject(HttpUtils.sendJson(url, get_block_hash_blockOne));
    String blockHashOfOne = blockOneResponse.getString("result");

    return new Object[][]{
        {buildJsonrpcRequest("get_block", blockHashOfOne)},
        {buildJsonrpcRequest("get_block", blockHashOfZero)},
        {buildJsonrpcRequest("get_block",
            "0x1234567890123456789012345678901234567890123456789012345678901234")}
    };

  }


}
