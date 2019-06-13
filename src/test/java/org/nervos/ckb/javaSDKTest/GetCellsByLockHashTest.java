package org.nervos.ckb.javaSDKTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import java.math.BigInteger;
import java.util.List;
import org.nervos.ckb.methods.response.CkbCells;
import org.nervos.ckb.methods.type.CellOutputWithOutPoint;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GetCellsByLockHashTest extends JavaSDKTestBase {

  // test case detail: ${TCMS}/testcase-view-1381-1
  // test case detail: ${TCMS}/testcase-view-1384-1
  @Test(dataProvider = "positiveData")
  public void testGetCellByLockHashPositive(String lockHash, String beginBlockHeight,
      String endBlockHeight) throws Exception {
    waitForBlockHeight(new BigInteger("2"), 60, 1);
    List<CellOutputWithOutPoint> cells = ckbService
        .getCellsByLockHash(lockHash, beginBlockHeight, endBlockHeight)
        .send()
        .getCells();
    int size = cells.size();
    assertThat(size, greaterThanOrEqualTo(1));
  }

  // test case detail: ${TCMS}/testcase-view-1382-1
  // test case detail: ${TCMS}/testcase-view-1383-1
  // test case detail: ${TCMS}/testcase-view-1385-1
  // test case detail: ${TCMS}/testcase-view-1386-1
  @Test(dataProvider = "negativeData")
  public void testGetCellByLockHashNegative(String lockHash, String beginBlockHeight,
      String endBlockHeight, String assertContent) throws Exception {
    CkbCells ckbCells = ckbService
        .getCellsByLockHash(lockHash, beginBlockHeight, endBlockHeight)
        .send();
    if ("errorCode".equals(assertContent)) {
      int code = ckbCells.error.code;
      Assert.assertEquals(code, -32602); // Invalid params
    } else if ("assertFromGreaterThanTo".equals(assertContent)) {
      int code = ckbCells.error.code;
      Assert.assertEquals(code, -3); // "from greater than to"
    } else {
      List<CellOutputWithOutPoint> result = ckbCells.result;
      int size = result.size();
      Assert.assertEquals(size, 0);
    }
  }

  @DataProvider
  public Object[][] positiveData() {
    return new Object[][]{
        {lockHash, "1", "2"},
        {lockHash, "1", "101"}// end bigger than current blockHeight
    };
  }

  @DataProvider
  public Object[][] negativeData() {
    return new Object[][]{
        {lockHash, "2", "1", "assertFromGreaterThanTo"},// begin bigger than end
        {lockHash, "99901", "100001", "assertNull"},// begin bigger than current blockHeight
        {"0x123", "1", "2", "errorCode"},// lock hash's length wrong
        {"0x1111111111111111111111111111111111111111111111111111111111111111", "1", "2",
            "assertNull"}// not exit lock hash
    };
  }


}
