package thesmith.webapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

import thesmith.webapp.conf.Conf;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.util.concurrent.AbstractService;

public class JettyServer extends AbstractService implements ServletBinder {

  private static final String WAR_DIR_IN_JAR = "webapp";
  private static final String LOCAL_WAR_DIR = "./src/main/webapp";

  public static final String PORT_CONF = "port";
  public static final String ACCEPT_QUEUE_SIZE_CONF = "accept_queue_size";
  public static final String THREAD_POOL_SIZE_CONF = "thread_pool_size";
  public static final String REQUEST_BUFFER_SIZE_CONF = "request_buffer_size";
  public static final String RESPONSE_HEADER_SIZE_CONF = "response_header_size";
  public static final String ACCEPTORS_CONF = "acceptors";

  private final Server server;
  private final WebAppContext ctx = new WebAppContext(warBase(), "/");
  private final Integer port;

  public JettyServer() {
    this(Conf.getInt(PORT_CONF, 8080));
  }

  public JettyServer(Integer port) {
    this.port = port;
    server = createServer();
    ctx.setExtractWAR(false);
    server.setHandler(ctx);
  }

  private static String warBase() {
    if (new File(LOCAL_WAR_DIR).exists()) {
      return LOCAL_WAR_DIR;
    }
    File tempWarBase = Files.createTempDir();
    ProtectionDomain domain = JettyServer.class.getProtectionDomain();
    ZipFile jarFile = null;
    try {
      URI jarPath = domain.getCodeSource().getLocation().toURI();
      jarFile = new ZipFile(new File(jarPath).getAbsoluteFile());
      Enumeration<? extends ZipEntry> filesInJar = jarFile.entries();
      while (filesInJar.hasMoreElements()) {
        ZipEntry fileInJar = filesInJar.nextElement();
        String fileInJarName = fileInJar.getName();
        if (!fileInJarName.startsWith(WAR_DIR_IN_JAR)) {
          continue;
        }
        fileInJarName = fileInJarName.substring(WAR_DIR_IN_JAR.length() + 1);
        File file = new File(tempWarBase, fileInJarName);
        if (fileInJar.isDirectory()) {
          file.mkdirs();
        } else {
          InputStream in = jarFile.getInputStream(fileInJar);
          ByteStreams.copy(in, new FileOutputStream(file));
        }
      }
      return tempWarBase.getAbsolutePath();
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      try {
        if (jarFile != null) {
          jarFile.close();
        }
      } catch (IOException e) {
        // ignore
      }
    }
  }

  @Override
  public ServletBinder bind(String path, HttpServlet servlet) {
    ctx.addServlet(new ServletHolder(servlet), path);
    return this;
  }

  private Server createServer() {

    Server server = new Server();

    final SelectChannelConnector connector = new SelectChannelConnector();
    connector.setPort(port);

    connector.setAcceptQueueSize(Conf.getInt(ACCEPT_QUEUE_SIZE_CONF, 2048));
    connector.setThreadPool(new QueuedThreadPool(Conf.getInt(THREAD_POOL_SIZE_CONF, 500)));
    connector.setAcceptors(Conf.getInt(ACCEPTORS_CONF, 4));
    connector.setRequestBufferSize(Conf.getInt(REQUEST_BUFFER_SIZE_CONF, 1024));
    connector.setResponseHeaderSize(Conf.getInt(RESPONSE_HEADER_SIZE_CONF, 1024));

    server.setConnectors(new Connector[] { connector });
    return server;
  }

  @Override
  protected void doStart() {
    try {
      server.start();
      notifyStarted();
    } catch (Exception e) {
      notifyFailed(e);
    }
  }

  @Override
  protected void doStop() {
    try {
      server.stop();
    } catch (Exception e) {
      // ignore
    }
    notifyStopped();
  }
}
