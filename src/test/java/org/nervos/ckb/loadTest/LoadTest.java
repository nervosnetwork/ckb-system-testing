package org.nervos.ckb.loadTest;

import static java.lang.Thread.sleep;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.nervos.ckb.framework.system.CKBSystem;
import org.nervos.ckb.util.CanRunMultipleTimes;
import org.nervos.ckb.util.CapacityUnits;
import org.nervos.ckb.util.HttpUtils;
import org.nervos.ckb.util.ReadConfig;
import org.nervos.ckb.util.SignWitness;
import org.nervos.ckb.util.WaitUntil;
import org.nervos.ckb.util.items.HashIndex;
import org.nervos.ckb.util.items.Inputs;
import org.nervos.ckb.util.items.OutPoint;
import org.nervos.ckb.util.items.Outputs;
import org.nervos.ckb.util.items.Script;
import org.nervos.ckb.util.items.Witness;
import org.nervos.ckb.utils.Numeric;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

public class LoadTest extends LoadTestBase {

  private static String YML_TITLE = "LoadConfig";
  // the response TX hash of send_transaction
  String sendTXRspHash;
  ReadConfig conf = new ReadConfig();
  SoftAssert assertion = new SoftAssert();
  int CKBStartLocal = System.getenv("CKB_START_LOCAL_TESTNET") == null ? 0
      : Integer.parseInt(System.getenv("CKB_START_LOCAL_TESTNET"));
  CKBSystem runLoadCkbSystem;
  CapacityUnits capacityUnits = new CapacityUnits();
  private String version = "0";
  private List<Witness> witnesses = Collections.singletonList(new Witness(Collections.emptyList()));
  // for deps
  private String depsHash;
  private String index = "0";
  // for outputs
  private String[] capacity;
  private String data;
  private Script[] lock;
  private Script type;
  private List<JSONObject> positivePrevious = new ArrayList<>();
  private JSONObject deadPrevious;
  private List url = (List) conf.getYMLValue(YML_TITLE, "rpcUrl");
  private String defaultUrl = (String) url.get(0);
  private String minerLockHash;
  private String minerCodeHash;
  private String minerArgs;
  private String minerPrivateKey;
  private String aliceCodeHash;
  private String aliceArgs;
  private String aliceLockHash;
  private String since = "0";
  private int differentTXsNum = (int) conf.getYMLValue(YML_TITLE, "liveCellsNumber");
  private String sendMultiTXRspHash;
  private int outputNum = (int) conf.getYMLValue(YML_TITLE, "withoutVeriTXsNumber");
  private long originCapacity = 50000;
  private long outputCapacity;

  @BeforeClass
  public void getVariableValue() throws Exception {
    // support for local running
    if (CKBStartLocal == 1) {
      printout("start local test chain");
      //for local running, get the first rpc url's port
      List<Integer> portList = new ArrayList<>();
      portList.add(Integer.valueOf(url.get(0).toString().substring(17)));
      List tmp = new ArrayList();
      tmp.add(url.get(0));
      url.clear();
      url.add(tmp.get(0));
      differentTXsNum = 2;
      startLocalTestChain(portList);
    } else {
      printout("start load test for testnet");
    }

    minerLockHash = (String) conf.getYMLValue(YML_TITLE, "minerLockHash");
    minerCodeHash = (String) conf.getYMLValue(YML_TITLE, "minerCodeHash");
    minerArgs = (String) conf.getYMLValue(YML_TITLE, "minerArgs");
    minerPrivateKey = (String) conf.getYMLValue(YML_TITLE, "minerPrivateKey");
    if ("".equals(minerCodeHash) || minerCodeHash.length() <= 0) {
      printout("The minerCodeHash in the config file is required!");
      throw new Exception();
    }
    printout("BeforeClass minerCodeHash is ", minerCodeHash);
    printout("BeforeClass minerArgs is ", minerArgs);
    if ("".equals(minerLockHash) || minerLockHash.length() <= 0) {
      Script script = new Script(minerCodeHash, Collections.singletonList(minerArgs));
      minerLockHash = script.getLockHash();
    }
    // 0xcb7bce98a778f130d34da522623d7e56705bddfe0dc4781bd2331211134a19a5
    printout("BeforeClass minerLockHash is ", minerLockHash);
    aliceCodeHash = (String) conf.getYMLValue(YML_TITLE, "aliceCodeHash");
    aliceArgs = (String) conf.getYMLValue(YML_TITLE, "aliceArgs");
    Script scriptBob = new Script(aliceCodeHash, Collections.singletonList(aliceArgs));
    aliceLockHash = scriptBob.getLockHash();
    printout("BeforeClass aliceLockHash is ", aliceLockHash);
    // get deps hash
    depsHash = getDepsHash();

    for (int i = 0; i < differentTXsNum; i++) {
      positivePrevious.add(getLiveCellOutPoint(defaultUrl, minerLockHash, i));
      printout("sendTXPositiveData positivePrevious[" + i + "] is ", positivePrevious.get(i));
    }
  }

