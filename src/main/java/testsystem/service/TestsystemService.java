package testsystem.service;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class TestsystemService {
    private String host;
    private String port;

    public TestsystemService(Environment env) {
        this.host = env.getProperty("testsystem.address");
        this.port = env.getProperty("testsystem.port");
    }

    public int sendRequestToTestingServer(String solId) throws IOException {
        URL url = new URL("http://" + host + ":" + port + "/" + solId);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        return con.getResponseCode();
    }
}
