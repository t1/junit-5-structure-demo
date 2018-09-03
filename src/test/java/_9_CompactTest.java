import lombok.RequiredArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class _9_CompactTest {
    @RequiredArgsConstructor
    abstract class WhenParseAllFirstAndSingle {
        final String input;

        Runnable allThen;
        Runnable firstThen;
        Runnable singleThen;

        Stream stream;
        Document document;
        ParseException thrown;

        private <T> T when(Function<String, T> function) {
            AtomicReference<T> result = new AtomicReference<>();
            thrown = catchThrowableOfType(() -> result.set(function.apply(input)), ParseException.class);
            return result.get();
        }

        @Test void whenParseAll() {
            stream = when(Parser::parseAll);
            allThen.run();
        }

        @Test void whenParseFirst() {
            document = when(Parser::parseFirst);
            firstThen.run();
        }

        @Test void whenParseSingle() {
            document = when(Parser::parseSingle);
            singleThen.run();
        }
    }

    @Nested class GivenEmptyDocument extends WhenParseAllFirstAndSingle {
        private GivenEmptyDocument() {
            super("");
            allThen = () -> assertThat(stream.documents()).isEmpty();
            firstThen = () -> assertThat(thrown).hasMessage("expected at least one document, but found none");
            singleThen = () -> assertThat(thrown).hasMessage("expected exactly one document, but found 0");
        }
    }


    @Nested class GivenSpaceOnlyDocument extends WhenParseAllFirstAndSingle {
        private GivenSpaceOnlyDocument() {
            super(" ");
            val expected = new Document();
            allThen = () -> assertThat(stream.documents()).isEqualTo(singletonList(expected));
            firstThen = () -> assertThat(document).isEqualTo(expected);
            singleThen = () -> assertThat(document).isEqualTo(expected);
        }
    }


    @Nested class GivenCommentOnlyDocument extends WhenParseAllFirstAndSingle {
        private GivenCommentOnlyDocument() {
            super("# test comment");
            val expected = new Document().comment(new Comment().text("test comment"));
            allThen = () -> assertThat(stream.documents()).isEqualTo(singletonList(expected));
            firstThen = () -> assertThat(document).isEqualTo(expected);
            singleThen = () -> assertThat(document).isEqualTo(expected);
        }
    }


    @Nested class GivenTwoCommentOnlyDocuments extends WhenParseAllFirstAndSingle {
        private GivenTwoCommentOnlyDocuments() {
            super("# test comment\n---\n# test comment 2");
            val expected1 = new Document().comment(new Comment().text("test comment"));
            val expected2 = new Document().comment(new Comment().text("test comment 2"));
            allThen = () -> assertThat(stream.documents()).isEqualTo(asList(expected1, expected2));
            firstThen = () -> assertThat(document).isEqualTo(expected1);
            singleThen = () -> assertThat(thrown).hasMessage("expected exactly one document, but found 2");
        }
    }
}
