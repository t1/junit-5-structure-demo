import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.Assertions.fail;

class _3_ExtractWhenTest {


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
            AtomicReference<Document> document = new AtomicReference<>();
            ParseException thrown = catchThrowableOfType(() -> document.set(Parser.parseFirst(input)), ParseException.class);

            if (thrown != null)
                verifyParseFirstException(thrown);
            else
                verifyParseFirst(document.get());
        }

        protected void verifyParseFirst(Document document) {
            fail("expected exception was not thrown. see the verifyParseFirstException method for details");
        }

        protected void verifyParseFirstException(ParseException thrown) {
            fail("unexpected exception. see verifyParseFirst for what was expected", thrown);
        }


        @Test void whenParseSingle() {
            AtomicReference<Document> document = new AtomicReference<>();
            ParseException thrown = catchThrowableOfType(() -> document.set(Parser.parseSingle(input)), ParseException.class);

            if (thrown != null)
                verifyParseSingleException(thrown);
            else
                verifyParseSingle(document.get());
        }

        protected void verifyParseSingle(Document document) {
            fail("expected exception was not thrown. see the verifyParseSingleException method for details");
        }

        protected void verifyParseSingleException(ParseException thrown) {
            fail("unexpected exception. see verifyParseSingle for what was expected", thrown);
        }
    }


    ///////////////////////////////////////////////////////////////////////// THEN

    private static final Document EMPTY_DOCUMENT = new Document().content(" ");
    private static final Document COMMENT_ONLY = new Document().comment(new Comment().text("test comment"));
    private static final Document COMMENT_ONLY_2 = new Document().comment(new Comment().text("test comment 2"));
}
