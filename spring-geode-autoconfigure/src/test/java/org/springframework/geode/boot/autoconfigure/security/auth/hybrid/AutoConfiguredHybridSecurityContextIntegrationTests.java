/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.springframework.geode.boot.autoconfigure.security.auth.hybrid;

import java.io.IOException;
import java.util.Properties;

import org.apache.geode.cache.client.ClientCache;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableLocator;
import org.springframework.data.gemfire.config.annotation.EnableLogging;
import org.springframework.geode.boot.autoconfigure.ClientSecurityAutoConfiguration;
import org.springframework.geode.boot.autoconfigure.security.auth.AbstractAutoConfiguredSecurityContextIntegrationTests;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration Tests testing the functionality and behavior of {@link ClientSecurityAutoConfiguration} when a
 * Spring Boot app is deployed (pushed) to Pivotal CloudFoundry (PCF), however, the app has not be bound to a
 * Pivotal Cloud Cache (PCC) service instance.
 *
 * This Use Case is common when users want to deploy their Spring Boot, {@link ClientCache} apps to
 * Pivotal CloudFoundry (PCF) however, want to connect those apps to an external Apache Geode or Pivotal GemFire
 * cluster.
 *
 * @author John Blum
 * @see java.util.Properties
 * @see org.junit.Test
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.data.gemfire.config.annotation.CacheServerApplication
 * @see org.springframework.data.gemfire.config.annotation.EnableLocator
 * @see org.springframework.data.gemfire.config.annotation.EnableLogging
 * @see org.springframework.geode.boot.autoconfigure.ClientSecurityAutoConfiguration
 * @see org.springframework.geode.boot.autoconfigure.security.auth.AbstractAutoConfiguredSecurityContextIntegrationTests
 * @see org.springframework.test.annotation.DirtiesContext
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.1.0
 */
@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
	classes = AutoConfiguredHybridSecurityContextIntegrationTests.GemFireClientConfiguration.class,
	properties = {
		"spring.data.gemfire.pool.locators=localhost[54441]",
		"spring.data.gemfire.security.username=phantom",
		"spring.data.gemfire.security.password=s3cr3t"
	},
	webEnvironment = SpringBootTest.WebEnvironment.NONE
)
public class AutoConfiguredHybridSecurityContextIntegrationTests
		extends AbstractAutoConfiguredSecurityContextIntegrationTests {

	private static final String GEMFIRE_LOG_LEVEL = "error";
	private static final String VCAP_APPLICATION_PROPERTIES = "application-vcap-hybrid.properties";

	private static Properties vcapApplicationProperties = new Properties();

	@BeforeClass
	public static void startGemFireServer() throws IOException {

		startGemFireServer(GemFireServerConfiguration.class,"-Dspring.profiles.active=security-hybrid");
		loadVcapApplicationProperties();
		unsetTestAutoConfiguredPoolServersPortSystemProperty();
	}

	public static void loadVcapApplicationProperties() throws IOException {

		vcapApplicationProperties.load(new ClassPathResource(VCAP_APPLICATION_PROPERTIES).getInputStream());

		vcapApplicationProperties.stringPropertyNames().forEach(property ->
			System.setProperty(property, vcapApplicationProperties.getProperty(property)));
	}

	public static void unsetTestAutoConfiguredPoolServersPortSystemProperty() {
		System.clearProperty(GEMFIRE_POOL_SERVERS_PROPERTY);
	}

	@AfterClass
	public static void cleanUpUsedResources() {
		vcapApplicationProperties.stringPropertyNames().forEach(System::clearProperty);
	}

	@SpringBootApplication
	@EnableLogging(logLevel = GEMFIRE_LOG_LEVEL)
	static class GemFireClientConfiguration extends BaseGemFireClientConfiguration { }

	@SpringBootApplication
	@EnableLocator(port = 54441)
	@CacheServerApplication(name = "AutoConfiguredHybridSecurityContextIntegrationTests", logLevel = GEMFIRE_LOG_LEVEL)
	static class GemFireServerConfiguration extends BaseGemFireServerConfiguration {

		public static void main(String[] args) {

			new SpringApplicationBuilder(GemFireServerConfiguration.class)
				.web(WebApplicationType.NONE)
				.build()
				.run(args);
		}
	}
}
