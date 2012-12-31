package thesmith.webapp.utils;

import com.google.common.base.Function;

public class Transformations {

  private Transformations() {}

  public static final Function<String, Integer> TO_INT = new Function<String, Integer>() {

    @Override
    public Integer apply(String input) {
      return Integer.valueOf(input);
    }
  };

}
