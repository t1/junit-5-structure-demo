import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class ExperimentalTest {
    static String input;
    static Document expected;

    static Stream stream;
    static Document document;
    static ParseException thrown;


    ///////////////////////////////////////////////////////////////////////// WHEN

    private <T> T when(Function<String, T> function) {
        AtomicReference<T> result = new AtomicReference<>();
        thrown = catchThrowableOfType(() -> result.set(function.apply(input)), ParseException.class);
        return result.get();
    }

    private class ParseAll {
        { stream = when(Parser::parseAll); }
    }

    private class ParseFirst {
        { document = when(Parser::parseFirst); }
    }

    private class ParseSingle {
        { document = when(Parser::parseSingle); }
    }


    ///////////////////////////////////////////////////////////////////////// THEN

    interface ThenIsEmptyStream {
        @Test default void isExpected() { assertThat(stream.documents()).isEmpty(); }
    }

    interface ThenIsExpectedStream {
        @Test default void isExpected() { assertThat(stream.documents()).isEqualTo(singletonList(expected)); }
    }

    interface ThenIsExpectedDocument {
        @Test default void isExpected() { assertThat(document).isEqualTo(expected); }
    }


    interface ThenThrowsExpectedAtLeastOne {
        @Test default void throwsExpectedAtLeastOne() { assertThat(thrown).hasMessage("expected at least one document, but found none"); }
    }

    interface ThenThrowsExpectedExactlyOne {
        @Test default void throwsExpectedExactlyOne() { assertThat(thrown).hasMessage("expected exactly one document, but found 0"); }
    }


    ///////////////////////////////////////////////////////////////////////// GIVEN

    @Nested class givenEmptyDocument {
        { input = ""; }

        @Nested class whenParseAll extends ParseAll implements ThenIsEmptyStream {}

        @Nested class whenParseFirst extends ParseFirst implements ThenThrowsExpectedAtLeastOne {}

        @Nested class whenParseSingle extends ParseSingle implements ThenThrowsExpectedExactlyOne {}
    }


    @Nested class givenSpaceOnlyDocument {
        {
            input = " ";
            expected = new Document();
        }

        @Nested class whenParseAll extends ParseAll implements ThenIsExpectedStream {}

        @Nested class whenParseFirst extends ParseFirst implements ThenIsExpectedDocument {}

        @Nested class whenParseSingle extends ParseSingle implements ThenIsExpectedDocument {}
    }


    @Nested class givenCommentOnlyDocument {
        {
            input = "# test comment";
            expected = new Document().comment(new Comment().text("test comment"));
        }

        @Nested class whenParseAll extends ParseAll implements ThenIsExpectedStream {}

        @Nested class whenParseFirst extends ParseFirst implements ThenIsExpectedDocument {}

        @Nested class whenParseSingle extends ParseSingle implements ThenIsExpectedDocument {}
    }
}
