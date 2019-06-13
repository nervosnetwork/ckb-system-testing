package org.nervos.ckb.jsonrpcTest;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;

import com.alibaba.fastjson.JSONObject;
import org.nervos.ckb.util.HttpUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;

public class GetBlockHashTest extends RPCTestBase {

  // test case detail: ${TCMS}/testcase-view-822-1
  // test case detail: ${TCMS}/testcase-view-823-1
  // test case detail: ${TCMS}/testcase-view-824-1
  @Test(dataProvider = "positiveData")
  public void testGetBlockHashPositive(String positiveData) {
    JSONObject jsonObject = JSONObject
        .parseObject(HttpUtils.sendJson(url, positiveData));

    Assert.assertNull(jsonObject.get("error"));


  }

  // test case detail: ${TCMS}/testcase-view-825-1
  // test case detail: ${TCMS}/testcase-view-826-1
  @Test(dataProvider = "negativeData")
  public void testGetBlockHashNegative(String negativeData) {
    JSONObject jsonObject = JSONObject
        .parseObject(HttpUtils.sendJson(url, negativeData));

    Assert.assertNotNull(jsonObject.get("error"));

  }

  // test case detail: ${TCMS}/testcase-view-851-1
  // test case detail: ${TCMS}/testcase-view-850-1
  // test case detail: ${TCMS}/testcase-view-847-1
  @Test(dataProvider = "publicUseCasesData")
  public void testPublicUseCases(String publicUseCasesData, String assertMessage) {
    JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(url, publicUseCasesData));
    assertThat(jsonObject.get("error").toString(), containsString(assertMessage));
  }

  @DataProvider
  public Object[][] publicUseCasesData() {
    return new Object[][]{
        {buildJsonrpcRequest(-10, "2.0", "get_block_hash", "1"),
            "{\"code\":-32700,\"message\":\"Parse error\"}"},
        {buildJsonrpcRequest(2, "1.0", "get_block_hash", "1"),
            "{\"code\":-32600,\"message\":\"Invalid request\"}"},
        {buildJsonrpcRequest("get_block_hash_wrong_method_name", "1"),
            "{\"code\":-32601,\"message\":\"Method not found\"}"}

    };
  }

  // test case detail: ${TCMS}/testcase-view-848-1
  @Test(dataProvider = "httpUnknownMethodData")
  public void testHttpUnknownMethod(String httpUnknownMethodData) {
    // using non-POST method
    String response = sendJson(url, httpUnknownMethodData, "GET");

    assertThat(response,
        containsString("Used HTTP Method is not allowed. POST or OPTIONS is required"));
  }

  @DataProvider
  public Object[][] httpUnknownMethodData(){
    return new Object[][]{
        {buildJsonrpcRequest("get_block_hash","0")}
    };
  }

  @DataProvider
  public Object[][] positiveData() throws Exception {
    waitForBlockHeight(1, 120, 1);

    return new Object[][]{
        {buildJsonrpcRequest("get_block_hash", "0")},
        {buildJsonrpcRequest("get_block_hash", "1")},
        {buildJsonrpcRequest("get_block_hash", "99999999")}
    };
  }


  @DataProvider
  public Object[][] negativeData() {
    return new Object[][]{
        {buildJsonrpcRequest("get_block_hash", null)},
        {buildJsonrpcRequest("get_block_hash", 123)},
    };

  }

  /**
   * Used to get the return value of the specified request method（not POST)
   * <i>sendJson("sendUrl","requestData","requestMethod")</i>
   *
   * @param sendUrl request url
   * @param data request data
   * @param method request method
   * @return responseContent
   */
  public String sendJson(String sendUrl, String data, final String method) {
    class HttpBadPost extends HttpEntityEnclosingRequestBase {

      public HttpBadPost(String uri) {
        this.setURI(URI.create(uri));
      }

      @Override
      public String getMethod() {
        return method;
      }
    }
    CloseableHttpClient client = HttpClients.createDefault();
    HttpBadPost post = new HttpBadPost(sendUrl);
    printout("Request address", sendUrl);
    printout("Request parameters", data);
    StringEntity postEntity = new StringEntity(data, "UTF-8");
    postEntity.setContentEncoding("UTF-8");
    postEntity.setContentType("application/json");

    post.setEntity(postEntity);
    String responseContent = null;
    CloseableHttpResponse response = null;
    try {
      response = client.execute(post);
      if (response.getStatusLine().getStatusCode() != 200) {
        HttpEntity entity = response.getEntity();
        responseContent = EntityUtils.toString(entity, "UTF-8");
        printout("Response content", responseContent);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      HttpUtils.close(response, client);
    }
    return responseContent;
  }
}
