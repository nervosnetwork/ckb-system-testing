package org.nervos.ckb.javaSDKTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.nervos.ckb.methods.Response.Error;
import org.nervos.ckb.methods.response.CkbTransactionHash;
import org.nervos.ckb.methods.type.CellInput;
import org.nervos.ckb.methods.type.CellOutPoint;
import org.nervos.ckb.methods.type.CellOutput;
import org.nervos.ckb.methods.type.CellOutputWithOutPoint;
import org.nervos.ckb.methods.type.OutPoint;
import org.nervos.ckb.methods.type.Script;
import org.nervos.ckb.methods.type.Transaction;
import org.nervos.ckb.methods.type.TransactionWithStatus.TxStatus;
import org.nervos.ckb.methods.type.Witness;
import org.nervos.ckb.util.CapacityUnits;
import org.nervos.ckb.utils.Numeric;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SendTransactionTest extends JavaSDKTestBase {

  CapacityUnits capacityUnits = new CapacityUnits();
  private List<Witness> witnesses = Collections.singletonList(new Witness(Collections.emptyList()));
  //  private String depsHash;
  private String index = "0";
  // for outputs
  private String bobCodeHash = codeHash;
  private String bobArgs = "0x36c329ed630d6ce750712a477543672adab57f4c";
  private String bobAddr = "ckt1q9gry5zgxmpjnmtrp4kww5r39frh2sm89tdt2l6v234ygf";
  private String minerCodeHash = codeHash;
  private String minerArgs = args;
  private String validSince = "0";
  private String data = "0x";
  private String version = "0";
  private long originCapacity = 50000;
  private String getCellMinBlock = "1";
  private String getCellMaxBlock = "20";

  @BeforeClass
  public void setOriginCapacity() throws IOException {
    originCapacity = getOriginCapacity();
  }

  // test case: ${TCMS}/testcase-view-1419-1
  @Test(dataProvider = "negativeBlankData")
  public void testSendTXBlankNegative(Transaction negativeBlankData) throws IOException {
    Error sendTXRsp = ckbService
        .sendTransaction(negativeBlankData)
        .send()
        .error;
    Assert.assertEquals(sendTXRsp.code, -3);
    assertThat(sendTXRsp.message, containsString("InvalidTx(Empty)"));
  }

  /**
   * The sum of output is larger than input; test case: ${TCMS}/testcase-view-1418-1
   */
  @Test(dataProvider = "overflowNegativeData")
  public void testSendTXOverFlowNegative(Transaction overflowNegativeData) throws IOException {
    Error sendTXRsp = ckbService
        .sendTransaction(overflowNegativeData)
        .send()
        .error;
    Assert.assertEquals(sendTXRsp.message, "InvalidTx(OutputsSumOverflow)");
  }

  // test case: ${TCMS}/testcase-view-1416-1
  @Test(dataProvider = "positiveData")
  public void testSendTXPositive(Transaction positiveData) throws Exception {
    CkbTransactionHash ckbTransaction = ckbService
        .sendTransaction(positiveData)
        .send();
    if (ckbTransaction.result == null) {
      printout(ckbTransaction.error.message);
    }
    String txHash = ckbTransaction.getTransactionHash();
    printout("sdk testSendTXPositive ckbTransactionHash is : " + txHash);
    Assert.assertNotNull(ckbTransaction.getTransactionHash(),
        "SDK: There is no send positive transaction response returned.");
    BigInteger currentBlockNum = ckbService
        .getTipBlockNumber()
        .send()
        .getBlockNumber();
    BigInteger maxBlockNum = currentBlockNum.add(BigInteger.valueOf(5));
    waitForGetTX(txHash, maxBlockNum, 240, 5);
    TxStatus ckbGetTransactionStatus = ckbService
        .getTransaction(txHash)
        .send()
        .getTransaction()
        .txStatus;
    Assert.assertEquals(ckbGetTransactionStatus.status, "committed",
        "SDK: The tx doesn't been packaged onto the blockchain or block height doesn't reach the maxBlockNum till time out!");
  }

  /**
   * The previous_output of input using the dead status of cell hash; test case:
   * ${TCMS}/testcase-view-1417-1
   */
  @Test(dataProvider = "deadPreviousNegativeData", priority = 1, dependsOnMethods = "testSendTXPositive")
  public void testSendTXPreviousNegative(Transaction negativeData) throws Exception {
    Error sendTXRsp = ckbService
        .sendTransaction(negativeData)
        .send()
        .error;
    Assert.assertEquals(sendTXRsp.code, -3);
    assertThat(sendTXRsp.message, CoreMatchers.containsString("Dead(OutPoint"));
  }

  @DataProvider
  public Object[][] negativeBlankData() {
    Transaction blankTransaction = new Transaction(version, Collections.emptyList(),
        Collections.emptyList(), Collections.emptyList(), witnesses);
    return new Object[][]{
        {blankTransaction},
    };
  }

  @DataProvider
  public Object[][] overflowNegativeData() throws Exception {
    CellOutPoint previous = getLiveCellOutPoint(lockHash);
    Transaction overTransaction = new Transaction(
        version,
        buildDeps(),
        buildInputs(previous.txHash, previous.index),
        buildOutputs(2000000, 4000000),
        witnesses);
    return new Object[][]{
        {overTransaction},
    };
  }

  @DataProvider
  public Object[][] positiveData() throws Exception {
    sdkPositivePrevious = getLiveCellOutPoint(lockHash);
    long outputCapacity = originCapacity - 100;
    Transaction positiveTx = new Transaction(version,
        buildDeps(),
        buildInputs(sdkPositivePrevious.txHash, sdkPositivePrevious.index),
        buildOutputs(60, outputCapacity),
        Collections.singletonList(new Witness(Collections.emptyList())));
    String txHash = ckbService.computeTransactionHash(positiveTx).send().getTransactionHash();
    printout("positiveTx computeTransactionHash is: " + txHash);
    Transaction signedTx = positiveTx.sign(Numeric.toBigInt(privateKey), txHash);
    printout("positiveTx.witnesses.get(0).data : " + signedTx.witnesses.get(0).data);
    return new Object[][]{
        {signedTx},
    };
  }

  @DataProvider
  public Object[][] deadPreviousNegativeData() throws Exception {
    CellOutPoint deadPrevious = sdkPositivePrevious;
    printout("SDK deadPreviousNegativeData deadPrevious is: ", deadPrevious);
    Transaction deadPreviousTx = new Transaction(version,
        buildDeps(),
        buildInputs(deadPrevious.txHash, deadPrevious.index),
        buildOutputs(20000, 30000),
        Collections.singletonList(new Witness(Collections.emptyList())));
    String txHash = ckbService.computeTransactionHash(deadPreviousTx).send().getTransactionHash();
    printout("deadPreviousTx computeTransactionHash is: " + txHash);
    Transaction signedTx = deadPreviousTx.sign(Numeric.toBigInt(privateKey), txHash);
    printout("deadPreviousTx.witnesses.get(0).data : " + signedTx.witnesses.get(0).data);
    return new Object[][]{
        {signedTx}
    };
  }

  public List<CellInput> buildInputs(String cellHash, String index) {
    OutPoint previousOutput = new OutPoint(new CellOutPoint(cellHash, index));
    CellInput cellInput = new CellInput(previousOutput, validSince);
    return Arrays.asList(cellInput);
  }

  public List<CellOutput> buildOutputs(long inputCapacity, long outputCapacity) {
    Script bobLock = new Script(bobCodeHash,
        bobArgs.length() == 0 ? Collections.emptyList() : Collections.singletonList(bobArgs));
    Script minerLock = new Script(minerCodeHash,
        minerArgs.length() == 0 ? Collections.emptyList() : Collections.singletonList(minerArgs));
    CellOutput bobOutPut = new CellOutput(
        capacityUnits.capacityToShannons(String.valueOf(inputCapacity)), data, bobLock);
    CellOutput minerOutPut = new CellOutput(
        capacityUnits.capacityToShannons(String.valueOf(outputCapacity)), data, minerLock);
    return Arrays.asList(bobOutPut, minerOutPut);
  }

  public CellOutPoint getLiveCellOutPoint(String lockHash) throws Exception {
    waitForBlockHeight(BigInteger.valueOf(2), 60, 2);
    CellOutPoint sdkLiveCellOutPoint = ckbService
        .getCellsByLockHash(lockHash, getCellMinBlock, getCellMaxBlock)
        .send()
        .getCells()
        .get(0)
        .outPoint
        .cell;
    printout("sdkLiveCellOutPoint cell tx_hash is: " + sdkLiveCellOutPoint.txHash);
    return sdkLiveCellOutPoint;
  }

  private String getDepsHash() throws Exception {
    waitForBlockHeight(BigInteger.valueOf(2), 60, 2);
    String blockHash = ckbService.getBlockHash("0").send().getBlockHash();
    String depsHash = ckbService
        .getBlock(blockHash)
        .send()
        .getBlock()
        .transactions
        .get(0)
        .hash;
    return depsHash;
  }

  private List<OutPoint> buildDeps() throws Exception {
    OutPoint outPoint = new OutPoint(null, new CellOutPoint(getDepsHash(), "1"));
    return Arrays.asList(outPoint);
  }

  public long getOriginCapacity() throws IOException {
    List<CellOutputWithOutPoint> cells = ckbService
        .getCellsByLockHash(lockHash, getCellMinBlock, getCellMaxBlock)
        .send()
        .getCells();
    originCapacity = Long.valueOf(cells.get(0).capacity);
    printout("SDK liveCell origin capacity is: " + originCapacity);
    originCapacity = originCapacity / 100000000;
    printout("SDK deal originCapacity is: " + originCapacity);
    return originCapacity;
  }


}
