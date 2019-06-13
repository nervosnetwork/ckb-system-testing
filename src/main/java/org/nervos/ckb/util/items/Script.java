package org.nervos.ckb.util.items;

import com.alibaba.fastjson.annotation.JSONField;
import org.nervos.ckb.util.Blake2b;
import java.util.List;

public class Script {

  private List<String> args;
  @JSONField(name = "code_hash")
  private String codeHash;

  public List<String> getArgs() {
    return args;
  }

  public void setArgs(List<String> args) {
    this.args = args;
  }

  public String getCodeHash() {
    return codeHash;
  }

  public void setCodeHash(String codeHash) {
    this.codeHash = codeHash;
  }

  @Override
  public String toString() {
    return "Script{" +
        "args=" + args +
        ", codeHash='" + codeHash + '\'' +
        '}';
  }

  public Script(String codeHash, List<String> args) {
    this.args = args;
    this.codeHash = codeHash;
  }

  @JSONField(serialize = false)
  public String getLockHash(){
    Blake2b blake2b = new Blake2b();
    codeHash = codeHash.substring(2);
    blake2b.update(blake2b.hexStringToByte(codeHash));
    for (String arg : args) {
      if(arg.length() == 0)
        blake2b.update(blake2b.hexStringToByte(arg));
      else
        // remove 0x prefix
        blake2b.update(blake2b.hexStringToByte(arg.substring(2)));
    }
    return (blake2b.finalHexString());
  }
}
