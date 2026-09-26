package com.teacheraid;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModulithArchitectureTest {

    @Test
    void modulesShouldBeValid() {
        ApplicationModules.of(TeacherAidApplication.class).verify();
    }
}
