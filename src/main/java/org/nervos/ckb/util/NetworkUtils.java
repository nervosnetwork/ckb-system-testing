package org.nervos.ckb.util;

import java.io.IOException;
import java.net.ServerSocket;

public class NetworkUtils {

  /**
   * Get an idle netowrk port
   * @return Idle network port
   */
  public static int getIdlePort(){
    try {
      ServerSocket serverSocket = new ServerSocket(0);
      int port = serverSocket.getLocalPort();
      serverSocket.close();
      return port;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return -1;
  }

}
