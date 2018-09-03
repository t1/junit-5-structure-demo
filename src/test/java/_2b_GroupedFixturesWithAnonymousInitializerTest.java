import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class _2b_GroupedFixturesWithAnonymousInitializerTest {


    private String input;


    ///////////////////////////////////////////////////////////////////////// GIVEN

    @Nested class givenEmptyDocument {
        { input = ""; }

        @Test void shouldParseAll() {
            Stream stream = whenParseAll();

            thenIsEmpty(stream);
        }

        @Test void shouldParseFirst() {
            ParseException thrown = whenParseFirstThrows();

            thenExpectedAtLeastOne(thrown);
        }

        @Test void shouldParseSingle() {
            ParseException thrown = whenParseSingleThrows();

            thenExpectedExactlyOne(thrown);
        }
    }


    @Nested class givenSpaceOnlyDocument {
        { input = " "; }

        @Test void shouldParseAll() {
            Stream stream = whenParseAll();

            thenHasOneEmptyDocument(stream);
        }

        @Test void shouldParseFirst() {
            Document document = whenParseFirst();

            thenIsEmptyDocument(document);
        }

        @Test void shouldParseSingle() {
            Document document = whenParseSingle();

            thenIsEmptyDocument(document);
        }
    }


    @Nested class givenCommentOnlyDocument {
        { input = "# test comment"; }

        @Test void shouldParseAll() {
            Stream stream = whenParseAll();

            thenHasOneCommentOnlyDocument(stream);
        }

        @Test void shouldParseFirst() {
            Document document = whenParseFirst();

            thenIsCommentOnlyDocument(document);
        }

        @Test void shouldParseSingle() {
            Document document = whenParseSingle();

            thenIsCommentOnlyDocument(document);
        }
    }


    ///////////////////////////////////////////////////////////////////////// WHEN

    private Stream whenParseAll() { return Parser.parseAll(input); }

    private Document whenParseFirst() { return Parser.parseFirst(input); }

    private Document whenParseSingle() { return Parser.parseSingle(input); }

    private ParseException whenParseSingleThrows() { return whenThrows(this::whenParseSingle); }

    private ParseException whenParseFirstThrows() { return whenThrows(this::whenParseFirst); }

    private ParseException whenThrows(ThrowingCallable callable) { return catchThrowableOfType(callable, ParseException.class); }


    ///////////////////////////////////////////////////////////////////////// THEN

    private static final Document EMPTY_DOCUMENT = new Document();
    private static final Document COMMENT_ONLY = new Document().comment(new Comment().text("test comment"));


    private void thenIsEmpty(Stream stream) { assertThat(stream.documents()).isEmpty(); }

    private void thenExpectedAtLeastOne(ParseException thrown) {
        assertThat(thrown).hasMessage("expected at least one document, but found none");
    }

    private void thenExpectedExactlyOne(ParseException thrown) {
        assertThat(thrown).hasMessage("expected exactly one document, but found 0");
    }

    private void thenHasOneEmptyDocument(Stream stream) {
        assertThat(stream.documents()).isEqualTo(singletonList(EMPTY_DOCUMENT));
    }

    private void thenIsEmptyDocument(Document document) {
        assertThat(document).isEqualTo(EMPTY_DOCUMENT);
    }

    private void thenHasOneCommentOnlyDocument(Stream stream) {
        assertThat(stream.documents()).isEqualTo(singletonList(COMMENT_ONLY));
    }

    private void thenIsCommentOnlyDocument(Document document) {
        assertThat(document).isEqualTo(COMMENT_ONLY);
    }
}
