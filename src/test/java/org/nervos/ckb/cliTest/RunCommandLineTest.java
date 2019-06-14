package org.nervos.ckb.cliTest;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import com.alibaba.fastjson.JSONObject;
import org.nervos.ckb.TestBase;
import org.nervos.ckb.framework.system.CKBSystem;
import org.nervos.ckb.framework.system.CKBSystem.ECode;
import org.nervos.ckb.util.NetworkUtils;
import org.nervos.ckb.util.WaitUntil;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class RunCommandLineTest extends TestBase {

  // test case detail: ${TCMS}/testcase-view-792-2
  @Test(dataProvider = "positiveData")
  public void testRunComLinePositive(String cmd, String assertStr) {
    //start a idle container
    CKBSystem runCkbSystem = new CKBSystem();
    int idlePort = NetworkUtils.getIdlePort();
    runCkbSystem
        .init(runCkbSystem.getDockerName(), runCkbSystem.getCkbDockerImageTagName(), idlePort);


    //start a node
    runCkbSystem.ckbInitRun();

    //wait for run logs
    StringBuffer stdoutString = new StringBuffer(100);
    waitFor(new WaitUntil() {
      @Override
      public boolean waitUntil() {
        stdoutString.delete(0, stdoutString.length());
        ECode eCode = runCkbSystem.runCommandWithDocker(cmd, "-i");
        stdoutString.append(runCkbSystem.getStdoutString());
        return stdoutString.toString().split(System.lineSeparator()).length >= 8;
      }
    }, 20, 1);

    runCkbSystem.cleanEnv();
    assertThat(stdoutString.toString(), containsString(assertStr));

  }

  // test case detail: ${TCMS}/testcase-view-796-2
  @Test
  public void testRestartCkbRun() {
    //start a idle container
    CKBSystem runCkbSystem = new CKBSystem();
    // runCkbSystem.enableDebug();
    runCkbSystem.setRunInDocker(true);
    int idlePort = NetworkUtils.getIdlePort();
    runCkbSystem
        .init(runCkbSystem.getDockerName(), runCkbSystem.getCkbDockerImageTagName(), idlePort);

    //start ckb run and miner
    String ckbInitForce = "ckb init --force";
    String ckbRun = "ckb run";
    String ckbMiner = "ckb miner";
    runCkbSystem.runCommandWithDocker(ckbInitForce, "-d -it");
    runCkbSystem.updateBlockAssemblerConfig();
    runCkbSystem.runCommandWithDocker(ckbRun, "-d -it");
    runCkbSystem.runCommandWithDocker(ckbMiner, "-d -it");

    //wait for block height
    String getCurrentBlockHeight =
        "curl -d '{\"id\": 2, \"jsonrpc\": \"2.0\", \"method\":\"get_tip_block_number\",\"params\": []}' -H 'content-type:application/json' 'http://127.0.0.1:"
            + idlePort + "'";
    waitFor(new WaitUntil() {
      @Override
      public boolean waitUntil() {
        runCkbSystem.runCommand(getCurrentBlockHeight);
        String stdoutString = runCkbSystem.getStdoutString();
        printout("get the stdoutStr is", stdoutString);
        if (stdoutString == null || stdoutString.isEmpty()) {
          return false;
        }
        int blockHeight = JSONObject.parseObject(stdoutString).getIntValue("result");
        printout("current block heigeh is", blockHeight);
        return blockHeight >= 2;
      }
    }, 120, 1);

    // stop miner
    String stopCkbMiner = "ps -ef |grep 'ckb miner'|grep -v 'grep'|cut -c 9-15 |xargs kill -9";
    runCkbSystem.runCommandWithDocker(stopCkbMiner);

    // get current blockheight
    runCkbSystem.runCommand(getCurrentBlockHeight);
    String stdoutString = runCkbSystem.getStdoutString();
    int currentBlockHeight = JSONObject.parseObject(stdoutString).getIntValue("result");

    //restart ckb
    String stopCkbRun = "ps -ef |grep 'ckb run'|grep -v 'grep'|cut -c 9-15 |xargs kill -9";
    runCkbSystem.runCommandWithDocker(stopCkbRun);
    runCkbSystem.runCommandWithDocker(ckbRun, "-d -it");
    runCkbSystem.runCommand("sleep 3");

    //get blockHeight
    runCkbSystem.runCommand(getCurrentBlockHeight);
    stdoutString = runCkbSystem.getStdoutString();
    int restartCkbBlockHeight = JSONObject.parseObject(stdoutString).getIntValue("result");

    Assert.assertEquals(currentBlockHeight, restartCkbBlockHeight);
  }

  @DataProvider
  public Object[][] positiveData() {
    return new Object[][]{
        {"cat " + defaultPath + "logs/run.log", "INFO network  Listen on address"}
    };
  }


}
