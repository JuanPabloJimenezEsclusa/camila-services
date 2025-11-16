package com.camila.discovery;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
@DisplayName("[IT][CamilaDiscoveryApplication] Spring boot smoke test")
class CamilaDiscoveryApplicationTests {
	@Autowired
	private ApplicationContext applicationContext;

	@Test
	@DisplayName("[CamilaDiscoveryApplication] context loaded")
	void contextLoads() {
		Assertions.assertNotNull(this.applicationContext);
	}

	@Test
	@DisplayName("[CamilaDiscoveryApplication] main method starts application")
	void mainMethodStartsApplication() {
		CamilaDiscoveryApplication.main(new String[]{});
		Assertions.assertTrue(true);
	}
}
