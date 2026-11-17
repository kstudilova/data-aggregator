package source;

import model.AggregatedRecord;
import java.util.List;

public interface DataSource {
    String getName();

    List< AggregatedRecord > fetch();
}
