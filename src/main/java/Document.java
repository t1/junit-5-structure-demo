import lombok.Data;

@Data
public class Document {
    private Comment comment;


    @Override public String toString() {
        StringBuilder out = new StringBuilder();
        if (comment != null)
            out.append(comment.toString());
        return out.toString();
    }
}
