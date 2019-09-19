package example.app.caching.inline;

import java.util.Arrays;
import java.util.function.Predicate;

import example.app.caching.inline.model.ResultHolder;
import example.app.caching.inline.repo.CalculatorRepository;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableLocator;
import org.springframework.data.gemfire.config.annotation.EnableManager;
import org.springframework.geode.cache.InlineCachingRegionConfigurer;

@SpringBootApplication
@CacheServerApplication(port = 0, useClusterConfiguration = true )
@EnableLocator(port = 20334)
@EnableManager
@EntityScan(basePackageClasses = ResultHolder.class)
public class SpringBootCluster{

  public static void main(String[] args) {
    new SpringApplicationBuilder(SpringBootCluster.class)
        .web(WebApplicationType.NONE)
        .build()
        .run(args);
  }

  @Bean
  InlineCachingRegionConfigurer<ResultHolder, ResultHolder.ResultKey> inlineCachingForCalculatorApplicationRegionsConfigurer(
      CalculatorRepository calculatorRepository) {

    Predicate<String> regionBeanNamePredicate = regionBeanName ->
        Arrays.asList("Factorials", "SquareRoots").contains(regionBeanName);

    return new InlineCachingRegionConfigurer<>(calculatorRepository, regionBeanNamePredicate);
  }
}
