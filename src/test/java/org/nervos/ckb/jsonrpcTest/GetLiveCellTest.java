package org.nervos.ckb.jsonrpcTest;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import com.alibaba.fastjson.JSONObject;
import org.nervos.ckb.util.HttpUtils;
import org.nervos.ckb.util.items.HashIndex;
import org.nervos.ckb.util.items.OutPoint;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GetLiveCellTest extends RPCTestBase {

  // test case: ${TCMS}/testcase-view-852-1
  @Test(dataProvider = "positiveGetLiveCellData", priority = 1)
  public void testGetLiveCellPositive(String positiveGetLiveCellData) {
    JSONObject jsonObject = JSONObject
        .parseObject(HttpUtils.sendJson(url, positiveGetLiveCellData));
    Assert.assertNotNull(jsonObject.getJSONObject("result").get("cell"));
    Assert.assertEquals(jsonObject.getJSONObject("result").get("status"), "live");
  }

  // test case: ${TCMS}/testcase-view-855-1
  @Test(dataProvider = "negativeUnknownData", priority = 1)
  public void testGetLiveCellNegativeUnknown(String negativeUnknownData) {
    JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(url, negativeUnknownData));
    Assert.assertNull(jsonObject.getJSONObject("result").get("cell"));
    Assert.assertEquals(jsonObject.getJSONObject("result").get("status"), "unknown");
  }

  // test case: ${TCMS}/testcase-view-854
  // test case: ${TCMS}/testcase-view-856-1
  // test case: ${TCMS}/testcase-view-857-1
  @Test(dataProvider = "negativeInvalidLengthData", priority = 1)
  public void testGetLiveCellNegativeLength(String negativeInvalidLengthData) {
    System.out.println(negativeInvalidLengthData);
    JSONObject jsonObject =
        JSONObject.parseObject(HttpUtils.sendJson(url, negativeInvalidLengthData));
    assertThat(jsonObject.getJSONObject("error").getString("message"), containsString("expected a 0x-prefixed hex string with 64 digits"));
  }

  // test case: ${TCMS}/testcase-view-853-1
  @Test(
      dataProvider = "positiveDeadCellData", priority = 1,
      dependsOnMethods = "org.nervos.ckb.jsonrpcTest.SendTransactionTest.testSendTXPositive")
  public void testGetLiveCellPositiveDead(String positiveDeadCellData) {
    JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(url, positiveDeadCellData));
    Assert.assertNull(jsonObject.getJSONObject("result").get("cell"));
    Assert.assertEquals(jsonObject.getJSONObject("result").get("status"), "dead");
  }

  @DataProvider
  public Object[][] positiveGetLiveCellData() {
    String blockHash = getBlockHash("0");
    String txHash = getTXHashFromBlock(blockHash);

    return new Object[][]{
        {buildJsonrpcRequest("get_live_cell", new OutPoint(null, new HashIndex(txHash, "0")))},
    };
  }

  @DataProvider
  public Object[][] negativeUnknownData() {
    return new Object[][]{
        {buildJsonrpcRequest("get_live_cell",
            new OutPoint(null, new HashIndex("0xc53a74864e9ee9f9080303f65a57820b8fd0e40304714577e6b0f96165231c9d", "0")))
        },
    };
  }

  @DataProvider(parallel = true)
  public Object[][] negativeInvalidLengthData() {
    return new Object[][]{
        {buildJsonrpcRequest("get_live_cell",
            new OutPoint(null, new HashIndex("0xc53a74864e9ee9f9080303f65a57820b8fd0e40304714577e6b0f96165231c9d3", "0")))
        },
        {buildJsonrpcRequest("get_live_cell", new OutPoint(null, new HashIndex("", "0")))},
        {buildJsonrpcRequest("get_live_cell", new OutPoint(null, new HashIndex("  ", "0")))},
    };
  }

  @DataProvider
  public Object[][] positiveDeadCellData() {
    JSONObject deadPrevious = positivePrevious;
    System.out.println("In GetLiveCellTest the deadPrevious is: " + deadPrevious);
    return new Object[][]{
        {buildJsonrpcRequest("get_live_cell",
            new OutPoint(null, new HashIndex(getCellTxHash(deadPrevious), getCellIndex(deadPrevious))))},
    };
  }
}
