package org.nervos.ckb.util.items;

public class Outputs {

  private String capacity;
  private String data;
  private Script lock;
  private Script type;

  public String getCapacity() {
    return capacity;
  }

  public void setCapacity(String capacity) {
    this.capacity = capacity;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public Script getLock() {
    return lock;
  }

  public void setLock(Script lock) {
    this.lock = lock;
  }

  public Object getType() {
    return type;
  }

  public void setType(Script type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return "Outputs{" +
        "capacity=" + capacity +
        ", data='" + data + '\'' +
        ", lock='" + lock + '\'' +
        ", type=" + type +
        '}';
  }

  public Outputs(String capacity, String data, Script lock, Script type) {
    this.capacity = capacity;
    this.data = data;
    this.lock = lock;
    this.type = type;
  }
}
