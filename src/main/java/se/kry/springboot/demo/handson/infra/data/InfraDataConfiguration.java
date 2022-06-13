package se.kry.springboot.demo.handson.infra.data;

import java.time.Clock;
import java.util.Optional;
import org.neo4j.driver.Driver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.neo4j.config.EnableReactiveNeo4jAuditing;
import org.springframework.data.neo4j.core.ReactiveDatabaseSelectionProvider;
import org.springframework.data.neo4j.core.transaction.ReactiveNeo4jTransactionManager;
import org.springframework.data.neo4j.repository.config.ReactiveNeo4jRepositoryConfigurationExtension;
import org.springframework.transaction.ReactiveTransactionManager;

@Configuration
@EnableReactiveNeo4jAuditing(dateTimeProviderRef = InfraDataConfiguration.DATE_TIME_PROVIDER_NAME)
public class InfraDataConfiguration {

  static final String DATE_TIME_PROVIDER_NAME = "dateTimeProvider";

  @Bean(name = DATE_TIME_PROVIDER_NAME)
  DateTimeProvider dateTimeProvider(Clock clock) {
    return () -> Optional.of(clock.instant());
  }

  @Bean(ReactiveNeo4jRepositoryConfigurationExtension.DEFAULT_TRANSACTION_MANAGER_BEAN_NAME)
  ReactiveTransactionManager reactiveTransactionManager(Driver driver,
                                                        ReactiveDatabaseSelectionProvider databaseSelectionProvider) {
    return new ReactiveNeo4jTransactionManager(driver, databaseSelectionProvider);
  }

}
