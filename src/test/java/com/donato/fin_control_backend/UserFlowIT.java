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

class UserFlowIT extends IntegrationTestBase {

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void userLifecycle_create_login_update_logout_delete() {
        String email = "user_" + UUID.randomUUID() + "@example.com";
        String password = "SenhaForte123";
        String newPassword = "SenhaForte456";
        String newEmail = "user_new_" + UUID.randomUUID() + "@example.com";

        // create user
        Map<String, Object> createBody = new HashMap<>();
        createBody.put("email", email);
        createBody.put("password", password);
        HttpResponse<String> createResponse = post("/finControl/user", createBody, null);
        assertThat(createResponse.statusCode()).isEqualTo(201);

        // login
        String token = login(email, password);
        assertThat(token).isNotBlank();

        // get user
        HttpResponse<String> getResponse = get("/finControl/user", token);
        assertThat(getResponse.statusCode()).isEqualTo(200);
        Map<String, Object> getUser = readMap(getResponse.body());
        assertThat(getUser.get("email")).isEqualTo(email);

        // update email + password
        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("email", newEmail);
        updateBody.put("nowPassword", password);
        updateBody.put("newPassword", newPassword);
        HttpResponse<String> updateResponse = put("/finControl/user", updateBody, token);
        assertThat(updateResponse.statusCode()).isEqualTo(200);
        Map<String, Object> updatedUser = readMap(updateResponse.body());
        assertThat(updatedUser.get("email")).isEqualTo(newEmail);

        HttpResponse<String> oldTokenResponse = get("/finControl/user", token);
        assertThat(oldTokenResponse.statusCode()).isEqualTo(401);

        // login with new password
        String token2 = login(newEmail, newPassword);
        assertThat(token2).isNotBlank();

        // logout
        HttpResponse<String> logoutResponse = post("/finControl/user/logout", null, token2);
        assertThat(logoutResponse.statusCode()).isEqualTo(204);

        // token should be revoked
        HttpResponse<String> afterLogout = get("/finControl/user", token2);
        assertThat(afterLogout.statusCode()).isEqualTo(401);

        // login again and delete user
        String token3 = login(newEmail, newPassword);
        Map<String, Object> deleteBody = new HashMap<>();
        deleteBody.put("password", newPassword);
        HttpResponse<String> deleteResponse = delete("/finControl/user", deleteBody, token3);
        assertThat(deleteResponse.statusCode()).isEqualTo(204);

        // login should fail after deactivation
        HttpResponse<String> loginAfterDelete = post("/finControl/user/login", loginBody(newEmail, newPassword), null);
        assertThat(loginAfterDelete.statusCode()).isEqualTo(401);
    }

    @Test
    void createUser_shouldReturnConflict_whenEmailAlreadyExists() {
        String email = "duplicate_" + UUID.randomUUID() + "@example.com";
        String password = "SenhaForte123";

        HttpResponse<String> firstResponse = post("/finControl/user", loginBody(email, password), null);
        assertThat(firstResponse.statusCode()).isEqualTo(201);

        HttpResponse<String> duplicateResponse = post("/finControl/user", loginBody(email, password), null);
        assertThat(duplicateResponse.statusCode()).isEqualTo(409);
        assertThat(readMap(duplicateResponse.body()).get("message")).isEqualTo("Usuário já existe");
    }

    @Test
    void login_shouldReturnUnauthorized_whenPasswordIsWrong() {
        String email = "wrong_pass_" + UUID.randomUUID() + "@example.com";
        String password = "SenhaForte123";

        HttpResponse<String> createResponse = post("/finControl/user", loginBody(email, password), null);
        assertThat(createResponse.statusCode()).isEqualTo(201);

        HttpResponse<String> loginResponse = post("/finControl/user/login", loginBody(email, "SenhaErrada123"), null);
        assertThat(loginResponse.statusCode()).isEqualTo(401);
        assertThat(readMap(loginResponse.body()).get("message")).isEqualTo("Credenciais inválidas.");
    }

    @Test
    void getUser_shouldReturnUnauthorized_whenAuthorizationHeaderIsMissing() {
        HttpResponse<String> response = get("/finControl/user", null);
        assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test
    void alterUser_shouldReturnBadRequest_whenNoChangesAreProvided() {
        String email = "no_change_" + UUID.randomUUID() + "@example.com";
        String password = "SenhaForte123";

        HttpResponse<String> createResponse = post("/finControl/user", loginBody(email, password), null);
        assertThat(createResponse.statusCode()).isEqualTo(201);

        String token = login(email, password);
        HttpResponse<String> updateResponse = put("/finControl/user", Map.of(), token);
        assertThat(updateResponse.statusCode()).isEqualTo(400);
        assertThat(readMap(updateResponse.body()).get("message")).isEqualTo("Informe ao menos um campo para alterar");
    }

    @Test
    void alterUser_shouldReturnBadRequest_whenChangingEmailWithoutCurrentPassword() {
        String email = "email_change_" + UUID.randomUUID() + "@example.com";
        String password = "SenhaForte123";

        HttpResponse<String> createResponse = post("/finControl/user", loginBody(email, password), null);
        assertThat(createResponse.statusCode()).isEqualTo(201);

        String token = login(email, password);
        HttpResponse<String> updateResponse = put("/finControl/user", Map.of("email", "novo_" + email), token);
        assertThat(updateResponse.statusCode()).isEqualTo(400);
        assertThat(readMap(updateResponse.body()).get("message"))
                .isEqualTo("A senha atual é obrigatória para alterar email ou senha");
    }

    @Test
    void alterUser_shouldReturnUnauthorized_whenCurrentPasswordIsWrong() {
        String email = "wrong_now_" + UUID.randomUUID() + "@example.com";
        String password = "SenhaForte123";

        HttpResponse<String> createResponse = post("/finControl/user", loginBody(email, password), null);
        assertThat(createResponse.statusCode()).isEqualTo(201);

        String token = login(email, password);
        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("email", "updated_" + email);
        updateBody.put("nowPassword", "SenhaErrada123");

        HttpResponse<String> updateResponse = put("/finControl/user", updateBody, token);
        assertThat(updateResponse.statusCode()).isEqualTo(401);
        assertThat(readMap(updateResponse.body()).get("message")).isEqualTo("A senha atual está incorreta");
    }

    @Test
    void deleteUser_shouldReturnUnauthorized_whenPasswordIsWrong() {
        String email = "delete_wrong_" + UUID.randomUUID() + "@example.com";
        String password = "SenhaForte123";

        HttpResponse<String> createResponse = post("/finControl/user", loginBody(email, password), null);
        assertThat(createResponse.statusCode()).isEqualTo(201);

        String token = login(email, password);
        HttpResponse<String> deleteResponse = delete("/finControl/user", Map.of("password", "SenhaErrada123"), token);
        assertThat(deleteResponse.statusCode()).isEqualTo(401);
        assertThat(readMap(deleteResponse.body()).get("message")).isEqualTo("A senha atual está incorreta");
    }

    private String login(String email, String password) {
        HttpResponse<String> response = post("/finControl/user/login", loginBody(email, password), null);
        assertThat(response.statusCode()).isEqualTo(200);
        Map<String, Object> payload = readMap(response.body());
        Object token = payload.get("token");
        assertThat(token).isInstanceOf(String.class);
        return (String) token;
    }

    private Map<String, Object> loginBody(String email, String password) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        return body;
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
                .header("Content-Type", "application/json")
                .method("DELETE", HttpRequest.BodyPublishers.ofString(toJson(body)));
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
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
