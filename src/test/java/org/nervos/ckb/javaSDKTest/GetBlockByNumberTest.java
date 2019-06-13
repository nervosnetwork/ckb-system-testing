package org.nervos.ckb.javaSDKTest;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.math.BigInteger;
import org.nervos.ckb.methods.type.Block;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GetBlockByNumberTest extends JavaSDKTestBase {

  // test case detail: ${TCMS}/testcase-view-1435-1
  @Test(dataProvider = "positiveData")
  public void testGetBlockByNumberPositive(String blockNum) throws Exception {
    waitForBlockHeight(new BigInteger("1"), 60, 1);

    String hash = ckbService.getBlockByNumber(blockNum).send().getBlock().header.hash;
    assertThat(hash, containsString("0x"));
  }

  // test case detail: ${TCMS}/testcase-view-1436-1
  @Test(dataProvider = "negativeData")
  public void testGetBlockByNumberNegative(String blockNum) throws Exception {
    Block block = ckbService.getBlockByNumber(blockNum).send().getBlock();
    Assert.assertNull(block);
  }

  @DataProvider
  public Object[][] positiveData() {
    return new Object[][]{
        {"1"} //block height is 1
    };
  }

  @DataProvider
  public Object[][] negativeData() {
    return new Object[][]{
        {String.valueOf(Long.MAX_VALUE)} //bigger than current block height
    };
  }

}
