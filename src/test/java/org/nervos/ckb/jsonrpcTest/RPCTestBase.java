package org.nervos.ckb.jsonrpcTest;

import static java.lang.Thread.sleep;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.nervos.ckb.TestBase;
import org.nervos.ckb.framework.system.CKBSystem;
import org.nervos.ckb.util.HttpUtils;
import org.nervos.ckb.util.JsonrpcRequestBody;
import org.nervos.ckb.util.WaitUntil;
import java.util.ArrayList;
import java.util.List;

public class RPCTestBase extends TestBase {

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
   * mapping JavaBean JsonrpcRequestBody
   * <i>buildJsonrpcRequest("id","jsonrpc","methodname","param1",param2...)</i>
   *
   * @param method invoked method name
   * @param params invoked method params
   */
  public String buildJsonrpcRequest(int id, String jsonrpc, String method, Object... params) {
    JsonrpcRequestBody jsonrpcRequestBody = new JsonrpcRequestBody(id, jsonrpc, method, params);
    JSON jsonObj = (JSON) JSONObject.toJSON(jsonrpcRequestBody);
    return jsonObj.toJSONString();
  }


  /**
   * get block hash
   * <i> getBlockHash(1) </i>
   *
   * @param num the height number of block
   * @return the block hash
   */
  public String getBlockHash(String num) {
    String getBlockHashReq = buildJsonrpcRequest("get_block_hash", num);
    JSONObject getBlockHashRsp = JSONObject
        .parseObject(HttpUtils.sendJson(url, getBlockHashReq));
    String blockHash = getBlockHashRsp.getString("result");
    return blockHash;
  }

  /**
   * get the first transaction hash from the block
   * <i> getTXHashFromBlock(blockHash) </i>
   *
   * @return transaction hash
   */
  public String getTXHashFromBlock(String blockHash) {
    String getBlockReq = buildJsonrpcRequest("get_block", blockHash);
    JSONObject getBlockRsp = JSONObject.parseObject(HttpUtils.sendJson(url, getBlockReq));
    JSONObject result = (JSONObject) getBlockRsp.getJSONObject("result")
        .getJSONArray("transactions").get(0);
    String txHash = (String) result.get("hash");
    return txHash;
  }


  /**
   * wait block height is equal to the specified value
   * <i>waitBlockHeight(minHeight,timeOut)</i>
   *
   * @param blockHeight specified block height value
   * @param timeOutInSecond Maximum waiting time in second
   * @param pollingTimesInSecond polling times in second
   */
  public void waitForBlockHeight(int blockHeight, int timeOutInSecond, int pollingTimesInSecond)
      throws Exception {
    boolean waitResult = waitFor(new WaitUntil() {
      @Override
      public boolean waitUntil() {
        String request = buildJsonrpcRequest("get_tip_block_number", null);
        JSONObject jsonRequest = JSONObject.parseObject(HttpUtils.sendJson(url, request));
        int currentBlockHeight = jsonRequest.getIntValue("result");
        System.out.println("now block heigth is " + currentBlockHeight);
        if (currentBlockHeight >= blockHeight) {
          return true;
        }

        return false;

      }
    }, timeOutInSecond, pollingTimesInSecond);
    if (!waitResult) {
      throw new Exception("wait for block height timeout");

    }
  }

  /**
   * Waits for the block height to be equal to the specified value at the targetRpcUrl
   * <i>waitBlockHeight("URL","blockHeight","timeOutInSecond","pollingTimesInSecond")</i>
   *
   * @param targetRpcUrl the specified URL
   * @param blockHeight the specified block height
   * @param timeOutInSecond Maximum waiting time in second
   * @param pollingTimesInSecond polling times in second
   */
  public void waitForBlockHeight(String targetRpcUrl, int blockHeight, int timeOutInSecond,
      int pollingTimesInSecond) throws Exception {

    boolean waitResult = waitFor(new WaitUntil() {
      @Override
      public boolean waitUntil() {
        String request = buildJsonrpcRequest("get_tip_block_number", null);
        JSONObject jsonRequest = JSONObject.parseObject(HttpUtils.sendJson(targetRpcUrl, request));
        int currentBlockHeight = jsonRequest.getIntValue("result");
        printout("now block heigth", currentBlockHeight);
        if (currentBlockHeight >= blockHeight) {
          return true;
        }
        return false;
      }
    }, timeOutInSecond, pollingTimesInSecond);
    if (!waitResult) {
      throw new Exception("wait for block height timeout");
    }
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
    System.out.println("list is: " + list);
    return list;
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

  /**
   * Start a new node under the nodePath folder
   *
   * @param rpcURL the url to send rpc request
   * @param newPeerCkbSystem newPeerCkbSystem CKBSystem
   * @param nodePath nodePath the path of the new node
   */
  public void startNewPeer(String rpcURL, CKBSystem newPeerCkbSystem, String nodePath)
      throws InterruptedException {
    // get local node info
    JSONObject jsonObject2 = JSONObject
        .parseObject(HttpUtils.sendJson(rpcURL, buildJsonrpcRequest("local_node_info")));
    JSONObject result = (JSONObject) jsonObject2.get("result");
    String localAddress = ((JSONObject) result.getJSONArray("addresses").get(0))
        .getString("address");
    printout("localAddress is ", localAddress);

    // update node2
    String updateAddrCmd =
        "echo " + localAddress + "| perl -pe 's|\\/|\\\\\\/|g;s|0.0.0.0|127.0.0.1|g'";
    newPeerCkbSystem.runCommandWithDocker(updateAddrCmd);
    String updateAddr = newPeerCkbSystem.getStdoutString();
    String changePort = "cd " + nodePath + " && sed -i 's/8115/8118/;s/8114/8117/' ckb.toml";
    newPeerCkbSystem.runCommandWithDocker(changePort, "-d -it");
    String changeBoot =
        "cd " + nodePath + " && sed -i 's/bootnodes = \\[]/bootnodes = \\[\\\"" + updateAddr
            + "\\\"]/' ckb.toml";
    newPeerCkbSystem.updateBlockAssemblerConfig();
    newPeerCkbSystem.runCommandWithDocker(changeBoot, "-d -it");

    // start node2
    String ckbNode2Run = "ckb -C " + nodePath + " run";
    newPeerCkbSystem.runCommandWithDocker(ckbNode2Run, "-d -it");
    sleep(5000);
  }

}
