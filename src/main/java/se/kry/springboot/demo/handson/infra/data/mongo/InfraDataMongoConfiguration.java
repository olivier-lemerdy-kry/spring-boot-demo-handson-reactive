package se.kry.springboot.demo.handson.infra.data.mongo;

import javax.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import se.kry.springboot.demo.handson.infra.data.InfraDataConfiguration;

@Configuration
@EnableReactiveMongoAuditing(dateTimeProviderRef = InfraDataConfiguration.BeanNames.DATE_TIME_PROVIDER_NAME)
public class InfraDataMongoConfiguration {

  @Bean
  ValidatingMongoEventListener validatingMongoEventListener(Validator validator) {
    return new ValidatingMongoEventListener(validator);
  }

}
