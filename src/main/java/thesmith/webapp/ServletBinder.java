package thesmith.webapp;

import javax.servlet.http.HttpServlet;

public interface ServletBinder {
  
  ServletBinder bind(String path, HttpServlet servlet);
  
}
