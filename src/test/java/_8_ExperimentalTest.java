import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class _8_ExperimentalTest {
    ///////////////////////////////////// inputs
    private static String input;
    private static Document expected;
    private static Document expected2;

    ///////////////////////////////////// outputs
    private static Stream stream;
    private static Document document;
    private static ParseException thrown;

    @BeforeEach void setup() {
        input = null;
        expected = null;
        expected2 = null;

        stream = null;
        document = null;
        thrown = null;
    }


    ///////////////////////////////////////////////////////////////////////// GIVEN

    @Nested class givenEmptyDocument {
        @BeforeEach void givenEmptyDocument() { input = ""; }

        @Nested class whenParseAll extends ParseAll implements ThenStreamIsEmpty, ThenStreamToStringIsSameAsInput {}

        @Nested class whenParseFirst extends ParseFirst implements ThenThrowsExpectedAtLeastOne {}

        @Nested class whenParseSingle extends ParseSingle implements ThenThrowsExpectedExactlyOneButFoundZero {}
    }


    @Nested class givenSpaceOnlyDocument {
        @BeforeEach void givenSpaceOnlyDocument() {
            input = " ";
            expected = new Document().content(" ");
        }

        @Nested class whenParseAll extends ParseAll implements ThenIsStreamOfOne {}

        @Nested class whenParseFirst extends ParseFirst implements ThenDocumentIsExpected {}

        @Nested class whenParseSingle extends ParseSingle implements ThenDocumentIsExpected {}
    }


    @Nested class givenCommentOnlyDocument {
        @BeforeEach void givenCommentOnlyDocument() {
            input = "# test comment";
            expected = new Document().comment(new Comment().text("test comment"));
        }

        @Nested class whenParseAll extends ParseAll implements ThenIsStreamOfOne, ThenStreamToStringIsSameAsInput {}

        @Nested class whenParseFirst extends ParseFirst implements ThenDocumentIsExpected, ThenDocumentToStringIsSameAsInput {}

        @Nested class whenParseSingle extends ParseSingle implements ThenDocumentIsExpected, ThenDocumentToStringIsSameAsInput {}
    }


    @Nested class givenTwoCommentOnlyDocuments {
        @BeforeEach void givenTwoCommentOnlyDocuments() {
            input = "# test comment\n---\n# test comment 2";
            expected = new Document().comment(new Comment().text("test comment"));
            expected2 = new Document().comment(new Comment().text("test comment 2"));
        }

        @Nested class whenParseAll extends ParseAll implements ThenIsStreamOfTwo, ThenStreamToStringIsSameAsInput {}

        @Nested class whenParseFirst extends ParseFirst implements ThenDocumentIsExpected {}

        @Nested class whenParseSingle extends ParseSingle implements ThenThrowsExpectedExactlyOneButFoundTwo {}
    }


    ///////////////////////////////////////////////////////////////////////// WHEN

    private <T> T when(Function<String, T> function) {
        AtomicReference<T> result = new AtomicReference<>();
        thrown = catchThrowableOfType(() -> result.set(function.apply(input)), ParseException.class);
        return result.get();
    }

    private class ParseAll {
        @BeforeEach void setup() { stream = when(Parser::parseAll); }
    }

    private class ParseFirst {
        @BeforeEach void setup() { document = when(Parser::parseFirst); }
    }

    private class ParseSingle {
        @BeforeEach void setup() { document = when(Parser::parseSingle); }
    }


    ///////////////////////////////////////////////////////////////////////// THEN

    interface ThenStreamIsEmpty {
        @Test default void thenStreamIsEmpty() { assertThat(stream.documents()).isEmpty(); }
    }

    interface ThenIsStreamOfOne {
        @Test default void thenIsStreamOfOne() { assertThat(stream.documents()).isEqualTo(singletonList(expected)); }
    }

    interface ThenIsStreamOfTwo {
        @Test default void thenIsStreamOfTwo() { assertThat(stream.documents()).isEqualTo(asList(expected, expected2)); }
    }

    interface ThenDocumentIsExpected {
        @Test default void thenDocumentIsExpected() { assertThat(document).isEqualTo(expected); }
    }

    interface ThenDocumentToStringIsSameAsInput {
        @Test default void thenDocumentToStringIsSameAsInput() { assertThat(document.toString()).isEqualTo(input); }
    }

    interface ThenStreamToStringIsSameAsInput {
        @Test default void thenStreamToStringIsSameAsInput() { assertThat(stream.toString()).isEqualTo(input); }
    }


    interface ThenThrowsExpectedAtLeastOne {
        @Test default void thenThrowsExpectedAtLeastOne() { assertThat(thrown).hasMessage("expected at least one document, but found none"); }
    }

    interface ThenThrowsExpectedExactlyOneButFoundZero {
        @Test default void thenThrowsExpectedExactlyOneButFoundZero() { assertThat(thrown).hasMessage("expected exactly one document, but found 0"); }
    }

    interface ThenThrowsExpectedExactlyOneButFoundTwo {
        @Test default void thenThrowsExpectedExactlyOneButFoundTwo() { assertThat(thrown).hasMessage("expected exactly one document, but found 2"); }
    }
}
