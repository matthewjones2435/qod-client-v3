package edu.cnm.deepdive.qodclient.model

import java.net.URI
import java.util.Date
import java.util.LinkedList
import java.util.UUID

class Quote {

    var id: UUID? = null

    var text: String? = null

    var created: Date? = null

    var href: URI? = null

    var sources: List<Source>? = LinkedList()

    fun getCombinedText(pattern: String, delimiter: String, unknownSource: String): String {
        val builder:java.lang.StringBuilder? = StringBuilder()
        if (sources?.isEmpty()== true) {
            builder?.append(unknownSource)
        } else {
            sources?.forEach {
                builder?.append(it)?.append(delimiter)
            }
        }
        return String.format(pattern,
                text, builder?.substring(0, builder.length - delimiter.length))
    }

    override fun toString(): String {
        return String.format(TO_STRING_FORMAT, text, sources)
    }

    companion object {

        private val TO_STRING_FORMAT = "%s %s"
    }

}
