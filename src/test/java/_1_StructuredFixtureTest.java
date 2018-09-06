import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class _1_StructuredFixtureTest {


    private String input;


    ///////////////////////////////////////////////////////////////////////// GIVEN

    @Nested class GivenEmptyDocument {
        @Test void shouldParseAllInEmpty() {
            givenEmptyDocument();

            Stream stream = whenParseAll();

            assertThat(stream.documents()).isEmpty();
        }

        @Test void shouldParseFirstInEmpty() {
            givenEmptyDocument();

            ParseException thrown = whenParseFirstThrows();

            assertThat(thrown).hasMessage("expected at least one document, but found none");
        }

        @Test void shouldParseSingleInEmpty() {
            givenEmptyDocument();

            ParseException thrown = whenParseSingleThrows();

            assertThat(thrown).hasMessage("expected exactly one document, but found 0");
        }
    }


    @Nested class GivenSpaceOnlyDocument {
        @Test void shouldParseAllInSpaceOnly() {
            givenSpaceOnlyDocument();

            Stream stream = whenParseAll();

            assertThat(stream.documents()).containsExactly(EMPTY_DOCUMENT);
        }

        @Test void shouldParseFirstInSpaceOnly() {
            givenSpaceOnlyDocument();

            Document document = whenParseFirst();

            assertThat(document).isEqualTo(EMPTY_DOCUMENT);
        }

        @Test void shouldParseSingleInSpaceOnly() {
            givenSpaceOnlyDocument();

            Document document = whenParseSingle();

            assertThat(document).isEqualTo(EMPTY_DOCUMENT);
        }
    }


    @Nested class GivenOneCommentOnlyDocument {
        @Test void shouldParseAllInDocumentOnly() {
            givenOneCommentOnlyDocument();

            Stream stream = whenParseAll();

            assertThat(stream.documents()).containsExactly(COMMENT_ONLY);
        }

        @Test void shouldParseFirstInDocumentOnly() {
            givenOneCommentOnlyDocument();

            Document document = whenParseFirst();

            assertThat(document).isEqualTo(COMMENT_ONLY);
        }

        @Test void shouldParseSingleInDocumentOnly() {
            givenOneCommentOnlyDocument();

            Document document = whenParseSingle();

            assertThat(document).isEqualTo(COMMENT_ONLY);
        }
    }


    @Nested class GivenTwoCommentOnlyDocuments {
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
}
