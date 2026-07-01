package com.api.sqlcopilot.shared.utils;

import com.api.sqlcopilot.exception.ForbiddenSqlException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.regex.Pattern;

@Slf4j
public class PromptGuardUtils {

    private static final List<Pattern> FORBIDDEN_PATTERNS = List.of(
            Pattern.compile("\\b(delete|update|insert|drop|alter|truncate|grant|revoke|merge|replace|exec|execute|call|create)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(apague|apagar|deletar|atualizar|remover|excluir|inserir|altere|alterar)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(ignore|desconsidere|esque[çc]a)\\b.{0,30}\\b(instru[çc]|regras|prompt|system)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bxp_cmdshell\\b", Pattern.CASE_INSENSITIVE)
    );

    public static void validateUserIntent(final String message) {
        if (message == null || message.isBlank()) {
            throw new ForbiddenSqlException("Mensagem não pode ser vazia");
        }

        for (Pattern pattern : FORBIDDEN_PATTERNS) {
            if (pattern.matcher(message).find()) {
                log.warn("Prompt rejeitado antes da LLM: {}", sanitize(message));
                throw new ForbiddenSqlException("Solicitação fora do escopo permitido (somente leitura)");
            }
        }
    }

    private static String sanitize(String s) {
        return s.length() > 100 ? s.substring(0, 100) + "..." : s;
    }
}
