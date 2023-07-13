package org.ard.jdbc.profiler.testutils;

import org.ard.jdbc.profiler.Profiler;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.List;

@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class TestSpringBootApplication {

    @Aspect
    @Component
    public static class ProfilerAspect {

        @Before("execution(* org.ard..*Dao.*(..)) || execution(* org.ard..*Service.*(..))")
        public void before() {
            Profiler.push();
        }

        @After("execution(* org.ard..*Dao.*(..)) || execution(* org.ard..*Service.*(..))")
        public void after(final JoinPoint jp) {
            Profiler.pop("invoke", jp.toShortString());
        }

    }

    @Configuration
    public static class Config {

        @Bean
        public DataSource dataSource() {
            final DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
            driverManagerDataSource.setUrl("proxy:jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
            driverManagerDataSource.setDriverClassName("org.ard.jdbc.profiler.driver.Driver");
            return driverManagerDataSource;
        }

        @Bean
        public JdbcTemplate jdbcTemplate() {
            return new JdbcTemplate(dataSource());
        }

    }

    @Repository
    public static class DummyDao {

        private final JdbcTemplate jdbcTemplate;

        @Autowired
        public DummyDao(final JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
        }

        public void createTable() {
            jdbcTemplate.execute("drop table if exists test_data");
            jdbcTemplate.execute("create table test_data(id int primary key, val varchar(255))");
        }

        public void insertData() {
            jdbcTemplate.execute("insert into test_data (id, val) values (1, 'testvalue1')");
            jdbcTemplate.execute("insert into test_data (id, val) values (3, 'testvalue2')");
            jdbcTemplate.execute("insert into test_data (id, val) values (4, 'testvalue3')");
        }

        public List<String> selectWithPreparedStatement(final int from, final int to) {
            return jdbcTemplate.query("select val from test_data where id between ? and ?",
                    (rs, rowNum) -> rs.getString(1), from, to);
        }

    }

    @Repository
    public static class AnotherDao {

        private final JdbcTemplate jdbcTemplate;

        @Autowired
        public AnotherDao(final JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
        }

        public List<String> anotherSelectWithPreparedStatement() {
            return jdbcTemplate.query("select val from test_data where id = ?",
                    (rs, rowNum) -> rs.getString(1), 365);
        }

    }

    @Service
    public static class SomeService {

        private final DummyDao dao1;
        private final AnotherDao dao2;

        @Autowired
        public SomeService(final DummyDao dao1, final AnotherDao dao2) {
            this.dao1 = dao1;
            this.dao2 = dao2;
        }

        public void someMethodToShowNesting() {
            dao1.selectWithPreparedStatement(1, 3);
            dao2.anotherSelectWithPreparedStatement();
        }

    }

    @Autowired
    public TestSpringBootApplication(final DummyDao dao, final SomeService someService) {
        dao.createTable();
        dao.insertData();
        dao.selectWithPreparedStatement(1, 4);
        someService.someMethodToShowNesting();
    }

}
