package cli;

import java.nio.file.Path;
import java.util.List;

public class CliOptions {
    private final String command_;
    private final List<String> sources_;
    private final String format_;
    private final boolean append_;
    private final Path path_;
    private final String sourceFilter_;

    public CliOptions(String command, List<String> sources, String format, boolean append, Path path, String sourceFilter) {
        command_ = command;
        sources_ = List.copyOf(sources);
        format_ = format;
        append_ = append;
        path_ = path;
        sourceFilter_ = sourceFilter;
    }

    public String getCommand() {
        return command_;
    }

    public List<String> getSources() {
        return sources_;
    }

    public String getFormat() {
        return format_;
    }

    public boolean isAppend() {
        return append_;
    }

    public Path getPath() {
        return path_;
    }

    public String getSourceFilter() {
        return sourceFilter_;
    }
}
