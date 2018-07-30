package org.apereo.cas.consent;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.config.CasConsentApiConfiguration;
import org.apereo.cas.config.CasConsentCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.junit.Assert.*;

/**
 * This is {@link DefaultConsentDecisionBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTest(classes = {
    CasConsentApiConfiguration.class,
    CasConsentCoreConfiguration.class,
    CasCoreAuditConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreUtilConfiguration.class})
public class DefaultConsentDecisionBuilderTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("consentDecisionBuilder")
    private ConsentDecisionBuilder consentDecisionBuilder;

    @Test
    public void verifyNewConsentDecision() {
        val consentDecision = getConsentDecision();
        assertNotNull(consentDecision);
        assertEquals("casuser", consentDecision.getPrincipal());
        assertEquals(consentDecision.getService(), RegisteredServiceTestUtils.getService().getId());
    }

    @Test
    public void verifyAttributesRequireConsent() {
        val consentDecision = getConsentDecision();
        assertTrue(consentDecisionBuilder.doesAttributeReleaseRequireConsent(consentDecision, CollectionUtils.wrap("attr2", "value2")));
        assertFalse(consentDecisionBuilder.doesAttributeReleaseRequireConsent(consentDecision, CollectionUtils.wrap("attr1", "something")));
    }

    @Test
    public void verifyAttributeValuesRequireConsent() {
        val consentDecision = getConsentDecision();
        consentDecision.setOptions(ConsentReminderOptions.ATTRIBUTE_VALUE);
        assertTrue(consentDecisionBuilder.doesAttributeReleaseRequireConsent(consentDecision, CollectionUtils.wrap("attr1", "value2")));
    }

    @Test
    public void verifyAttributesAreRetrieved() {
        val consentDecision = getConsentDecision();
        val attrs = consentDecisionBuilder.getConsentableAttributesFrom(consentDecision);
        assertTrue(attrs.containsKey("attr1"));
        assertEquals("value1", attrs.get("attr1"));
    }

    private ConsentDecision getConsentDecision() {
        return consentDecisionBuilder.build(RegisteredServiceTestUtils.getService(),
            RegisteredServiceTestUtils.getRegisteredService("test"),
            "casuser", CollectionUtils.wrap("attr1", "value1"));
    }

}
