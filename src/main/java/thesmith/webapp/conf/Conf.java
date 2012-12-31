package thesmith.webapp.conf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import thesmith.webapp.utils.Transformations;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class Conf {
  
  private static final ClassLoader CLASS_LOADER = Conf.class.getClassLoader();
  private static final String CONF_FILE = "server.properties";
  
  private static final ImmutableMap<String, String> conf;
  
  private static final Logger logger = LoggerFactory.getLogger(Conf.class);

  static {
    Properties properties = new Properties();
    try {
      Optional<InputStream> resourceAsStream = Optional.fromNullable(CLASS_LOADER.getResourceAsStream(CONF_FILE));
      if (resourceAsStream.isPresent()) {
        properties.load(resourceAsStream.get());
      } else {
        logger.info("{} does not exist", CONF_FILE);
      }
    } catch (IOException e) {
      logger.warn("Unable to load {}", CONF_FILE, e);
    }
    
    properties.putAll(System.getProperties());
    
    for (Entry<String, String> entry: System.getenv().entrySet()) {
      properties.setProperty(entry.getKey(), entry.getValue());
    }
    
    conf = Maps.fromProperties(properties);
  }
  
  private Conf(){}
  
  public static Optional<String> get(String key) {
    return Optional.fromNullable(conf.get(key));
  }
  
  public static String get(String key, String otherwise) {
    return get(key).or(otherwise);
  }
  
  public static Optional<Integer> getInt(String key) {
    return get(key).transform(Transformations.TO_INT);
  }
  
  public static Integer getInt(String key, Integer otherwise) {
    return getInt(key).or(otherwise);
  }
}
