import lombok.Data;

@Data
public class Document {
    private String content;
    private Comment comment;

    public Document content(String content) {
        this.content = content;
        return this;
    }

    @Override public String toString() {
        StringBuilder out = new StringBuilder();
        if (comment != null)
            out.append(comment.toString());
        if (content != null)
            out.append(content);
        return out.toString();
    }
}
