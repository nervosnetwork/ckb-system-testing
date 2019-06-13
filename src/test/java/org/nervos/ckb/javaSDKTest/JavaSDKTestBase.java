package org.nervos.ckb.javaSDKTest;

import static java.lang.Thread.sleep;

import org.nervos.ckb.TestBase;
import org.nervos.ckb.framework.system.CKBSystem;
import org.nervos.ckb.util.WaitUntil;
import java.io.IOException;
import java.math.BigInteger;
import org.nervos.ckb.methods.type.TransactionWithStatus.TxStatus;
import org.nervos.ckb.service.CKBService;

public class JavaSDKTestBase extends TestBase {

  /**
   * wait block height is equal to the specified value
   * <i>waitForBlockHeight(minHeight,timeOutInSecond,pollingTimesInSecond)</i>
   *
   * @param blockHeight specified block height value
   * @param timeOutInSecond Maximum waiting time in second
   * @param pollingTimesInSecond polling times in second
   */
  public void waitForBlockHeight(BigInteger blockHeight, int timeOutInSecond,
      int pollingTimesInSecond) throws Exception {
    waitFor(new WaitUntil() {
      @Override
      public boolean waitUntil() {
        try {
          BigInteger currentBlockNumber = ckbService.getTipBlockNumber().send().getBlockNumber();
          printout("the block height", currentBlockNumber);
          int compareResult = currentBlockNumber.compareTo(blockHeight);
          if (compareResult == 0 || compareResult == 1) {
            return true;
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
        return false;
      }
    }, timeOutInSecond, pollingTimesInSecond);

  }


  /**
   * wait for get transaction response is returned
   * <i>waitForBlockHeight(minHeight,timeOutInSecond,pollingTimesInSecond)</i>
   *
   * @param transactionHash specified transaction hash
   * @param maxBlockHeight specified block height value
   * @param timeOutInSecond Maximum waiting time in second
   * @param pollingTimesInSecond polling times in second
   */
  public void waitForGetTX(String transactionHash, BigInteger maxBlockHeight, int timeOutInSecond,
      int pollingTimesInSecond) {
    waitFor(new WaitUntil() {
      @Override
      public boolean waitUntil() {
        try {
          TxStatus ckbTransactionStatus = ckbService
              .getTransaction(transactionHash)
              .send()
              .getTransaction()
              .txStatus;
          if ("committed".equals(ckbTransactionStatus.status)) {
            printout("The SDK transaction is committed!");
            return true;
          }
          BigInteger currentBlockNumber = ckbService
              .getTipBlockNumber()
              .send()
              .getBlockNumber();
          printout("the block height", currentBlockNumber);
          int compareResult = currentBlockNumber.compareTo(maxBlockHeight);
          if (compareResult == 1) {
            return true;
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
        return false;
      }
    }, timeOutInSecond, pollingTimesInSecond);
  }

  /**
   *  Start a new node under the nodePath folder
   *
   * @param ckbService  CKBService
   * @param newPeerCkbSystem  CKBSytem
   * @param nodePath the path of the new node
   * @throws Exception
   */
  public void startNewPeer(CKBService ckbService, CKBSystem newPeerCkbSystem, String nodePath)
      throws InterruptedException, IOException {
    // get local node info
    int size = ckbService.localNodeInfo().send().getNodeInfo().addresses.size();
    printout("size is" + size);
    String localAddress = ckbService.localNodeInfo().send().getNodeInfo().addresses.get(0).address;
    printout("localAddress is ", localAddress);

    // update node2
    String updateAddrCmd = "echo " + localAddress + "| perl -pe 's|\\/|\\\\\\/|g;s|0.0.0.0|127.0.0.1|g'";
    newPeerCkbSystem.runCommandWithDocker(updateAddrCmd);
    String updateAddr = newPeerCkbSystem.getStdoutString();
    newPeerCkbSystem.updateBlockAssemblerConfig();
    String changePort = "cd " + nodePath + " && sed -i 's/8115/8117/;s/8114/8116/' ckb.toml";
    newPeerCkbSystem.runCommandWithDocker(changePort, "-d -it");
    String changeBoot = "cd " + nodePath + " && sed -i 's/bootnodes = \\[]/bootnodes = \\[\\\"" + updateAddr
        + "\\\"]/' ckb.toml";
    newPeerCkbSystem.runCommandWithDocker(changeBoot, "-d -it");
    // start node2
    String ckbNode2Run = "ckb -C " + nodePath + " run";
    newPeerCkbSystem.runCommandWithDocker(ckbNode2Run, "-d -it");
    sleep(5000);
  }

}
