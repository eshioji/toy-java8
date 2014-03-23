package eshioji.java8;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.codahale.metrics.servlets.AdminServlet;
import com.codahale.metrics.servlets.AdminServletContextListener;
import com.codahale.metrics.servlets.MetricsServlet;
import com.google.common.base.Throwables;
import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


/**
 * @author Enno Shioji (eshioji@gmail.com)
 */
public class Java8Module implements com.google.inject.Module {
    private static final Logger log = LoggerFactory.getLogger(Java8Module.class);

    @Override
    public void configure(Binder binder) {
        try {
            Properties properties = read("test.properties");
            Names.bindProperties(binder, properties);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

    }

    private Properties read(String resourceName) {
        try (InputStream is = new FileInputStream(resourceName)) {
            Properties p = new Properties();
            p.load(is);
            return p;
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Provides
    @Singleton
    private Server metricServer(final MetricRegistry metricRegistry) throws Exception {
        metricRegistry.registerAll(new GarbageCollectorMetricSet());
        metricRegistry.registerAll(new MemoryUsageGaugeSet());
        metricRegistry.registerAll(new ThreadStatesGaugeSet());

        final HealthCheckRegistry notUsed = new HealthCheckRegistry();

        AdminServletContextListener ascl =  new AdminServletContextListener() {

            @Override
            protected MetricRegistry getMetricRegistry() {
                return metricRegistry;
            }

            @Override
            protected HealthCheckRegistry getHealthCheckRegistry() {
                return notUsed;
            }
        };

        final Server server = new Server(7070);
        ServletContextHandler context = new ServletContextHandler();
        context.addEventListener(ascl);
        context.setContextPath("/");
        context.setInitParameter(MetricsServlet.DURATION_UNIT, TimeUnit.MILLISECONDS.toString());
        context.setInitParameter(MetricsServlet.RATE_UNIT, TimeUnit.SECONDS.toString());
        server.setHandler(context);
        ServletHolder holder = new ServletHolder(new AdminServlet());
        context.addServlet(holder, "/*");

        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    server.stop();
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            }
        });

        return server;
    }


}
