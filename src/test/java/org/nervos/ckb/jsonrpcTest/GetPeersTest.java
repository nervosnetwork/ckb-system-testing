package org.nervos.ckb.jsonrpcTest;

import static java.lang.Thread.sleep;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import com.alibaba.fastjson.JSONObject;
import org.nervos.ckb.framework.system.CKBSystem;
import org.nervos.ckb.util.HttpUtils;
import org.nervos.ckb.util.NetworkUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GetPeersTest extends RPCTestBase {

  private int idlePort = NetworkUtils.getIdlePort();
  private CKBSystem getPeersCkbSystem;
  private String dockerName;
  private String ckbDockerImageTagName;
  private String rpcURL = "http://127.0.0.1:" + idlePort;

  @BeforeClass
  public void initGetPeerEnv() throws InterruptedException {
    // start a local chain
    getPeersCkbSystem = new CKBSystem();
    dockerName = getPeersCkbSystem.getDockerName();
    ckbDockerImageTagName = getPeersCkbSystem.getCkbDockerImageTagName();
    getPeersCkbSystem.enableDebug();
    getPeersCkbSystem.cleanEnv();

    // run a idle container
    getPeersCkbSystem.init(dockerName, ckbDockerImageTagName, idlePort);

    // init ckb
    String createNode = "mkdir node1 node2 && ckb -C node1 init && ckb -C node2 init";
    getPeersCkbSystem.runCommandWithDocker(createNode, "-d -it");
    // start node1
    String ckbNode1Run = "ckb -C node1 run";
    getPeersCkbSystem.runCommandWithDocker(ckbNode1Run, "-d -it");

    Thread.sleep(3000);
  }

  @AfterClass
  public void cleanGetPeerEnv() {
    getPeersCkbSystem.cleanEnv();
  }

  //test case detail: ${TCMS}/testcase-view-1357-1
  @Test(dataProvider = "getCurrentPeersData")
  public void testNoOtherPeersPositive(String testPeersPositive) {
    JSONObject jsonObject = JSONObject
        .parseObject(HttpUtils.sendJson(rpcURL, testPeersPositive));
    Assert.assertEquals(jsonObject.getJSONArray("result").size(), 0, "should get no peers info");
  }

  //test case detail: ${TCMS}/testcase-view-1355-1
  @Test(dataProvider = "getCurrentPeersData", priority = 1)
  public void testNewPeerPositive(String testNewPeerPositive) throws InterruptedException {
    startNewPeer(rpcURL, getPeersCkbSystem, "node2");
    //get new peer info
    JSONObject newPeerRsp = JSONObject
        .parseObject(HttpUtils.sendJson(rpcURL, testNewPeerPositive));
    int peersSize = newPeerRsp.getJSONArray("result").size();
    assertThat(peersSize, greaterThanOrEqualTo(1));
  }

  //test case detail: ${TCMS}/testcase-view-1354-1
  @Test(dataProvider = "getCurrentPeersData", dependsOnMethods = "testNewPeerPositive", priority = 1)
  public void testAllPeersPositve(String allPeers) {
    JSONObject jsonObject = JSONObject
        .parseObject(HttpUtils.sendJson(rpcURL, allPeers));
    Assert.assertEquals(jsonObject.getJSONArray("result").size(), 1);
  }

  //test case detail: ${TCMS}/testcase-view-1356-1
  @Test(dataProvider = "getCurrentPeersData", dependsOnMethods = "testNewPeerPositive", priority = 2)
  public void testDelPeer(String currentPeer) throws InterruptedException {
    String stopNode2 = "ps -ef |grep 'node2 run'|grep -v 'grep'|cut -c 9-15|xargs kill -9";
    getPeersCkbSystem.runCommandWithDocker(stopNode2);
    sleep(2000);
    JSONObject jsonObject = JSONObject
        .parseObject(HttpUtils.sendJson(rpcURL, currentPeer));
    Assert.assertEquals(jsonObject.getJSONArray("result").size(), 0,
        "should get no peers info after delete other peer");
  }

  //test case detail: ${TCMS}/testcase-view-1358-1
  @Test(dataProvider = "negativeWrongParamsData")
  public void testWrongParamsNegative(String negativeWrongParamsData) {
    JSONObject jsonObject = JSONObject
        .parseObject(HttpUtils.sendJson(url, negativeWrongParamsData));
    JSONObject errorMsg = (JSONObject) jsonObject.get("error");
    Assert.assertNotNull(errorMsg, "should return error message");
    Assert.assertEquals(errorMsg.getIntValue("code"), -32602);
    Assert.assertEquals(errorMsg.getString("message"),
        "Invalid parameters: No parameters were expected");
  }

  @DataProvider
  public Object[][] getCurrentPeersData() {
    return new Object[][]{
        {buildJsonrpcRequest("get_peers")}
    };
  }

  @DataProvider
  public Object[][] negativeWrongParamsData() {
    return new Object[][]{
        {buildJsonrpcRequest("get_peers", 1)}
    };
  }

}
