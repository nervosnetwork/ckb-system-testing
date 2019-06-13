package org.nervos.ckb.cliTest;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import com.alibaba.fastjson.JSONObject;
import org.nervos.ckb.TestBase;
import org.nervos.ckb.framework.system.CKBSystem;
import org.nervos.ckb.framework.system.CKBSystem.ECode;
import org.nervos.ckb.util.NetworkUtils;
import org.nervos.ckb.util.WaitUntil;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

public class ImportCommanLineTest extends TestBase {

  private int idlePort = NetworkUtils.getIdlePort();
  private CKBSystem importCkbSystem;
  private String dockerName;
  private String ckbDockerImageTagName;
  private int theRightBlockHeight;
  private SoftAssert softAssert = new SoftAssert();

  private String getCurrentBlockHeight =
      "curl -d '{\"id\": 2, \"jsonrpc\": \"2.0\", \"method\":\"get_tip_block_number\",\"params\": []}' -H 'content-type:application/json' 'http://127.0.0.1:"
          + idlePort + "'";


  @BeforeMethod
  public void exportDB() {
    importCkbSystem = new CKBSystem();
    dockerName = importCkbSystem.getDockerName();
    ckbDockerImageTagName = importCkbSystem.getCkbDockerImageTagName();
    importCkbSystem.enableDebug();

    importCkbSystem.cleanEnv();

    //run a idle container with miner
    importCkbSystem.init(dockerName, ckbDockerImageTagName, idlePort);

    //start ckb run and miner
    importCkbSystem.ckbInitRun();
    String ckbMiner = "ckb miner";
    importCkbSystem.runCommandWithDocker(ckbMiner, "-d -it");

    //wait for block height is 2
    waitFor(new WaitUntil() {
      @Override
      public boolean waitUntil() {
        importCkbSystem.runCommand(getCurrentBlockHeight);
        String stdoutString = importCkbSystem.getStdoutString();
        printout("get the stdoutStr is", stdoutString);
        if (stdoutString == null || stdoutString.isEmpty()) {
          return false;
        }
        int blockHeight = JSONObject.parseObject(stdoutString).getIntValue("result");
        printout("current block heigeh is", blockHeight);
        return blockHeight >= 2;
      }
    }, 240, 1);

    //stop this miner
    String stopMiner = "ps -ef |grep 'ckb miner'|grep -v 'grep'|cut -c 9-15 |xargs kill -9";
    importCkbSystem.runCommandWithDocker(stopMiner);

    //get current block height
    importCkbSystem.runCommand(getCurrentBlockHeight);
    String stdoutStringOfBlockHeight = importCkbSystem.getStdoutString();
    theRightBlockHeight = JSONObject.parseObject(stdoutStringOfBlockHeight).getIntValue("result");
    printout("after stop miner,the block height is", theRightBlockHeight);

    //stop this node
    String stopCkbRun = "ps -ef |grep 'ckb run'|grep -v 'grep'|cut -c 9-15 |xargs kill -9";
    importCkbSystem.runCommandWithDocker(stopCkbRun);

    //delete ckb.log
    String deleteCKBLog = "cd " + defaultPath + "logs && rm -rf run.log";
    importCkbSystem.runCommandWithDocker(deleteCKBLog);

    //export data
    String export = "ckb export /tmp/export --format json "
        + " && cat /tmp/export/*";
    importCkbSystem.runCommandWithDocker(export);
    String backupContent = importCkbSystem.getStdoutString();

  }

  @AfterMethod
  public void cleanEnv() {
    //clean env
    importCkbSystem.cleanEnv();
  }

  // test case detail: ${TCMS}/testcase-view-812-5-testtask-44
  // test case detail: ${TCMS}/testcase-view-813-4-testtask-44
  @Test(dataProvider = "positiveData")
  public void testImportComLinePositive(String config) throws Exception {

    //delete db
    String deleteDB = "cd " + defaultPath + "db && rm -rf *";
    importCkbSystem.runCommandWithDocker(deleteDB);

    //import db
    String importDB = "ckb import " + config + " /tmp/export/ckb_dev.json --format json ";
    importCkbSystem.runCommandWithDocker(importDB);

    //restart node
    String ckbRun = "ckb run";
    importCkbSystem.runCommandWithDocker(ckbRun, "-d -it");

    //wait for ckb run is started
    StringBuffer stdoutStringOfRun = new StringBuffer(100);
    waitFor(new WaitUntil() {
      @Override
      public boolean waitUntil() {
        stdoutStringOfRun.delete(0, stdoutStringOfRun.length());
        ECode eCode = importCkbSystem
            .runCommandWithDocker("cat " + defaultPath + "logs/run.log", "-i");
        if (eCode == ECode.success) {
          stdoutStringOfRun.append(importCkbSystem.getStdoutString());
        } else {
          stdoutStringOfRun.append(importCkbSystem.getStandardError());
        }
        return stdoutStringOfRun.toString().split(System.lineSeparator()).length >= 4;
      }
    }, 20, 1);

    //get current block height
    importCkbSystem.runCommand(getCurrentBlockHeight);
    String stdoutStringOfBlockHeight = importCkbSystem.getStdoutString();
    int blockHeight = JSONObject.parseObject(stdoutStringOfBlockHeight).getIntValue("result");
    printout("delete db and import ,get the block height is", blockHeight);

    Assert.assertEquals(blockHeight, theRightBlockHeight,
        "after import db,the blockHeight should be equals theRightBlockHeight");

  }

