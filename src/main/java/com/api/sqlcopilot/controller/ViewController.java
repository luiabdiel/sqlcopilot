package com.api.sqlcopilot.controller;

import com.api.sqlcopilot.dto.ChatRequest;
import com.api.sqlcopilot.dto.ChatResponse;
import com.api.sqlcopilot.enums.ActionType;
import com.api.sqlcopilot.service.ChatService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/chat")
public class ViewController {

    private final ChatService chatService;

    public ViewController(final ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public String index() {
        return "index";
    }

    @PostMapping
    public String process(@RequestParam final String message,
                          @RequestParam final ActionType action,
                          final Model model) {
        final ChatResponse response = chatService.process(new ChatRequest(action, message));

        model.addAttribute("action", action);
        model.addAttribute("message", message);
        model.addAttribute("result", action == ActionType.GENERATE ? response.sql() : response.explanation());

        return "index";
    }
}
