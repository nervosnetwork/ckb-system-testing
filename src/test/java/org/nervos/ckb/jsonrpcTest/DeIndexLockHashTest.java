package org.nervos.ckb.jsonrpcTest;

import com.alibaba.fastjson.JSONObject;
import org.nervos.ckb.framework.system.CKBSystem;
import org.nervos.ckb.util.HttpUtils;
import org.nervos.ckb.util.NetworkUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DeIndexLockHashTest extends RPCTestBase {


  private int idlePort = NetworkUtils.getIdlePort();
  private CKBSystem deIndexLockHashCkbSystem;
  private String dockerName;
  private String ckbDockerImageTagName;
  private String rpcURL = "http://127.0.0.1:" + idlePort;

  @BeforeClass
  public void initDeIndexLockHashEnv() throws InterruptedException {
    // start a local chain
    deIndexLockHashCkbSystem = new CKBSystem();
    dockerName = deIndexLockHashCkbSystem.getDockerName();
    ckbDockerImageTagName = deIndexLockHashCkbSystem.getCkbDockerImageTagName();
    deIndexLockHashCkbSystem.enableDebug();
    deIndexLockHashCkbSystem.cleanEnv();

    // run a idle container
    deIndexLockHashCkbSystem.init(dockerName, ckbDockerImageTagName, idlePort);

    deIndexLockHashCkbSystem.ckbInitAddIndexerRun();
    deIndexLockHashCkbSystem.startCKBMiner();

    Thread.sleep(3000);
  }

  @AfterClass
  public void cleanDeIndexLockHashEnv() {
    deIndexLockHashCkbSystem.cleanEnv();
  }

  // test case detail: ${TCMS}/testcase-view-1490-1
  @Test(dataProvider = "positiveData")
  public void testDeIndexLockHashPositive(String positiveData) {
    JSONObject jsonObject = JSONObject
        .parseObject(HttpUtils.sendJson(rpcURL, positiveData));
    Assert.assertNull(jsonObject.get("error"));
  }

  // test case detail: ${TCMS}/testcase-view-1491-2
  @Test(dataProvider = "positiveUnIndexData")
  public void testDeIndexLockHashUnIndexPositive(String positiveData) {
    JSONObject jsonObject = JSONObject
        .parseObject(HttpUtils.sendJson(rpcURL, positiveData));
    Assert.assertNull(jsonObject.get("error"));
  }

  // test case detail: ${TCMS}/testcase-view-1492-2
  @Test(dataProvider = "positiveUnKnownData")
  public void testDeIndexLockHashUnKnownPositive(String positiveData) {
    JSONObject jsonObject = JSONObject
        .parseObject(HttpUtils.sendJson(rpcURL, positiveData));
    Assert.assertNull(jsonObject.get("error"));
  }

  // test case detail: ${TCMS}/testcase-view-1493-3
  // test case detail: ${TCMS}/testcase-view-1494-3
  // test case detail: ${TCMS}/testcase-view-1495-3
  // test case detail: ${TCMS}/testcase-view-1496-3
  @Test(dataProvider = "negativeData")
  public void testDeIndexLockHashNegative(String negativeData) {
    JSONObject jsonObject = JSONObject
        .parseObject(HttpUtils.sendJson(rpcURL, negativeData));
    printout(negativeData);
    printout(jsonObject.toJSONString());
    Assert.assertNotNull(jsonObject.get("error"));

  }

  @DataProvider
  public Object[][] negativeData() {

    return new Object[][]{
        {buildJsonrpcRequest("deindex_lock_hash")},
        {buildJsonrpcRequest("deindex_lock_hash", "")},
        {buildJsonrpcRequest("deindex_lock_hash", lockHash, "1")},
        {buildJsonrpcRequest("deindex_lock_hash", lockHash + "98")},
    };
  }

  @DataProvider
  public Object[][] positiveData() throws Exception {
    waitForBlockHeight(1, 180, 1);
    JSONObject jsonObject = JSONObject
        .parseObject(
            HttpUtils.sendJson(url, buildJsonrpcRequest("index_lock_hash", lockHash, "1")));
    return new Object[][]{
        {buildJsonrpcRequest("deindex_lock_hash", lockHash)},
    };
  }

  @DataProvider
  public Object[][] positiveUnIndexData() throws Exception {
    waitForBlockHeight(1, 180, 1);
    return new Object[][]{
        {buildJsonrpcRequest("deindex_lock_hash", lockHash)},
    };
  }

  @DataProvider
  public Object[][] positiveUnKnownData() throws Exception {
    waitForBlockHeight(1, 180, 1);

    return new Object[][]{
        {buildJsonrpcRequest("deindex_lock_hash", lockHash.substring(0, 64) + "66")},
    };
  }

}
