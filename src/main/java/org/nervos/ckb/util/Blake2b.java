package org.nervos.ckb.util;

import java.nio.charset.StandardCharsets;
import org.bouncycastle.crypto.digests.Blake2bDigest;

public class Blake2b {

  byte[] CKB_HASH_PERSONALIZATION = "ckb-default-hash".getBytes(StandardCharsets.UTF_8);
  private Blake2bDigest blake2bDigest = new Blake2bDigest(null, 32, null, CKB_HASH_PERSONALIZATION);

  public String finalHexString() {
    byte[] out = new byte[32];
    if (blake2bDigest != null) {
      blake2bDigest.doFinal(out, 0);
    }
    return ("0x" + bytesToHexString(out));
  }


  public void update(byte[] input) {
    if (blake2bDigest != null) {
      blake2bDigest.update(input, 0, input.length);
    }
  }

  public static byte[] hexStringToByte(String hex) {
    byte[] b = new byte[hex.length() / 2];
    int j = 0;
    for (int i = 0; i < b.length; i++) {
      char c0 = hex.charAt(j++);
      char c1 = hex.charAt(j++);
      b[i] = (byte) ((parse(c0) << 4) | parse(c1));
    }
    return b;
  }

  private static int parse(char c) {
    if (c >= 'a')
      return (c - 'a' + 10) & 0xff;
    if (c >= 'A')
      return (c - 'A' + 10) & 0xff;
    return (c - '0') & 0xff;
  }

  public static final String bytesToHexString(byte[] bArray) {
    StringBuffer sb = new StringBuffer(bArray.length);
    for (int i = 0; i < bArray.length; i++) {
      sb.append(String.format("%02x", bArray[i] & 0xFF));
    }
    return sb.toString();
  }

}