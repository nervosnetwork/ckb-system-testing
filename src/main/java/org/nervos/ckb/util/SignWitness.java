package org.nervos.ckb.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.nervos.ckb.crypto.Blake2b;
import org.nervos.ckb.crypto.secp256k1.ECKeyPair;
import org.nervos.ckb.crypto.secp256k1.Sign;
import org.nervos.ckb.util.items.Witness;
import org.nervos.ckb.utils.Numeric;

// refer to ckb-java-sdk
public class SignWitness {
  List<Witness> witnesses;

  public List<Witness> SignedWitness(BigInteger privateKey, String txHash, List<Witness> witnesses) {
    ECKeyPair ecKeyPair = ECKeyPair.createWithPrivateKey(privateKey, false);
    String publicKey =
        Numeric.toHexStringWithPrefixZeroPadded(Sign.publicKeyFromPrivate(privateKey, true), 66);
    List<Witness> signedWitnesses = new ArrayList<>();
    for (Witness witness : witnesses) {
      List<String> oldData = witness.data;
      Blake2b blake2b = new Blake2b();
      blake2b.update(Numeric.hexStringToByteArray(txHash));
      for (String datum : witness.data) {
        blake2b.update(Numeric.hexStringToByteArray(datum));
      }
      String message = blake2b.doFinalString();

      String signature =
          Numeric.toHexString(
              Sign.signMessage(Numeric.hexStringToByteArray(message), ecKeyPair).getDerSignature());
      witness.data = new ArrayList<>();
      witness.data.add(publicKey);
      witness.data.add(signature);
      witness.data.addAll(oldData);
      signedWitnesses.add(witness);
    }
    return signedWitnesses;
  }
}
