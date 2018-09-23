package util

import java.io.IOException
import java.io.Reader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Throws(IOException::class)
fun main(args: Array<String>) {
    val html = SimplifiedMarkDownToHtml(README)
        .withBasePath("https://blog.codecentric.de/files/2018/09")
        .map("structured-test-run" to "250x223" to "56176")
        .map("grouped-test-run" to "250x270" to "56175")
        .convert()

    println(html)
}

val README = Paths.get("README.md")!!

class SimplifiedMarkDownToHtml(private val text: String) {
    constructor(path: Path) : this(Files.newBufferedReader(path))
    constructor(reader: Reader) : this(reader.readText())

    companion object {
        private val TITLE_LINE = "\\A# .*\n\n".toRegex()
        private val SECTION_LINE = "(?s)\n## ([^\n]*)\n\n".toRegex()
        private val SINGLE_NEW_LINE = "(?s)([^\n])\n([^\n])".toRegex()
        private val IMG = "!\\[(.*)]\\(img/(.*)\\.png\\)".toRegex()
        private val LINK = "\\[(.*?)]\\((.*?)\\)".toRegex()
        private val CODE = "`(.*?)`".toRegex()
        private val EM = "\\*(.*?)\\*".toRegex()
    }

    private lateinit var basePath: String

    private val imageMappings = mutableMapOf<String, Pair<String, String>>()

    fun withBasePath(basePath: String): SimplifiedMarkDownToHtml {
        this.basePath = basePath
        return this
    }

    fun map(pair: Pair<Pair<String, String>, String>): SimplifiedMarkDownToHtml {
        imageMappings[pair.first.first] = pair.first.second to pair.second
        return this
    }

    fun convert(): String {
        val split = text.split("```")
        assert(split.size.isOdd) { "expected ``` blocks to be closed" }
        return split.asSequence()
            .mapIndexed { index, part -> if (index.isEven) convertText(part) else convertCode(part) }
            .joinToString(separator = "")
    }

    private fun convertText(text: String): String = text
        .replace(TITLE_LINE, "")
        .replace(SECTION_LINE, "\n<h1>$1</h1>\n\n")
        .replace(SINGLE_NEW_LINE, "$1 $2")
        .replace(IMG) { this.imageLink(it.groupValues[2]) }
        .replace(LINK, "<a href=\"$2\" rel=\"noopener\" target=\"_blank\">$1</a>")
        .replace(CODE, "<code>$1</code>")
        .replace(EM, "<em>$1</em>")

    private fun imageLink(fileName: String): String {
        val mapping = imageMappings[fileName] ?: throw IllegalStateException("missing image mapping for $fileName")
        return "<a href=\"$basePath/$fileName.png\"><img src=\"$basePath/$fileName-${mapping.first}.png\" alt=\"\" class=\"alignnone size-medium wp-image-${mapping.second}\"/></a>"
    }

    private fun convertCode(text: String): String =
        "<pre" + when {
            text.startsWith("java") -> " lang=\"java5\">" + text.substring(4)
            else -> ">$text"
        } + "</pre>"
}

private val Int.isOdd: Boolean get() = (this % 2) == 1
private val Int.isEven: Boolean get() = !this.isOdd
