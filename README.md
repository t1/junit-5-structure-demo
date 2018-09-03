# Structured JUnit 5 Testing

Automated tests are critical for any reasonable software project.
Some even say that tests are more important than the production code,
because it's easier to recreate the production code from the tests than the other way around.
Anyway, as they are such important assets, it's necessary to keep them clean:
Adding new tests is something you'll be doing every day, but this must not be write-only;
when you refactor the production code, changes to the test code should be kept to a minimum.
So you'll have to reduce duplication to a minimum also in your test code.
This is not so easy, when you mainly think about adding new tests.
And most of the problems only show up when the code base gets big... and then it's often too late.
Having guidelines can help, if the team really understands the reasoning and doesn't follow them blindly.

JUnit 5 gives us some opportunities to improve our tests, and I'll show you some techniques here.
Any examples are necessarily simplified, as they can't have the full complexity of a real system,
so bear with me while I try to contrive examples with enough complexity to show the effects,
and I'm going to challenge your fantasy that some things, while they are just over engineered at this scale,
will prove useful when things get bigger.

If you like, you can follow the refactorings done here by looking at the tests
in [this](https://github.com/t1/junit-5-structure-demo) git project.
They are numbered to match the order presented here.


## Example: Testing a Parser With Three Methods for Four Documents

Let's take a parser for a stream of documents (like in YAML) as an example. It has three methods:

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
Following the BDD `given-when-then` schema and using AssertJ, the tests will look somewhat like this:

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
When tests get more complex, they produce or require more than one object, so you'll have to pass them via field.
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
Normally I would extract such methods only, when they provide a better level of abstraction (i.e. they have a _very_ good name).


## Adding Structure

It would be nice to group all tests with the same input together, so it's easier to find them in a larger test base.
Extracting them to separate source files is possible, but then you'd have to
a) share a lot of `when...` and `then...` methods, and
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

JUnit now runs the tests as a nested group, so we can see the test structure like this:

![structured-test-run](img/structured-test-run.png)

Nice, but we can go a step further.
Instead of calling the respective `given...` method from each test method, we can call it in a `@BeforeEach` setup method,
and as there is now only one call for each `given...` method, we can inline it:

```java
@Nested class GivenTwoCommentOnlyDocuments {
    @BeforeEach void givenTwoCommentOnlyDocuments() {
        input = "# test comment\n---\n# test comment 2";
    }
}
```

We could have a little bit less code (which is generally a good thing) by using a constructor like this:

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
I sometimes even have several `@BeforeEach` methods in a single class, when they do separate setup work.
This gives me the advantage that I don't have to read the method body to understand what it does,
and when some setup doesn't work as expected, I can start by looking at the method that is responsible for that.

Now the test method names still describe the setup they run in, i.e. the `InDocumentOnly` part in `shouldParseSingleInDocumentOnly`.
In the code structure as well as in the output provided by the JUnit runner, this is redundant, so we should remove it: `shouldParseSingle`.
The JUnit runner now looks like this:

![grouped-test-run](img/grouped-test-run.png)

When I group some `Given...` classes, I often add `Given...` classes for all setups, even when they contain only a single test,
just to make things symmetrical.

Sometimes tests share only part of the setup of another test.
You can extract the common setup and add the local setup in the test;
just make sure to express that additional setup step in the name of the test, or you may oversee it.
When things get more complex, it's probably better to nest several layers of `Given...` classes,
even for `Given...` classes with only one test, just to make all setup steps visible at one place, the class names,
and not some in the class names and some in the method names (which is easier to forget).


## Extracting `when...`

You may have noticed that the four classes not only all have the same three test method names (except for the tests that catch exceptions),
these three also call exactly the same `when...` methods; they only differ in the checks performed.

We can extract an abstract class `WhenParseAllFirstAndSingle` to contain the three test methods,
that delegate to abstract methods for the verification.
As the `when...` methods are not reused any more and the test methods have the same level of abstraction, we can inline them.

