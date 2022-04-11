package se.kry.springboot.demo.handson.infra;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class InfraConfiguration {

  @Bean
  @ConditionalOnMissingBean(Clock.class)
  Clock clock() {
    return Clock.systemUTC();
  }

  @Bean
  @Scope(SCOPE_PROTOTYPE)
  Logger logger(InjectionPoint injectionPoint) {
    return LoggerFactory.getLogger(injectionPoint.getMember().getDeclaringClass());
  }
}
