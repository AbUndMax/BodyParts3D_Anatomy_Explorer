package explorer.model;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class AiApiService extends Service<String> {

    @Override
    public Task<String> createTask() {
        return new Task<String>() {

            // TODO: implement AI API call
            // advice the api to generate a full Regex expression that matches all nodes
            // that fits the user description query

            @Override
            protected String call() throws Exception {
                Thread.sleep(1000);

                // return the regular expression
                return "REGEX STRING";
            }
        };
    }
}
