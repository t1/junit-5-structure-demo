import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class CompactTest {
    @RequiredArgsConstructor
    private abstract class AllFirstAndSingle {
        final String input;

        Document EXPECTED;

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

    @Nested class GivenEmptyDocument extends AllFirstAndSingle {
        private GivenEmptyDocument() {
            super("");
            allThen = () -> assertThat(stream.documents()).isEmpty();
            firstThen = () -> assertThat(thrown).hasMessage("expected at least one document, but found none");
            singleThen = () -> assertThat(thrown).hasMessage("expected exactly one document, but found 0");
        }
    }


    @Nested class GivenSpaceOnlyDocument extends AllFirstAndSingle {
        private GivenSpaceOnlyDocument() {
            super(" ");
            EXPECTED = new Document();
            allThen = () -> assertThat(stream.documents()).isEqualTo(singletonList(EXPECTED));
            firstThen = () -> assertThat(document).isEqualTo(EXPECTED);
            singleThen = () -> assertThat(document).isEqualTo(EXPECTED);
        }
    }


    @Nested class GivenCommentOnlyDocument extends AllFirstAndSingle {
        private GivenCommentOnlyDocument() {
            super("# test comment");
            EXPECTED = new Document().comment(new Comment().text("test comment"));
            allThen = () -> assertThat(stream.documents()).isEqualTo(singletonList(EXPECTED));
            firstThen = () -> assertThat(document).isEqualTo(EXPECTED);
            singleThen = () -> assertThat(document).isEqualTo(EXPECTED);
        }
    }
}
