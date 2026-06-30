package com.api.sqlcopilot.shared.utils;

import com.api.sqlcopilot.exception.ForbiddenSqlException;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

@Slf4j
public class SqlValidatorUtils {

    public static void validate(final String sql) {
        if (sql == null || sql.isBlank()) {
            throw new ForbiddenSqlException("SQL cannot be empty");
        }

        try {
            Statement statement = CCJSqlParserUtil.parse(sql);

            if (!(statement instanceof Select)) {
                log.warn("SQL rejected — not a SELECT: {}", sanitizeForLog(sql));
                throw new ForbiddenSqlException("Only SELECT queries are allowed");
            }

        } catch (ForbiddenSqlException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("SQL rejected — failed to parse: {}", sanitizeForLog(sql));
            throw new ForbiddenSqlException("Invalid or unparseable SQL");
        }
    }

    private static String sanitizeForLog(final String sql) {
        return sql.length() > 100 ? sql.substring(0, 100) + "..." : sql;
    }
}
