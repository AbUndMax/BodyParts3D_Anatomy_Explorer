package explorer.model;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.List;

public class AiApiService extends Service<List<String>> {

    @Override
    public Task<List<String>> createTask() {
        return new Task<List<String>>() {

            @Override
            protected List<String> call() throws Exception {
                Thread.sleep(6000);
                return List.of("12", "13", "14");
            }
        };
    }
}