```java
abstract class WhenParseAllFirstAndSingle {
    @Test void whenParseAll() {
        Stream stream = Parser.parseAll(input);
        verifyParseAll(stream);
    }

    protected abstract void verifyParseAll(Stream stream);
}
```

The verification is done in the implementations, so we can't say something like `thenIsEmpty`, we'll need a generic name.
`thenParseAll` would be misleading, so `verify` with the method called is a good name, e.g. `verifyParseFirst`.

This extraction works fine for `parseAll`, as the call that never fails,
but, e.g., `parseSingle` throws an exception, when there is more than one document.
So the `whenParseSingle` test has to delegate the exception for verification, too.

Let's introduce a second `verifyParseSingleException` method for that check.
When we expect an exception, we don't want to implement the `verifyParseSingle` method any more,
and when we don't expect an exception, we don't want to implement the `verifyParseSingleException`,
so we give both `verify...` methods a default implementation instead:

```java
abstract class WhenParseAllFirstAndSingle {
    @Test void whenParseSingle() {
        ParseException thrown = catchThrowableOfType(() -> {
            Document document = Parser.parseSingle(input);

            verifyParseSingle(document);
        }, ParseException.class);

        if (thrown != null)
            verifyParseSingleException(thrown);
    }

    protected void verifyParseSingle(Document document) {
        fail("expected exception was not thrown. see the verifyParseSingleParseException method for details");
    }

    protected void verifyParseSingleException(ParseException thrown) {
        fail("unexpected exception. see verifyParseSingle for what was expected", thrown);
    }
}
```

When you expect an exception, but it doesn't throw and the verification fails instead, you'll get a test failure that is not very helpful:

```
java.lang.AssertionError: Expecting code to throw <class ParseException> but threw <class java.lang.AssertionError> instead
```

So we need an even smarter `whenParseSingle`:

```java
abstract class WhenParseAllFirstAndSingle {
    @Test void whenParseSingle() {
        AtomicReference<Document> document = new AtomicReference<>();
        ParseException thrown = catchThrowableOfType(() -> document.set(Parser.parseSingle(input)), ParseException.class);

        if (thrown != null)
            verifyParseSingleException(thrown);
        else
            verifyParseSingle(document.get());
    }
}
```

This does add quite some complexity to the `when...` methods, bloating them from 2 lines to 6 with a non-trivial flow.
But we can extract that to a generic `whenVerify` method that we can put into a test utilities class or even module.

```java
abstract class WhenParseAllFirstAndSingle {
    @Test void whenParseSingle() {
        whenVerify(() -> Parser.parseSingle(input), ParseException.class, this::verifyParseSingle, this::verifyParseSingleException);
    }

    protected void verifyParseSingle(Document document) {
        fail("expected exception was not thrown. see the verifyParseSingleException method for details");
    }

    protected void verifyParseSingleException(ParseException thrown) {
        fail("unexpected exception. see verifyParseSingle for what was expected", thrown);
    }

    public static <T, E extends Throwable> void whenVerify(Supplier<T> call, Class<E> exceptionClass, Consumer<T> verify, Consumer<E> verifyException) {
        AtomicReference<T> success = new AtomicReference<>();
        E failure = catchThrowableOfType(() -> success.set(call.get()), exceptionClass);

        if (failure != null)
            verifyException.accept(failure);
        else
            verify.accept(success.get());
    }
}
```

In this way, even when a test that expects a result throws an exception or vice versa, the error message is nice and helpful,
e.g. when `GivenEmptyDocument.whenParseSingle` would expect `thenIsEmptyDocument`, the exception would be (stacktraces omitted):

```
AssertionError: unexpected exception. see verifyParseSingle for what was expected
Caused by: ParseException: expected exactly one document, but found 0
``` 

And the tests themselves look nice, too:

