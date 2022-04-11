package se.kry.springboot.demo.handson.infra.data;

import java.time.Clock;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;

@Configuration
@EnableR2dbcAuditing(dateTimeProviderRef = InfraDataConfiguration.DATE_TIME_PROVIDER_NAME)
public class InfraDataConfiguration {

  static final String DATE_TIME_PROVIDER_NAME = "dateTimeProvider";

  @Bean(name = DATE_TIME_PROVIDER_NAME)
  DateTimeProvider dateTimeProvider(Clock clock) {
    return () -> Optional.of(clock.instant());
  }

}
