package org.nervos.ckb.util.items;

import com.alibaba.fastjson.annotation.JSONField;

public class OutPoint {

  @JSONField(name = "block_hash")
  private String blockHash;
  private HashIndex cell;

  public String getBlockHash() {
    return blockHash;
  }

  public void setBlockHash(String blockHash) {
    this.blockHash = blockHash;
  }

  public HashIndex getCell() {
    return cell;
  }

  public void setCell(HashIndex cell) {
    this.cell = cell;
  }

  public OutPoint(String blockHash, HashIndex cell) {
    this.blockHash = blockHash;
    this.cell = cell;
  }
}
