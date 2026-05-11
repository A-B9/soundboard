package com.soundboard.soundboard.unit.config;

import com.soundboard.soundboard.config.AdminProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:adminpropstest;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class TestAdminProperties {

    @Autowired
    AdminProperties adminProperties;

    /**
     * Under the "test" profile, application-test.properties sets
     * app.admin.force-password-change=true.  The Spring binding infrastructure
     * must honour that value and inject it into the record component.
     */
    @Test
    void forcePasswordChange_isTrue_underTestProfile() {
        assertThat(adminProperties.forcePasswordChange()).isTrue();
    }

    /**
     * Canonical all-args constructor: calling new AdminProperties(false) directly
     * (no Spring context) must return a record whose accessor returns false.
     */
    @Test
    void adminProperties_canBeInstantiatedWithFalse() {
        AdminProperties props = new AdminProperties(false);

        assertThat(props.forcePasswordChange()).isFalse();
    }

    /**
     * Compact no-arg constructor delegates to this(true) — fail-closed default.
     * Insecure behaviour (false) must be explicitly opted into via properties.
     */
    @Test
    void adminProperties_defaultConstructor_returnsTrue() {
        AdminProperties props = new AdminProperties();

        assertThat(props.forcePasswordChange()).isTrue();
    }
}
