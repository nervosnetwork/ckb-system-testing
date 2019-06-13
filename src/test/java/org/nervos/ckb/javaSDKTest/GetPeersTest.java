package org.nervos.ckb.javaSDKTest;

import static java.lang.Thread.sleep;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import org.nervos.ckb.framework.system.CKBSystem;
import org.nervos.ckb.util.NetworkUtils;
import java.io.IOException;
import org.nervos.ckb.service.CKBService;
import org.nervos.ckb.service.HttpService;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GetPeersTest extends JavaSDKTestBase {

  private int idlePort = NetworkUtils.getIdlePort();
  private CKBSystem getPeersCkbSystem;
  private String dockerName;
  private String ckbDockerImageTagName;
  private String rpcURL = "http://127.0.0.1:" + idlePort;
  CKBService ckbService = CKBService.build(new HttpService(rpcURL));

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

  //test case detail: ${TCMS}/testcase-view-1412-1
  @Test
  public void testNoOtherPeersPositive() throws IOException {
    int size = ckbService
        .getPeers()
        .send()
        .getPeers()
        .size();
    Assert.assertEquals(size, 0, "should get no peers info");
  }

  //test case detail: ${TCMS}/testcase-view-1410-1
  @Test(priority = 1)
  public void testNewPeerPositive() throws IOException, InterruptedException {
    startNewPeer(ckbService, getPeersCkbSystem, "node2");
    int peersSize = ckbService
        .getPeers()
        .send()
        .getPeers()
        .size();
    assertThat(peersSize, greaterThanOrEqualTo(1));
  }

  //test case detail: ${TCMS}/testcase-view-1409-1
  @Test(dependsOnMethods = "testNewPeerPositive", priority = 1)
  public void testAllPeersPositive() throws IOException {
    int size = ckbService
        .getPeers()
        .send()
        .getPeers()
        .size();
    assertThat(size, greaterThanOrEqualTo(1));
  }

  //test case detail: ${TCMS}/testcase-view-1411-1
  @Test(dependsOnMethods = "testNewPeerPositive", priority = 2)
  public void testDelPeer() throws InterruptedException, IOException {
    String stopNode2 = "ps -ef |grep 'node2 run'|grep -v 'grep'|cut -c 9-15|xargs kill -9";
    getPeersCkbSystem.runCommandWithDocker(stopNode2);
    sleep(2000);
    int size = ckbService
        .getPeers()
        .send()
        .getPeers()
        .size();
    Assert.assertEquals(size, 0, "should get no peers info after delete other peer");
  }

}
