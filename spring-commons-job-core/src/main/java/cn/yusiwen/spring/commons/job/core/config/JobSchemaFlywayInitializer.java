package cn.yusiwen.spring.commons.job.core.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.InitializingBean;

import javax.sql.DataSource;

public class JobSchemaFlywayInitializer implements InitializingBean {

    private final DataSource dataSource;

    public JobSchemaFlywayInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void afterPropertiesSet() {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/job")
                .table("flyway_job_schema_history")
                .load();
        flyway.migrate();
    }
}
