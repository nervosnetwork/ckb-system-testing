package org.nervos.ckb.javaSDKTest;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;

import java.math.BigInteger;
import org.nervos.ckb.methods.Response;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GetBlockHashTest extends JavaSDKTestBase {

  // test case detail: ${TCMS}/testcase-view-1373-1
  // test case detail: ${TCMS}/testcase-view-1374-1
  @Test(dataProvider = "positiveData")
  public void testGetBlockHashPositive(String blockHeight) throws Exception {
    waitForBlockHeight(new BigInteger("2"), 60, 1);

    String blockHash = ckbService.getBlockHash(blockHeight).send().getBlockHash();
    printout("block[" + blockHeight + "]'s hash ", blockHash);
    assertThat(blockHash, containsString("0x"));
  }

  // test case detail: ${TCMS}/testcase-view-1375-1
  @Test(dataProvider = "negativeData")
  public void testGetBlockHashNegative(String blockHeight) throws Exception {
    Response ckbBlockHash = ckbService.getBlockHash(blockHeight).send();
    Object result = ckbBlockHash.result;
    Assert.assertNull(result);
  }

  @DataProvider
  public Object[][] positiveData() {
    return new Object[][]{
        {"0"},//block height is 0
        {"1"}//block height is 1
    };
  }

  @DataProvider
  public Object[][] negativeData() {
    return new Object[][]{
        {String.valueOf(Long.MAX_VALUE)}//bigger than current block height
    };
  }
}
