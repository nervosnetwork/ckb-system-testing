package org.nervos.ckb.javaSDKTest;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;

import org.testng.annotations.Test;

public class GetBlockchainInfoTest extends JavaSDKTestBase {

  // test case detail: ${TCMS}/testcase-view-1433-1
  @Test
  public void testGetBlockchainInfoPositive() throws Exception {
    String chain = ckbService
        .getBlockchainInfo()
        .send()
        .getBlockchainInfo()
        .chain;
    assertThat(chain, containsString("ckb"));
  }
}
