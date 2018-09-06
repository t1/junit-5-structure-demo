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

            assertThat(stream.documents()).isEmpty();
        }

        @Test void shouldParseFirst() {
            ParseException thrown = whenParseFirstThrows();

            assertThat(thrown).hasMessage("expected at least one document, but found none");
        }

        @Test void shouldParseSingle() {
            ParseException thrown = whenParseSingleThrows();

            assertThat(thrown).hasMessage("expected exactly one document, but found 0");
        }
    }


    @Nested class givenSpaceOnlyDocument {
        { input = " "; }

        @Test void shouldParseAll() {
            Stream stream = whenParseAll();

            assertThat(stream.documents()).isEqualTo(singletonList(EMPTY_DOCUMENT));
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


    @Nested class givenCommentOnlyDocument {
        { input = "# test comment"; }

        @Test void shouldParseAll() {
            Stream stream = whenParseAll();

            assertThat(stream.documents()).isEqualTo(singletonList(COMMENT_ONLY));
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
}
