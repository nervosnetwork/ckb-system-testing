package org.nervos.ckb.javaSDKTest;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.math.BigInteger;
import org.nervos.ckb.methods.Response.Error;
import org.nervos.ckb.methods.type.TransactionWithStatus;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GetTransactionTest extends JavaSDKTestBase {

  // test case detail: ${TCMS}/testcase-view-1413-1
  @Test(dataProvider = "existPositiveData")
  public void testGetTransactionExistPositive(String existPositiveData) throws IOException {
    String txHash = ckbService
        .getTransaction(existPositiveData)
        .send()
        .getTransaction()
        .transaction
        .hash;
    Assert.assertNotNull(txHash);
  }

  // test case detail: ${TCMS}/testcase-view-1415-1
  @Test(dataProvider = "nonExistPositiveData")
  public void testGetTransactionNonExistPositive(String nonExistPositiveData) throws IOException {
    TransactionWithStatus nonExistResult = ckbService
        .getTransaction(nonExistPositiveData)
        .send()
        .result;
    Assert.assertNull(nonExistResult, "Non-exist tx hash should return null result.");
  }

  // test case detail: ${TCMS}/testcase-view-1414-1
  @Test(dataProvider = "negativeData")
  public void testGetTransactionNegative(String negativeData) throws IOException {
    Error getTxError = ckbService
        .getTransaction(negativeData)
        .send()
        .error;
    Assert.assertNotNull(getTxError);
    Assert.assertEquals(getTxError.code, -32602);
    assertThat(getTxError.message,
        containsString("expected a 0x-prefixed hex string with 64 digits"));
  }

  @DataProvider
  public Object[][] existPositiveData() throws Exception {
    waitForBlockHeight(BigInteger.valueOf(1), 60, 2);
    String blockHash = ckbService
        .getBlockHash("1")
        .send()
        .getBlockHash();
    String txHash = ckbService
        .getBlock(blockHash)
        .send()
        .getBlock()
        .transactions
        .get(0)
        .hash;
    return new Object[][]{
        {txHash}
    };
  }

  @DataProvider
  public Object[][] nonExistPositiveData() throws Exception {
    return new Object[][]{
        {"0x1234567890123456789012345678901234567890123456789012345678901234"}
    };
  }

  @DataProvider
  public Object[][] negativeData() {
    return new Object[][]{
        {"0x1234567"}
    };
  }

}
