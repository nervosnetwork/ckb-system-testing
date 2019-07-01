package org.nervos.ckb.jsonrpcTest;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.nervos.ckb.framework.system.CKBSystem.ECode;
import org.nervos.ckb.util.CapacityUnits;
import org.nervos.ckb.util.HttpUtils;
import org.nervos.ckb.util.SignWitness;
import org.nervos.ckb.util.items.HashIndex;
import org.nervos.ckb.util.items.Inputs;
import org.nervos.ckb.util.items.OutPoint;
import org.nervos.ckb.util.items.Outputs;
import org.nervos.ckb.util.items.Script;
import org.nervos.ckb.util.items.Witness;
import org.nervos.ckb.utils.Numeric;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SendTransactionTest extends RPCTestBase {

  private static final String CKB_V080_RELEASE_DATE = "2019-03-28";
  // the response TX hash of send_transaction
  String sendTXRspHash;
  CapacityUnits capacityUnits = new CapacityUnits();
  private String version = "0";
  private List<Witness> witnesses = Collections.singletonList(new Witness(Collections.emptyList()));
  // for deps
  private String depsHash;
  private String index = "0";
  // for outputs
  private String aliceCodeHash = codeHash;
  private String aliceArgs = "0x3f1573b44218d4c12a91919a58a863be415a2bc3";
  private String minerCodeHash = codeHash;
  private String minerArgs = args;
  private String since = "0";
  private String[] capacity;
  private String data;
  private Script[] lock;
  private Script type;
  private long originCapacity = 50000;
  private String getCellMinBlock = "12";
  private String getCellMaxBlock = "20";

  @BeforeClass
  public void getVariableValue() {
    try {
      // getLiveCell
      positivePrevious = getLiveCellOutPoint(lockHash);
      printout("positivePrevious tx_hash and index is: "
          + positivePrevious.getString("tx_hash")
          + positivePrevious.getString("index"));
      // get deps hash
      depsHash = getDepsHash();
    } catch (Exception e) {
      e.printStackTrace();
    }

    originCapacity = getOriginCapacity();
  }

  // test case: ${TCMS}/testcase-view-865-1
  @Test(dataProvider = "negativeBlankData", priority = 1)
  public void testSendTXBlankNegative(String negativeBlankData) {
    JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(url, negativeBlankData));
    JSONObject errorMsg = (JSONObject) jsonObject.get("error");
    Assert.assertNotNull(errorMsg,
        "Error message of send transaction with blank data should not be null.");
    Assert.assertEquals(errorMsg.get("message"), "InvalidTx(Empty)");
  }

  /**
   * test case: ${TCMS}/testcase-view-862-1
   */
  @Test(dataProvider = "positiveData")
  public void testSendTXPositive(String positiveData) throws Exception {
    JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(url, positiveData));
    sendTXRspHash = jsonObject.getString("result");
    printout("testSendTXPositive sendTXRspHash:" + sendTXRspHash);
    Assert.assertNotNull(sendTXRspHash, "There is no send positive transaction response returned.");

    JSONObject getTXRspResult = verifyTXRsp(sendTXRspHash, 35);
    String txStatus = getTXRspResult.getJSONObject("tx_status").getString("status");
    if (getTXRspResult == null) {
      throw new Exception();
    } else {
      Assert.assertEquals(txStatus, "committed",
          "The transaction doesn't been packaged onto the blockchain till the maxBlockNum!");
    }

  }

  /**
   * The sum of output is larger than input; test case: ${TCMS}/testcase-view-864-1
   */
  @Test(dataProvider = "overflowNegativeData")
  public void testSendTXNegative(String negativeData) {
    JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(url, negativeData));
    JSONObject errorMsg = (JSONObject) jsonObject.get("error");
    Assert.assertNotNull(errorMsg);
    Assert.assertEquals(errorMsg.get("message"), "InvalidTx(OutputsSumOverflow)");
  }

  /**
   * The previous_output of input using the dead status of cell hash; test case:
   * ${TCMS}/testcase-view-863-1
   */
  @Test(dataProvider = "deadPreviousNegativeData", priority = 1, dependsOnMethods = "testSendTXPositive")
  public void testSendTXPreviousNegative(String negativeData) throws Exception {
    JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(url, negativeData));

    ECode eCode = ckbSystem.runCommandWithDocker("ckb --version");
    ckbSystem
        .runCommand("echo \"" + ckbSystem.getStdoutString() + "\"|awk '{print $4}'|cut -b 1-10");
    String date = ckbSystem.getStdoutString();
    printout("The CKB version date is ", date);
    if (date.compareTo(CKB_V080_RELEASE_DATE) >= 0) {
      verifyDeadPrevTXRspAfterCKBv080RelsDate(jsonObject);
    } else {
      throw new Exception("Not support for version older than v0.8.0");
    }
  }

  // support for after CKB v0.8.0 which published after CKB_V080_RELEASE_DATE(include)
  public void verifyDeadPrevTXRspAfterCKBv080RelsDate(JSONObject jsonObject) {
    JSONObject errorMsg = (JSONObject) jsonObject.get("error");
    Assert.assertNotNull(errorMsg, "The error message response should not be null.");
    Assert.assertEquals(errorMsg.getIntValue("code"), -3);
    assertThat(errorMsg.getString("message"), containsString("Dead(OutPoint"));
  }

  @DataProvider
  public Object[][] negativeBlankData() {
    return new Object[][]{
        {buildJsonrpcRequest("send_transaction",
            buildJson(version, Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), witnesses))},
    };
  }

  @DataProvider
  public Object[][] positiveData() {
    printout("sendTXPositiveData positivePrevious is: " + positivePrevious);
    String outputCapacity = String.valueOf((originCapacity - 100));
    JSONObject buildTX = buildJson(version,
        buildDeps(),
        buildInputs(getCellTxHash(positivePrevious), getCellIndex(positivePrevious), since),
        buildOutputs("60", outputCapacity),
        witnesses);
    String computeTxRequest = buildJsonrpcRequest("_compute_transaction_hash", buildTX);
    JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(url, computeTxRequest));
    String computeTxHash = jsonObject.getString("result");

    SignWitness signWitness = new SignWitness();
    List<Witness> signedWitness = signWitness
        .SignedWitness(Numeric.toBigInt(privateKey), computeTxHash, witnesses);
    buildTX.put("witnesses", signedWitness);
    printout("positiveData signedTX is: " + buildTX);
    String txRequest = buildJsonrpcRequest("send_transaction", buildTX);
    return new Object[][]{
        {txRequest},
    };
  }

  @DataProvider
  public Object[][] overflowNegativeData() throws Exception {
    JSONObject previous = getLiveCellOutPoint(lockHash);
    printout("overflowNegativeData previous is: ", previous);
    return new Object[][]{
        {buildJsonrpcRequest("send_transaction",
            buildJson(version, buildDeps(), buildInputs(getCellTxHash(previous), index, since),
                buildOutputs("2000000", "4000000"), witnesses))},
    };
  }

  @DataProvider
  public Object[][] deadPreviousNegativeData() {
    JSONObject deadPrevious = positivePrevious;
    printout("deadPreviousNegativeData deadPrevious is: ", deadPrevious);

    JSONObject buildDeadPrevTX = buildJson(version,
        buildDeps(),
        buildInputs(getCellTxHash(deadPrevious), getCellIndex(positivePrevious), since),
        buildOutputs("1000", "3000"),
        new ArrayList<>());

    String computeTxRequest = buildJsonrpcRequest("_compute_transaction_hash", buildDeadPrevTX);
    JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(url, computeTxRequest));
    String computeDeadTxHash = jsonObject.getString("result");
    printout("deadPreviousTx computeTransactionHash is: " + computeDeadTxHash);

    SignWitness signWitness = new SignWitness();
    List<Witness> signedWitness = signWitness
        .SignedWitness(Numeric.toBigInt(privateKey), computeDeadTxHash, witnesses);
    buildDeadPrevTX.put("witnesses", signedWitness);
    return new Object[][]{
        {buildJsonrpcRequest("send_transaction", buildDeadPrevTX)},
    };
  }

  /**
   * Return the response of send_transaction after trying to get the new block many times
   *
   * @param TXHash The transaction hash
   * @param times Times to try to get the new block
   * @return Return the response of send_transaction if new block generated, otherwise return null
   */
  public JSONObject verifyTXRsp(String TXHash, int times) {
// Set the current block number plus 5 blocks as overtime if get the transaction result is null
    String getTipBlockNumReq = buildJsonrpcRequest("get_tip_block_number");
    JSONObject rspGetTipBlockNum = JSONObject
        .parseObject(HttpUtils.sendJson(url, getTipBlockNumReq));

    int initCurrentBlockNum = rspGetTipBlockNum.getInteger("result");
// try N times, every time wait for 5s within 5 blocks height above current block.
    int currentBlockNum = waitTXInfo(TXHash, initCurrentBlockNum, times);

    String getTXRsp = buildJsonrpcRequest("get_transaction", TXHash);
    JSONObject rspGetTX = JSONObject.parseObject(HttpUtils.sendJson(url, getTXRsp));
    JSONObject getTXResult = rspGetTX.getJSONObject("result");
    String txStatus = getTXResult.getJSONObject("tx_status").getString("status");

    if (!"committed".equals(txStatus) && currentBlockNum == initCurrentBlockNum) {
      printout("TX doesn't committed and there is no new block generated till the maxBlockNum!");
      return null;
    } else {
      return getTXResult;
    }

  }


  /**
   * Auto wait to get the transaction info within current block number + 5 blocks.
   * <i> waitTXInfo(transactionHash, initCurrentBlockNumber, tryTimes) </i>
   * Note: Every try time wait for 5s, so the maximum total waiting time is (tryTimes * 5) seconds.
   */
  public int waitTXInfo(String TXHash, int initCurrentBlockNum, int tryTimes) {
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
      printout("currentBlockNum is: " + currentBlockNum);

      jsonObjectGetTX = JSONObject.parseObject(HttpUtils.sendJson(url, getTXRsp))
          .getJSONObject("result");

      printout("jsonObjectGetTX is: " + jsonObjectGetTX);
      if ("committed".equals(jsonObjectGetTX.getJSONObject("tx_status").getString("status"))) {
        printout("Now the RPC transaction is committed!");
        break;
      } else if (currentBlockNum > maxBlockNum) {
        printout("Current block number is lager than the maxBlockNum!");
        break;
      } else {
        initTime++;
      }
      try {
        Thread.sleep(5000);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return currentBlockNum;
  }

  private JSONObject buildJson(String version, List<JSON> deps, List<JSON> inputs,
      List<JSON> outputs,
      List<Witness> witnesses) {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("version", version);
    jsonObject.put("deps", deps);
    jsonObject.put("inputs", inputs);
    jsonObject.put("outputs", outputs);
    jsonObject.put("witnesses", witnesses);
    return jsonObject;
  }

  public JSONObject getLiveCellOutPoint(String lockHash) throws Exception {
    waitForBlockHeight(12, 360, 5);
    String request = buildJsonrpcRequest("get_cells_by_lock_hash", lockHash, getCellMinBlock,
        getCellMaxBlock);
    JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(url, request));
    JSONObject liveCellOutPoint = ((JSONObject) jsonObject.getJSONArray("result").get(0))
        .getJSONObject("out_point").getJSONObject("cell");
    printout("liveCellOutPoint is: " + liveCellOutPoint);
    return liveCellOutPoint;
  }

  /**
   * Get the block result
   */
  public JSONObject getBlock(String index) {
    String getBlockHashReq = buildJsonrpcRequest("get_block_hash", index);
    JSONObject blockHashRsp = JSONObject.parseObject(HttpUtils.sendJson(url, getBlockHashReq));
    String blockHash = blockHashRsp.getString("result");
    printout("BlockHash[" + index + "]", blockHash);

    String getBlockReq = buildJsonrpcRequest("get_block", blockHash);
    JSONObject getBlockRsp = JSONObject.parseObject(HttpUtils.sendJson(url, getBlockReq));
    return getBlockRsp;
  }

  /**
   * Get the deps hash
   */
  public String getDepsHash() {
    JSONObject getBlockRsp = getBlock("0");
    String depsHash = ((JSONObject) getBlockRsp.getJSONObject("result")
        .getJSONArray("transactions").get(0)).getString("hash");
    printout("depsHash ", depsHash);
    return depsHash;
  }

  /**
   * assemble deps item
   */
  public List<JSON> buildDeps() {
    OutPoint hashIndex = new OutPoint(null, new HashIndex(depsHash, "1"));
    printout("Deps hashIndex List is: " + jsonToList(hashIndex));
    return jsonToList(hashIndex);
  }

  /**
   * assemble inputs item
   */
  public List<JSON> buildInputs(String previous, String index, String validSince) {
    OutPoint previousOutput = new OutPoint(null, new HashIndex(previous, index));
    Inputs inputs = new Inputs(previousOutput, Collections.emptyList(), validSince);
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
        aliceArgs.length() == 0 ? Collections.emptyList() : Collections.singletonList(aliceArgs));
    Script minerLock = new Script(minerCodeHash,
        minerArgs.length() == 0 ? Collections.emptyList() : Collections.singletonList(minerArgs));
    lock = new Script[]{bobLock, minerLock};
    type = null;

    Outputs outputs0 = new Outputs(capacity[0], data, lock[0], type);
    Outputs outputs1 = new Outputs(capacity[1], data, lock[1], type);

    return jsonToList(outputs0, outputs1);
  }


  public long getOriginCapacity() {
    String request = buildJsonrpcRequest("get_cells_by_lock_hash", lockHash,
        getCellMinBlock, getCellMaxBlock);
    JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendJson(url, request));
    JSONObject liveCell = (JSONObject) jsonObject.getJSONArray("result").get(0);
    JSONObject liveCellOutPoint = liveCell.getJSONObject("out_point").getJSONObject("cell");
    printout("liveCellOutPoint is ", liveCellOutPoint);
    printout("liveCell origin capacity is: " + liveCell.getLongValue("capacity"));
    originCapacity = liveCell.getLongValue("capacity") / 100000000;
    printout("deal originCapacity is: " + originCapacity);
    return originCapacity;
  }

}