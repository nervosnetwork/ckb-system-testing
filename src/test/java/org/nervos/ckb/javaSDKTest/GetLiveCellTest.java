package org.nervos.ckb.javaSDKTest;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import org.nervos.ckb.TestBase;
import org.nervos.ckb.methods.response.CkbCell;
import org.nervos.ckb.methods.type.OutPoint;
import org.nervos.ckb.methods.type.cell.Cell;
import org.nervos.ckb.methods.type.cell.CellOutPoint;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GetLiveCellTest extends TestBase {

  private String index = "0";

  // test case: ${TCMS}/testcase-view-1387-1
  @Test(dataProvider = "positiveGetLiveCellData", priority = 1)
  public void testGetLiveCellPositive(String positiveGetLiveCellData) throws IOException {
    Cell cell = ckbService
        .getLiveCell(new OutPoint(new CellOutPoint(positiveGetLiveCellData, index)))
        .send()
        .getCell();
    Assert.assertEquals(cell.status, "live");
  }

  // test case: ${TCMS}/testcase-view-1389-1
  @Test(dataProvider = "negativeUnknownData", priority = 1)
  public void testGetLiveCellNegativeUnknown(String negativeUnknownData) throws IOException {
    Cell cell = ckbService
        .getLiveCell(new OutPoint(null, new CellOutPoint(negativeUnknownData, index)))
        .send()
        .getCell();
    Assert.assertEquals(cell.status, "unknown");
  }

  // test case: ${TCMS}/testcase-view-1388-1
  // test case: ${TCMS}/testcase-view-1390-1
  // test case: ${TCMS}/testcase-view-1391-1
  @Test(dataProvider = "negativeInvalidLengthData", priority = 1)
  public void testGetLiveCellNegativeLength(String negativeInvalidLengthData) throws IOException {
    CkbCell cell = ckbService
        .getLiveCell(new OutPoint(null, new CellOutPoint(negativeInvalidLengthData, index)))
        .send();
    assertThat(cell.error.message,
        containsString("expected a 0x-prefixed hex string with 64 digits"));
  }

  // test case: ${TCMS}/testcase-view-1392-1
  @Test(
      dataProvider = "positiveDeadCellData", priority = 1,
      dependsOnMethods = "org.nervos.ckb.javaSDKTest.SendTransactionTest.testSendTXPositive")
  public void testGetLiveCellPositiveDead(CellOutPoint positiveDeadCellData) throws IOException {
    Cell cell = ckbService
        .getLiveCell(new OutPoint(null, positiveDeadCellData))
        .send()
        .getCell();
    // from CKB v0.15.0, if the previous_output is fully dead, the cell's status will be unknown instead of dead
    Assert.assertEquals(cell.status, "unknown");
  }

  @DataProvider
  public Object[][] positiveGetLiveCellData() throws IOException {
    String blockHash = ckbService
        .getBlockHash("0")
        .send()
        .getBlockHash();
    System.out.println("blockHash: " + blockHash);
    String txHash = ckbService
        .getBlock(blockHash)
        .send()
        .getBlock()
        .transactions
        .get(0)
        .hash;
    System.out.println("txHash: " + txHash);
    return new Object[][]{
        {txHash},
    };
  }

  @DataProvider
  public Object[][] negativeUnknownData() {
    return new Object[][]{
        {"0xc53a74864e9ee9f9080303f65a57820b8fd0e40304714577e6b0f96165231c9d"
        },
    };
  }

  @DataProvider(parallel = true)
  public Object[][] negativeInvalidLengthData() {
    return new Object[][]{
        {"0xc53a74864e9ee9f9080303f65a57820b8fd0e40304714577e6b0f96165231c9d3"
        },
        {""},
        {"  "},
    };
  }

  @DataProvider
  public Object[][] positiveDeadCellData() {
    CellOutPoint deadPrevious = sdkPositivePrevious;
    printout("In SDK GetLiveCellTest the deadPrevious hash and index: "
        + deadPrevious.txHash + ", " + deadPrevious.index);
    return new Object[][]{
        {deadPrevious},
    };
  }

}
