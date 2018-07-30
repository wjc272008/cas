package org.apereo.cas.authentication;

import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.SurrogateAuthenticationAuditConfiguration;
import org.apereo.cas.config.SurrogateAuthenticationConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;

import lombok.val;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SurrogateAuthenticationPostProcessorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    SurrogateAuthenticationAuditConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreHttpConfiguration.class,
    SurrogateAuthenticationConfiguration.class
})
@TestPropertySource(locations = {"classpath:/surrogate.properties"})
public class SurrogateAuthenticationPostProcessorTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    @Qualifier("surrogateAuthenticationPostProcessor")
    private AuthenticationPostProcessor surrogateAuthenticationPostProcessor;

    @Test
    public void verifySupports() {
        assertFalse(surrogateAuthenticationPostProcessor.supports(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
        assertTrue(surrogateAuthenticationPostProcessor.supports(new SurrogateUsernamePasswordCredential()));
    }

    @Test
    public void verifySurrogateCredentialNotFound() {
        val c = new SurrogateUsernamePasswordCredential();
        c.setUsername("casuser");
        c.setPassword("Mellon");
        val transaction = DefaultAuthenticationTransaction.of(RegisteredServiceTestUtils.getService("service"), c);
        val builder = mock(AuthenticationBuilder.class);
        when(builder.build()).thenReturn(CoreAuthenticationTestUtils.getAuthentication("casuser"));
        thrown.expect(AuthenticationException.class);
        surrogateAuthenticationPostProcessor.process(builder, transaction);
    }

    @Test
    public void verifyProcessorWorks() {
        val c = new SurrogateUsernamePasswordCredential();
        c.setUsername("casuser");
        c.setPassword("Mellon");
        c.setSurrogateUsername("cassurrogate");
        val transaction = DefaultAuthenticationTransaction.of(
            RegisteredServiceTestUtils.getService("https://localhost"), c);
        val builder = mock(AuthenticationBuilder.class);
        when(builder.build()).thenReturn(CoreAuthenticationTestUtils.getAuthentication("casuser"));
        surrogateAuthenticationPostProcessor.process(builder, transaction);
    }
}
