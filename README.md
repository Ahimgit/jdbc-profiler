# JDBC Profiler

Lightweight no-dependency JDBC profiler created in 2012 to analyze complex applications with heavy ORM/SQL usage.
Code is a bit outdated for the modern Java but still works and might be useful for someone.  
There are more mature alternatives, such as p6spy, available for logging alone.
This definitely does not support any reactive/R2DBC drivers.

Consists from two parts:

## Driver

Driver that proxies a real JDBC driver collects statistics for calls and outputs SQL and timings into **jdbc-profiler-sql.log** 

#### Configuration
 - requires at least Java 8 at runtime
 - requires at least Java 11 to build
 - build jar with maven `mvn clean install`
 - place jdbc-profiler.jar onto monitored application classpath
 - change jdbc url to `proxy:class=original_real_driver_class|original_real_jdbc_url`
 - change driver class to `org.ard.jdbc.profiler.driver.Driver`

E.g. if original url was `jdbc:postgresql://hostname/test?user=fred&password=secret&ssl=true` and driver `org.postgresql.Driver`    
new values will be `proxy:class=org.postgresql.Driver|jdbc:postgresql://hostname/test?user=fred&password=secret&ssl=true` and `org.ard.jdbc.profiler.driver.Driver`.

For newer drivers that support service provider mechanism you can simply specify url in form of `proxy:jdbc:postgresql://hostname/test?user=fred&password=secret&ssl=true`

Additionally, default logger be configured via providing factory class name environment or system variable.    
Following two factories are provided out of the box:
- [`org.ard.jdbc.profiler.logging.LoggerFactoryDefault`](/src/main/java/org/ard/jdbc/profiler/logging/LoggerFactoryDefault.java) - default simple file logging to the current work dir
- [`org.ard.jdbc.profiler.logging.LoggerFactorySlf4j`](/src/main/java/org/ard/jdbc/profiler/logging/LoggerFactorySlf4j.java) - slf4j bindings logger factory (logger names will be jdbc-profiler-sql & jdbc-profiler)    
E.g. use `set jdbc_proxy_logger=org.ard.jdbc.profiler.logging.LoggerFactorySlf4j` before starting profiled application to change.

#### Sample output in jdbc-profiler-sql.log 
Generated from Jira Server acting as profiled application with jar placed to `atlassian-jira\lib\jdbc-profiler.jar`
And `jira-home\dbconfig.xml` modified as   
```
<jdbc-datasource>
    <url>proxy:class=org.postgresql.Driver|jdbc:postgresql://localhost/jira?user=jira&password=jira</url>
    <driver-class>org.ard.jdbc.profiler.driver.Driver</driver-class>
```

```
2023-07-13 07:11:43.197 pool-18-thread-1 [statement] time: 0, sql: SELECT ID, JOB_ID, START_TIME, RUN_DURATION, RUN_OUTCOME, INFO_MESSAGE FROM PUBLIC.rundetails WHERE JOB_ID=[?=jira-migration-analytics-server-analysis-job-id] ORDER BY START_TIME DESC LIMIT 1 
2023-07-13 07:11:43.197 pool-18-thread-1 [statement] time: 0, sql: SELECT ID, JOB_ID, JOB_RUNNER_KEY, SCHED_TYPE, INTERVAL_MILLIS, FIRST_RUN, CRON_EXPRESSION, TIME_ZONE, NEXT_RUN, VERSION, PARAMETERS FROM PUBLIC.clusteredjob WHERE JOB_RUNNER_KEY=[?=jira-migration-user-email-analytics-events-collector-job-key] 
2023-07-13 07:11:43.197 pool-18-thread-1 [statement] time: 0, sql: SELECT ID, JOB_ID, START_TIME, RUN_DURATION, RUN_OUTCOME, INFO_MESSAGE FROM PUBLIC.rundetails WHERE JOB_ID=[?=jira-migration-user-email-analytics-events-collector-job-id] ORDER BY START_TIME DESC LIMIT 1 
2023-07-13 07:11:44.885 http-nio-8080-exec-15 [statement] time: 3, sql: select "AO_550953_SHORTCUT"."NAME", "AO_550953_SHORTCUT"."SHORTCUT_URL", "AO_550953_SHORTCUT"."ICON", "AO_550953_SHORTCUT"."PROJECT_ID", "AO_550953_SHORTCUT"."ID" from "AO_550953_SHORTCUT" "AO_550953_SHORTCUT" where "AO_550953_SHORTCUT"."PROJECT_ID" = [?=10000] order by "AO_550953_SHORTCUT"."PROJECT_ID" asc 
2023-07-13 07:11:44.886 http-nio-8080-exec-15 [statement] time: 0, sql: commit 
2023-07-13 07:11:44.979 http-nio-8080-exec-7 [statement] time: 12, sql: select O_S_PROPERTY_ENTRY.id, O_S_PROPERTY_ENTRY.propertytype
from PUBLIC.propertyentry O_S_PROPERTY_ENTRY
where O_S_PROPERTY_ENTRY.entity_name = [?=ApplicationUser] and O_S_PROPERTY_ENTRY.entity_id = [?=10000] and O_S_PROPERTY_ENTRY.property_key = [?=last-visited-item.10000]
order by O_S_PROPERTY_ENTRY.id desc
for update 
2023-07-13 07:11:45.320 automation-queue-claimer:thread-1 [statement] time: 0, sql: select "AO_589059_AUTOMATION_QUEUE"."ID"
from "PUBLIC"."AO_589059_AUTOMATION_QUEUE" "AO_589059_AUTOMATION_QUEUE"
where "AO_589059_AUTOMATION_QUEUE"."CLAIMANT" is null
order by "AO_589059_AUTOMATION_QUEUE"."PRIORITY" asc, "AO_589059_AUTOMATION_QUEUE"."ID" asc
limit [?=6] 
2023-07-13 07:11:45.321 automation-queue-claimer:thread-1 [statement] time: 0, sql: commit 
2023-07-13 07:11:48.142 pool-18-thread-1 [statement] time: 0, sql: SELECT DISTINCT JOB_RUNNER_KEY FROM PUBLIC.clusteredjob 
```

