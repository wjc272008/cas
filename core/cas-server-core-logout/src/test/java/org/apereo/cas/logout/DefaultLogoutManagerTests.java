package org.apereo.cas.logout;

import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.AbstractWebApplicationService;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegexMatchingRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredService.LogoutType;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.HttpMessage;
import org.apereo.cas.web.SimpleUrlValidatorFactoryBean;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;

import java.net.URL;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Jerome Leleu
 * @since 4.0.0
 */
public class DefaultLogoutManagerTests {
    private static final String ID = "id";
    private static final String URL = "http://www.github.com";

    private DefaultLogoutManager logoutManager;

    @Mock
    private TicketGrantingTicket tgt;

    private AbstractWebApplicationService simpleWebApplicationServiceImpl;

    private AbstractRegisteredService registeredService;

    @Mock
    private ServicesManager servicesManager;

    @Mock
    private HttpClient client;

    private DefaultSingleLogoutServiceMessageHandler singleLogoutServiceMessageHandler;

    public DefaultLogoutManagerTests() {
        MockitoAnnotations.initMocks(this);
    }

    @SneakyThrows
    public static AbstractRegisteredService getRegisteredService(final String id) {
        val s = new RegexRegisteredService();
        s.setServiceId(id);
        s.setName("Test registered service " + id);
        s.setDescription("Registered service description");
        s.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy("^https?://.+"));
        s.setId(RandomUtils.getNativeInstance().nextInt(Math.abs(s.hashCode())));
        return s;
    }

    public static AbstractWebApplicationService getService(final String url) {
        val request = new MockHttpServletRequest();
        request.addParameter("service", url);
        return (AbstractWebApplicationService) new WebApplicationServiceFactory().createService(request);
    }

    @Before
    public void initialize() {
        when(client.isValidEndPoint(any(String.class))).thenReturn(true);
        when(client.isValidEndPoint(any(URL.class))).thenReturn(true);
        when(client.sendMessageToEndPoint(any(HttpMessage.class))).thenReturn(true);

        val validator = new SimpleUrlValidatorFactoryBean(true).getObject();

        singleLogoutServiceMessageHandler = new DefaultSingleLogoutServiceMessageHandler(client,
            new SamlCompliantLogoutMessageCreator(), servicesManager,
            new DefaultSingleLogoutServiceLogoutUrlBuilder(validator), true,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()));

        val services = new HashMap<String, Service>();
        this.simpleWebApplicationServiceImpl = getService(URL);
        services.put(ID, this.simpleWebApplicationServiceImpl);
        when(this.tgt.getServices()).thenReturn(services);

        this.logoutManager = new DefaultLogoutManager(new SamlCompliantLogoutMessageCreator(),
            singleLogoutServiceMessageHandler, false, mock(LogoutExecutionPlan.class));
        this.registeredService = getRegisteredService(URL);
        when(servicesManager.findServiceBy(this.simpleWebApplicationServiceImpl)).thenReturn(this.registeredService);
    }

    @Test
    public void verifyServiceLogoutUrlIsUsed() throws Exception {
        this.registeredService.setLogoutUrl(new URL("https://www.apereo.org"));
        val logoutRequests = this.logoutManager.performLogout(tgt);
        val logoutRequest = logoutRequests.iterator().next();
        assertEquals(logoutRequest.getLogoutUrl(), this.registeredService.getLogoutUrl());
    }

    @Test
    public void verifyLogoutDisabled() {
        this.logoutManager = new DefaultLogoutManager(new SamlCompliantLogoutMessageCreator(),
            singleLogoutServiceMessageHandler, true, mock(LogoutExecutionPlan.class));

        val logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(0, logoutRequests.size());
    }

    @Test
    public void verifyLogoutAlreadyLoggedOut() {
        this.simpleWebApplicationServiceImpl.setLoggedOutAlready(true);
        val logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(0, logoutRequests.size());
    }

    @Test
    public void verifyLogoutTypeNotSet() {
        val logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(1, logoutRequests.size());
        val logoutRequest = logoutRequests.iterator().next();
        assertEquals(ID, logoutRequest.getTicketId());
        assertEquals(this.simpleWebApplicationServiceImpl, logoutRequest.getService());
        assertEquals(LogoutRequestStatus.SUCCESS, logoutRequest.getStatus());
    }

    @Test
    public void verifyLogoutTypeBack() {
        this.registeredService.setLogoutType(LogoutType.BACK_CHANNEL);
        val logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(1, logoutRequests.size());
        val logoutRequest = logoutRequests.iterator().next();
        assertEquals(ID, logoutRequest.getTicketId());
        assertEquals(this.simpleWebApplicationServiceImpl, logoutRequest.getService());
        assertEquals(LogoutRequestStatus.SUCCESS, logoutRequest.getStatus());
    }

    @Test
    public void verifyLogoutTypeNone() {
        this.registeredService.setLogoutType(LogoutType.NONE);
        val logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(0, logoutRequests.size());
    }

    @Test
    public void verifyLogoutTypeNull() {
        this.registeredService.setLogoutType(null);
        val logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(1, logoutRequests.size());
        val logoutRequest = logoutRequests.iterator().next();
        assertEquals(ID, logoutRequest.getTicketId());
    }

    @Test
    public void verifyLogoutTypeFront() {
        this.registeredService.setLogoutType(LogoutType.FRONT_CHANNEL);
        val logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(1, logoutRequests.size());
        val logoutRequest = logoutRequests.iterator().next();
        assertEquals(ID, logoutRequest.getTicketId());
        assertEquals(this.simpleWebApplicationServiceImpl, logoutRequest.getService());
        assertEquals(LogoutRequestStatus.NOT_ATTEMPTED, logoutRequest.getStatus());
    }

    @Test
    public void verifyAsynchronousLogout() {
        this.registeredService.setLogoutType(LogoutType.BACK_CHANNEL);
        val logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(1, logoutRequests.size());
    }
}
