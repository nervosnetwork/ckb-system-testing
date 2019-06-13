package org.nervos.ckb.jsonrpcTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import com.alibaba.fastjson.JSONObject;
import org.nervos.ckb.framework.system.CKBSystem;
import org.nervos.ckb.util.HttpUtils;
import org.nervos.ckb.util.NetworkUtils;
import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GetPeersStateTest extends RPCTestBase {

  private int idlePort = NetworkUtils.getIdlePort();
  private CKBSystem getPeersStateCkbSystem;
  private String dockerName;
  private String ckbDockerImageTagName;
  private String rpcURL = "http://127.0.0.1:" + idlePort;


  @BeforeClass
  public void startOneNode() throws Exception {
    // start a local chain
    getPeersStateCkbSystem = new CKBSystem();
    dockerName = getPeersStateCkbSystem.getDockerName();
    ckbDockerImageTagName = getPeersStateCkbSystem.getCkbDockerImageTagName();
    getPeersStateCkbSystem.enableDebug();
    getPeersStateCkbSystem.cleanEnv();

    // run a idle container
    getPeersStateCkbSystem.init(dockerName, ckbDockerImageTagName, idlePort);

    // init ckb
    String createNode = "mkdir node1 node2 && ckb -C node1 init && ckb -C node2 init";
    getPeersStateCkbSystem.runCommandWithDocker(createNode, "-d -it");
    // start node1
    String ckbNode1Run = "ckb -C node1 run";
    getPeersStateCkbSystem.runCommandWithDocker(ckbNode1Run, "-d -it");

    Thread.sleep(3000);
  }

  // FIXME: for version v0.12.0 the get_peers_state is Deprecated, open this case when dev fixed this rpc issue
  // test case detail: ${TCMS}/testcase-view-1428-1
  @Test(dataProvider = "positiveData", priority = 1, enabled = false)
  public void testGetPeersStatePositive(String positiveData) throws Exception {
    startNewPeer(rpcURL, getPeersStateCkbSystem, "node2");
    JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(rpcURL, positiveData));
    int resultSize = jsonObject.getJSONArray("result").size();
    assertThat(resultSize, greaterThanOrEqualTo(1));
  }

  // test case detail: ${TCMS}/testcase-view-1429-1
  @Test(dataProvider = "testNoPeersData")
  public void testNoPeers(String testNoPeersData) {
    JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(rpcURL, testNoPeersData));
    int resultSize = jsonObject.getJSONArray("result").size();
    Assert.assertEquals(resultSize, 0);
  }

  // test case detail: ${TCMS}/testcase-view-1430-1
  @Test(dataProvider = "negativeData")
  public void testGetPeersStateNegative(String negativeData) {
    JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(rpcURL, negativeData));
    String errorCode = jsonObject.getJSONObject("error").getString("code");
    Assert.assertEquals(errorCode, "-32602");
  }

  @DataProvider
  public Object[][] positiveData() {
    return new Object[][]{
        {buildJsonrpcRequest("get_peers_state")}
    };
  }

  @DataProvider
  public Object[][] testNoPeersData() {
    return new Object[][]{
        {buildJsonrpcRequest("get_peers_state")}
    };
  }

  @DataProvider
  public Object[][] negativeData() {
    return new Object[][]{
        {buildJsonrpcRequest("get_peers_state", 1212)} //wrong params
    };
  }

}
