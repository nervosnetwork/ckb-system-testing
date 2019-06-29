package org.nervos.ckb.javaSDKTest;

import org.nervos.ckb.TestBase;
import java.io.IOException;
import org.nervos.ckb.methods.response.CkbEpoch;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GetCurrentEpochTest extends TestBase {

  // test case detail: ${TCMS}/testcase-view-1442-1
  @Test
  public void testGetCurrentEpochPositive() throws IOException {
    CkbEpoch ckbEpoch = ckbService
        .getCurrentEpoch()
        .send();
    long epochReward = Long.parseLong(ckbEpoch.result.epochReward);
    Assert.assertTrue(epochReward <= 125000000000000L,
        "The block reward should be smaller than 125000000000000");
  }

}
