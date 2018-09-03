import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
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

    private ParseException whenParseSingleThrows() { return whenThrows(this::whenParseSingle); }

    private ParseException whenParseFirstThrows() { return whenThrows(this::whenParseFirst); }

    private ParseException whenThrows(ThrowingCallable callable) { return catchThrowableOfType(callable, ParseException.class); }


    ///////////////////////////////////////////////////////////////////////// THEN

    private static final Document EMPTY_DOCUMENT = new Document().content(" ");
    private static final Document COMMENT_ONLY = new Document().comment(new Comment().text("test comment"));
    private static final Document COMMENT_ONLY_2 = new Document().comment(new Comment().text("test comment 2"));


    private void thenIsEmpty(Stream stream) { assertThat(stream.documents()).isEmpty(); }

    private void thenExpectedAtLeastOne(ParseException thrown) {
        assertThat(thrown).hasMessage("expected at least one document, but found none");
    }

    private void thenExpectedExactlyOneButFoundNone(ParseException thrown) {
        assertThat(thrown).hasMessage("expected exactly one document, but found 0");
    }

    private void thenExpectedExactlyOneButFoundTwo(ParseException thrown) {
        assertThat(thrown).hasMessage("expected exactly one document, but found 2");
    }

    private void thenHasOneEmptyDocument(Stream stream) {
        assertThat(stream.documents()).containsExactly(EMPTY_DOCUMENT);
    }

    private void thenIsEmptyDocument(Document document) {
        assertThat(document).isEqualTo(EMPTY_DOCUMENT);
    }

    private void thenHasOneCommentOnlyDocument(Stream stream) {
        assertThat(stream.documents()).containsExactly(COMMENT_ONLY);
    }

    private void thenIsCommentOnlyDocument(Document document) {
        assertThat(document).isEqualTo(COMMENT_ONLY);
    }

    private void thenHasTwoCommentOnlyDocuments(Stream stream) {
        assertThat(stream.documents()).containsExactly(COMMENT_ONLY, COMMENT_ONLY_2);
    }


    ///////////////////////////////////////////////////////////////////////// TESTS

    @Test void shouldParseAllInEmptyDocument() {
        givenEmptyDocument();

        Stream stream = whenParseAll();

        thenIsEmpty(stream);
    }

    @Test void shouldParseFirstInEmptyDocument() {
        givenEmptyDocument();

        ParseException thrown = whenParseFirstThrows();

        thenExpectedAtLeastOne(thrown);
    }

    @Test void shouldParseSingleInEmptyDocument() {
        givenEmptyDocument();

        ParseException thrown = whenParseSingleThrows();

        thenExpectedExactlyOneButFoundNone(thrown);
    }


    @Test void shouldParseAllInSpaceOnlyDocument() {
        givenSpaceOnlyDocument();

        Stream stream = whenParseAll();

        thenHasOneEmptyDocument(stream);
    }

    @Test void shouldParseFirstInSpaceOnlyDocument() {
        givenSpaceOnlyDocument();

        Document document = whenParseFirst();

        thenIsEmptyDocument(document);
    }

    @Test void shouldParseSingleInSpaceOnlyDocument() {
        givenSpaceOnlyDocument();

        Document document = whenParseSingle();

        thenIsEmptyDocument(document);
    }


    @Test void shouldParseAllInDocumentOnlyDocument() {
        givenOneCommentOnlyDocument();

        Stream stream = whenParseAll();

        thenHasOneCommentOnlyDocument(stream);
    }

    @Test void shouldParseFirstInDocumentOnlyDocument() {
        givenOneCommentOnlyDocument();

        Document document = whenParseFirst();

        thenIsCommentOnlyDocument(document);
    }

    @Test void shouldParseSingleInDocumentOnlyDocument() {
        givenOneCommentOnlyDocument();

        Document document = whenParseSingle();

        thenIsCommentOnlyDocument(document);
    }


    @Test void shouldParseAllInTwoDocuments() {
        givenTwoCommentOnlyDocuments();

        Stream stream = whenParseAll();

        thenHasTwoCommentOnlyDocuments(stream);
    }

    @Test void shouldParseFirstInTwoDocuments() {
        givenTwoCommentOnlyDocuments();

        Document document = whenParseFirst();

        thenIsCommentOnlyDocument(document);
    }

    @Test void shouldFailToParseSingleInTwoDocuments() {
        givenTwoCommentOnlyDocuments();

        ParseException thrown = whenParseSingleThrows();

        thenExpectedExactlyOneButFoundTwo(thrown);
    }
}
