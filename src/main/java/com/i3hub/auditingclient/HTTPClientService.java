package com.i3hub.auditingclient;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class HTTPClientService {

    @Value("${auditing.restURL:DEFINE IN YML}")
    private String auditRestURL;

    public void makeRequest(ObjectNode message) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(auditRestURL))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(message.toString()))
            .build();
        HttpClient.newHttpClient()
            .sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(HttpResponse::statusCode)
            .thenAccept(System.out::println);
    }
}
