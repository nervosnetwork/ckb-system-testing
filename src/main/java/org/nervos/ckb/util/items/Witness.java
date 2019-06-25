package org.nervos.ckb.util.items;

import java.util.ArrayList;
import java.util.List;

public class Witness {

  public List<String> data = new ArrayList<>();

  public Witness(List<String> data) {
    this.data = data;
  }
}
