package com.api.sqlcopilot.shared.utils;

import com.api.sqlcopilot.exception.ForbiddenSqlException;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.util.regex.Pattern;

@Slf4j
public class SqlValidatorUtils {

    private static final Pattern DANGEROUS_PATTERNS = Pattern.compile(
            "\\b(into\\s+outfile|into\\s+dumpfile|load_file|sleep\\s*\\(|benchmark\\s*\\(|for\\s+update|xp_cmdshell)\\b",
            Pattern.CASE_INSENSITIVE
    );

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

            if (DANGEROUS_PATTERNS.matcher(sql).find()) {
                log.warn("SQL rejected — dangerous construct: {}", sanitizeForLog(sql));
                throw new ForbiddenSqlException("SQL contains disallowed constructs");
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
