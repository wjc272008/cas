package org.apereo.cas.support.saml.mdui;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.opensaml.saml.ext.saml2mdui.Description;
import org.opensaml.saml.ext.saml2mdui.DisplayName;
import org.opensaml.saml.ext.saml2mdui.UIInfo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlMetadataUIInfoTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class
})
@DirtiesContext
public class SamlMetadataUIInfoTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Test
    public void verifyInfoNotAvailable() {
        val service = RegisteredServiceTestUtils.getRegisteredService();
        service.setPrivacyUrl("http://cas.example.org");
        service.setInformationUrl("http://cas.example.org");
        val info = new SamlMetadataUIInfo(service, "en");
        assertEquals(service.getName(), info.getDisplayName());
        assertEquals(service.getDescription(), info.getDescription());
        assertEquals(service.getInformationUrl(), info.getInformationURL());
        assertEquals("en", info.getLocale());
        assertEquals(service.getPrivacyUrl(), info.getPrivacyStatementURL());
    }

    @Test
    public void verifyInfo() {
        val mdui = mock(UIInfo.class);
        val description = mock(Description.class);
        when(description.getValue()).thenReturn("Description");
        when(description.getXMLLang()).thenReturn("en");

        val names = mock(DisplayName.class);
        when(names.getValue()).thenReturn("Name");
        when(names.getXMLLang()).thenReturn("en");

        when(mdui.getDescriptions()).thenReturn(CollectionUtils.wrapList(description));
        when(mdui.getDisplayNames()).thenReturn(CollectionUtils.wrapList(names));

        val service = RegisteredServiceTestUtils.getRegisteredService();
        val info = new SamlMetadataUIInfo(mdui, service);
        assertEquals(names.getValue(), info.getDisplayName());
        assertEquals(description.getValue(), info.getDescription());
    }
}
