# Advanced JUnit 5 Testing

Automated tests are critical for any reasonable software project.
Some even say that tests are more important than the production code,
because it's easier to recreate the production code from the tests than the other way around.
Anyway, as they are such important assets, it's necessary to keep them clean:
Adding new tests is something you'll be doing every day, but this must not be write-only;
when you refactor the production code, changes to the test code should be kept to a minimum.

So you have to reduce duplication to a minimum.
This is not so easy, when you mainly think about adding new tests.
And most of the problems only show when the code base gets big... and then it's often too late.

Having guidelines may be helpful, if the team really understands the reasoning behind.
But examples are necessarily simplified, as they can't have the full complexity of a real system,
So bear with me while I try to contrieve an example with enough complexity to show the effects:

## A Parser With Three Methods

Let's take a parser for a stream of documents. It has three methods:

```java
public class Parser {
    /** Parse the one and only document in the input; it's an error if there are more */
    public static Document parseSingle(String input);

    /** Parse only the first document in the input; it's an error if there is none */
    public static Document parseFirst(String input);

    /** Parse the list of documents int the input; may be empty, too */
    public static Stream parseAll(String input);
}
```

We write tests for three streams: One is empty, one contains a document with only one blank,
and one contains a document with only a comment (not to make things too complex).

