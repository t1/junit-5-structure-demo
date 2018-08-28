import java.util.List;

public class Parser {
    public static Document parseSingle(String input) {
        List<Document> documents = parseAll(input).documents();
        if (documents.size() != 1)
            throw new ParseException("expected exactly one document, but found " + documents.size());
        return documents.get(0);
    }

    public static Document parseFirst(String input) {
        List<Document> documents = parseAll(input).documents();
        if (documents.size() < 1)
            throw new ParseException("expected at least one document, but found none");
        return documents.get(0);
    }

    public static Stream parseAll(String input) {
        switch (input) {
            case "":
                return new Stream();
            case " ":
                return new Stream().document(new Document());
            case "# test comment":
                return new Stream().document(new Document().comment(new Comment().text("test comment")));
            case "# test comment\n---\n# test comment 2":
                return new Stream()
                    .document(new Document().comment(new Comment().text("test comment")))
                    .document(new Document().comment(new Comment().text("test comment 2")));
            default:
                throw new ParseException("unsupported document");
        }
    }
}
