import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class _0_ClassicTest {


    private String input;


    ///////////////////////////////////////////////////////////////////////// GIVEN

    private void givenEmptyDocument() { input = ""; }

    private void givenSpaceOnlyDocument() { input = " "; }

    private void givenOneCommentOnlyDocument() { input = "# test comment"; }

    private void givenTwoCommentOnlyDocuments() { input = "# test comment\n---\n# test comment 2"; }


    ///////////////////////////////////////////////////////////////////////// WHEN

    private Stream whenParseAll() { return Parser.parseAll(input); }

    private Document whenParseFirst() { return Parser.parseFirst(input); }

    private Document whenParseSingle() { return Parser.parseSingle(input); }

    private ParseException whenParseSingleThrows() { return catchThrowableOfType(this::whenParseSingle, ParseException.class); }

    private ParseException whenParseFirstThrows() { return catchThrowableOfType(this::whenParseFirst, ParseException.class); }


    ///////////////////////////////////////////////////////////////////////// THEN

    private static final Document EMPTY_DOCUMENT = new Document().content(" ");
    private static final Document COMMENT_ONLY = new Document().comment(new Comment().text("test comment"));
    private static final Document COMMENT_ONLY_2 = new Document().comment(new Comment().text("test comment 2"));

    ///////////////////////////////////////////////////////////////////////// TESTS

    @Test void shouldParseAllInEmptyDocument() {
        givenEmptyDocument();

        Stream stream = whenParseAll();

        assertThat(stream.documents()).isEmpty();
    }

    @Test void shouldParseFirstInEmptyDocument() {
        givenEmptyDocument();

        ParseException thrown = whenParseFirstThrows();

        assertThat(thrown).hasMessage("expected at least one document, but found none");
    }

    @Test void shouldParseSingleInEmptyDocument() {
        givenEmptyDocument();

        ParseException thrown = whenParseSingleThrows();

        assertThat(thrown).hasMessage("expected exactly one document, but found 0");
    }


    @Test void shouldParseAllInSpaceOnlyDocument() {
        givenSpaceOnlyDocument();

        Stream stream = whenParseAll();

        assertThat(stream.documents()).containsExactly(EMPTY_DOCUMENT);
    }

    @Test void shouldParseFirstInSpaceOnlyDocument() {
        givenSpaceOnlyDocument();

        Document document = whenParseFirst();

        assertThat(document).isEqualTo(EMPTY_DOCUMENT);
    }

    @Test void shouldParseSingleInSpaceOnlyDocument() {
        givenSpaceOnlyDocument();

        Document document = whenParseSingle();

        assertThat(document).isEqualTo(EMPTY_DOCUMENT);
    }


    @Test void shouldParseAllInDocumentOnlyDocument() {
        givenOneCommentOnlyDocument();

        Stream stream = whenParseAll();

        assertThat(stream.documents()).containsExactly(COMMENT_ONLY);
    }

    @Test void shouldParseFirstInDocumentOnlyDocument() {
        givenOneCommentOnlyDocument();

        Document document = whenParseFirst();

        assertThat(document).isEqualTo(COMMENT_ONLY);
    }

    @Test void shouldParseSingleInDocumentOnlyDocument() {
        givenOneCommentOnlyDocument();

        Document document = whenParseSingle();

        assertThat(document).isEqualTo(COMMENT_ONLY);
    }


    @Test void shouldParseAllInTwoDocuments() {
        givenTwoCommentOnlyDocuments();

        Stream stream = whenParseAll();

        assertThat(stream.documents()).containsExactly(COMMENT_ONLY, COMMENT_ONLY_2);
    }

    @Test void shouldParseFirstInTwoDocuments() {
        givenTwoCommentOnlyDocuments();

        Document document = whenParseFirst();

        assertThat(document).isEqualTo(COMMENT_ONLY);
    }

    @Test void shouldFailToParseSingleInTwoDocuments() {
        givenTwoCommentOnlyDocuments();

        ParseException thrown = whenParseSingleThrows();

        assertThat(thrown).hasMessage("expected exactly one document, but found 2");
    }
}
