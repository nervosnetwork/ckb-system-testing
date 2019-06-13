package org.nervos.ckb;

import com.alibaba.fastjson.JSONObject;
import org.nervos.ckb.framework.system.CKBSystem;
import org.nervos.ckb.framework.system.CKBSystem.ECode;
import org.nervos.ckb.util.WaitUntil;
import org.nervos.ckb.util.items.Script;
import java.util.Collections;
import org.nervos.ckb.methods.type.CellOutPoint;
import org.nervos.ckb.service.CKBService;
import org.nervos.ckb.service.HttpService;
import org.testng.Reporter;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

public class TestBase {

  public static CKBSystem ckbSystem = new CKBSystem();
  public static String containerName;
  public static String url = System.getenv("CKB_CHAIN_URL") == null ? "http://127.0.0.1:8114"
      : System.getenv("CKB_CHAIN_URL");
  public static String codeHash = "0x9e3b3557f11b2b3532ce352bfe8017e9fd11d154c4c7f9b7aaaa1e621b539a08";
  public static String args = "0x7f52f0fccdd1d11391c441adfb174f87bca612b0";
  public static String lockHash;
  public static String privateKey = "5c2514fb16b83259d3326a0acf05901c15a87dc46239b77b0a501cd58198dca0";
  // the previous live cell outpoint
  public static JSONObject positivePrevious;
  public static CellOutPoint sdkPositivePrevious;
  public static String ckbBasePath = "/home/ckb/";
  public static String defaultPath = ckbBasePath + "data/";
  public static CKBService ckbService = CKBService.build(new HttpService(url));
  public static String version = "0";

  public void printout(String objName, Object value) {
    if (value == null) {
      System.out.println("object is null");
      return;
    }
    System.out.println(objName + " Value: " + JSONObject.toJSONString(value));
  }

  public void printout(String loginfo) {
    System.out.println(loginfo);
  }

  @BeforeSuite
  public void globalSetup() throws Exception {
    ckbSystem.enableDebug();
    // init ckb running env
    ckbSystem.cleanEnv(ckbSystem.getRpcPort(), "run-ckb-", ckbSystem.getCkbDockerImageTagName());
    // ckbSystem.enableDebug();
    ckbSystem.init();
//    ckbSystem.initEnv(); // for CKB v0.13.0 this is not suitable
    containerName = ckbSystem.getDockerName();

    ckbSystem.startNodeAndMiner();
    // report ckb version and docker image tag
    reporterLog();
    lockHash = returnLockHash();
  }

  @AfterSuite
  public void globalTearDown() {
    ECode result = ckbSystem.cleanEnv();
    System.out.println("result:" + result);
  }

  @BeforeMethod
  public void beforeMethod() {
    ckbSystem.enableDebug();
    ckbSystem.setRunInDocker(true);
  }

  public void reporterLog() {
    // display the log in the reporter
    ckbSystem.runCommandWithDocker("ckb --version");
    Reporter.log("CKB version is: " + ckbSystem.getStdoutString(), true);
    Reporter.log("CKB Docker Image tag is: " + ckbSystem.getCkbDockerImageTagName(), true);
  }

  /**
   * recursive call WaitUnitl method in timeout
   * <i>waitFor(IntelligentWait wait, long timeOutInSecond, int pollingTimesInSecond)</i>
   *
   * @param timeOutInSecond Maximum waiting time in second
   * @param pollingTimesInSecond polling times in second
   */
  public boolean waitFor(WaitUntil wait, int timeOutInSecond, int pollingTimesInSecond) {
    int currentTime = 0;
    while (currentTime < timeOutInSecond) {
      if (wait.waitUntil()) {
        return true;
      }
      currentTime += pollingTimesInSecond;
      try {
        Thread.sleep(pollingTimesInSecond * 1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    return false;
  }

  public String returnLockHash() throws Exception {
    String configCodeHash;
    String configArgs;

    // Support for TestNet (from CKB v0.12.0)
    String initCmd = "ckb init";
    ckbSystem.runCommandWithDocker(initCmd);
    ckbSystem.runCommand("sleep 3");
    
    String getBinaryHashCmd = "cat ckb.toml |sed '/^code_hash =/!d;s/.*= //' |sed 's/[\\\",]//g'";
    ckbSystem.runCommandWithDocker(getBinaryHashCmd);
    configCodeHash = ckbSystem.getStdoutString();
    printout("codeHash form config", configCodeHash);

    String getArgsCmd = "cat ckb.toml |sed '/^args = /!d;s/.*= //;s/[][]//g;s/\\\"//g'";
    ckbSystem.runCommand("sleep 3");
    ckbSystem.runCommandWithDocker(getArgsCmd);
    configArgs = ckbSystem.getStdoutString();
    printout("args form config", configArgs);

    Script script = new Script(configCodeHash, Collections.singletonList(configArgs));
    lockHash = script.getLockHash();
    printout("for version from CKB v0.12.0 the lockHash is ", lockHash);
    if ("".equals(lockHash) || lockHash.length() <= 0) {
      printout("The lockhash is null!");
      throw new Exception();
    }

    return lockHash;
  }

}
