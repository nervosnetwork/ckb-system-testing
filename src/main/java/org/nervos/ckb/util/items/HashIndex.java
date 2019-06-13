package org.nervos.ckb.util.items;

import com.alibaba.fastjson.annotation.JSONField;

public class HashIndex {

  /**
   *  can be used for deps or inputs.previous_output
   *
   *  {
   * 		"tx_hash": "0x15c809f08c7bca63d2b661e1dbc26c74551a6f982f7631c718dc43bd2bb5c90e",
   * 		"index": 0
   *  }
   */
  @JSONField(name = "tx_hash")
  private String txHash;
  private String index;

  public String getTxHash() {
    return txHash;
  }

  public void setTxHash(String txHash) {
    this.txHash = txHash;
  }

  public String getIndex() {
    return index;
  }

  public void setIndex(String index) {
    this.index = index;
  }

  @Override
  public String toString() {
    return "HashIndex{" +
        "txHash='" + txHash + '\'' +
        ", index=" + index +
        '}';
  }

  public HashIndex(String txHash, String index) {
    this.txHash = txHash;
    this.index = index;
  }
}
