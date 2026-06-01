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

class TransactionFlowIT extends IntegrationTestBase {

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void income_shouldIncreaseBalance() {
        String token = registerAndLogin();
        Long accountId = createAccount(token, "Carteira", 500.00);

        // create income
        Map<String, Object> income = Map.of(
                "accountId", accountId,
                "amount", 200.00,
                "date", "2026-05-01",
                "type", "INCOME",
                "description", "Salário",
                "status", "PAID");
        HttpResponse<String> resp = post("/finControl/transactions", income, token);
        assertThat(resp.statusCode()).isEqualTo(201);

        // balance should be 500 + 200 = 700
        Map<String, Object> account = readMap(get("/finControl/accounts/" + accountId, token).body());
        assertThat(((Number) account.get("balance")).doubleValue()).isEqualTo(700.0);
    }

    @Test
    void expense_shouldDecreaseBalance() {
        String token = registerAndLogin();
        Long accountId = createAccount(token, "Conta Corrente", 1000.00);

        Map<String, Object> expense = Map.of(
                "accountId", accountId,
                "amount", 300.00,
                "date", "2026-05-01",
                "type", "EXPENSE",
                "description", "Aluguel",
                "status", "PAID");
        assertThat(post("/finControl/transactions", expense, token).statusCode()).isEqualTo(201);

        Map<String, Object> account = readMap(get("/finControl/accounts/" + accountId, token).body());
        assertThat(((Number) account.get("balance")).doubleValue()).isEqualTo(700.0);
    }

    @Test
    void transfer_shouldPreserveConsolidatedNetWorth() {
        String token = registerAndLogin();
        Long srcId = createAccount(token, "Conta A", 1000.00);
        Long dstId = createAccount(token, "Conta B", 500.00);

        Map<String, Object> transfer = Map.of(
                "accountId", srcId,
                "destinationAccountId", dstId,
                "amount", 300.00,
                "date", "2026-05-01",
                "type", "TRANSFER",
                "status", "PAID");
        HttpResponse<String> resp = post("/finControl/transactions", transfer, token);
        assertThat(resp.statusCode()).isEqualTo(201);

        double srcBalance = ((Number) readMap(get("/finControl/accounts/" + srcId, token).body()).get("balance")).doubleValue();
        double dstBalance = ((Number) readMap(get("/finControl/accounts/" + dstId, token).body()).get("balance")).doubleValue();

        assertThat(srcBalance).isEqualTo(700.0); // 1000 - 300
        assertThat(dstBalance).isEqualTo(800.0); // 500 + 300
        assertThat(srcBalance + dstBalance).isEqualTo(1500.0); // total preserved
    }

    @Test
    void pending_transaction_shouldNotAffectBalance() {
        String token = registerAndLogin();
        Long accountId = createAccount(token, "Poupança", 1000.00);

        Map<String, Object> pending = Map.of(
                "accountId", accountId,
                "amount", 200.00,
                "date", "2026-05-01",
                "type", "EXPENSE",
                "description", "Conta de luz",
                "status", "PENDING");
        assertThat(post("/finControl/transactions", pending, token).statusCode()).isEqualTo(201);

        // pending should not change balance
        Map<String, Object> account = readMap(get("/finControl/accounts/" + accountId, token).body());
        assertThat(((Number) account.get("balance")).doubleValue()).isEqualTo(1000.0);
    }

    @Test
    void deleteTransaction_shouldRecalculateBalance() {
        String token = registerAndLogin();
        Long accountId = createAccount(token, "Conta", 500.00);

        Map<String, Object> income = Map.of(
                "accountId", accountId, "amount", 100.00,
                "date", "2026-05-01", "type", "INCOME",
                "description", "Bonus", "status", "PAID");
        List<?> created = readList(post("/finControl/transactions", income, token).body());
        Long txId = ((Number) ((Map<?, ?>) created.get(0)).get("id")).longValue();

        // balance should be 600
        assertThat(((Number) readMap(get("/finControl/accounts/" + accountId, token).body()).get("balance")).doubleValue())
                .isEqualTo(600.0);

        // delete transaction
        assertThat(delete("/finControl/transactions/" + txId, token).statusCode()).isEqualTo(204);

        // balance should revert to 500
        assertThat(((Number) readMap(get("/finControl/accounts/" + accountId, token).body()).get("balance")).doubleValue())
                .isEqualTo(500.0);
    }

