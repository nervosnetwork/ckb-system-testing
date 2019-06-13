package org.nervos.ckb.cliTest;

import org.nervos.ckb.TestBase;
import org.nervos.ckb.framework.system.CKBSystem.ECode;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class HelpComLineTest extends TestBase {

  // test case detail: ${TCMS}/testcase-view-787-1
  // test case detail: ${TCMS}/testcase-view-789-1
  @Test(dataProvider = "positiveData")
  public void testHelpComLinePositive(String cmd, String assertStr) {
    StringBuffer stdoutString = new StringBuffer(100);
    ECode eCode = ckbSystem.runCommandWithDocker(cmd);

    if (eCode == ECode.success) {
      stdoutString.append(ckbSystem.getStdoutString());
    } else {
      stdoutString.append(ckbSystem.getStandardError());
    }
    Assert.assertTrue(stdoutString.toString().contains(assertStr));
  }

  // test case detail: ${TCMS}/testcase-view-1316-1
  @Test(dataProvider = "negativeData")
  public void testHelpComLineNegative(String cmd, String assertStr) {
    StringBuffer stdoutString = new StringBuffer(100);
    ECode eCode = ckbSystem.runCommandWithDocker(cmd);

    if (eCode == ECode.success) {
      stdoutString.append(ckbSystem.getStdoutString());
    } else {
      stdoutString.append(ckbSystem.getStandardError());
    }
    Assert.assertTrue(stdoutString.toString().contains(assertStr));
  }

  @DataProvider
  public Object[][] positiveData() {
    return new Object[][]{
        {"ckb help", "Nervos CKB - The Common Knowledge Base"},
        {"ckb", "Nervos CKB - The Common Knowledge Base"}
    };
  }

  @DataProvider
  public Object[][] negativeData() {
    return new Object[][]{
        {"ckb xxx",
            "error: Found argument 'xxx' which wasn't expected, or isn't valid in this context"}
    };
  }
}
