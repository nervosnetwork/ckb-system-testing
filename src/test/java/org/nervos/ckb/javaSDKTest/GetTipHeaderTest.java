package org.nervos.ckb.javaSDKTest;

import org.nervos.ckb.methods.type.Header;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GetTipHeaderTest extends JavaSDKTestBase {

  // test case detail: ${TCMS}/testcase-view-1394-1
  @Test
  public void testGetTipHeaderPositive() throws Exception {
    Header header = ckbService.getTipHeader().send().getHeader();
    Assert.assertNotNull(header);
  }
}