  @AfterClass
  public void verifyDeadCell() {
    //verify the used previous_output cell should be dead
    for (int i = 0; i < positivePrevious.size(); i++) {
      String deadCellReq = buildJsonrpcRequest("get_live_cell",
          new OutPoint(null, new HashIndex(getCellTxHash(positivePrevious.get(i)),
              getCellIndex(positivePrevious.get(i)))));

      JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(defaultUrl, deadCellReq));
      Assert.assertNull(jsonObject.getJSONObject("result").get("cell"),
          "get_live_cell: " + positivePrevious.get(i) + " should be null");
      Assert.assertEquals(jsonObject.getJSONObject("result").get("status"), "dead");
    }
    if (CKBStartLocal == 1) {
      runLoadCkbSystem.cleanEnv();
    }
  }

  @Test(dataProvider = "multiOutputsData")
  public void testSendTXWithoutVerification(String multiOutputs) {
    printout("aliceLockHash is ", aliceLockHash);
    Iterator<String> iterator = url.iterator();
    String tmpURL;
    while (iterator.hasNext()) {
      tmpURL = iterator.next();
      System.out.println("SendTXPositive tmpURL is： " + tmpURL);
      JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(tmpURL, multiOutputs));
      sendMultiTXRspHash = jsonObject.getString("result");
      JSONObject sendTXRspErr = jsonObject.getJSONObject("error");
      printout("getTXPositiveData sendTXRspHash is ", sendMultiTXRspHash);
      assertion
          .assertNotNull(sendMultiTXRspHash,
              "There is no send positive transaction response returned.");
      // for load test intend to assert null for sendTX(positive) response error
      if (sendTXRspErr != null) {
        assertion.assertNull(sendTXRspErr,
            "There is error when sending tx: " + sendTXRspErr.getString("message"));
      }
    }
    verifyTXCommitted(defaultUrl, sendMultiTXRspHash, 120, assertion);
    long inputCapacity = outputCapacity;
    // send multi txs without tx response verification
    int i = 0;
    while (i < outputNum) {
      HttpUtils.sendJson(defaultUrl, buildJsonrpcRequest("send_transaction",
          buildJson(version,
              buildDeps(),
              buildInputs(sendMultiTXRspHash, aliceArgs, String.valueOf(i)),
              buildMultiOutputs(inputCapacity, 1, false),
              witnesses)));
      i++;
    }
  }

  @DataProvider
  public Iterator<Object[]> multiOutputsData() {
    //get the current live cell
    JSONObject baseLiveCell = getLiveCellOutPoint(defaultUrl, minerLockHash, 0);

    List<Object[]> result = new ArrayList<>();

    JSONObject buildMultiTX = (JSONObject) buildJson(version, buildDeps(),
        buildInputs(getCellTxHash(baseLiveCell), getCellIndex(baseLiveCell)),
        buildMultiOutputs(originCapacity, outputNum, true), new ArrayList<>());
    String computeTxRequest = buildJsonrpcRequest("_compute_transaction_hash", buildMultiTX);
    JSONObject jsonObject = JSONObject
        .parseObject(HttpUtils.sendJson(defaultUrl, computeTxRequest));
    String computeTxHash = jsonObject.getString("result");
    SignWitness signWitness = new SignWitness();
    List<Witness> signedWitness = signWitness
        .SignedWitness(Numeric.toBigInt(minerPrivateKey), computeTxHash, witnesses);
    buildMultiTX.put("witnesses", signedWitness);
    result.add(new Object[]{buildJsonrpcRequest("send_transaction", buildMultiTX)});
    return result.iterator();
  }

  /**
   * assemble outputs item with multi outputs
   */
  public List<JSON> buildMultiOutputs(long originCapacity, long outputNum, boolean divide) {
    List<JSON> outputsList = new ArrayList<>();
    printout("in buildMultiOutputs the originCapacity is: " + originCapacity);
    if (divide) {
      outputCapacity = originCapacity / outputNum;
    } else {
      outputCapacity = originCapacity;
    }
    data = "0x";
    type = null;
    Script bobLock = new Script(aliceCodeHash,
        aliceArgs.length() == 0 ? emptyList() : Collections.singletonList(aliceArgs));
    for (int i = 0; i < outputNum; i++) {
      outputsList.add(
          (JSON) JSONObject
              .toJSON(new Outputs(capacityUnits.capacityToShannons(String.valueOf(outputCapacity)),
                  data, bobLock, type)));
    }
    return outputsList;
  }

  public void verifyTXCommitted(String defaultUrl, String sendTXRspHash, int times,
      SoftAssert assertion) {
    //verify the send transaction response with timeout of (N(times) * 5) seconds for maximum of 5 blocks;
    Boolean nonMaxBlockNum = verifyTXRsp(defaultUrl, sendTXRspHash, times);
    String getTXRsp = buildJsonrpcRequest("get_transaction", sendTXRspHash);
    String rspGetTXString = HttpUtils.sendJson(defaultUrl, getTXRsp);
    JSONObject rspGetTX = JSONObject.parseObject(rspGetTXString);

    if (rspGetTXString.contains("result") && rspGetTX.getJSONObject("result") == null
        && nonMaxBlockNum) {
      // for load test intend to assert fail to report error as block height doesn't reach maxBlockNum
      assertion.assertNotNull(rspGetTX.get("result"),
          "for txRsp " + sendTXRspHash
              + " The the block height doesn't reach the maxBlockNum till time out and tx doesn't been packaged!");
    } else if (rspGetTXString.contains("error")) {
      // for load test intend to assert fail to report the error message due to there is error happened
      assertion.assertNull(rspGetTX.getString("error"),
          "for txRsp " + sendTXRspHash + " There is error for getTX: " + rspGetTX
              .getString("error"));
    } else {
      assertion.assertNotNull(rspGetTX.get("result"),
          "for txRsp " + sendTXRspHash
              + " The transaction doesn't been packaged onto the blockchain till the maxBlockNum!");
    }
    assertion.assertAll();
  }

  // test case: ${TCMS}/testcase-view-862-1
  @CanRunMultipleTimes(invocationCountKey = "positiveInvocationCount", threadPoolSizeKey = "positiveThreadPoolSize")
  @Test(dataProvider = "positiveData")
  public void testSendTXPositive(String positiveData) {
    SoftAssert assertion = new SoftAssert();
    Iterator<String> iterator = url.iterator();
    String tmpURL;
    while (iterator.hasNext()) {
      tmpURL = iterator.next();
      System.out.println("SendTXPositive tmpURL is： " + tmpURL);
      JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(tmpURL, positiveData));
      sendTXRspHash = jsonObject.getString("result");
      JSONObject sendTXRspErr = jsonObject.getJSONObject("error");
      printout("getTXPositiveData sendTXRspHash is ", sendTXRspHash);
      assertion
          .assertNotNull(sendTXRspHash, "There is no send positive transaction response returned.");
      // for load test intend to assert null for sendTX(positive) response error
      if (sendTXRspErr != null) {
        assertion.assertNull(sendTXRspErr,
            "There is error when sending tx: " + sendTXRspErr.getString("message"));
      }
    }

    //verify the send transaction response with timeout of (120(times) * 5) seconds for maximum of 5 blocks;
    verifyTXCommitted(defaultUrl, sendTXRspHash, 120, assertion);

    positivePrevious.set(0, getLiveCellOutPoint(defaultUrl, minerLockHash, 0));
    deadPrevious = positivePrevious.get(0);
  }

  // test case: ${TCMS}/testcase-view-865-1
  @CanRunMultipleTimes(invocationCountKey = "negativeBlankInvocationCount", threadPoolSizeKey = "negativeBlankThreadPoolSize")
  @Test(dataProvider = "negativeBlankData", priority = 1)
  public void testSendTXBlankNegative(String negativeBlankData) {
    Iterator<String> iterator = url.iterator();
    String tmpURL;
    while (iterator.hasNext()) {
      tmpURL = iterator.next();
      System.out.println("SendTXBlankNegative tmpURL is： " + tmpURL);
      JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(tmpURL, negativeBlankData));
      JSONObject errorMsg = (JSONObject) jsonObject.get("error");
      Assert.assertNotNull(errorMsg,
          "Error message of send transaction with blank data should not be null.");
      Assert.assertEquals(errorMsg.get("message"), "InvalidTx(Empty)");
    }
  }


  /**
   * The sum of output is larger than input;
   * test case: ${TCMS}/testcase-view-864-1
   */
  @CanRunMultipleTimes(invocationCountKey = "negativeOverflowInvocationCount", threadPoolSizeKey = "negativeOverflowThreadPoolSize")
  @Test(dataProvider = "overflowNegativeData")
  public void testSendTXOverNegative(String negativeData) {
    Iterator<String> iterator = url.iterator();
    String tmpURL;
    while (iterator.hasNext()) {
      tmpURL = iterator.next();
      printout("SendTXOverNegative tmpURL is： " + tmpURL);
      JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(tmpURL, negativeData));
      JSONObject errorMsg = (JSONObject) jsonObject.get("error");
      Assert.assertNotNull(errorMsg);
      Assert.assertEquals(errorMsg.get("message"), "InvalidTx(OutputsSumOverflow)");
    }
  }

  /**
   * The previous_output of input using the dead status of cell hash;
   * test case: ${TCMS}/testcase-view-863-1
   */
  @CanRunMultipleTimes(invocationCountKey = "negativeDeadPreviousInvocationCount", threadPoolSizeKey = "negativeDeadPreviousThreadPoolSize")
  @Test(dataProvider = "deadPreviousNegativeData", priority = 1, dependsOnMethods = "testSendTXPositive")
  public void testSendTXPreviousNegative(String negativeData) {
    Iterator<String> iterator = url.iterator();
    String tmpURL;
    while (iterator.hasNext()) {
      tmpURL = iterator.next();
      printout("SendTXOverNegative tmpURL is ", tmpURL);
      JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(tmpURL, negativeData));
      JSONObject errorMsg = (JSONObject) jsonObject.get("error");
      Assert.assertNotNull(errorMsg, "The error message response should not be null.");
      assertThat(errorMsg.getString("message"), containsString("Dead(OutPoint"));
    }
  }

  @DataProvider
  public Object[][] negativeBlankData() {
    return new Object[][]{
        {buildJsonrpcRequest("send_transaction",
            buildJson(version, emptyList(), emptyList(), emptyList(), witnesses))},
    };
  }

  @DataProvider
  public Iterator<Object[]> positiveData() {
    Iterator iterator = positivePrevious.iterator();
    while (iterator.hasNext()) {
      Object next = iterator.next();
      printout("size is " + positivePrevious.size() + " and next positivePrevious is ", next);
    }

    List<Object[]> result = new ArrayList<>();
    List<Object> requests = new ArrayList<>();
    for (int i = 0; i < positivePrevious.size(); i++) {
      if (i % 2 == 0) {
        String outputCapacity = String.valueOf((originCapacity - 100));
        JSONObject buildTX = buildSendTXParams(
            getCellTxHash(positivePrevious.get(i)),
            getCellIndex(positivePrevious.get(i)),
            "60", outputCapacity);
        requests.add(buildJsonrpcRequest("send_transaction", buildTX));
      } else {
        String outputCapacity = String.valueOf((originCapacity - 60));
        JSONObject buildTX = buildSendTXParams(
            getCellTxHash(positivePrevious.get(i)),
            getCellIndex(positivePrevious.get(i)),
            "60", outputCapacity);
        requests.add(buildJsonrpcRequest("send_transaction", buildTX));
      }
    }
    Iterator iterator1 = requests.iterator();
    while (iterator1.hasNext()) {
      result.add(new Object[]{iterator1.next()});
    }
    return result.iterator();
  }

  public JSONObject buildSendTXParams(String preHash, String preIndex, String inputCapacity,
      String outputCapacity) {
    JSONObject buildTX = (JSONObject) buildJson(version, buildDeps(),
        buildInputs(preHash, preIndex),
        buildOutputs(inputCapacity, outputCapacity), new ArrayList<>());
    String computeTxRequest = buildJsonrpcRequest("_compute_transaction_hash", buildTX);
    JSONObject jsonObject = JSONObject
        .parseObject(HttpUtils.sendJson(defaultUrl, computeTxRequest));
    String computeTxHash = jsonObject.getString("result");
    SignWitness signWitness = new SignWitness();
    List<Witness> signedWitness = signWitness
        .SignedWitness(Numeric.toBigInt(minerPrivateKey), computeTxHash, witnesses);
    buildTX.put("witnesses", signedWitness);
    printout("signedTX is: " + buildTX);
    return buildTX;
  }

  @DataProvider
  public Object[][] overflowNegativeData() {
    JSONObject previous = getLiveCellOutPoint(defaultUrl, minerLockHash, 0);
    printout("overflowNegativeData previous is: ", previous);
    return new Object[][]{
        {buildJsonrpcRequest("send_transaction",
            buildJson(version,
                buildDeps(),
                buildInputs(getCellTxHash(previous), getCellIndex(previous)),
                buildOutputs("2000000", "4000000"),
                witnesses))},
    };
  }

  @DataProvider
  public Object[][] deadPreviousNegativeData() {
    printout("deadPreviousNegativeData deadPrevious is: ", deadPrevious);
    return new Object[][]{
        {buildJsonrpcRequest("send_transaction",
            buildJson(version,
                buildDeps(),
                buildInputs(getCellTxHash(deadPrevious), getCellIndex(deadPrevious)),
                buildOutputs("1000", "3000"),
                witnesses))},
    };
  }


  /**
   * Return the response of send_transaction after trying to get the new block many times
   *
   * @param TXHash The transaction hash
   * @param times Times to try to get the new block
   * @return Return whether reach the max block number(current block height + 5) or not after auto
   * wait the get_transaction response
   */
  public Boolean verifyTXRsp(String url, String TXHash, int times) {
// Set the current block number plus 5 blocks as overtime if get the transaction result is null
    String getTipBlockNumReq = buildJsonrpcRequest("get_tip_block_number");
    JSONObject rspGetTipBlockNum = JSONObject
        .parseObject(HttpUtils.sendJson(url, getTipBlockNumReq));

    int initCurrentBlockNum = rspGetTipBlockNum.getInteger("result");
// try N times, every time wait for 5s within 5 blocks height above current block.
    int currentBlockNum = waitTXInfo(url, TXHash, initCurrentBlockNum, times);
    Boolean notMaxBlockNum;
    if ((currentBlockNum - initCurrentBlockNum) < 5) {
      notMaxBlockNum = true;
    } else {
      notMaxBlockNum = false;
    }
    return notMaxBlockNum;
  }


  /**
   * Auto wait to get the transaction info within current block number + 5 blocks.
   * <i> waitTXInfo(transactionHash, initCurrentBlockNumber, tryTimes) </i>
   * Note: Every try time wait for 5s, so the maximum total waiting time is (tryTimes * 5) seconds.
   */
  public int waitTXInfo(String url, String TXHash, int initCurrentBlockNum, int tryTimes) {
    String getTipBlockNumReq = buildJsonrpcRequest("get_tip_block_number");
    JSONObject jsonObjectBlockNum;

    String getTXRsp = buildJsonrpcRequest("get_transaction", TXHash);
    JSONObject jsonObjectGetTX;

    int currentBlockNum = initCurrentBlockNum;
    int maxBlockNum = initCurrentBlockNum + 5;
    int initTime = 0;

    if (initTime > tryTimes) {
      printout("The initTime is lager than tryTimes!");
      throw new RuntimeException();
    }

    while (initTime <= tryTimes) {
      jsonObjectBlockNum = JSONObject.parseObject(HttpUtils.sendJson(url, getTipBlockNumReq));
      currentBlockNum = jsonObjectBlockNum.getInteger("result");
      printout("currentBlockNum is", currentBlockNum);

      jsonObjectGetTX = JSONObject.parseObject(HttpUtils.sendJson(url, getTXRsp));
      if (jsonObjectGetTX.getJSONObject("error") != null) {
        printout("request of jsonObjectGetTX is", jsonObjectGetTX);
        printout(
            "There is error message of get_tx response: " + jsonObjectGetTX.getJSONObject("error"));
        break;
      } else if ("committed".equals(
          jsonObjectGetTX.getJSONObject("result").getJSONObject("tx_status").getString("status"))) {
        printout("Now the transaction is committed!");
        break;
      } else if (currentBlockNum > maxBlockNum) {
        printout("Current block number is lager than the maxBlockNum!");
        break;
      } else {
        initTime++;
      }
      try {
        sleep(5000);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return currentBlockNum;
  }

  private Object buildJson(String version, List<JSON> deps, List<JSON> inputs, List<JSON> outputs,
      List<Witness> witnesses) {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("version", version);
    jsonObject.put("deps", deps);
    jsonObject.put("inputs", inputs);
    jsonObject.put("outputs", outputs);
    jsonObject.put("witnesses", witnesses);
    return jsonObject;
  }

  // for verify getLiveCell
  private JSONObject buildJson(String hash, String index) {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("hash", hash);
    jsonObject.put("index", index);
    return jsonObject;
  }

  public JSONObject getLiveCellOutPoint(String url, String lockHash, int index) {

    String getTipBlockNumReq = buildJsonrpcRequest("get_tip_block_number");
    JSONObject rspGetTipBlockNum = JSONObject
        .parseObject(HttpUtils.sendJson(url, getTipBlockNumReq));
    int currentBlockNum = rspGetTipBlockNum.getInteger("result");
    int minBlockNum;
    if (CKBStartLocal == 1) {
      //for local running
      minBlockNum = currentBlockNum - index;
    } else {
      minBlockNum = currentBlockNum - index - 10; // for testnet
    }

    minBlockNum = minBlockNum <= 0 ? 1 : minBlockNum;
    printout("currentBlockNum and minBlockNum is: " + currentBlockNum + " and " + minBlockNum);

    String request = buildJsonrpcRequest("get_cells_by_lock_hash", lockHash,
        String.valueOf(minBlockNum),
        String.valueOf(currentBlockNum));
    JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(url, request));
    JSONObject liveCell = (JSONObject) jsonObject.getJSONArray("result").get(0);
    JSONObject liveCellOutPoint = liveCell.getJSONObject("out_point").getJSONObject("cell");
    printout("liveCellOutPoint is ", liveCellOutPoint);
    printout("liveCell origin capacity is: " + liveCell.getLongValue("capacity"));
    originCapacity = liveCell.getLongValue("capacity") / 100000000;
    printout("deal originCapacity is: " + originCapacity);
    return liveCellOutPoint;
  }

  /**
   * Get the block result
   */
  public JSONObject getBlock(String index) {
    String getBlockHashReq = buildJsonrpcRequest("get_block_hash", index);
    JSONObject blockHashRsp = JSONObject
        .parseObject(HttpUtils.sendJson(defaultUrl, getBlockHashReq));
    String blockHash = blockHashRsp.getString("result");
    printout("BlockHash[" + index + "]", blockHash);

    String getBlockReq = buildJsonrpcRequest("get_block", blockHash);
    JSONObject getBlockRsp = JSONObject.parseObject(HttpUtils.sendJson(defaultUrl, getBlockReq));
    return getBlockRsp;
  }

  /**
   * Get the deps hash
   */
  public String getDepsHash() {
    JSONObject getBlockRsp = getBlock("0");
    String depsHash = ((JSONObject) getBlockRsp.getJSONObject("result")
        .getJSONArray("transactions").get(0)).getString("hash");
    printout("depsHash", depsHash);
    return depsHash;
  }

  /**
   * assemble deps item
   */
  public List<JSON> buildDeps() {
    OutPoint hashIndex = new OutPoint(null, new HashIndex(depsHash, "1"));
    printout("Deps hashIndex List is", jsonToList(hashIndex));
    return jsonToList(hashIndex);
  }

  /**
   * assemble inputs item
   */
  public List<JSON> buildInputs(String previousHash, String previousIndex) {
    return buildInputs(previousHash, previousIndex, since);
  }

  /**
   * assemble inputs item
   */
  public List<JSON> buildInputs(String previousHash, String previousIndex, String validSince) {
    OutPoint previousOutput = new OutPoint(null, new HashIndex(previousHash, previousIndex));
    Inputs inputs = new Inputs(previousOutput, Collections.emptyList(), validSince);
    printout("inputs is: " + inputs);
    return jsonToList(inputs);
  }

  /**
   * assemble outputs item
   */
  public List<JSON> buildOutputs(String inputCapacity, String outputCapacity) {
    capacity = new String[]{capacityUnits.capacityToShannons(inputCapacity),
        capacityUnits.capacityToShannons(outputCapacity)};
    data = "0x";
    Script bobLock = new Script(aliceCodeHash,
        aliceArgs.length() == 0 ? emptyList() : Collections.singletonList(aliceArgs));
    Script minerLock = new Script(minerCodeHash,
        minerArgs.length() == 0 ? emptyList() : Collections.singletonList(minerArgs));
    lock = new Script[]{bobLock, minerLock};
    type = null;

    Outputs outputs0 = new Outputs(capacity[0], data, lock[0], type);
    Outputs outputs1 = new Outputs(capacity[1], data, lock[1], type);

    return jsonToList(outputs0, outputs1);
  }

  public void startLocalTestChain(List<Integer> portList) {
    // start a local chain
    runLoadCkbSystem = new CKBSystem();
    runLoadCkbSystem.enableDebug();
    runLoadCkbSystem.cleanEnv(portList.get(0), runLoadCkbSystem.getDockerName(),
        runLoadCkbSystem.getCkbDockerImageTagName());

    // init a docker container using the first port
    runLoadCkbSystem
        .init(runLoadCkbSystem.getDockerName(), runLoadCkbSystem.getCkbDockerImageTagName(),
            portList.get(0));
    //start a node and miner
    runLoadCkbSystem.ckbInitRun();
    String ckbMiner = "ckb miner";
    runLoadCkbSystem.runCommandWithDocker(ckbMiner, "-d -it");

    //wait for block height is 3
    String getCurrentBlockHeight =
        "curl -d '{\"id\": 2, \"jsonrpc\": \"2.0\", \"method\":\"get_tip_block_number\",\"params\": []}' -H 'content-type:application/json' 'http://127.0.0.1:"
            + portList.get(0) + "'";
    waitFor(new WaitUntil() {
      @Override
      public boolean waitUntil() {
        runLoadCkbSystem.runCommand(getCurrentBlockHeight);
        String stdoutString = runLoadCkbSystem.getStdoutString();
        printout("get the stdoutStr is", stdoutString);
        if (stdoutString == null || stdoutString.isEmpty()) {
          return false;
        }
        int blockHeight = JSONObject.parseObject(stdoutString).getIntValue("result");
        printout("current block heigeh is", blockHeight);
        return blockHeight >= 3;
      }
    }, 240, 5);
  }

}