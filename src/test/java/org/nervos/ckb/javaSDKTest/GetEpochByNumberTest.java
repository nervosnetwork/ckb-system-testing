package org.nervos.ckb.javaSDKTest;

import org.nervos.ckb.TestBase;
import java.io.IOException;
import org.nervos.ckb.methods.response.CkbEpoch;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GetEpochByNumberTest extends TestBase {

  // test case detail: ${TCMS}/testcase-view-1443
  @Test(dataProvider = "getEpochByNumPositiveData")
  public void getEpochByNumPositive(String getEpochByNumPositiveData) throws IOException {
    CkbEpoch ckbEpoch = ckbService
        .getEpochByNumber(getEpochByNumPositiveData)
        .send();
    long blockReward = Long.parseLong(ckbEpoch.result.blockReward);
    Assert.assertTrue(blockReward <= 5000000000000L,
        "The block reward should be smaller than 5000000000000");
  }

  // test case detail: ${TCMS}/testcase-view-1444-1
  // test case detail: ${TCMS}/testcase-view-1445-3
  @Test(dataProvider = "getEpochByNumNegativeData")
  public void getEpochByNumInvalidNegative(String getEpochByNumNegativeData, String assertContent)
      throws IOException {
    CkbEpoch ckbEpoch = ckbService
        .getEpochByNumber(getEpochByNumNegativeData)
        .send();
    if ("assertError".equals(assertContent)) {
      int errorCode = ckbEpoch.error.code;
      Assert.assertEquals(errorCode, -32602);
    } else {
      Assert.assertNull(ckbEpoch.result,
          "should return null result if larger than current epoch num");
    }
  }

  @DataProvider
  public Object[][] getEpochByNumPositiveData() {
    return new Object[][]{
        {"0"}
    };
  }

  @DataProvider
  public Object[][] getEpochByNumNegativeData() {
    return new Object[][]{
        {"-1", "assertError"}, // invalid parameter
        {"10000", "assertNull"}, // larger than current epoch num
    };
  }

}
