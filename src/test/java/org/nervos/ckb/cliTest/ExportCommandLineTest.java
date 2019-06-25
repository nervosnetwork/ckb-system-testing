package org.nervos.ckb.cliTest;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;

import com.alibaba.fastjson.JSONObject;
import org.nervos.ckb.TestBase;
import org.nervos.ckb.framework.system.CKBSystem;
import org.nervos.ckb.util.NetworkUtils;
import org.nervos.ckb.util.WaitUntil;
import java.util.ArrayList;
import java.util.Arrays;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ExportCommandLineTest extends TestBase {

  private int idlePort;
  private CKBSystem exportCkbSystem;
  private String dockerName;
  private String ckbDockerImageTagName;


  @BeforeMethod
  public void startNodeAndMiner() {
    idlePort = NetworkUtils.getIdlePort();
    exportCkbSystem = new CKBSystem();
    dockerName = exportCkbSystem.getDockerName();
    ckbDockerImageTagName = exportCkbSystem.getCkbDockerImageTagName();
    // exportCkbSystem.enableDebug();

    exportCkbSystem.cleanEnv();

    //run a idle container with miner
    exportCkbSystem.init(dockerName, ckbDockerImageTagName, idlePort);

    exportCkbSystem.ckbInitRun();
    String ckbMiner = "ckb miner";
    exportCkbSystem.runCommandWithDocker(ckbMiner, "-d -it");

    //wait for block height is 2
    String getCurrentBlockHeight =
        "curl -d '{\"id\": 2, \"jsonrpc\": \"2.0\", \"method\":\"get_tip_block_number\",\"params\": []}' -H 'content-type:application/json' 'http://127.0.0.1:"
            + idlePort + "'";
    waitFor(new WaitUntil() {
      @Override
      public boolean waitUntil() {
        exportCkbSystem.runCommand(getCurrentBlockHeight);
        String stdoutString = exportCkbSystem.getStdoutString();
        printout("get the stdoutStr is", stdoutString);
        if (stdoutString == null || stdoutString.isEmpty()) {
          return false;
        }
        int blockHeight = JSONObject.parseObject(stdoutString).getIntValue("result");
        printout("current block heigeh is", blockHeight);
        return blockHeight >= 2;
      }
    }, 300, 1);
  }

  @AfterMethod
  public void afterMethod() {
    //clean env
    exportCkbSystem.cleanEnv();
  }


  // test case detail: ${TCMS}/testcase-view-808-1
  // test case detail: ${TCMS}/testcase-view-807-3
  // test case detail: ${TCMS}/testcase-view-811-3
  @Test(dataProvider = "positiveData")
  public void testExportComLinePositive(String config, boolean isStopMiner)
      throws InterruptedException {
    //stop this node
    exportCkbSystem.stopCkbRun();

    if (isStopMiner) {
      //stop this miner
      exportCkbSystem.stopCkbMiner();
    }

    //export data
    String export =
        "ckb export /tmp/export " + config + " --format json " + " && cat /tmp/export/*";
    exportCkbSystem.runCommandWithDocker(export);
    Thread.sleep(5000);
    String backupContent = exportCkbSystem.getStdoutString();

    assertThat(backupContent, containsString("\"number\":2"));
  }

  // test case detail: ${TCMS}/testcase-view-810-1
  // test case detail: ${TCMS}/testcase-view-809-3
  @Test(dataProvider = "negativeData")
  public void testExportComLineNegative(Boolean stopNode) throws Exception {
    //export data
    String export = "ckb export /tmp/export --format json && cat /tmp/export/*";

    //before stop this node, to export
    if (!stopNode) {
      exportCkbSystem.runCommandWithDocker(export);
      String stdoutString = exportCkbSystem.getStdoutString();
      assertThat(stdoutString, containsString("IO error"));

    } else {
      //stop this node
      exportCkbSystem.stopCkbRun();

      //export
      exportCkbSystem.runCommandWithDocker(export);

      //export again
      exportCkbSystem.runCommandWithDocker(export);

      ArrayList<String> standardError = exportCkbSystem.getStandardError();
      assertThat(Arrays.deepToString(standardError.toArray()),
          containsString("code: 17, kind: AlreadyExists"));
    }

  }

  @DataProvider
  public Object[][] positiveData() {
    Boolean stopMiner = true;
    return new Object[][]{
        {"", stopMiner},
        {" -C /home/ckb", stopMiner},
        {"", !stopMiner}
    };
  }


  @DataProvider
  public Object[][] negativeData() {
    return new Object[][]{
        {false},
        {true}
    };
  }


}
