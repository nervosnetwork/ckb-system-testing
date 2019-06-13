package org.nervos.ckb.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

public class ReadConfig {

  public static final String YAML_FILE =
      System.getenv("LOAD_CONFIG") == null ? "../loadConfig.yml" : System.getenv("LOAD_CONFIG");

  public Object getYMLValue(String title, String key) {
    Map<String, Object> map = getYMLMap();
    Map<String, Object> data = (Map<String, Object>) map.get(title);
    Object value = data.get(key);
    return value;
  }

  public Map<String, Object> getYMLMap() {
    InputStream input;
    try {
      input = new FileInputStream(YAML_FILE);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return null;
    }
    Yaml yaml = new Yaml();
    Map<String, Object> object = yaml.load(input);
    return object;
  }

}
