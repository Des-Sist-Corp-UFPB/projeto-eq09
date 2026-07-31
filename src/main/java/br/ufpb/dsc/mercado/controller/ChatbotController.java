package br.ufpb.dsc.mercado.controller;

import br.ufpb.dsc.mercado.dto.ChatRequest;
import br.ufpb.dsc.mercado.dto.ChatResponse;
import br.ufpb.dsc.mercado.service.ChatbotService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/conversar")
    public ResponseEntity<ChatResponse> conversar(@AuthenticationPrincipal UserDetails userDetails,
                                                 @Valid @RequestBody ChatRequest request) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        ChatResponse response = chatbotService.conversar(username, request.mensagem());
        return ResponseEntity.ok(response);
    }
}
