package com.donato.fin_control_backend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FinancialModulesIT extends IntegrationTestBase {

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    // ---- REPORTS ----

    @Test
    void reports_monthly_shouldReturnData() {
        String token = registerAndLogin();
        Long accId = createAccount(token, 1000.0);
        createTransaction(token, accId, "INCOME", 500.0, "PAID", LocalDate.now().toString());
        createTransaction(token, accId, "EXPENSE", 200.0, "PAID", LocalDate.now().toString());

        HttpResponse<String> resp = get("/finControl/reports/monthly", token);
        assertThat(resp.statusCode()).isEqualTo(200);
        List<?> entries = readList(resp.body());
        assertThat(entries).isNotEmpty();
    }

    @Test
    void reports_byCategory_shouldReturnGroupedData() {
        String token = registerAndLogin();
        Long accId = createAccount(token, 2000.0);
        Long catId = createCategory(token, "Alimentação", "EXPENSE");
        createTransactionWithCategory(token, accId, catId, "EXPENSE", 300.0, "PAID");

        HttpResponse<String> resp = get("/finControl/reports/by-category", token);
        assertThat(resp.statusCode()).isEqualTo(200);
        List<?> entries = readList(resp.body());
        assertThat(entries).isNotEmpty();
    }

    @Test
    void reports_export_shouldReturnCsv() {
        String token = registerAndLogin();
        Long accId = createAccount(token, 1000.0);
        createTransaction(token, accId, "EXPENSE", 100.0, "PAID", LocalDate.now().toString());

        HttpResponse<String> resp = get("/finControl/reports/export", token);
        assertThat(resp.statusCode()).isEqualTo(200);
        assertThat(resp.headers().firstValue("Content-Type").orElse(""))
                .contains("text/csv");
        assertThat(resp.body()).contains("id,date,type");
    }

    // ---- BUDGETS ----

    @Test
    void budget_create_and_list_withSpent() {
        String token = registerAndLogin();
        Long accId = createAccount(token, 2000.0);
        Long catId = createCategory(token, "Lazer", "EXPENSE");
        createTransactionWithCategory(token, accId, catId, "EXPENSE", 150.0, "PAID");

        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();

        HttpResponse<String> createResp = post("/finControl/budgets",
                Map.of("categoryId", catId, "month", month, "year", year, "amount", 200.0), token);
        assertThat(createResp.statusCode()).isEqualTo(201);
        Map<String, Object> budget = readMap(createResp.body());
        assertThat(((Number) budget.get("amount")).doubleValue()).isEqualTo(200.0);
        assertThat(((Number) budget.get("spent")).doubleValue()).isEqualTo(150.0);
        assertThat(budget.get("exceeded")).isEqualTo(false);

        HttpResponse<String> listResp = get("/finControl/budgets", token);
        assertThat(readList(listResp.body())).hasSize(1);
    }

    @Test
    void budget_exceeded_flagsCorrectly() {
        String token = registerAndLogin();
        Long accId = createAccount(token, 2000.0);
        Long catId = createCategory(token, "Saúde", "EXPENSE");
        createTransactionWithCategory(token, accId, catId, "EXPENSE", 500.0, "PAID");

        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();
        HttpResponse<String> resp = post("/finControl/budgets",
                Map.of("categoryId", catId, "month", month, "year", year, "amount", 300.0), token);
        Map<String, Object> budget = readMap(resp.body());
        assertThat(budget.get("exceeded")).isEqualTo(true);
    }

    // ---- GOALS ----

    @Test
    void goal_lifecycle_create_contribute_deactivate() {
        String token = registerAndLogin();

        HttpResponse<String> createResp = post("/finControl/goals",
                Map.of("name", "Viagem", "targetAmount", 5000.0), token);
        assertThat(createResp.statusCode()).isEqualTo(201);
        Map<String, Object> goal = readMap(createResp.body());
        Long goalId = ((Number) goal.get("id")).longValue();
        assertThat(((Number) goal.get("currentAmount")).doubleValue()).isEqualTo(0.0);

        // contribute
        HttpResponse<String> contResp = patch("/finControl/goals/" + goalId + "/contribute",
                Map.of("amount", 1000.0), token);
        assertThat(contResp.statusCode()).isEqualTo(200);
        Map<String, Object> updated = readMap(contResp.body());
        assertThat(((Number) updated.get("currentAmount")).doubleValue()).isEqualTo(1000.0);
        assertThat(((Number) updated.get("progressPercent")).doubleValue()).isEqualTo(20.0);

        // deactivate
        HttpResponse<String> deactResp = patch("/finControl/goals/" + goalId + "/deactivate", null, token);
        assertThat(deactResp.statusCode()).isEqualTo(200);
        assertThat(readMap(deactResp.body()).get("active")).isEqualTo(false);
    }

    // ---- INVESTMENTS ----

    @Test
    void investment_lifecycle_create_movements_balance() {
        String token = registerAndLogin();

        HttpResponse<String> createResp = post("/finControl/investments",
                Map.of("name", "Tesouro Selic", "type", "TESOURO"), token);
        assertThat(createResp.statusCode()).isEqualTo(201);
        Long invId = ((Number) readMap(createResp.body()).get("id")).longValue();

        // add contribution
        HttpResponse<String> contResp = post("/finControl/investments/" + invId + "/movements",
                Map.of("movementType", "CONTRIBUTION", "amount", 1000.0, "date", LocalDate.now().toString()),
                token);
        assertThat(contResp.statusCode()).isEqualTo(201);

        // add yield
        post("/finControl/investments/" + invId + "/movements",
                Map.of("movementType", "YIELD", "amount", 50.0, "date", LocalDate.now().toString()), token);

        // check balance
        Map<String, Object> inv = readMap(get("/finControl/investments/" + invId, token).body());
        assertThat(((Number) inv.get("balance")).doubleValue()).isEqualTo(1050.0);
        assertThat(((List<?>) inv.get("movements"))).hasSize(2);
    }

    @Test
    void investment_withdrawal_reducesBalance() {
        String token = registerAndLogin();
        HttpResponse<String> createResp = post("/finControl/investments",
                Map.of("name", "CDB Banco", "type", "CDB"), token);
        Long invId = ((Number) readMap(createResp.body()).get("id")).longValue();

        post("/finControl/investments/" + invId + "/movements",
                Map.of("movementType", "CONTRIBUTION", "amount", 5000.0, "date", LocalDate.now().toString()), token);
        post("/finControl/investments/" + invId + "/movements",
                Map.of("movementType", "WITHDRAWAL", "amount", 2000.0, "date", LocalDate.now().toString()), token);

        Map<String, Object> inv = readMap(get("/finControl/investments/" + invId, token).body());
        assertThat(((Number) inv.get("balance")).doubleValue()).isEqualTo(3000.0);
    }

    // ---- RECURRENCE ----

    @Test
    void recurrence_create_pause_resume() {
        String token = registerAndLogin();
        Long accId = createAccount(token, 5000.0);

        HttpResponse<String> createResp = post("/finControl/recurrences", Map.of(
                "accountId", accId, "type", "EXPENSE", "amount", 500.0,
                "description", "Aluguel", "frequency", "MONTHLY",
                "startDate", LocalDate.now().toString()), token);
        assertThat(createResp.statusCode()).isEqualTo(201);
        Long ruleId = ((Number) readMap(createResp.body()).get("id")).longValue();
        assertThat(readMap(createResp.body()).get("active")).isEqualTo(true);

        // pause
        HttpResponse<String> pauseResp = patch("/finControl/recurrences/" + ruleId + "/pause", null, token);
        assertThat(readMap(pauseResp.body()).get("active")).isEqualTo(false);

        // resume
        HttpResponse<String> resumeResp = patch("/finControl/recurrences/" + ruleId + "/resume", null, token);
        assertThat(readMap(resumeResp.body()).get("active")).isEqualTo(true);

        // list active only
        List<?> active = readList(get("/finControl/recurrences", token).body());
        assertThat(active).hasSize(1);
    }

    @Test
    void recurrence_generate_createsPendingTransactions() {
        String token = registerAndLogin();
        Long accId = createAccount(token, 10000.0);

        LocalDate start = LocalDate.now().withDayOfMonth(1);
        HttpResponse<String> createResp = post("/finControl/recurrences", Map.of(
                "accountId", accId, "type", "INCOME", "amount", 3000.0,
                "description", "Salário", "frequency", "MONTHLY",
                "startDate", start.toString()), token);
        Long ruleId = ((Number) readMap(createResp.body()).get("id")).longValue();

        // generate 3 months
        LocalDate upTo = start.plusMonths(2);
        HttpResponse<String> genResp = post("/finControl/recurrences/" + ruleId + "/generate",
                Map.of("upTo", upTo.toString()), token);
        assertThat(genResp.statusCode()).isEqualTo(200);
        assertThat(((Number) readMap(genResp.body()).get("generated")).intValue()).isEqualTo(3);

        // verify transactions created
        Map<String, Object> page = readMap(get("/finControl/transactions?type=INCOME", token).body());
        assertThat(((Number) page.get("totalElements")).intValue()).isGreaterThanOrEqualTo(3);
    }

    // --- helpers ---

    private String registerAndLogin() {
        String email = "mod_" + UUID.randomUUID() + "@test.com";
        String pass = "SenhaForte123";
        post("/finControl/user", Map.of("email", email, "password", pass), null);
        return (String) readMap(post("/finControl/user/login",
                Map.of("email", email, "password", pass), null).body()).get("token");
    }

    private Long createAccount(String token, double balance) {
        return ((Number) readMap(post("/finControl/accounts",
                Map.of("name", "Conta " + UUID.randomUUID().toString().substring(0, 8),
                        "type", "CHECKING", "initialBalance", balance, "currency", "BRL"), token)
                .body()).get("id")).longValue();
    }

    private Long createCategory(String token, String name, String type) {
        return ((Number) readMap(post("/finControl/categories",
                Map.of("name", name, "type", type), token).body()).get("id")).longValue();
    }

    private void createTransaction(String token, Long accId, String type, double amount,
                                   String status, String date) {
        post("/finControl/transactions", Map.of(
                "accountId", accId, "amount", amount,
                "date", date, "type", type, "status", status), token);
    }

    private void createTransactionWithCategory(String token, Long accId, Long catId,
                                               String type, double amount, String status) {
        post("/finControl/transactions", Map.of(
                "accountId", accId, "categoryId", catId, "amount", amount,
                "date", LocalDate.now().toString(), "type", type, "status", status), token);
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
