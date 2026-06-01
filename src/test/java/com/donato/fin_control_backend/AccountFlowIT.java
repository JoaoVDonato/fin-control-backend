package com.donato.fin_control_backend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AccountFlowIT extends IntegrationTestBase {

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void accountLifecycle_create_list_update_archive() {
        String token = registerAndLogin();

        // create account
        Map<String, Object> createBody = new HashMap<>();
        createBody.put("name", "Conta Corrente");
        createBody.put("type", "CHECKING");
        createBody.put("initialBalance", 1000.00);
        createBody.put("currency", "BRL");
        createBody.put("institution", "Banco do Brasil");

        HttpResponse<String> createResp = post("/finControl/accounts", createBody, token);
        assertThat(createResp.statusCode()).isEqualTo(201);
        Map<String, Object> account = readMap(createResp.body());
        assertThat(account.get("name")).isEqualTo("Conta Corrente");
        assertThat(account.get("type")).isEqualTo("CHECKING");
        assertThat(account.get("archived")).isEqualTo(false);
        Long id = ((Number) account.get("id")).longValue();

        // list accounts
        HttpResponse<String> listResp = get("/finControl/accounts", token);
        assertThat(listResp.statusCode()).isEqualTo(200);
        List<?> list = readList(listResp.body());
        assertThat(list).hasSize(1);

        // get by id
        HttpResponse<String> getResp = get("/finControl/accounts/" + id, token);
        assertThat(getResp.statusCode()).isEqualTo(200);

        // update account
        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("name", "Conta Corrente Atualizada");
        updateBody.put("institution", "Itaú");

        HttpResponse<String> updateResp = put("/finControl/accounts/" + id, updateBody, token);
        assertThat(updateResp.statusCode()).isEqualTo(200);
        Map<String, Object> updated = readMap(updateResp.body());
        assertThat(updated.get("name")).isEqualTo("Conta Corrente Atualizada");
        assertThat(updated.get("institution")).isEqualTo("Itaú");

        // archive account
        HttpResponse<String> archiveResp = patch("/finControl/accounts/" + id + "/archive", null, token);
        assertThat(archiveResp.statusCode()).isEqualTo(204);

        // archived account should not appear in default list
        HttpResponse<String> listAfterArchive = get("/finControl/accounts", token);
        List<?> listAfter = readList(listAfterArchive.body());
        assertThat(listAfter).isEmpty();

        // but appears when includeArchived=true
        HttpResponse<String> listWithArchived = get("/finControl/accounts?includeArchived=true", token);
        List<?> listAll = readList(listWithArchived.body());
        assertThat(listAll).hasSize(1);
    }

    @Test
    void account_ownership_shouldReturn404_forOtherUsersAccount() {
        String tokenA = registerAndLogin();
        String tokenB = registerAndLogin();

        // user A creates account
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Conta A");
        body.put("type", "SAVINGS");
        HttpResponse<String> createResp = post("/finControl/accounts", body, tokenA);
        Long id = ((Number) readMap(createResp.body()).get("id")).longValue();

        // user B tries to access
        HttpResponse<String> getResp = get("/finControl/accounts/" + id, tokenB);
        assertThat(getResp.statusCode()).isEqualTo(404);
    }

    @Test
    void createAccount_shouldReturn400_whenNameIsMissing() {
        String token = registerAndLogin();
        Map<String, Object> body = new HashMap<>();
        body.put("type", "CHECKING");
        HttpResponse<String> resp = post("/finControl/accounts", body, token);
        assertThat(resp.statusCode()).isEqualTo(400);
    }

    // --- helpers ---

    private String registerAndLogin() {
        String email = "acc_" + UUID.randomUUID() + "@test.com";
        String password = "SenhaForte123";
        post("/finControl/user", Map.of("email", email, "password", password), null);
        HttpResponse<String> loginResp = post("/finControl/user/login", Map.of("email", email, "password", password), null);
        return (String) readMap(loginResp.body()).get("token");
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private HttpResponse<String> get(String path, String token) {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url(path))).GET();
        if (token != null) b.header("Authorization", "Bearer " + token);
        return send(b);
    }

    private HttpResponse<String> post(String path, Object body, String token) {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url(path)))
                .header("Content-Type", "application/json");
        if (token != null) b.header("Authorization", "Bearer " + token);
        b.POST(body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(toJson(body)));
        return send(b);
    }

    private HttpResponse<String> put(String path, Object body, String token) {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url(path)))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(toJson(body)));
        if (token != null) b.header("Authorization", "Bearer " + token);
        return send(b);
    }

    private HttpResponse<String> patch(String path, Object body, String token) {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url(path)))
                .header("Content-Type", "application/json")
                .method("PATCH", body == null
                        ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(toJson(body)));
        if (token != null) b.header("Authorization", "Bearer " + token);
        return send(b);
    }

    private HttpResponse<String> send(HttpRequest.Builder b) {
        try {
            return client.send(b.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new IllegalStateException("HTTP request failed", e);
        }
    }

    private Map<String, Object> readMap(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse: " + json, e);
        }
    }

    private List<?> readList(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<?>>() {});
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse: " + json, e);
        }
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize", e);
        }
    }
}