  //test case detail: ${TCMS}/testcase-view-815-1-testtask-44
  //test case detail: ${TCMS}/testcase-view-814-1-testtask-44
  @Test(dataProvider = "deleteLastLineOfExportedFileData")
  public void testDeleteLastLineOfExportedFile(Boolean isDeleteDB) {
    //delete the last line of the exported file
    String deleteTheLastLine = "cd /tmp/export/ && head -n 2 ckb_dev.json>ckb2.json && mv ckb2.json ckb_dev.json -f"; //"sed -i '$d' /tmp/export/ckb.json && cat /tmp/export/ckb.json";
    importCkbSystem.runCommandWithDocker(deleteTheLastLine, "-i");

    if (isDeleteDB) {
      //delete db
      String deleteDB = "cd " + defaultPath + "db && rm -rf *";
      importCkbSystem.runCommandWithDocker(deleteDB);
    }

    //import db
    String importDB = "ckb import /tmp/export/ckb_dev.json --format json ";
    importCkbSystem.runCommandWithDocker(importDB);

    //restart node
    String ckbRun = "ckb run";
    importCkbSystem.runCommandWithDocker(ckbRun, "-d -it");

    //wait for ckb run is started
    StringBuffer stdoutStringOfRun = new StringBuffer(100);
    waitFor(new WaitUntil() {
      @Override
      public boolean waitUntil() {
        stdoutStringOfRun.delete(0, stdoutStringOfRun.length());
        ECode eCode = importCkbSystem
            .runCommandWithDocker("cat " + defaultPath + "logs/ckb.log", "-i");
        if (eCode == ECode.success) {
          stdoutStringOfRun.append(importCkbSystem.getStdoutString());
        } else {
          stdoutStringOfRun.append(importCkbSystem.getStandardError());
        }
        return stdoutStringOfRun.toString().split(System.lineSeparator()).length >= 4;
      }
    }, 20, 1);

    //get current block height
    importCkbSystem.runCommand(getCurrentBlockHeight);
    String stdoutStringOfBlockHeight = importCkbSystem.getStdoutString();
    int blockHeight = JSONObject.parseObject(stdoutStringOfBlockHeight).getIntValue("result");
    printout("delete db and the last line of the exported file ,get the block height is",
        blockHeight);

    if (isDeleteDB) {
      assertThat(blockHeight, greaterThanOrEqualTo(theRightBlockHeight - 1));
    } else {
      Assert.assertEquals(blockHeight, theRightBlockHeight,
          "after import db,the blockHeight is equals theRightBlockHeight");
    }

  }

  //test case detail:${TCMS}/testcase-view-820-2
  @Test
  public void testDeleteMiddleLineOfExportedFile() {
    //delete middle line of exported file
    String deleteTheMiddleLine = "cd /tmp/export/ && head -n 1 ckb_dev.json>ckb2.json && tail -n 1 ckb_dev.json>>ckb2.json && mv ckb2.json ckb_dev.json -f"; //"sed -i '$d' /tmp/export/ckb.json && cat /tmp/export/ckb.json";;
    importCkbSystem.runCommandWithDocker(deleteTheMiddleLine, "-i");

    //delete db
    String deleteDB = "cd " + defaultPath + "db && rm -rf *";
    importCkbSystem.runCommandWithDocker(deleteDB);

    //import db
    String importDB = "ckb import /tmp/export/ckb_dev.json --format json ";
    importCkbSystem.runCommandWithDocker(importDB);
    String stdoutString = importCkbSystem.getStdoutString();
    assertThat(stdoutString, containsString("'ImportChainService' panicked at 'parent already store'"));
  }

