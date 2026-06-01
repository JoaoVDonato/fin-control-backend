package com.donato.fin_control_backend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryTagFlowIT extends IntegrationTestBase {

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void categoryLifecycle_create_list_update_archive() {
        String token = registerAndLogin();

        // create EXPENSE category
        Map<String, Object> body = Map.of("name", "Alimentação", "type", "EXPENSE");
        HttpResponse<String> createResp = post("/finControl/categories", body, token);
        assertThat(createResp.statusCode()).isEqualTo(201);
        Map<String, Object> cat = readMap(createResp.body());
        assertThat(cat.get("type")).isEqualTo("EXPENSE");
        assertThat(cat.get("active")).isEqualTo(true);
        Long id = ((Number) cat.get("id")).longValue();

        // list all
        HttpResponse<String> listResp = get("/finControl/categories", token);
        assertThat(readList(listResp.body())).hasSize(1);

        // list by type
        HttpResponse<String> listIncome = get("/finControl/categories?type=INCOME", token);
        assertThat(readList(listIncome.body())).isEmpty();

        // update
        HttpResponse<String> updateResp = put("/finControl/categories/" + id,
                Map.of("name", "Alimentação e Bebidas"), token);
        assertThat(updateResp.statusCode()).isEqualTo(200);
        assertThat(readMap(updateResp.body()).get("name")).isEqualTo("Alimentação e Bebidas");

        // archive
        HttpResponse<String> archiveResp = patch("/finControl/categories/" + id + "/archive", null, token);
        assertThat(archiveResp.statusCode()).isEqualTo(204);

        // archived should not appear in default list
        assertThat(readList(get("/finControl/categories", token).body())).isEmpty();

        // appears with includeInactive=true
        assertThat(readList(get("/finControl/categories?includeInactive=true", token).body())).hasSize(1);
    }

    @Test
    void category_type_validation_shouldReject_invalidType() {
        String token = registerAndLogin();
        HttpResponse<String> resp = post("/finControl/categories",
                Map.of("name", "Test", "type", "INVALID"), token);
        assertThat(resp.statusCode()).isEqualTo(400);
    }

    @Test
    void category_ownership_shouldReturn404_forOtherUsersCategory() {
        String tokenA = registerAndLogin();
        String tokenB = registerAndLogin();
        Map<String, Object> body = Map.of("name", "Cat A", "type", "EXPENSE");
        HttpResponse<String> createResp = post("/finControl/categories", body, tokenA);
        Long id = ((Number) readMap(createResp.body()).get("id")).longValue();
        assertThat(get("/finControl/categories/" + id, tokenB).statusCode()).isEqualTo(404);
    }

    @Test
    void tagLifecycle_create_list_delete() {
        String token = registerAndLogin();

        HttpResponse<String> createResp = post("/finControl/tags", Map.of("name", "viagem"), token);
        assertThat(createResp.statusCode()).isEqualTo(201);
        Long id = ((Number) readMap(createResp.body()).get("id")).longValue();

        List<?> tags = readList(get("/finControl/tags", token).body());
        assertThat(tags).hasSize(1);

        assertThat(delete("/finControl/tags/" + id, token).statusCode()).isEqualTo(204);
        assertThat(readList(get("/finControl/tags", token).body())).isEmpty();
    }

    // --- helpers ---

    private String registerAndLogin() {
        String email = "cat_" + UUID.randomUUID() + "@test.com";
        String pass = "SenhaForte123";
        post("/finControl/user", Map.of("email", email, "password", pass), null);
        return (String) readMap(post("/finControl/user/login",
                Map.of("email", email, "password", pass), null).body()).get("token");
    }

    private String url(String path) { return "http://localhost:" + port + path; }

    private HttpResponse<String> get(String path, String token) {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url(path))).GET();
        if (token != null) b.header("Authorization", "Bearer " + token);
        return send(b);
    }

    private HttpResponse<String> post(String path, Object body, String token) {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url(path)))
                .header("Content-Type", "application/json");
        if (token != null) b.header("Authorization", "Bearer " + token);
        b.POST(HttpRequest.BodyPublishers.ofString(toJson(body)));
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

    private HttpResponse<String> delete(String path, String token) {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url(path)))
                .method("DELETE", HttpRequest.BodyPublishers.noBody());
        if (token != null) b.header("Authorization", "Bearer " + token);
        return send(b);
    }

    private HttpResponse<String> send(HttpRequest.Builder b) {
        try { return client.send(b.build(), HttpResponse.BodyHandlers.ofString()); }
        catch (Exception e) { throw new IllegalStateException("HTTP request failed", e); }
    }

    private Map<String, Object> readMap(String json) {
        try { return objectMapper.readValue(json, new TypeReference<>() {}); }
        catch (Exception e) { throw new IllegalStateException("Failed to parse: " + json, e); }
    }

    private List<?> readList(String json) {
        try { return objectMapper.readValue(json, new TypeReference<List<?>>() {}); }
        catch (Exception e) { throw new IllegalStateException("Failed to parse: " + json, e); }
    }

    private String toJson(Object o) {
        try { return objectMapper.writeValueAsString(o); }
        catch (Exception e) { throw new IllegalStateException("Failed to serialize", e); }
    }
}
