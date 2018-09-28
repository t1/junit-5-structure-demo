package util

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class SimplifiedMarkDownToHtmlTest {
    companion object {
        private const val DUMMY_BASE_PATH = "dummy-base-path"
    }

    private val mappings = mutableMapOf<Pair<String, String>, String>()

    @Test
    fun shouldConvertToHtml() {
        val converter = SimplifiedMarkDownToHtml(README)
            .withBasePath("https://blog.codecentric.de/files/2018/09")

        val html = converter.convert()

        Assertions.assertThat(html).isEqualTo(Assertions.contentOf(Paths.get("README.html").toFile()))
    }

    private fun shouldConvert(pair: Pair<String, String>) {
        val converter = SimplifiedMarkDownToHtml(pair.first)
        init(converter)

        val html = converter.convert()

        Assertions.assertThat(html).isEqualTo(pair.second)
    }

    private fun init(converter: SimplifiedMarkDownToHtml) {
        for (entry in mappings.entries)
            converter.map(entry.key.first to entry.key.second to entry.value)
        converter.withBasePath(DUMMY_BASE_PATH)
    }

    private fun withImageMapping(mapping: Pair<Pair<String, String>, String>): SimplifiedMarkDownToHtmlTest {
        this.mappings[mapping.first] = mapping.second
        return this
    }

    @Test fun shouldRemoveInitialH1() = shouldConvert("# hi\n\nho\n" to "ho\n")
    @Test fun shouldConvertH1() = shouldConvert("pre\n\n# hi\n\nho\n" to "pre\n\n<h1>hi</h1>\n\nho\n")
    @Test fun shouldConvertH2() = shouldConvert("pre\n\n## hi\n\nho\n" to "pre\n\n<h2>hi</h2>\n\nho\n")
    @Test fun shouldConvertH3() = shouldConvert("pre\n\n### hi\n\nho\n" to "pre\n\n<h3>hi</h3>\n\nho\n")
    @Test fun shouldConvertH4() = shouldConvert("pre\n\n#### hi\n\nho\n" to "pre\n\n<h4>hi</h4>\n\nho\n")
    @Test fun shouldJoinSingleNewlines() = shouldConvert("hi\nho\n" to "hi ho\n")
    @Test fun shouldConvertLink() = shouldConvert("hi [label](https://uri) ho\n" to "hi <a href=\"https://uri\" rel=\"noopener\" target=\"_blank\">label</a> ho\n")
    @Test fun shouldConvertTT() = shouldConvert("hi `code tt` ho\n" to "hi <tt>code tt</tt> ho\n")
    @Test fun shouldConvertEM() = shouldConvert("hi *text* ho\n" to "hi <em>text</em> ho\n")
    @Test fun shouldConvertImageLink() = withImageMapping("image-name" to "250x300" to "1234").shouldConvert("hi ![label](img/image-name.png) ho\n"
        to "hi <a href=\"$DUMMY_BASE_PATH/image-name.png\"><img src=\"$DUMMY_BASE_PATH/image-name-250x300.png\" alt=\"image name\" class=\"alignnone size-medium wp-image-1234\" /></a> ho\n")

    @Test fun shouldConvertJavaBlock() = shouldConvert("before\n\n```java\ncode1\ncode2\n```\n\nafter" to "before\n\n<pre lang=\"java5\">\ncode1\ncode2\n</pre>\n\nafter")
    @Test fun shouldConvertNonJavaBlock() = shouldConvert("before\n\n```\ncode1\ncode2\n```\n\nafter" to "before\n\n<pre>\ncode1\ncode2\n</pre>\n\nafter")
    @Test fun shouldNotRemoveJavaWithinJavaBlock() = shouldConvert("before\n\n```java\ncode1\njava\ncode2\n```\n\nafter" to "before\n\n<pre lang=\"java5\">\ncode1\njava\ncode2\n</pre>\n\nafter")
    @Test fun shouldNotRemoveJavaWithinNonJavaBlock() = shouldConvert("before\n\n```\ncode1\njava\ncode2\n```\n\nafter" to "before\n\n<pre>\ncode1\njava\ncode2\n</pre>\n\nafter")
}
