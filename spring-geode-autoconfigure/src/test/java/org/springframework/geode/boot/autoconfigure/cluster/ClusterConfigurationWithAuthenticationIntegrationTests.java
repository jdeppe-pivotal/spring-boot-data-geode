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
package org.springframework.geode.boot.autoconfigure.cluster;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.distributed.internal.DistributionConfig;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.GemfireUtils;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableClusterConfiguration;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.config.annotation.EnableLogging;
import org.springframework.data.gemfire.config.annotation.EnableManager;
import org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport;
import org.springframework.geode.security.TestSecurityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import example.app.books.model.Book;
import example.app.books.model.ISBN;

/**
 * Integration tests testing the SDG {@link EnableClusterConfiguration} annotation functionality
 * when the GemFire/Geode server is configured with Security (Authentication).
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.data.gemfire.config.annotation.EnableClusterConfiguration
 * @see org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.0.0
 */
@ActiveProfiles("cluster-configuration-with-auth-client")
@RunWith(SpringRunner.class)
@SpringBootTest(
	classes = ClusterConfigurationWithAuthenticationIntegrationTests.GeodeClientConfiguration.class,
	webEnvironment = SpringBootTest.WebEnvironment.NONE,
	properties = {
		"spring.data.gemfire.management.require-https=false",
		"spring.data.gemfire.security.username=test",
		"spring.data.gemfire.security.password=test"
	}
)
@SuppressWarnings("unused")
public class ClusterConfigurationWithAuthenticationIntegrationTests extends ForkingClientServerIntegrationTestsSupport {

	private static final String GEMFIRE_LOG_LEVEL = "off";

	@BeforeClass
	public static void startGemFireServer() throws IOException {
		startGemFireServer(GeodeServerConfiguration.class,
			"-Dspring.profiles.active=cluster-configuration-with-auth-server");
	}

	@Autowired
	@Qualifier("booksTemplate")
	private GemfireTemplate booksTemplate;

	@Before
	public void setup() {
		assertThat(this.booksTemplate).isNotNull();
	}

	@Test
	public void clusterConfigurationAndRegionDataAccessOperationsAreSuccessful() {

		Book expetedSeriesOfUnfortunateEvents = Book
			.newBook("A Serious of Unfortunate Events")
			.identifiedBy(ISBN.autoGenerated());

		this.booksTemplate.put(expetedSeriesOfUnfortunateEvents.getIsbn(), expetedSeriesOfUnfortunateEvents);

		Book actualSeriesOfUnfortunateEvents = this.booksTemplate.get(expetedSeriesOfUnfortunateEvents.getIsbn());

		assertThat(actualSeriesOfUnfortunateEvents).isNotNull();
		assertThat(actualSeriesOfUnfortunateEvents).isEqualTo(expetedSeriesOfUnfortunateEvents);
		assertThat(actualSeriesOfUnfortunateEvents).isNotSameAs(expetedSeriesOfUnfortunateEvents);
	}

	@SpringBootApplication
	@Profile("cluster-configuration-with-auth-client")
	@EnableClusterConfiguration(useHttp = true)
	@EnableLogging(logLevel = GEMFIRE_LOG_LEVEL)
	@EnableEntityDefinedRegions(basePackageClasses = Book.class)
	static class GeodeClientConfiguration { }

	@SpringBootApplication
	@Profile("cluster-configuration-with-auth-server")
	@CacheServerApplication(name = "ClusterConfigurationWithAuthenticationIntegrationTests", logLevel = GEMFIRE_LOG_LEVEL)
	@EnableManager(start = true)
	static class GeodeServerConfiguration {

		private static final String GEODE_HOME_PROPERTY = DistributionConfig.GEMFIRE_PREFIX + "home";

		public static void main(String[] args) throws IOException {

			resolveAndConfigureGeodeHome();

			//System.err.printf("%s [%s]%n", GEODE_HOME_PROPERTY, System.getProperty(GEODE_HOME_PROPERTY));

			new SpringApplicationBuilder(GeodeServerConfiguration.class)
				.web(WebApplicationType.NONE)
				.build()
				.run(args);
		}

		private static void resolveAndConfigureGeodeHome() throws IOException {

			ClassPathResource resource = new ClassPathResource("/geode-home");

			File resourceFile = resource.getFile();

			System.setProperty(GEODE_HOME_PROPERTY, resourceFile.getAbsolutePath());
		}

		@Bean
		org.apache.geode.security.SecurityManager testSecurityManager() {
			return new TestSecurityManager();
		}

		@Bean
		ApplicationRunner peerCacheVerifier(GemFireCache cache) {

			return args -> {

				assertThat(cache).isNotNull();
				assertThat(GemfireUtils.isPeer(cache)).isTrue();
				assertThat(cache.getName())
					.isEqualTo(ClusterConfigurationWithAuthenticationIntegrationTests.class.getSimpleName());

				List<String> regionNames = cache.rootRegions().stream()
					.map(Region::getName)
					.collect(Collectors.toList());

				assertThat(regionNames)
					.describedAs("Expected no Regions; but was [%s]", regionNames)
					.isEmpty();
			};
		}
	}
}
