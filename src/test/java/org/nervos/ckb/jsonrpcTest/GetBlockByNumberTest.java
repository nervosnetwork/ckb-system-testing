package org.nervos.ckb.jsonrpcTest;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;

import com.alibaba.fastjson.JSONObject;
import org.nervos.ckb.util.HttpUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GetBlockByNumberTest extends RPCTestBase {

  // test case detail: ${TCMS}/testcase-view-1420-2
  @Test(dataProvider = "positiveData")
  public void testGetBlockByNumberPositive(String positiveData) {
    JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(url, positiveData));
    String result = jsonObject.getJSONObject("result").getJSONObject("header").getString("hash");
    assertThat(result, containsString("0x"));
  }

  // test case detail: ${TCMS}/testcase-view-1421-1
  // test case detail: ${TCMS}/testcase-view-1422-1
  // test case detail: ${TCMS}/testcase-view-1423-1
  @Test(dataProvider = "negativeData")
  public void testGetBlockByNumberNegative(String negativeData,String assertWhat) {
    JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(url, negativeData));
    if ("assertNull".equals(assertWhat)){
      JSONObject result = jsonObject.getJSONObject("result");
      Assert.assertNull(result);
    }else {
      String errorCode = jsonObject.getJSONObject("error").getString("code");
      Assert.assertEquals(errorCode,"-32602");
    }

  }

  @DataProvider
  public Object[][] positiveData() throws Exception {
    waitForBlockHeight(1, 60, 1);
    return new Object[][]{
        {buildJsonrpcRequest("get_block_by_number", "1")}
    };
  }

  @DataProvider
  public Object[][] negativeData() throws Exception {
    return new Object[][]{
        {buildJsonrpcRequest("get_block_by_number", "100000"),"assertNull"},//block height is bigger than current block height
        {buildJsonrpcRequest("get_block_by_number"),"assertErrorCode"},//params is null
        {buildJsonrpcRequest("get_block_by_number", 0),"assertErrorCode"}//params type is wrong
    };
  }
}
