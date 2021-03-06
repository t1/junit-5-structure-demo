import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.Assertions.fail;

class _4_ExtractWhenVerifyTest {


    private String input;


    ///////////////////////////////////////////////////////////////////////// GIVEN

    @Nested class GivenEmptyDocument extends WhenParseAllFirstAndSingle {
        @BeforeEach
        void givenEmptyDocument() {
            input = "";
        }

        @Override protected void verifyParseAll(Stream stream) {
            assertThat(stream.documents()).isEmpty();
        }

        @Override protected void verifyParseFirstException(ParseException thrown) {
            assertThat(thrown).hasMessage("expected at least one document, but found none");
        }

        @Override protected void verifyParseSingleException(ParseException thrown) {
            assertThat(thrown).hasMessage("expected exactly one document, but found 0");
        }
    }


    @Nested class GivenSpaceOnlyDocument extends WhenParseAllFirstAndSingle {
        @BeforeEach
        void givenSpaceOnlyDocument() {
            input = " ";
        }

        @Override protected void verifyParseAll(Stream stream) {
            assertThat(stream.documents()).containsExactly(EMPTY_DOCUMENT);
        }

        @Override protected void verifyParseFirst(Document document) {
            assertThat(document).isEqualTo(EMPTY_DOCUMENT);
        }

        @Override protected void verifyParseSingle(Document document) {
            assertThat(document).isEqualTo(EMPTY_DOCUMENT);
        }
    }


    @Nested class GivenOneCommentOnlyDocument extends WhenParseAllFirstAndSingle {
        @BeforeEach
        void givenOneCommentOnlyDocument() {
            input = "# test comment";
        }

        @Override protected void verifyParseAll(Stream stream) {
            assertThat(stream.documents()).containsExactly(COMMENT_ONLY);
        }

        @Override protected void verifyParseFirst(Document document) {
            assertThat(document).isEqualTo(COMMENT_ONLY);
        }

        @Override protected void verifyParseSingle(Document document) {
            assertThat(document).isEqualTo(COMMENT_ONLY);
        }
    }


    @Nested class GivenTwoCommentOnlyDocuments extends WhenParseAllFirstAndSingle {
        @BeforeEach void givenTwoCommentOnlyDocuments() {
            input = "# test comment\n---\n# test comment 2";
        }

        @Override protected void verifyParseAll(Stream stream) {
            assertThat(stream.documents()).containsExactly(COMMENT_ONLY, COMMENT_ONLY_2);
        }

        @Override protected void verifyParseFirst(Document document) {
            assertThat(document).isEqualTo(COMMENT_ONLY);
        }

        @Override protected void verifyParseSingleException(ParseException thrown) {
            assertThat(thrown).hasMessage("expected exactly one document, but found 2");
        }
    }


    ///////////////////////////////////////////////////////////////////////// WHEN

    abstract class WhenParseAllFirstAndSingle {
        @Test void whenParseAll() {
            Stream stream = Parser.parseAll(input);
            verifyParseAll(stream);
        }

        protected abstract void verifyParseAll(Stream stream);


        @Test void whenParseFirst() {
            whenVerify(() -> Parser.parseFirst(input), ParseException.class, this::verifyParseFirst, this::verifyParseFirstException);
        }

        protected void verifyParseFirst(Document document) {
            fail("expected exception was not thrown. see the verifyParseFirstException method for details");
        }

        protected void verifyParseFirstException(ParseException thrown) {
            fail("unexpected exception. see verifyParseFirst for what was expected", thrown);
        }


        @Test void whenParseSingle() {
            whenVerify(() -> Parser.parseSingle(input), ParseException.class, this::verifyParseSingle, this::verifyParseSingleException);
        }

        protected void verifyParseSingle(Document document) {
            fail("expected exception was not thrown. see the verifyParseSingleException method for details");
        }

        protected void verifyParseSingleException(ParseException thrown) {
            fail("unexpected exception. see verifyParseSingle for what was expected", thrown);
        }
    }

    /**
     * Calls the <code>call</code> and verifies the outcome.
     * If it succeeds, it calls <code>verify</code>.
     * If it fails with an exception of type <code>exceptionClass</code>, it calls <code>verifyException</code>.
     *
     * @param call The `when` part to invoke on the system under test
     * @param exceptionClass The type of exception that may be expected
     * @param verify The `then` part to check a successful outcome
     * @param verifyException The `then` part to check an expected exception
     * @param <T> The type of the result of the `call`
     * @param <E> The type of the expected exception
     */
    public static <T, E extends Throwable> void whenVerify(Supplier<T> call, Class<E> exceptionClass, Consumer<T> verify, Consumer<E> verifyException) {
        AtomicReference<T> success = new AtomicReference<>();
        E failure = catchThrowableOfType(() -> success.set(call.get()), exceptionClass);

        if (failure != null)
            verifyException.accept(failure);
        else
            verify.accept(success.get());
    }


    ///////////////////////////////////////////////////////////////////////// THEN

    private static final Document EMPTY_DOCUMENT = new Document().content(" ");
    private static final Document COMMENT_ONLY = new Document().comment(new Comment().text("test comment"));
    private static final Document COMMENT_ONLY_2 = new Document().comment(new Comment().text("test comment 2"));
}
