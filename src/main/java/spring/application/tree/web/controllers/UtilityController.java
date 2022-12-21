package spring.application.tree.web.controllers;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.application.tree.web.webscoket.models.Endpoints;
import spring.application.tree.web.webscoket.models.WebSocketEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/utility")
public class UtilityController {
    @GetMapping("/websocket/endpoints")
    public ResponseEntity<Object> getWebSocketEndpoints() {
        Map<Endpoints, String> endpoints = new HashMap<>();
        Arrays.stream(Endpoints.values()).forEach(endpoint -> endpoints.put(endpoint, endpoint.getEndpointPrefix()));
        return ResponseEntity.ok(endpoints);
    }

    @GetMapping("/websocket/events")
    public ResponseEntity<Object> getWebSocketEvents() {
        return ResponseEntity.ok(WebSocketEvent.values());
    }
}