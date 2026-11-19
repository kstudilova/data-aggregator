package service;

import model.AggregatedRecord;
import source.DataSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataAggregator {
    public List< AggregatedRecord > collect(List<DataSource> sources) throws IOException, InterruptedException {
        List< AggregatedRecord > records = new ArrayList< >();

        for (DataSource source : sources) {
            records.addAll(source.fetch());
        }

        return records;
    }

}
