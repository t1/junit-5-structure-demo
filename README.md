# Advanced JUnit 5 Testing

Automated tests are critical for any reasonable software project.
Some even say that tests are more important than the production code,
because it's easier to recreate the production code from the tests than the other way around.
Anyway, as they are such important assets, it's necessary to keep them clean:
Adding new tests is something you'll be doing every day, but this must not be write-only;
when you refactor the production code, changes to the test code should be kept to a minimum.
So you'll have to reduce duplication to a minimum.
This is not so easy, when you mainly think about adding new tests.
And most of the problems only show when the code base gets big... and then it's often too late.

Having guidelines may be helpful, if the team really understands the reasoning behind.
But any examples are necessarily simplified, as they can't have the full complexity of a real system,
so bear with me while I try to contrive an example with enough complexity to show the effects.
And I'm going to challenge your fantasy that some things, while they are just over engineered at this scale,
will prove very useful when things get bigger.

## Example: Testing a Parser With Three Methods for Four Documents

Let's take a parser for a stream of documents (like in YAML). It has three methods:

```java
public class Parser {
    /** Parse the one and only document in the input; it's an error if there is none or more than one. */
    public static Document parseSingle(String input);

    /** Parse only the first document in the input; it's an error if there is none. */
    public static Document parseFirst(String input);

    /** Parse the list of documents in the input; may be empty, too. */
    public static Stream parseAll(String input);
}
```

We write tests for four input files:
One is empty,
one contains a document with only one blank,
one contains a single document containing only a comment,
and one contains two documents with each only containing a comment
(not to make things too complex).
That makes a total of 12 tests.
Following the BDD `given-when-then` schema and using AssertJ, they'll look somewhat like this:

```java
class ParserTest { 
    @Test void shouldParseSingleInDocumentOnly() {
        input = "# test comment";

        Document document = Parser.parseSingle(input);

        assertThat(document).isEqualTo(new Document().comment(new Comment().text("test comment")));
    }
}
```

To reduce duplicated code, we extract all three phases into methods, so we can reuse them in the other tests:

```java
class ParserTest {
    @Test void shouldParseSingleInDocumentOnly() {
        String input = givenCommentOnlyDocument();

        Document document = whenParseSingle(input);

        thenIsCommentOnlyDocument(document);
    }
}
```

Or when the parser should fail:

```java
class ParserTest {
    @Test void shouldParseSingleInEmpty() {
        String input = givenEmptyDocument();

        ParseException thrown = whenParseSingleThrows(input);

        thenExpectedExactlyOneButFoundNone(thrown);
    }
}
```

These `given...` methods all return an `input` String, while all `when...` methods take that string as an argument.
When tests get more complex, they produce or require more than one object, so you'll have to pass them via member variables.
Let's do that here, too:

```java
class ParserTest {
    private String input;

    private void givenEmptyDocument() {
        input = "";
    }

    private Stream whenParseAll() {
        return Parser.parseAll(input);
    }

    @Test void shouldParseAllInEmptyDocument() {
        givenEmptyDocument();

        Stream stream = whenParseAll();

        thenIsEmpty(stream);
    }
}
``` 

Let me repeat: I don't claim it to be a best practice to extract everything into methods like this.
I do it here only to stress the points I want to make later.
Normally I would extract such methods only, when they provide a better level of abstraction (i.e. they have a very good name).


## Adding Structure

It would be nice to group all tests with the same input together, so it's easier to find them in a larger test base.
Extracting them to separate source files is possible, but they you'd have to
a) share a lot of methods, and
b) use packages to create a sub structure.
Instead, you can surround all tests that call, e.g., `givenTwoCommentOnlyDocuments()` with an inner class `GivenTwoCommentOnlyDocuments`,
but to have JUnit still invoke the tests, we'll have add a `@Nested` annotation:

```java
@Nested class GivenOneCommentOnlyDocument {
    @Test void shouldParseAllInDocumentOnly() {
        givenOneCommentOnlyDocument();

        Stream stream = whenParseAll();

        thenHasOneCommentOnlyDocument(stream);
    }
}
```

JUnit now also runs the tests as a nested group, so we can see the test setup like this:

![structured-test-run](img/structured-test-run.png)

Nice, but we can go a step further.
Instead of calling the respective `given...` method from each test method, we can call it in a `@BeforeEach` setup method,
and as there is now only one call for each `given...` method, we can inline them:

```java
@Nested class GivenTwoCommentOnlyDocuments {
    @BeforeEach void givenTwoCommentOnlyDocuments() {
        input = "# test comment\n---\n# test comment 2";
    }
}
```

I could have a little bit less code (which is generally a good thing) by using a constructor like this:

```java
@Nested class GivenTwoCommentOnlyDocuments {
    GivenTwoCommentOnlyDocuments() {
        input = "# test comment\n---\n# test comment 2";
    }
}
```

...or even an anonymous initializer like this:

```java
@Nested class GivenTwoCommentOnlyDocuments {
    {
        input = "# test comment\n---\n# test comment 2";
    }
}
```

But I prefer methods to have names that say what they do, and as you can see in the first variant, setup methods are no exception.
I sometimes even have several `@BeforeEach` methods, even in a single class, when they do separate setup work.
This gives me the advantage that I don't have to read the method body to understand what it does,
and when some setup doesn't work as expected, I can start looking only at the method that is responsible for that.

Now the test method names still describe the setup they run in, i.e. the `InDocumentOnly` in `shouldParseSingleInDocumentOnly`.
In the code structure as well as in the output provided by the JUnit runner, this is redundant, so we should remove it: `shouldParseSingle`.
The JUnit runner now looks like this:

![grouped-test-run](img/grouped-test-run.png)

## Conclusion (semi-finals)

This is what I really recommend: Group the test setup (a fancy term for that is fixture) into `@Nested` inner classes.

I could stop here, but there is more that you could do;
I'm just not 100% sure, if this is really pulls its weight, as it really adds complexity.
Maybe it would be best to start with the things above, and when everybody is fluent with it, an extra step may prove helpful.
