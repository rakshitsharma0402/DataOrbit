package io.github.rakshitsharma0402.dataorbit.app.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

import io.github.rakshitsharma0402.dataorbit.app.DataOrbitApplication;

/**
 * The API accepts browser requests only from dataorbit.frontend-origin (application.yml default
 * http://127.0.0.1:5173). Real HTTP on a random port, so Tomcat and Spring's CORS handling both run.
 */
@SpringBootTest(classes = DataOrbitApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
class CorsTest {

    private static final String FRONTEND = "http://127.0.0.1:5173";

    private final HttpClient http = HttpClient.newHttpClient();

    @LocalServerPort
    private int port;

    @Test
    void allowsTheConfiguredFrontendOrigin() throws Exception {
        HttpResponse<String> response = send(get("/api/v1/version").header("Origin", FRONTEND));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Access-Control-Allow-Origin")).hasValue(FRONTEND);
        assertThat(response.headers().firstValue("Access-Control-Allow-Credentials")).isEmpty();
    }

    @Test
    void rejectsAnyOtherBrowserOrigin() throws Exception {
        for (String origin : new String[] {"http://evil.example.com", "http://localhost:5173", "http://127.0.0.1:5174"}) {
            HttpResponse<String> response = send(get("/api/v1/version").header("Origin", origin));

            assertThat(response.statusCode()).as("origin %s", origin).isEqualTo(403);
            assertThat(response.headers().firstValue("Access-Control-Allow-Origin")).as("origin %s", origin).isEmpty();
        }
    }

    @Test
    void preflightAllowsOnlyTheContractMethods() throws Exception {
        HttpResponse<String> put = send(preflight("PUT", FRONTEND));
        assertThat(put.statusCode()).isEqualTo(200);
        assertThat(put.headers().firstValue("Access-Control-Allow-Methods")).hasValue("GET,POST,PUT");

        assertThat(send(preflight("DELETE", FRONTEND)).statusCode()).isEqualTo(403);
        assertThat(send(preflight("PUT", "http://evil.example.com")).statusCode()).isEqualTo(403);
    }

    @Test
    void requestsWithoutAnOriginAreUnaffected() throws Exception {
        assertThat(send(get("/api/v1/version")).statusCode()).isEqualTo(200);
    }

    private HttpRequest.Builder get(String path) {
        return HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).GET();
    }

    private HttpRequest.Builder preflight(String method, String origin) {
        return HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/v1/version"))
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .header("Origin", origin)
                .header("Access-Control-Request-Method", method);
    }

    private HttpResponse<String> send(HttpRequest.Builder request) throws IOException, InterruptedException {
        return http.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }
}
