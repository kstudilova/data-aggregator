package source;

import model.AggregatedRecord;

import java.io.IOException;
import java.util.List;

public interface DataSource {
    String getName();

    List< AggregatedRecord > fetch() throws IOException, InterruptedException;
}
