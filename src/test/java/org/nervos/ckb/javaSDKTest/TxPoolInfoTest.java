package org.nervos.ckb.javaSDKTest;

import org.nervos.ckb.methods.type.TxPoolInfo;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TxPoolInfoTest extends JavaSDKTestBase {

  // test case detail: ${TCMS}/testcase-view-1434-1
  @Test(dependsOnMethods = "org.nervos.ckb.javaSDKTest.SendTransactionTest.testSendTXPositive")
  public void testTxPoolInfoPositive() throws Exception {
    TxPoolInfo txPoolInfo = ckbService
        .txPoolInfo()
        .send()
        .getTxPoolInfo();

    String lastTxsUpdatedAt = txPoolInfo.lastTxsUpdatedAt;
    long orphan = txPoolInfo.orphan;
    long pending = txPoolInfo.pending;
    long staging = txPoolInfo.staging;
    Assert.assertNotEquals(lastTxsUpdatedAt, "0");
    Assert.assertEquals(orphan, 0);
    Assert.assertEquals(pending, 0);
    Assert.assertEquals(staging, 0);
  }

}
