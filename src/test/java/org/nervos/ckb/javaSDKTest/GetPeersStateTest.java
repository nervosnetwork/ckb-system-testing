package org.nervos.ckb.javaSDKTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import org.nervos.ckb.framework.system.CKBSystem;
import org.nervos.ckb.util.NetworkUtils;
import org.nervos.ckb.service.CKBService;
import org.nervos.ckb.service.HttpService;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GetPeersStateTest extends JavaSDKTestBase {

  private int idlePort = NetworkUtils.getIdlePort();
  private CKBSystem getPeersStateCkbSystem;
  private String dockerName;
  private String ckbDockerImageTagName;
  private String rpcURL = "http://127.0.0.1:" + idlePort;
  CKBService ckbService = CKBService.build(new HttpService(rpcURL));

  @BeforeClass
  public void startNode() throws Exception {
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

    Thread.sleep(5000);
  }

  // FIXME: for version v0.12.0 the get_peers_state is Deprecated, open this case when dev fixed this rpc issue
  // test case detail: ${TCMS}/testcase-view-1432-1
  @Test(priority = 1, enabled = false)
  public void testGetPeersStatePositive() throws Exception {
    startNewPeer(ckbService, getPeersStateCkbSystem, "node2");
    int size = ckbService.getPeersState().send().getPeersState().size();
    assertThat(size, greaterThanOrEqualTo(1));
  }

  // test case detail: ${TCMS}/testcase-view-1431-1
  @Test
  public void testNoPeers() throws Exception {
    int size = ckbService.getPeersState().send().getPeersState().size();
    Assert.assertEquals(size, 0);
  }

}
