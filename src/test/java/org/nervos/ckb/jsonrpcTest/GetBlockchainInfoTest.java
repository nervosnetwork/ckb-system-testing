package org.nervos.ckb.jsonrpcTest;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;

import com.alibaba.fastjson.JSONObject;
import org.nervos.ckb.util.HttpUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GetBlockchainInfoTest extends RPCTestBase {

  // test case detail: ${TCMS}/testcase-view-1426-1
  @Test(dataProvider = "positiveData")
  public void testGetBlockchainInfoPositive(String positiveData){
    JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(url, positiveData));
    String chain = jsonObject.getJSONObject("result").getString("chain");
    assertThat(chain,containsString("ckb"));
  }

  // test case detail: ${TCMS}/testcase-view-1427-1
  @Test(dataProvider = "negativeData")
  public void testGetBlockchainInfoNegative(String negativeData){
    JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(url, negativeData));
    JSONObject error = jsonObject.getJSONObject("error");
    assertThat(error.getString("message"), containsString("Invalid parameters: No parameters were expected"));
    Assert.assertEquals(error.getString("code"),"-32602");
  }

  @DataProvider
  public Object[][] positiveData(){
    return new Object[][]{
        {buildJsonrpcRequest("get_blockchain_info")}
    };
  }

  @DataProvider
  public Object[][] negativeData(){
    return new Object[][]{
        {buildJsonrpcRequest("get_blockchain_info",1212)}// wrong params
    };
  }
}
