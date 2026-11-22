package io.github.rakshitsharma0402.dataorbit.app.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

import io.github.rakshitsharma0402.dataorbit.app.DataOrbitApplication;

/** Starts the real application on a random port, bound as configured in application.yml. */
@SpringBootTest(classes = DataOrbitApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
class VersionEndpointTest {

    private final HttpClient http = HttpClient.newHttpClient();

    @LocalServerPort
    private int port;

    @Autowired
    private BuildProperties buildProperties;

    @Test
    void versionEndpointReturnsNameAndBuildVersion() throws Exception {
        HttpResponse<String> response = get("http://127.0.0.1:" + port + "/api/v1/version");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Content-Type")).hasValueSatisfying(
                type -> assertThat(type).startsWith("application/json"));
        assertThat(response.body()).isEqualTo(
                "{\"name\":\"DataOrbit\",\"version\":\"" + buildProperties.getVersion() + "\"}");
    }

    @Test
    void versionMatchesTheMavenProjectVersion() {
        assertThat(buildProperties.getVersion()).isNotBlank().doesNotContain("${");
    }

    @Test
    void serverIsNotReachableOnNonLoopbackAddresses() throws IOException {
        List<InetAddress> nonLoopback = NetworkInterface.networkInterfaces()
                .flatMap(NetworkInterface::inetAddresses)
                .filter(address -> !address.isLoopbackAddress() && !address.isLinkLocalAddress())
                .toList();
        assumeFalse(nonLoopback.isEmpty(), "this machine has no non-loopback address to test against");

        for (InetAddress address : nonLoopback) {
            assertThatThrownBy(() -> {
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(address, port), 1_000);
                }
            }).as("connection to %s:%d should be refused", address.getHostAddress(), port)
                    .isInstanceOf(IOException.class);
        }
    }

    private HttpResponse<String> get(String url) throws IOException, InterruptedException {
        return http.send(HttpRequest.newBuilder(URI.create(url)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }
}
