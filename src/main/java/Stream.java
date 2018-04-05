import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;

@Data
public class Stream {
    private List<Document> documents = new ArrayList<>();

    public Stream document(Document document) {
        this.documents.add(document);
        return this;
    }

    @Override public String toString() { return documents.stream().map(Document::toString).collect(joining("\n---\n")); }
}
