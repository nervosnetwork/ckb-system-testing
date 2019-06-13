package org.nervos.ckb.util;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * @author hust
 * @ClassName: HttpUtils
 * @Description:
 * @date 2016年11月17日 上午10:08
 */
public class HttpUtils {

  //private static Logger logger = LoggerFactory.getLogger(HttpUtils.class);

  /**
   * 发送json
   */
  public static String sendJson(String sendUrl, String data) {
    CloseableHttpClient client = HttpClients.createDefault();
    HttpPost post = new HttpPost(sendUrl);
    System.out.println("Request address:" + sendUrl);
    System.out.println("Request parameters:" + data);
    //StringEntity myEntity = new StringEntity(data, ContentType.APPLICATION_JSON);
    StringEntity myEntity = new StringEntity(data, "UTF-8");
    myEntity.setContentEncoding("UTF-8");
    myEntity.setContentType("application/json");

    post.setEntity(myEntity);
    String responseContent = null;
    CloseableHttpResponse response = null;
    try {
      response = client.execute(post);
      if (response.getStatusLine().getStatusCode() == 200) {
        HttpEntity entity = response.getEntity();
        responseContent = EntityUtils.toString(entity, "UTF-8");
        System.out.println("Response content:" + responseContent);

      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      close(response, client);
    }
    return responseContent;
  }


  /**
   * 关闭
   */
  public static void close(CloseableHttpResponse response, CloseableHttpClient client) {
    try {
      if (response != null) {
        response.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (client != null) {
          client.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
