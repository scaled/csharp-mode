//
// Scaled C# Mode - a Scaled major mode for editing C# code
// http://github.com/scaled/csharp-mode/blob/master/LICENSE

package scaled.csharp

import scaled._
import scaled.code.Commenter

/** Extends [[Commenter]] with some sharpdoc smarts. */
class CSharpCommenter extends Commenter {
  import scaled.code.CodeConfig._

  val atCmdM = Matcher.regexp("@[a-z]+")

  def inDoc (buffer :BufferV, p :Loc) :Boolean = {
    val line = buffer.line(p)
    // we need to be on doc-styled text...
    ((buffer.stylesNear(p) contains docStyle) &&
     // and not on the open doc (///)
     !line.matches(docPrefixM, p.col))
  }

  def insertDocPre (buffer :Buffer, p :Loc) :Loc = {
    buffer.insert(p, Line(docPrefix))
    p + (0, docPrefix.length)
  }

  override def linePrefix  = "//"
  override def blockOpen = "/*"
  override def blockClose = "*/"
  override def blockPrefix = "*"
  override def docPrefix   = "///"

  override def mkParagrapher (syn :Syntax, buf :Buffer) = new CommentParagrapher(syn, buf) {
    private def isAtCmdLine (line :LineV) = line.matches(atCmdM, commentStart(line))
    // don't extend paragraph upwards if the current top is an @cmd
    override def canPrepend (row :Int) =
      super.canPrepend(row) && !isAtCmdLine(line(row+1))
    // don't extend paragraph downwards if the new line is at an @cmd
    override def canAppend (row :Int) =
      super.canAppend(row) && !isAtCmdLine(line(row))
  }
}
