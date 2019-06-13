package org.nervos.ckb.util;

public class CapacityUnits {

  /**
   * Convert capacity from byte to shannons;
   * byte is the basic unit in Capacity;
   * 1 byte = 10^8 shannons;
   *
   * @param capacity bytes of capacity
   * @return capacity of shannons
   */
  public String capacityToShannons(String capacity){
    return capacity + "00000000";
  }

}
