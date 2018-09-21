import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static util.WhenParseBuilder.when;

class _6_MoreWhenTest {


    private static String input;

    @BeforeEach void setup() {
        input = null;
    }

    ///////////////////////////////////////////////////////////////////////// GIVEN

    @Nested class GivenEmptyDocument implements WhenParseAllFirstAndSingle {
        @BeforeEach
        void givenEmptyDocument() {
            input = "";
        }

        @Override public void verifyParseAll(Stream stream) {
            assertThat(stream.documents()).isEmpty();
        }

        @Override public void verifyParseFirstException(ParseException thrown) {
            assertThat(thrown).hasMessage("expected at least one document, but found none");
        }

        @Override public void verifyParseSingleException(ParseException thrown) {
            assertThat(thrown).hasMessage("expected exactly one document, but found 0");
        }
    }


    @Nested class GivenSpaceOnlyDocument implements WhenParseAllFirstAndSingle {
        @BeforeEach
        void givenSpaceOnlyDocument() {
            input = " ";
        }

        @Override public void verifyParseAll(Stream stream) {
            assertThat(stream.documents()).containsExactly(EMPTY_DOCUMENT);
        }

        @Override public void verifyParseFirst(Document document) {
            assertThat(document).isEqualTo(EMPTY_DOCUMENT);
        }

        @Override public void verifyParseSingle(Document document) {
            assertThat(document).isEqualTo(EMPTY_DOCUMENT);
        }
    }


    @Nested class GivenOneCommentOnlyDocument implements WhenParseAllFirstAndSingle {
        @BeforeEach
        void givenOneCommentOnlyDocument() {
            input = "# test comment";
        }

        @Override public void verifyParseAll(Stream stream) {
            assertThat(stream.documents()).containsExactly(COMMENT_ONLY);
        }

        @Override public void verifyParseFirst(Document document) {
            assertThat(document).isEqualTo(COMMENT_ONLY);
        }

        @Override public void verifyParseSingle(Document document) {
            assertThat(document).isEqualTo(COMMENT_ONLY);
        }
    }


    @Nested class GivenTwoCommentOnlyDocuments implements WhenParseAllFirstAndSingle {
        @BeforeEach void givenTwoCommentOnlyDocuments() {
            input = "# test comment\n---\n# test comment 2";
        }

        @Override public void verifyParseAll(Stream stream) {
            assertThat(stream.documents()).containsExactly(COMMENT_ONLY, COMMENT_ONLY_2);
        }

        @Override public void verifyParseFirst(Document document) {
            assertThat(document).isEqualTo(COMMENT_ONLY);
        }

        @Override public void verifyToStringEqualsInput(Document document) {
            assertThat(document).hasToString("# test comment"); // only first
        }

        @Override public void verifyParseSingleException(ParseException thrown) {
            assertThat(thrown).hasMessage("expected exactly one document, but found 2");
        }
    }


    ///////////////////////////////////////////////////////////////////////// WHEN

    interface WhenParseAllFirstAndSingle {
        @Test default void whenParseAll() {
            Stream stream = Parser.parseAll(input);

            verifyParseAll(stream);
            assertThat(stream).hasToString(input);
        }

        void verifyParseAll(Stream stream);


        @Test default void whenParseFirst() {
            when(() -> Parser.parseFirst(input))
                .failsWith(ParseException.class).then(this::verifyParseFirstException)
                .succeeds().then(document ->
            {
                verifyParseFirst(document);
                verifyToStringEqualsInput(document);
            });
        }

        default void verifyParseFirst(Document document) {
            fail("expected exception was not thrown. see the verifyParseFirstException method for details");
        }

        default void verifyParseFirstException(ParseException thrown) {
            fail("unexpected exception. see verifyParseFirst for what was expected", thrown);
        }

        default void verifyToStringEqualsInput(Document document) {
            assertThat(document).hasToString(input);
        }


        @Test default void whenParseSingle() {
            when(() -> Parser.parseSingle(input))
                .failsWith(ParseException.class).then(this::verifyParseSingleException)
                .succeeds().then(document ->
            {
                verifyParseSingle(document);
                verifyToStringEqualsInput(document);
            });
        }

        default void verifyParseSingle(Document document) {
            fail("expected exception was not thrown. see the verifyParseSingleException method for details");
        }

        default void verifyParseSingleException(ParseException thrown) {
            fail("unexpected exception. see verifyParseSingle for what was expected", thrown);
        }
    }


    ///////////////////////////////////////////////////////////////////////// THEN

    private static final Document EMPTY_DOCUMENT = new Document().content(" ");
    private static final Document COMMENT_ONLY = new Document().comment(new Comment().text("test comment"));
    private static final Document COMMENT_ONLY_2 = new Document().comment(new Comment().text("test comment 2"));
}
