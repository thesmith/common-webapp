package thesmith.webapp.conf;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.common.base.Optional;

public class ConfTest {
  
  static {
    System.setProperty("override", "property");
    System.setProperty("property", "isset");
  }

  @Test public void shouldGetServerPropertiesValue() {
    assertEquals(Conf.get("port"), Optional.of("7979"));
  }
  
  @Test public void shouldGetSystemProperties() {
    assertEquals(Conf.get("property"), Optional.of("isset"));
    assertEquals(Conf.get("override"), Optional.of("property"));
  }
}
