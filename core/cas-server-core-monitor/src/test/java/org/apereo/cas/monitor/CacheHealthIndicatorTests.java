package org.apereo.cas.monitor;

import org.apereo.cas.config.support.EnvironmentConversionServiceInitializer;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.junit.Assert.*;

/**
 * Unit test for {@link AbstractCacheHealthIndicator}.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
@ContextConfiguration(initializers = EnvironmentConversionServiceInitializer.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CacheHealthIndicatorTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    private CasConfigurationProperties casProperties;

    protected static SimpleCacheStatistics[] statsArray(final SimpleCacheStatistics... statistics) {
        return statistics;
    }

    @Test
    public void verifyObserveOk() {
        val warn = casProperties.getMonitor().getWarn();
        final AbstractCacheHealthIndicator monitor = new AbstractCacheHealthIndicator(
            warn.getEvictionThreshold(),
            warn.getThreshold()) {
            @Override
            protected SimpleCacheStatistics[] getStatistics() {
                return statsArray(new SimpleCacheStatistics(100, 200, 0));
            }
        };
        val status = monitor.health().getStatus();
        assertEquals(Status.UP, status);
    }

    @Test
    public void verifyObserveWarn() {
        val warn = casProperties.getMonitor().getWarn();
        final AbstractCacheHealthIndicator monitor = new AbstractCacheHealthIndicator(
            warn.getEvictionThreshold(),
            warn.getThreshold()
        ) {
            @Override
            protected SimpleCacheStatistics[] getStatistics() {
                return statsArray(new SimpleCacheStatistics(199, 200, 100));
            }
        };
        val status = monitor.health().getStatus();
        assertEquals("WARN", status.getCode());
    }

    @Test
    public void verifyObserveError() {
        val warn = casProperties.getMonitor().getWarn();
        final AbstractCacheHealthIndicator monitor = new AbstractCacheHealthIndicator(
            warn.getEvictionThreshold(),
            warn.getThreshold()) {
            @Override
            protected SimpleCacheStatistics[] getStatistics() {
                return statsArray(new SimpleCacheStatistics(100, 110, 0));
            }
        };
        val status = monitor.health().getStatus();
        assertEquals(Status.OUT_OF_SERVICE, status);
    }

    @Test
    public void verifyObserveError2() {
        val warn = casProperties.getMonitor().getWarn();
        final AbstractCacheHealthIndicator monitor = new AbstractCacheHealthIndicator(
            warn.getEvictionThreshold(),
            warn.getThreshold()) {
            @Override
            protected SimpleCacheStatistics[] getStatistics() {
                return statsArray(new SimpleCacheStatistics(199, 200, 1));
            }
        };
        assertEquals("WARN", monitor.health().getStatus().getCode());
    }
}
