package org.apereo.cas.monitor;

import org.apereo.cas.config.support.EnvironmentConversionServiceInitializer;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import javax.sql.DataSource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

/**
 * Unit test for {@link JdbcDataSourceHealthIndicator}.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
@SpringBootTest(classes = {RefreshAutoConfiguration.class})
@ContextConfiguration(initializers = EnvironmentConversionServiceInitializer.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class JdbcDataSourceHealthIndicatorTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Autowired
    private CasConfigurationProperties casProperties;

    private DataSource dataSource;

    @Before
    public void initialize() {
        val ctx = new ClassPathXmlApplicationContext("classpath:/jpaTestApplicationContext.xml");
        this.dataSource = ctx.getBean("dataSource", DataSource.class);
    }

    @Test
    public void verifyObserve() {
        val monitor = new JdbcDataSourceHealthIndicator(5000,
            this.dataSource, this.executor,
            "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS");
        val status = monitor.health();
        assertEquals(Status.UP, status.getStatus());
    }
}
