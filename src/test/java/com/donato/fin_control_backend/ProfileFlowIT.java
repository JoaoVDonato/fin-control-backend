package com.donato.fin_control_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileFlowIT extends IntegrationTestBase {

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void profileLifecycle_create_get_update_delete() {
        String email = "profile_" + UUID.randomUUID() + "@example.com";
        String password = "SenhaForte123";

        createUser(email, password);
        String token = login(email, password);

        Map<String, Object> createProfile = new HashMap<>();
        createProfile.put("fullName", "Usuario Perfil");
        createProfile.put("displayName", "u.perfil");
        createProfile.put("birthdate", "1995-06-15");
        createProfile.put("locale", "pt-BR");
        createProfile.put("timezone", "America/Sao_Paulo");
        createProfile.put("currency", "BRL");
        createProfile.put("phone", "11999990000");
        createProfile.put("avatarUrl", "https://example.com/avatar.png");

        HttpResponse<String> createResponse = post("/finControl/profile", createProfile, token);
        assertThat(createResponse.statusCode()).isEqualTo(201);
        Map<String, Object> createdProfile = readMap(createResponse.body());
        assertThat(createdProfile.get("fullName")).isEqualTo("Usuario Perfil");

        HttpResponse<String> getResponse = get("/finControl/profile", token);
        assertThat(getResponse.statusCode()).isEqualTo(200);
        Map<String, Object> gotProfile = readMap(getResponse.body());
        assertThat(gotProfile.get("displayName")).isEqualTo("u.perfil");

        Map<String, Object> updateProfile = new HashMap<>();
        updateProfile.put("displayName", "perfil.atualizado");
        updateProfile.put("phone", "11911112222");
        HttpResponse<String> updateResponse = put("/finControl/profile", updateProfile, token);
        assertThat(updateResponse.statusCode()).isEqualTo(200);
        Map<String, Object> updatedProfile = readMap(updateResponse.body());
        assertThat(updatedProfile.get("displayName")).isEqualTo("perfil.atualizado");
        assertThat(updatedProfile.get("phone")).isEqualTo("11911112222");

        HttpResponse<String> deleteResponse = delete("/finControl/profile", null, token);
        assertThat(deleteResponse.statusCode()).isEqualTo(204);

        HttpResponse<String> getAfterDelete = get("/finControl/profile", token);
        assertThat(getAfterDelete.statusCode()).isEqualTo(400);
    }

    private void createUser(String email, String password) {
        Map<String, Object> createBody = new HashMap<>();
        createBody.put("email", email);
        createBody.put("password", password);
        HttpResponse<String> response = post("/finControl/user", createBody, null);
        assertThat(response.statusCode()).isEqualTo(201);
    }

    private String login(String email, String password) {
        Map<String, Object> loginBody = new HashMap<>();
        loginBody.put("email", email);
        loginBody.put("password", password);
        HttpResponse<String> response = post("/finControl/user/login", loginBody, null);
        assertThat(response.statusCode()).isEqualTo(200);
        Map<String, Object> payload = readMap(response.body());
        Object token = payload.get("token");
        assertThat(token).isInstanceOf(String.class);
        return (String) token;
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private HttpResponse<String> get(String path, String token) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url(path))).GET();
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return send(builder);
    }

    private HttpResponse<String> post(String path, Object body, String token) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url(path)))
                .header("Content-Type", "application/json");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        if (body == null) {
            builder.POST(HttpRequest.BodyPublishers.noBody());
        } else {
            builder.POST(HttpRequest.BodyPublishers.ofString(toJson(body)));
        }
        return send(builder);
    }

    private HttpResponse<String> put(String path, Object body, String token) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url(path)))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(toJson(body)));
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return send(builder);
    }

    private HttpResponse<String> delete(String path, Object body, String token) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url(path)))
                .header("Content-Type", "application/json");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        if (body == null) {
            builder.method("DELETE", HttpRequest.BodyPublishers.noBody());
        } else {
            builder.method("DELETE", HttpRequest.BodyPublishers.ofString(toJson(body)));
        }
        return send(builder);
    }

    private HttpResponse<String> send(HttpRequest.Builder builder) {
        try {
            return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new IllegalStateException("HTTP request failed", e);
        }
    }

    private Map<String, Object> readMap(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse JSON response", e);
        }
    }

    private String toJson(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize JSON body", e);
        }
    }
}
