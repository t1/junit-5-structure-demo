package util

import java.io.IOException
import java.io.Reader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.test.fail

@Throws(IOException::class)
fun main(args: Array<String>) {
    if (args.size != 1) fail("Requires exactly one attributes: the name of the markdown file")
    val file = Paths.get(args[0])

    val html = SimplifiedMarkDownToHtml(file).convert()

    Files.write(Paths.get("README.html"), html.toByteArray())
}

class SimplifiedMarkDownToHtml(private val text: String) {
    constructor(path: Path) : this(Files.newBufferedReader(path)) {
        withImageMappings(Paths.get("$path.image-mappings"))
    }

    constructor(reader: Reader) : this(reader.readText())

    fun withImageMappings(path: Path) {
        if (Files.exists(path))
            Files.readAllLines(path).forEach {
                if (it.startsWith(BASE_PATH_PREFIX)) {
                    basePath = it.substring(BASE_PATH_PREFIX.length)
                } else {
                    val (name, resolution, id) = IMAGE_MAPPING.matchEntire(it)!!.destructured
                    map(name to resolution to id)
                }
            }
    }

    private lateinit var basePath: String

    private val imageMappings = mutableMapOf<String, ImageMapping>()

    private data class ImageMapping(val name: String, val resolution: String, val id: String) {
        val alt get() = name.replace('-', ' ')
    }

    fun withBasePath(basePath: String): SimplifiedMarkDownToHtml {
        this.basePath = basePath
        return this
    }

    fun map(pair: Pair<Pair<String, String>, String>): SimplifiedMarkDownToHtml {
        imageMappings[pair.first.first] = ImageMapping(pair.first.first, pair.first.second, pair.second)
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
        .replace(H1, "\n<h1>$1</h1>\n\n")
        .replace(H2, "\n<h2>$1</h2>\n\n")
        .replace(H3, "\n<h3>$1</h3>\n\n")
        .replace(H4, "\n<h4>$1</h4>\n\n")
        .replace(SINGLE_NEW_LINE, "$1 $2")
        .replace(IMG) { this.imageLink(it.groupValues[2]) }
        .replace(LINK, "<a href=\"$2\" rel=\"noopener\" target=\"_blank\">$1</a>")
        .replace(CODE, "<tt>$1</tt>")
        .replace(EM, "<em>$1</em>")

    private fun imageLink(fileName: String): String {
        val mapping = imageMappings[fileName] ?: throw IllegalStateException("missing image mapping for $fileName")
        return "<a href=\"$basePath/${mapping.name}.png\"><img src=\"$basePath/${mapping.name}-${mapping.resolution}.png\" alt=\"${mapping.alt}\" " +
            "class=\"alignnone size-medium wp-image-${mapping.id}\" /></a>"
    }

    private fun convertCode(text: String): String =
        "<pre" + when {
            text.startsWith("java") -> " lang=\"java5\">" + text.substring(4)
            else -> ">$text"
        } + "</pre>"


    companion object {
        private const val BASE_PATH_PREFIX = "base-bath: "
        private val IMAGE_MAPPING = "(.*): ([0-9x]*):([0-9]*)".toRegex()

        private val TITLE_LINE = "\\A# .*\n\n".toRegex()
        private val H1 = "(?s)\n# ([^\n]*)\n\n".toRegex()
        private val H2 = "(?s)\n## ([^\n]*)\n\n".toRegex()
        private val H3 = "(?s)\n### ([^\n]*)\n\n".toRegex()
        private val H4 = "(?s)\n#### ([^\n]*)\n\n".toRegex()
        private val SINGLE_NEW_LINE = "(?s)([^\n])\n([^\n])".toRegex()
        private val IMG = "!\\[(.*)]\\(img/(.*)\\.png\\)".toRegex()
        private val LINK = "\\[(.*?)]\\((.*?)\\)".toRegex()
        private val CODE = "`(.*?)`".toRegex()
        private val EM = "\\*(.*?)\\*".toRegex()
    }
}

private val Int.isOdd: Boolean get() = (this % 2) == 1
private val Int.isEven: Boolean get() = !this.isOdd