```java
@Nested class GivenTwoCommentOnlyDocuments extends WhenParseAllFirstAndSingle {
    @BeforeEach void givenTwoCommentOnlyDocuments() {
        input = "# test comment\n---\n# test comment 2";
    }

    @Override protected void verifyParseAll(Stream stream) {
        thenHasTwoCommentOnlyDocuments(stream);
    }

    @Override protected void verifyParseFirst(Document document) {
        thenIsCommentOnlyDocument(document);
    }

    @Override protected void verifyParseSingleException(ParseException thrown) {
        thenExpectedExactlyOneButFoundTwo(thrown);
    }
}
```

## Multiple `When...` Classes

If you need more than one set of `when...` test methods, you can change the `When...` class to an interface with default methods.
We also have to change the fields we use to pass test setup objects (the `input` String in this case) to the `When...` class to be static,
as interfaces can't access non-static fields.
This looks like a simple change, but it can cause nasty bugs: You now have to set these fields for every test, or you may inherit them
from tests that ran before, i.e. your tests depend on the execution order, which can bend your mind when you try to debug it.
So be extra careful, here. It's probably worth resetting it to `null` in a top level `@BeforeEach`:

```java
class ParserTest {
    private static String input;

    @BeforeEach void setup() {
        input = null;
    }
}
```

The top level `@BeforeEach` are executed before the nested ones.

Otherwise, the change is straight forward:

```java
private static String input;

interface WhenParseAllFirstAndSingle {
    @Test default void whenParseAll() {
        Stream stream = Parser.parseAll(input);
        verifyParseAll(stream);
    }

    void verifyParseAll(Stream stream);
}
```

You will commonly have tests that only apply to one setup;
just combine tests from a `When...` class or interface with test methods local to a `Given...` class.

If a test is never expected to fail (like `whenParseAll`), you can also add more verifications there.
E.g., YAML documents and streams should render `toString` the same as the input, so we can do:

```java
interface WhenParseAllFirstAndSingle {
    @Test default void whenParseAll() {
        Stream stream = Parser.parseAll(input);
        verifyParseAll(stream);
        thenToStringEqualsInput(stream);
    }
}
```

This is not so easy with the tests that are sometimes expected to fail (e.g. `whenParseSingle`).
They use the `whenVerify` which we wanted to be generic; you can give up on that and inline it like this:

```java
interface WhenParseAllFirstAndSingle {
    @Test default void whenParseAll() {
        Stream stream = Parser.parseAll(input);
        verifyParseAll(stream);
        thenToStringEqualsInput(stream);
    }

    @Test default void whenParseFirst() {
        AtomicReference<Document> success = new AtomicReference<>();
        ParseException failure = catchThrowableOfType(() -> success.set(Parser.parseFirst(input)), ParseException.class);

        if (failure != null)
            verifyParseFirstException(failure);
        else {
            Document document = success.get();
            verifyParseFirst(document);
            thenToStringEqualsInput(document);
        }
    }
}
```

Or you can add the `thenToStringEqualsInput` to all overloaded `verifyParseFirst` methods:

```java
@Nested class GivenSpaceOnlyDocument implements WhenParseAllFirstAndSingle {
    @Override public void verifyParseSingle(Document document) {
        thenIsEmptyDocument(document);
        thenToStringEqualsInput(document);
    }
}
```

Both options have drawbacks: The first hinders you from a reusable `whenVerify`, while the latter adds duplication and makes it easy to forget.
The other options I have tried out were even worse.
Over all, probably the first option is the best one, it means the `when...` methods share that extra complexity, but that is quite stable.


## tl;dr

To add structure to a long sequence of test methods in a class, structure them by grouping them according to their test setup
expressed by inner classes annotated as `@Nested`. Name these classes `Given...` and set it up in one or more `@BeforeEach` methods.
Pass the objects that are set up and then used in your `when...` method in fields.

When there are sets of tests that should be executed in several setups, extract them to a super class,
or if you need more than one such set in one setup, to an interface (and make the setup fields static).
