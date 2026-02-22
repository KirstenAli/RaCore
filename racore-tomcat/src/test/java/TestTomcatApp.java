import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.racore.servers.servlet.RaCoreServlet;

import java.io.File;
import java.net.ServerSocket;

import static org.racore.core.Endpoint.get;

public final class TestTomcatApp {

    private static Tomcat tomcat;
    private static int port;

    private TestTomcatApp() {}

    public static int start() throws Exception {
        if (tomcat != null) return port;

        port = findFreePort();

        tomcat = new Tomcat();
        tomcat.setPort(port);

        tomcat.getConnector();

        tomcat.setBaseDir(new File("target/tomcat").getAbsolutePath());

        Context ctx = tomcat.addContext("", new File(".").getAbsolutePath());

        Tomcat.addServlet(ctx, "racore", new RaCoreServlet());
        ctx.addServletMappingDecoded("/*", "racore");

        defineRoutes();

        tomcat.start();
        return port;
    }

    public static void defineRoutes() {
        get("/getPerson/{id}", _ -> "Hello Person");
    }

    public static void stop() throws Exception {
        if (tomcat == null) return;
        tomcat.stop();
        tomcat.destroy();
        tomcat = null;
    }

    private static int findFreePort() throws Exception {
        try (ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();
        }
    }
}