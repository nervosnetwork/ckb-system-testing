package org.nervos.ckb.javaSDKTest;

import org.nervos.ckb.TestBase;
import java.io.IOException;
import java.math.BigInteger;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GetTipBlockNumberTest extends TestBase {

  // test case detail: ${TCMS}/testcase-view-1393-1
  @Test
  public void testGetTipBlockNumberPositive() throws IOException {
    BigInteger blockNum = ckbService.getTipBlockNumber().send().getBlockNumber();
    Assert.assertEquals(blockNum.compareTo(BigInteger.ZERO), 1);
  }
}
