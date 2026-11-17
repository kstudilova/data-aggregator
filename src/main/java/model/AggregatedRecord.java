package model;

import java.time.Instant;
import java.util.Map;

public class AggregatedRecord {
    private final String source_;
    private final String category_;
    private final String title_;
    private final Instant fetchedAt_;
    private final Map<String, String> attributes_;

    public AggregatedRecord(String source, String category, String title, Instant time, Map< String, String > attributes) {
        source_ = source;
        category_ = category;
        title_ = title;
        fetchedAt_ = time;
        attributes_ = Map.copyOf(attributes);
    }

    public String getSource() {
        return source_;
    }

    public String getCategory() {
        return category_;
    }

    public String getTitle() {
        return title_;
    }

    public Instant getFetchedAt() {
        return fetchedAt_;
    }

    public Map< String, String > getAttributes() {
        return attributes_;
    }
}
