import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Stream {
    private List<Document> documents = new ArrayList<>();

    public Stream document(Document document) {
        this.documents.add(document);
        return this;
    }
}
