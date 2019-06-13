package org.nervos.ckb.util;
import java.util.Arrays;

public class JsonrpcRequestBody {

  private int id = 2;
  private String jsonrpc = "2.0";
  private String method;
  private Object[] params;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getJsonrpc() {
    return jsonrpc;
  }

  public void setJsonrpc(String jsonrpc) {
    this.jsonrpc = jsonrpc;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public Object[] getParams() {
    return params;
  }

  public void setParams(Object[] params) {
    this.params = params;
  }

  @Override
  public String toString() {
    return "JsonrpcRequestBody{" +
        "id=" + id +
        ", jsonrpc='" + jsonrpc + '\'' +
        ", method='" + method + '\'' +
        ", params=" + Arrays.toString(params) +
        '}';
  }

  public JsonrpcRequestBody(int id, String jsonrpc, String method, Object[] params) {
    this.id = id;
    this.jsonrpc = jsonrpc;
    this.method = method;
    this.params = params;


  }

  public JsonrpcRequestBody( String method, Object[] params) {

    this.method = method;
    this.params = params;
  }


}
