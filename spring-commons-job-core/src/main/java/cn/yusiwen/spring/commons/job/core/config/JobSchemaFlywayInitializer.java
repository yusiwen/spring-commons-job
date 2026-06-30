package cn.yusiwen.spring.commons.job.core.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.InitializingBean;

import javax.sql.DataSource;

/**
 * Initializes the database schema using Flyway with an isolated history table.
 * <p>Uses {@code flyway_job_schema_history} as the schema history table to avoid
 * conflicts with the application's own Flyway configuration.</p>
 */
public class JobSchemaFlywayInitializer implements InitializingBean {

    private final DataSource dataSource;

    /**
     * Creates a new initializer for the given data source.
     *
     * @param dataSource the application data source
     */
    public JobSchemaFlywayInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Runs Flyway migrations from {@code classpath:db/migration/job}.
     */
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
