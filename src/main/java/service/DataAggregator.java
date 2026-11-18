package service;

import model.AggregatedRecord;
import source.DataSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataAggregator {
    private final List< DataSource > sources_;

    public DataAggregator(List< DataSource > sources) {
        sources_ = List.copyOf(sources);
    }

    public List< AggregatedRecord > collect() throws IOException, InterruptedException {
        List< AggregatedRecord > records = new ArrayList< >();

        for (DataSource source : sources_) {
            List< AggregatedRecord > sourceRecords = source.fetch();
            records.addAll(sourceRecords);
        }

        return records;
    }

}
