package explorer.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AiApiService extends Service<String> {

    private String query = null;
    private String selectedTree = null;

    private String templateJson = null;
    private Map<String, List<String>> conceptTerms = null;

    /**
     * API key used for authenticating requests to the OpenAI API.
     * Retrieved from the environment variable 'OPENAI_API_KEY'.
     */
    public static final String KEY = System.getenv("OPENAI_API_KEY");

    /**
     * Sets the user-defined query and the selected ontology tree to be used in the API request.
     *
     * @param query The natural language query to interpret.
     * @param selectedTree The ontology subtree to use as term input.
     */
    public void setQuery(String query, String selectedTree) {
        this.query = query;
        this.selectedTree = selectedTree;
    }

    /**
     * Creates a background task that builds and sends a request to the OpenAI API using a predefined template and
     * a list of ontology terms, then parses the response to extract the generated regular expression.
     *
     * @return Task which returns a regex string response from the AI model.
     */
    @Override
    public Task<String> createTask() {
        return new Task<String>() {

            @Override
            protected String call() throws Exception {

                String content = null;
                try {
                    ObjectMapper mapper = new ObjectMapper();

                    // Prepare the request String:

                    // load a template from the resources
                    String filledTemplate;
                    if (templateJson == null ) {
                        InputStream requestStream = Objects.requireNonNull(
                                getClass().getResourceAsStream("/requests/gptApiRequest.json"));
                        templateJson = new String(requestStream.readAllBytes(), StandardCharsets.UTF_8);
                    }

                    filledTemplate = templateJson;

                    if (conceptTerms == null) {
                        conceptTerms = KryoUtils.thawStringMapFromKryo("/requests/conceptTerms.kryo");
                    }

                    // replace the fields in the  template values with the actual terms and user query
                    filledTemplate = filledTemplate
                            .replace("{{terms}}", "[ " + String.join(", ", conceptTerms.get(selectedTree)) + " ]")
                            .replace("{{query}}", query);

                    // instantiate the HTTP request
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("http://134.2.9.180/v1/chat/completions"))
                            .header("Authorization", "Bearer " + KEY)
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(filledTemplate))
                            .build();

                    // send Request
                    HttpClient client = HttpClient.newHttpClient();
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() != 200) {
                        // TODO: need better logging
                        throw new IOException("INVALID API RESPONSE: " + response.body());
                    }

                    // parse the output
                    Map<String, Object> jsonResponse = mapper.readValue(response.body(), Map.class);
                    @SuppressWarnings("unchecked") // Safe due to expected structure of the OpenAI API JSON response.
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) jsonResponse.get("choices");
                    @SuppressWarnings("unchecked") // Safe due to expected structure of the OpenAI API JSON response.
                    Map<String, Object> message = (Map<String, Object>) choices.getFirst().get("message");
                    content = (String) message.get("content");
                } catch (Exception e) {
                    // TODO: better logging
                    e.printStackTrace();
                }

                // return the regular expression
                if (content == null) {
                    throw new IOException("No valid AI response received.");
                }
                return content.trim();
            }
        };
    }
}
