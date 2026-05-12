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

    @Test
    void forcePasswordChange_isTrue_underTestProfile() {
        assertThat(adminProperties.forcePasswordChange()).isTrue();
    }

    @Test
    void adminProperties_canonicalConstructor_acceptsFalse() {
        AdminProperties props = new AdminProperties(false);

        assertThat(props.forcePasswordChange()).isFalse();
    }

    @Test
    void adminProperties_canonicalConstructor_acceptsTrue() {
        AdminProperties props = new AdminProperties(true);

        assertThat(props.forcePasswordChange()).isTrue();
    }
}
