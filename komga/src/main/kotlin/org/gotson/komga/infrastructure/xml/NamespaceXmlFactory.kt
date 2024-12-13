package org.gotson.komga.infrastructure.xml

import com.fasterxml.jackson.core.JsonEncoding
import com.fasterxml.jackson.core.io.IOContext
import com.fasterxml.jackson.dataformat.xml.XmlFactory
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import com.fasterxml.jackson.dataformat.xml.util.StaxUtil
import java.io.File
import java.io.OutputStream
import java.io.Writer
import javax.xml.stream.XMLStreamException
import javax.xml.stream.XMLStreamWriter

class NamespaceXmlFactory(
  private val defaultNamespace: String? = null,
  private val prefixToNamespace: Map<String, String> = emptyMap(),
) : XmlFactory() {
  override fun _createXmlWriter(
    ctxt: IOContext?,
    w: Writer?,
  ): XMLStreamWriter = super._createXmlWriter(ctxt, w).apply { configure() }

  override fun createGenerator(
    out: OutputStream?,
    enc: JsonEncoding?,
  ): ToXmlGenerator = super.createGenerator(out, enc).apply { staxWriter.configure() }

  override fun createGenerator(out: OutputStream?): ToXmlGenerator = super.createGenerator(out).apply { staxWriter.configure() }

  override fun createGenerator(out: Writer?): ToXmlGenerator = super.createGenerator(out).apply { staxWriter.configure() }

  override fun createGenerator(
    f: File?,
    enc: JsonEncoding?,
  ): ToXmlGenerator = super.createGenerator(f, enc).apply { staxWriter.configure() }

  override fun createGenerator(sw: XMLStreamWriter?): ToXmlGenerator = super.createGenerator(sw).apply { staxWriter.configure() }

  private fun XMLStreamWriter.configure() =
    try {
      defaultNamespace?.let { this.setDefaultNamespace(it) }
      for ((key, value) in prefixToNamespace) {
        this.setPrefix(key, value)
      }
    } catch (e: XMLStreamException) {
      StaxUtil.throwAsGenerationException(e, null)
    }
}