## Profiler
Profiler - a thread-local profiler that utilizes metrics collected by the Proxy Driver,
generates **jdbc-profiler.log** that shows stack depth, execution & jdbc timings and number of calls between push() & pop().

#### Usage
##### Directly using calling Profiler class methods:

- `Profiler.push()` - pushes a profiling stack level
- `Profiler.pop(...)` - pops a profiling stack level, prints entry to log with elapsed time, time spent in jdbc and jdbc statement count
- `Profiler.statsTotal(...)` - logs stats on overall jdbc & wrapped executions

##### As an interceptor in a container (e.g AspectJ in Spring or Guice).
See Spring Boot [@Aspect configuration](/src/test/java/org/ard/jdbc/profiler/testutils/TestSpringBootApplication.java#L28)
and [test](/src/test/java/org/ard/jdbc/profiler/ProfilerInterceptorFunctionalTest.java).

#### Sample output for above test:
jdbc-profiler-sql.log:
```
2023-07-13 07:16:53.668 main [statement] time: 1, sql: drop table if exists test_data
2023-07-13 07:16:53.687 main [statement] time: 5, sql: create table test_data(id int primary key, val varchar(255))
2023-07-13 07:16:53.693 main [statement] time: 3, sql: insert into test_data (id, val) values (1, 'testvalue1')
2023-07-13 07:16:53.694 main [statement] time: 0, sql: insert into test_data (id, val) values (3, 'testvalue2')
2023-07-13 07:16:53.695 main [statement] time: 0, sql: insert into test_data (id, val) values (4, 'testvalue3')
2023-07-13 07:16:53.736 main [statement] time: 14, sql: select val from test_data where id between [?=1] and [?=4]
2023-07-13 07:16:53.739 main [statement] time: 0, sql: select val from test_data where id between [?=1] and [?=3]
2023-07-13 07:16:53.742 main [statement] time: 0, sql: select val from test_data where id = [?=365]
```
jdbc-profiler.log:
```
2023-07-13 07:16:53.689 main [invoke] time: 29, db time: 12, count: 2, execution(DummyDao.createTable())
2023-07-13 07:16:53.696 main [invoke] time: 6, db time: 4, count: 3, execution(DummyDao.insertData())
2023-07-13 07:16:53.738 main [invoke] time: 40, db time: 34, count: 1, execution(DummyDao.selectWithPreparedStatement(..))
2023-07-13 07:16:53.740 main [invoke] .. time: 3, db time: 2, count: 1, execution(DummyDao.selectWithPreparedStatement(..))
2023-07-13 07:16:53.743 main [invoke] .. time: 2, db time: 1, count: 1, execution(AnotherDao.anotherSelectWithPreparedStatement())
2023-07-13 07:16:53.743 main [invoke] time: 5, db time: 3, count: 2, execution(SomeService.someMethodToShowNesting()), stack:...
```
- First line `time: 199, db time: 12, count: 2, execution(DummyDao.createTable())` 
\- DummyDao.createTable took total 29ms, 12ms spent in jdbc, did 2 queries.
- Forth and fifth lines`.. time: 1, db time: 0, count: 1, execution(AnotherDao.anotherSelectWithPreparedStatement()`
\- Two dots here denote that this call is deeper in the stack and is "summed" up in line six
- Line six someMethodToShowNesting() called both DummyDao.select... & AnotherDao.anotherSelect..., it shows combined timings & query count from those deeper in the stack.