    @Test
    void updateStatus_fromPending_toPaid_shouldUpdateBalance() {
        String token = registerAndLogin();
        Long accountId = createAccount(token, "Conta", 1000.00);

        Map<String, Object> pending = Map.of(
                "accountId", accountId, "amount", 200.00,
                "date", "2026-05-01", "type", "EXPENSE",
                "description", "Conta", "status", "PENDING");
        List<?> created = readList(post("/finControl/transactions", pending, token).body());
        Long txId = ((Number) ((Map<?, ?>) created.get(0)).get("id")).longValue();

        // balance unchanged (pending)
        assertThat(((Number) readMap(get("/finControl/accounts/" + accountId, token).body()).get("balance")).doubleValue())
                .isEqualTo(1000.0);

        // mark as paid
        patch("/finControl/transactions/" + txId + "/status", Map.of("status", "PAID"), token);

        // now balance should decrease
        assertThat(((Number) readMap(get("/finControl/accounts/" + accountId, token).body()).get("balance")).doubleValue())
                .isEqualTo(800.0);
    }

    @Test
    void categoryTypeMismatch_shouldReturn422() {
        String token = registerAndLogin();
        Long accountId = createAccount(token, "Conta", 1000.00);

        // create INCOME category
        Long catId = ((Number) readMap(post("/finControl/categories",
                Map.of("name", "Salário", "type", "INCOME"), token).body()).get("id")).longValue();

        // try to use INCOME category for EXPENSE transaction
        Map<String, Object> expense = Map.of(
                "accountId", accountId, "amount", 100.00,
                "date", "2026-05-01", "type", "EXPENSE",
                "categoryId", catId, "status", "PAID");
        HttpResponse<String> resp = post("/finControl/transactions", expense, token);
        assertThat(resp.statusCode()).isEqualTo(422);
    }

    @Test
    void listTransactions_shouldSupportFiltersAndPagination() {
        String token = registerAndLogin();
        Long accountId = createAccount(token, "Conta", 2000.00);

        // create 3 transactions
        for (int i = 1; i <= 3; i++) {
            post("/finControl/transactions", Map.of(
                    "accountId", accountId, "amount", (double) (i * 100),
                    "date", "2026-05-0" + i, "type", "EXPENSE",
                    "description", "Gasto " + i, "status", "PAID"), token);
        }

        // list all
        Map<String, Object> page = readMap(get("/finControl/transactions", token).body());
        assertThat(((Number) page.get("totalElements")).intValue()).isEqualTo(3);

        // filter by type
        Map<String, Object> incomeOnly = readMap(get("/finControl/transactions?type=INCOME", token).body());
        assertThat(((Number) incomeOnly.get("totalElements")).intValue()).isEqualTo(0);

        // pagination
        Map<String, Object> firstPage = readMap(get("/finControl/transactions?page=0&size=2", token).body());
        assertThat(((List<?>) firstPage.get("content"))).hasSize(2);
    }

    @Test
    void transaction_ownership_shouldReturn404_forOtherUsersTx() {
        String tokenA = registerAndLogin();
        String tokenB = registerAndLogin();
        Long accountId = createAccount(tokenA, "Conta A", 1000.00);
        List<?> created = readList(post("/finControl/transactions", Map.of(
                "accountId", accountId, "amount", 100.00,
                "date", "2026-05-01", "type", "INCOME",
                "status", "PAID"), tokenA).body());
        Long txId = ((Number) ((Map<?, ?>) created.get(0)).get("id")).longValue();
        assertThat(get("/finControl/transactions/" + txId, tokenB).statusCode()).isEqualTo(404);
    }

    // --- helpers ---

    private String registerAndLogin() {
        String email = "tx_" + UUID.randomUUID() + "@test.com";
        String pass = "SenhaForte123";
        post("/finControl/user", Map.of("email", email, "password", pass), null);
        return (String) readMap(post("/finControl/user/login", Map.of("email", email, "password", pass), null).body()).get("token");
    }

    private Long createAccount(String token, String name, double initialBalance) {
        HttpResponse<String> resp = post("/finControl/accounts",
                Map.of("name", name, "type", "CHECKING", "initialBalance", initialBalance, "currency", "BRL"), token);
        return ((Number) readMap(resp.body()).get("id")).longValue();
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
