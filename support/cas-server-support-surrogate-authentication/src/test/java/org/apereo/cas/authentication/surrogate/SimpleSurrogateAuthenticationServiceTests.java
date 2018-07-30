package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.config.BaseSurrogateAuthenticationTestsConfiguration;
import org.apereo.cas.util.CollectionUtils;

import lombok.Getter;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

/**
 * This is {@link SimpleSurrogateAuthenticationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@SpringBootTest(classes = {
    BaseSurrogateAuthenticationTestsConfiguration.class,
    RefreshAutoConfiguration.class
})
public class SimpleSurrogateAuthenticationServiceTests extends BaseSurrogateAuthenticationServiceTests {
    private final SurrogateAuthenticationService service = new SimpleSurrogateAuthenticationService(
            CollectionUtils.wrap("casuser", CollectionUtils.wrapList("banderson")), servicesManager);
}
