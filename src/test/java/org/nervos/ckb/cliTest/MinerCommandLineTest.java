package org.nervos.ckb.cliTest;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;

import org.nervos.ckb.TestBase;
import org.nervos.ckb.framework.system.CKBSystem;
import org.nervos.ckb.framework.system.CKBSystem.ECode;
import org.nervos.ckb.util.NetworkUtils;
import org.nervos.ckb.util.WaitUntil;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MinerCommandLineTest extends TestBase {

  private int idlePort;
  private CKBSystem minerCKBSystem;
  private String dockerName;
  private String ckbDockerImageTagName;

  @BeforeMethod
  public void startNode() {
    idlePort = NetworkUtils.getIdlePort();
    minerCKBSystem = new CKBSystem();
    dockerName = minerCKBSystem.getDockerName();
    ckbDockerImageTagName = minerCKBSystem.getCkbDockerImageTagName();
    minerCKBSystem.enableDebug();

    minerCKBSystem.cleanEnv();

    //run a idle container with miner
    minerCKBSystem.init(dockerName, ckbDockerImageTagName, idlePort);

    //start a node
    minerCKBSystem.ckbInitRun();

    //wait for ckb run is started
    StringBuffer stdoutStringOfRun = new StringBuffer(100);
    waitFor(new WaitUntil() {
      @Override
      public boolean waitUntil() {
        stdoutStringOfRun.delete(0, stdoutStringOfRun.length());
        ECode eCode = minerCKBSystem
            .runCommandWithDocker("cat " + defaultPath + "logs/run.log", "-i");
        if (eCode == ECode.success) {
          stdoutStringOfRun.append(minerCKBSystem.getStdoutString());
        } else {
          stdoutStringOfRun.append(minerCKBSystem.getStandardError());
        }
        return stdoutStringOfRun.toString().split(System.lineSeparator()).length >= 5;
      }
    }, 20, 1);
  }

  @AfterMethod
  public void cleanEnv() {
    minerCKBSystem.cleanEnv();
  }

  // test case detail: ${TCMS}/testcase-view-802-2
  @Test
  public void testMinerComLinePositive() {
    //start miner
    String ckbMiner = "ckb miner";
    minerCKBSystem.runCommandWithDocker(ckbMiner, "-d -it");

    //wait for ckb miner log
    StringBuffer stdoutStringOfMiner = new StringBuffer(100);
    waitFor(new WaitUntil() {
      @Override
      public boolean waitUntil() {
        stdoutStringOfMiner.delete(0, stdoutStringOfMiner.length());
        ECode eCode = minerCKBSystem
            .runCommandWithDocker("cat " + defaultPath + "logs/miner.log", "-i");
        stdoutStringOfMiner.append(minerCKBSystem.getStdoutString());
        return stdoutStringOfMiner.toString().split(System.lineSeparator()).length >= 2;
      }
    }, 120, 1);

    assertThat(stdoutStringOfMiner.toString(), containsString("INFO miner  found seal: Seal"));
  }


  // test case detail: ${TCMS}/testcase-view-803-2
  @Test
  public void testNodeStop() {
    //stop this node
    String stopCkbRun = "ps -ef |grep 'ckb run'|grep -v 'grep'|cut -c 9-15 |xargs kill -9";
    minerCKBSystem.runCommandWithDocker(stopCkbRun);

    //start miner
    String ckbMiner = "ckb miner";
    minerCKBSystem.runCommandWithDocker(ckbMiner, "-d -it");

    //wait for ckb miner log
    StringBuffer stdoutStringOfMiner = new StringBuffer(100);
    waitFor(new WaitUntil() {
      @Override
      public boolean waitUntil() {
        stdoutStringOfMiner.delete(0, stdoutStringOfMiner.length());
        minerCKBSystem
            .runCommandWithDocker("cat " + defaultPath + "logs/miner.log", "-i");
        stdoutStringOfMiner.append(minerCKBSystem.getStdoutString());

        return (stdoutStringOfMiner.toString().split(System.lineSeparator()).length >= 2);
      }
    }, 120, 1);

    assertThat(stdoutStringOfMiner.toString(),
        containsString("rpc call get_block_template error"));

  }

  // test case detail: ${TCMS}/testcase-view-804-1
  @Test
  public void testStartMoreMiner() {
    //start miner
    String ckbMiner = "ckb miner";
    minerCKBSystem.runCommandWithDocker(ckbMiner, "-d -it");

    //start another one miner
    String ckbAnotherMiner = "ckb miner";
    minerCKBSystem.runCommandWithDocker(ckbAnotherMiner, "-d -it");

    //wait for ckb miner log
    StringBuffer stdoutStringOfMiner = new StringBuffer(100);
    waitFor(new WaitUntil() {
      @Override
      public boolean waitUntil() {
        stdoutStringOfMiner.delete(0, stdoutStringOfMiner.length());
        ECode eCode = minerCKBSystem
            .runCommandWithDocker("cat " + defaultPath + "logs/miner.log", "-i");
        stdoutStringOfMiner.append(minerCKBSystem.getStdoutString());

        return stdoutStringOfMiner.toString().split(System.lineSeparator()).length >= 5;
      }
    }, 120, 1);
    int strTimes = getStrTimes(stdoutStringOfMiner.toString(), "main INFO sentry");

    Assert.assertEquals(strTimes, 2, "muti miner run error");
  }

  private int getStrTimes(String originStr, String strToFind) {
    int times =
        (originStr.length() - originStr.replace(strToFind, "").length()) / strToFind.length();
    return times;
  }


}



