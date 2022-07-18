package se.kry.springboot.demo.handson.infra.data;

import java.time.Clock;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;

@Configuration
public class InfraDataConfiguration {

  public interface BeanNames {
    String DATE_TIME_PROVIDER_NAME = "dateTimeProvider"
  }

  @Bean(name = BeanNames.DATE_TIME_PROVIDER_NAME)
  DateTimeProvider dateTimeProvider(Clock clock) {
    return () -> Optional.of(clock.instant());
  }

}
