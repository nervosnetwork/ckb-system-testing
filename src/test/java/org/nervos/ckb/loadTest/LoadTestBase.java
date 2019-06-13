package org.nervos.ckb.loadTest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.nervos.ckb.util.JsonrpcRequestBody;
import org.nervos.ckb.util.WaitUntil;
import java.util.ArrayList;
import java.util.List;

public class LoadTestBase {

  /**
   * mapping JavaBean JsonrpcRequestBody
   * <i>buildJsonrpcRequest("methodname","param1",param2...)</i>
   *
   * @param method invoked method name
   * @param params invoke method params
   * @return ckb request format as json string
   */
  public String buildJsonrpcRequest(String method, Object... params) {
    JsonrpcRequestBody jsonrpcRequestBody = new JsonrpcRequestBody(method, params);
    JSON jsonObj = (JSON) JSONObject.toJSON(jsonrpcRequestBody);
    return jsonObj.toJSONString();
  }

  /**
   * Assemble jsons to List <i> jsonToList(object1, object2, ...) </i>
   *
   * @return JsonArray
   */
  public List<JSON> jsonToList(Object... object) {
    List<JSON> list = new ArrayList<>();
    JSON jsonObj;

    for (int i = 0; i < object.length; i++) {
      jsonObj = (JSON) JSONObject.toJSON(object[i]);
      list.add(jsonObj);
    }
    return list;
  }

  public void printout(String objName, Object value) {
    if (value == null) {
      System.out.println("object is null");
      return;
    }
    System.out.println(objName + " Value: " + JSONObject.toJSONString(value));
  }

  public void printout(String loginfo) {
    System.out.println(loginfo);
  }

  /**
   * recursive call WaitUnitl method in timeout
   * <i>waitFor(IntelligentWait wait, long timeOutInSecond, int pollingTimesInSecond)</i>
   *
   * @param timeOutInSecond Maximum waiting time in second
   * @param pollingTimesInSecond polling times in second
   */
  public boolean waitFor(WaitUntil wait, int timeOutInSecond, int pollingTimesInSecond) {
    int currentTime = 0;
    while (currentTime < timeOutInSecond) {
      if (wait.waitUntil()) {
        return true;
      }
      currentTime += pollingTimesInSecond;
      try {
        Thread.sleep(pollingTimesInSecond * 1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    return false;
  }

  /**
   * Get the cell outpoint's tx_hash
   *
   * @param cellOutPoint the cell's outpoint
   * @return cell outpoint's tx_hash
   */
  public String getCellTxHash(JSONObject cellOutPoint) {
    return cellOutPoint.getString("tx_hash");
  }

  /**
   * Get the cell outpoint's index
   *
   * @param cellOutPoint the cell's outpoint
   * @return cell outpoint's index
   */
  public String getCellIndex(JSONObject cellOutPoint) {
    return cellOutPoint.getString("index");
  }

}
