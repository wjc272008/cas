package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.config.BaseSurrogateAuthenticationTestsConfiguration;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Before;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ClassPathResource;

/**
 * This is {@link JsonResourceSurrogateAuthenticationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@SpringBootTest(classes = {
    BaseSurrogateAuthenticationTestsConfiguration.class,
    RefreshAutoConfiguration.class
})
public class JsonResourceSurrogateAuthenticationServiceTests extends BaseSurrogateAuthenticationServiceTests {
    private SurrogateAuthenticationService service;

    @Before
    @SneakyThrows
    public void initTests() {
        val resource = new ClassPathResource("surrogates.json");
        service = new JsonResourceSurrogateAuthenticationService(resource, servicesManager);
    }
}
