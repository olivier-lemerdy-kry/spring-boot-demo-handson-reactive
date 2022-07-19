package se.kry.springboot.demo.handson.infra.data.r2dbc;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import se.kry.springboot.demo.handson.infra.data.InfraDataConfiguration;

@Configuration
@EnableR2dbcAuditing(dateTimeProviderRef = InfraDataConfiguration.BeanNames.DATE_TIME_PROVIDER_NAME)
public class InfraDataR2dbcConfiguration {
}
