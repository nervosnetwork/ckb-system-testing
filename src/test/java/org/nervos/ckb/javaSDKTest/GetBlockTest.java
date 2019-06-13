package org.nervos.ckb.javaSDKTest;

import java.math.BigInteger;
import java.util.List;
import org.nervos.ckb.methods.response.CkbBlock;
import org.nervos.ckb.methods.type.Block;
import org.nervos.ckb.methods.type.Transaction;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GetBlockTest extends JavaSDKTestBase {

  //test case detail: ${TCMS}/testcase-view-1376-1
  //test case detail: ${TCMS}/testcase-view-1377-1
  @Test(dataProvider = "positiveData")
  public void testGetBlockPositive(String blockHeight) throws Exception {
    waitForBlockHeight(new BigInteger("1"), 60, 1);

    String blockHash = ckbService.getBlockHash(blockHeight).send().getBlockHash();
    printout("block[" + blockHeight + "]'s hash ", blockHash);
    Block block = ckbService.getBlock(blockHash).send().getBlock();
    List<Transaction> commitTransactions = block.transactions;
    Assert.assertNotNull(commitTransactions);
  }

  //test case detail: ${TCMS}/testcase-view-1378-1
  //test case detail: ${TCMS}/testcase-view-1379-1
  //test case detail: ${TCMS}/testcase-view-1380-1
  @Test(dataProvider = "negativeData")
  public void testGetBlockNegative(String blockHash, String assertContent) throws Exception {
    CkbBlock ckbBlock = ckbService.getBlock(blockHash).send();
    if ("errorCode".equals(assertContent)) {
      int code = ckbBlock.error.code;
      Assert.assertEquals(code, -32602);
    } else {
      Block result = ckbBlock.result;
      Assert.assertNull(result);
    }
  }


  @DataProvider
  public Object[][] positiveData() {
    return new Object[][]{
        {"0"},
        {"1"}
    };
  }

  @DataProvider
  public Object[][] negativeData() {
    return new Object[][]{
        {"0xa4cc6ce5bfef80763d6ea905ca9847cca181c59dcef08416", "errorCode"},//wrong length
        {"0x1111111111111111111111111111111111111111111111111111111111111111", "resultNull"},//does not exist hash
        {null, "errorCode"}//null params
    };
  }
}
