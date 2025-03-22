package org.languagetool.server;


import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Statement;
import java.util.regex.Pattern;

@Intercepts({
  @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class}),
  @Signature(type = StatementHandler.class, method = "update", args = {Statement.class}),
  @Signature(type = StatementHandler.class, method = "batch", args = {Statement.class})})
public class LoggingInterceptor implements Interceptor {

  private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);
  private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    Object target = invocation.getTarget();
    long startTime = System.currentTimeMillis();
    StatementHandler statementHandler = (StatementHandler) target;
    try {
      return invocation.proceed();
    } finally {
      long endTime = System.currentTimeMillis();
      long time = endTime - startTime;

      BoundSql boundSql = statementHandler.getBoundSql();
      String sql = WHITESPACE_PATTERN.matcher(boundSql.getSql()).replaceAll(" ");
      Object parameterObject = boundSql.getParameterObject();
      logger.info("Executing SQL: [{}, {}] takes {}ms", sql, parameterObject, time);
    }
  }
}
