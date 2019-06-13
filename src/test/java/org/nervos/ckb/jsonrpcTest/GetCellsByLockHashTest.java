package org.nervos.ckb.jsonrpcTest;

import com.alibaba.fastjson.JSONObject;
import org.nervos.ckb.util.HttpUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GetCellsByLockHashTest extends RPCTestBase {

  // test case detail: ${TCMS}/testcase-view-836-1
  @Test(dataProvider = "positiveData")
  public void testGetCellByLockHashPositive(String positiveData) {

    JSONObject jsonObject = JSONObject
        .parseObject(HttpUtils.sendJson(url, positiveData));

    Assert.assertNull(jsonObject.get("error"));
  }

  // test case detail: ${TCMS}/testcase-view-841-1
  @Test(dataProvider = "blankData")
  public void testGetCellByLockHashBlank(String positiveData) {
    JSONObject jsonObject = JSONObject
        .parseObject(HttpUtils.sendJson(url, positiveData));
    Assert.assertEquals(jsonObject.getJSONArray("result").size(), 0);
  }

  // test case detail: ${TCMS}/testcase-view-837-1
  // test case detail: ${TCMS}/testcase-view-838-1
  // test case detail: ${TCMS}/testcase-view-839-1
  // test case detail: ${TCMS}/testcase-view-840-1
  // test case detail: ${TCMS}/testcase-view-842-1
  // test case detail: ${TCMS}/testcase-view-843-1
  @Test(dataProvider = "negativeData")
  public void testGetCellByLockHashNegative(String negativeData) {
    JSONObject jsonObject = JSONObject
        .parseObject(HttpUtils.sendJson(url, negativeData));

    Assert.assertNotNull(jsonObject.get("error"));

  }

  @DataProvider
  public Object[][] negativeData() {
    return new Object[][]{
        {buildJsonrpcRequest("get_cells_by_lock_hash", lockHash, "1", 1)},
        {buildJsonrpcRequest("get_cells_by_lock_hash", "0x0da2fe99fe549", 1, 1)},
        {buildJsonrpcRequest("get_cells_by_lock_hash", lockHash, 1)},
        {buildJsonrpcRequest("get_cells_by_lock_hash", lockHash, "99999", "1")},
        {buildJsonrpcRequest("get_cells_by_lock_hash", lockHash, "2", "1")},
        {buildJsonrpcRequest("get_cells_by_lock_hash", lockHash, "1", "99999")},

    };
  }

  @DataProvider
  public Object[][] blankData() throws Exception {
    waitForBlockHeight(2, 180, 1);
    return new Object[][]{
        {buildJsonrpcRequest("get_cells_by_lock_hash",
            "0x1234567890123456789012345678901234567890123456789012345678901234", "1", "1")},
    };
  }

  @DataProvider
  public Object[][] positiveData() throws Exception {
    waitForBlockHeight(2, 180, 1);
    return new Object[][]{
        {buildJsonrpcRequest("get_cells_by_lock_hash", lockHash, "1", "2")},
    };
  }


}
