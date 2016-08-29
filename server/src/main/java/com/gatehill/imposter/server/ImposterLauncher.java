package com.gatehill.imposter.server;

import com.gatehill.imposter.ImposterConfig;
import com.gatehill.imposter.util.InjectorUtil;
import com.gatehill.imposter.util.LogUtil;
import com.google.common.collect.Lists;
import io.vertx.core.Launcher;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import javax.inject.Inject;
import java.util.List;

import static com.gatehill.imposter.util.CryptoUtil.DEFAULT_KEYSTORE_PASSWORD;
import static com.gatehill.imposter.util.CryptoUtil.DEFAULT_KEYSTORE_PATH;
import static com.gatehill.imposter.util.FileUtil.CLASSPATH_PREFIX;
import static com.gatehill.imposter.util.HttpUtil.BIND_ALL_HOSTS;

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public class ImposterLauncher extends Launcher {
    private static final Logger LOGGER = LogManager.getLogger(ImposterLauncher.class);
    private static final String VERTX_LOGGER_FACTORY = "vertx.logger-delegate-factory-class-name";
    private static final String VERTX_LOGGER_IMPL = "io.vertx.core.logging.SLF4JLogDelegateFactory";

    @Option(name = "--help", aliases = {"-h"}, usage = "Display usage only", help = true)
    private boolean displayHelp;

    @Option(name = "--version", aliases = {"-v"}, usage = "Print version and exit", help = true)
    private boolean displayVersion;

    @Option(name = "--configDir", aliases = {"-c"}, usage = "Directory containing mock configuration files", required = true)
    private String configDir;

    @Option(name = "--plugin", aliases = {"-p"}, usage = "Plugin class name", required = true)
    private String[] pluginClassNames;

    @Option(name = "--listenPort", aliases = {"-l"}, usage = "Listen port")
    private Integer listenPort = 8443;

    @Option(name = "--host", aliases = {"-b"}, usage = "Bind host")
    private String host = BIND_ALL_HOSTS;

    @Option(name = "--serverUrl", aliases = {"-u"}, usage = "Explicitly set the server address")
    private String serverUrl;

    @Option(name = "--tlsEnabled", aliases = {"-t"}, usage = "Whether TLS (HTTPS) is enabled (requires keystore to be configured)")
    private boolean tlsEnabled;

    @Option(name = "--keystorePath", usage = "Path to the keystore")
    private String keystorePath = CLASSPATH_PREFIX + DEFAULT_KEYSTORE_PATH;

    @Option(name = "--keystorePassword", usage = "Password for the keystore")
    private String keystorePassword = DEFAULT_KEYSTORE_PASSWORD;

    @Inject
    private ImposterConfig imposterConfig;

    static {
        // delegate all Vert.x logging to SLF4J
        System.setProperty(VERTX_LOGGER_FACTORY, VERTX_LOGGER_IMPL);
    }

    /**
     * Main entry point.
     *
     * @param args the user command line arguments.
     */
    public static void main(String[] args) {
        LogUtil.configureLogging(System.getProperty(LogUtil.PROPERTY_LOG_LEVEL, "DEBUG"));
        new ImposterLauncher().dispatch(args);
    }

    @Override
    public void dispatch(String[] originalArgs) {
        final CmdLineParser parser = new CmdLineParser(this);

        try {
            parser.parseArgument(originalArgs);

            if (displayHelp) {
                printUsage(parser, 0);
            } else if (displayVersion) {
                printVersion();
            } else {
                startServer(originalArgs);
            }

        } catch (CmdLineException e) {
            // handling of wrong arguments
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(e);
            } else {
                LOGGER.error(e.getMessage());
            }

            printUsage(parser, 255);

        } catch (Exception e) {
            LOGGER.error("Error starting server", e);
        }
    }

    private void startServer(String[] originalArgs) {
        InjectorUtil.create(new BootstrapModule()).injectMembers(this);

        imposterConfig.setConfigDir(configDir);
        imposterConfig.setPluginClassNames(pluginClassNames);
        imposterConfig.setListenPort(listenPort);
        imposterConfig.setHost(host);
        imposterConfig.setServerUrl(serverUrl);
        imposterConfig.setTlsEnabled(tlsEnabled);
        imposterConfig.setKeystorePath(keystorePath);
        imposterConfig.setKeystorePassword(keystorePassword);

        final List<String> args = Lists.newArrayList(originalArgs);
        args.add(0, "run");
        args.add(1, ImposterVerticle.class.getCanonicalName());
        super.dispatch(args.toArray(new String[args.size()]));
    }

    private void printVersion() {
        final String version = "0.1.0"; // TODO

        System.out.println("Imposter: A scriptable, multipurpose mock server.");
        System.out.println("Version: " + version);
    }

    /**
     * Print usage information, then exit.
     *
     * @param parser   the command line parser containing usage information
     * @param exitCode the exit code
     */
    private void printUsage(CmdLineParser parser, int exitCode) {
        System.out.println("Imposter usage:");
        parser.printUsage(System.out);
        System.exit(exitCode);
    }

}
