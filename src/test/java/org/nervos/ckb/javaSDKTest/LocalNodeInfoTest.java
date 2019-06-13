package org.nervos.ckb.javaSDKTest;

import org.nervos.ckb.TestBase;
import java.io.IOException;
import org.nervos.ckb.methods.type.NodeInfo;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LocalNodeInfoTest extends TestBase {

  // test case: ${TCMS}/testcase-view-1398-1
  @Test
  public void testLocalNodeInfoPositive() throws IOException {
    NodeInfo nodeInfo = ckbService.localNodeInfo().send().getNodeInfo();
    Assert.assertNotNull(nodeInfo.addresses);
    Assert.assertNotNull(nodeInfo.nodeId);
    Assert.assertNotNull(nodeInfo.version);
  }
}
