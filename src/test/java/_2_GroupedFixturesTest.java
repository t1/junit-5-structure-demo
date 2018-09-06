import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class _2_GroupedFixturesTest {


    private String input;


    ///////////////////////////////////////////////////////////////////////// GIVEN

    @Nested class GivenEmptyDocument {
        @BeforeEach
        void givenEmptyDocument() {
            input = "";
        }

        @Test void shouldParseAll() {
            Stream stream = whenParseAll();

            assertThat(stream.documents()).isEmpty();
        }

        @Test void shouldFailToParseFirst() {
            ParseException thrown = whenParseFirstThrows();

            assertThat(thrown).hasMessage("expected at least one document, but found none");
        }

        @Test void shouldFailToParseSingle() {
            ParseException thrown = whenParseSingleThrows();

            assertThat(thrown).hasMessage("expected exactly one document, but found 0");
        }
    }


    @Nested class GivenSpaceOnlyDocument {
        @BeforeEach
        void givenSpaceOnlyDocument() {
            input = " ";
        }

        @Test void shouldParseAll() {
            Stream stream = whenParseAll();

            assertThat(stream.documents()).containsExactly(EMPTY_DOCUMENT);
        }

        @Test void shouldParseFirst() {
            Document document = whenParseFirst();

            assertThat(document).isEqualTo(EMPTY_DOCUMENT);
        }

        @Test void shouldParseSingle() {
            Document document = whenParseSingle();

            assertThat(document).isEqualTo(EMPTY_DOCUMENT);
        }
    }


    @Nested class GivenOneCommentOnlyDocument {
        @BeforeEach
        void givenOneCommentOnlyDocument() {
            input = "# test comment";
        }

        @Test void shouldParseAll() {
            Stream stream = whenParseAll();

            assertThat(stream.documents()).containsExactly(COMMENT_ONLY);
        }

        @Test void shouldParseFirst() {
            Document document = whenParseFirst();

            assertThat(document).isEqualTo(COMMENT_ONLY);
        }

        @Test void shouldParseSingle() {
            Document document = whenParseSingle();

            assertThat(document).isEqualTo(COMMENT_ONLY);
        }
    }


    @Nested class GivenTwoCommentOnlyDocuments {
        @BeforeEach void givenTwoCommentOnlyDocuments() {
            input = "# test comment\n---\n# test comment 2";
        }

        @Test void shouldParseAll() {
            Stream stream = whenParseAll();

            assertThat(stream.documents()).containsExactly(COMMENT_ONLY, COMMENT_ONLY_2);
        }

        @Test void shouldParseFirst() {
            Document document = whenParseFirst();

            assertThat(document).isEqualTo(COMMENT_ONLY);
        }

        @Test void shouldFailToParseSingle() {
            ParseException thrown = whenParseSingleThrows();

            assertThat(thrown).hasMessage("expected exactly one document, but found 2");
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

    private static final Document EMPTY_DOCUMENT = new Document().content(" ");
    private static final Document COMMENT_ONLY = new Document().comment(new Comment().text("test comment"));
    private static final Document COMMENT_ONLY_2 = new Document().comment(new Comment().text("test comment 2"));
}