  //test case detail:${TCMS}/testcase-view-819-1
  @Test
  public void testIncorrectFormat() {
    //change the format of exported file
    String changeTheFormat = "cd /tmp/export/ && echo \"abc\" >> ckb.json";
    importCkbSystem.runCommandWithDocker(changeTheFormat);

    //delete db
    String deleteDB = "cd " + defaultPath + "db && rm -rf *";
    importCkbSystem.runCommandWithDocker(deleteDB);

    //import db
    String importDB = "ckb import /tmp/export/ckb.json --format json ";
    importCkbSystem.runCommandWithDocker(importDB);
    String stdErrorString = importCkbSystem.getStdErrorString();
    printout("change the format of exported file,the error meaasge is ", stdErrorString);

    assertThat(stdErrorString, containsString("Import error"));
  }

  //test case detail:${TCMS}/testcase-view-818-1
  @Test
  public void testMinerRunningToImport() {
    //start miner
    String ckbMiner = "ckb miner";
    importCkbSystem.runCommandWithDocker(ckbMiner, "-d -it");

    //delete db
    String deleteDB = "cd " + defaultPath + "db && rm -rf *";
    importCkbSystem.runCommandWithDocker(deleteDB);

    //import db
    String importDB = "ckb import /tmp/export/ckb_dev.json --format json ";
    importCkbSystem.runCommandWithDocker(importDB);
    String stdErrorString = importCkbSystem.getStdErrorString();

    //restart node
    String ckbRun = "ckb run";
    importCkbSystem.runCommandWithDocker(ckbRun, "-d -it");

    //wait for ckb run is started
    StringBuffer stdoutStringOfRun = new StringBuffer(100);
    waitFor(new WaitUntil() {
      @Override
      public boolean waitUntil() {
        stdoutStringOfRun.delete(0, stdoutStringOfRun.length());
        ECode eCode = importCkbSystem
            .runCommandWithDocker("cat " + defaultPath + "logs/ckb.log", "-i");
        stdoutStringOfRun.append(importCkbSystem.getStdoutString());
        return stdoutStringOfRun.toString().split(System.lineSeparator()).length >= 4;
      }
    }, 20, 1);

    //get current block height
    importCkbSystem.runCommand(getCurrentBlockHeight);
    String stdoutStringOfBlockHeight = importCkbSystem.getStdoutString();
    int blockHeight = JSONObject.parseObject(stdoutStringOfBlockHeight).getIntValue("result");
    printout("get the block height is", blockHeight);

    assertThat(blockHeight, greaterThanOrEqualTo(2));
  }

  //test case detail: ${TCMS}/testcase-view-817-3-testtask-44
  @Test
  public void testCKbRunningToImport() {
    //restart node
    String ckbRun = "ckb run";
    importCkbSystem.runCommandWithDocker(ckbRun, "-d -it");

    //wait for ckb run is started
    StringBuffer stdoutStringOfRun = new StringBuffer(100);
    waitFor(new WaitUntil() {
      @Override
      public boolean waitUntil() {
        stdoutStringOfRun.delete(0, stdoutStringOfRun.length());
        ECode eCode = importCkbSystem
            .runCommandWithDocker("cat " + defaultPath + "logs/ckb.log", "-i");
        if (eCode == ECode.success) {
          stdoutStringOfRun.append(importCkbSystem.getStdoutString());
        } else {
          stdoutStringOfRun.append(importCkbSystem.getStandardError());
        }
        return stdoutStringOfRun.toString().split(System.lineSeparator()).length >= 4;
      }
    }, 20, 1);

    //import db
    String importDB = "ckb import /tmp/export/ckb.json --format json ";
    importCkbSystem.runCommandWithDocker(importDB);
    String stdoutString = importCkbSystem.getStdoutString();
    assertThat(stdoutString, containsString("IO error"));
  }

  //test case detail: ${TCMS}/testcase-view-816-3-testtask-44
  @Test
  public void testNoJsonFileToImport() {
    //import db(there is no ckb2.json file )
    String importDB = "ckb import /tmp/export/ckb2.json --format json ";
    importCkbSystem.runCommandWithDocker(importDB);
    String stdErrorString = importCkbSystem.getStdErrorString();
    printout("no json file to import,the error message is" + stdErrorString);

    assertThat(stdErrorString, containsString("No such file or directory"));
  }

  @DataProvider
  public Object[][] positiveData() {
    return new Object[][]{
        {""},
        {"-C /home/ckb"}
    };
  }

  @DataProvider
  public Object[][] deleteLastLineOfExportedFileData() {
    return new Object[][]{
        {true},
        {false}
    };
  }

}



