//
// Scaled C# Mode - a Scaled major mode for editing C# code
// http://github.com/scaled/csharp-mode/blob/master/LICENSE

package scaled.csharp

import scaled._
import scaled.code.Commenter
import scaled.grammar.GrammarCodeMode

@Major(name="csharp",
       tags=Array("code", "project", "csharp"),
       pats=Array(".*\\.cs"),
       desc="A major editing mode for the C# language.")
class CSharpMode (env :Env) extends GrammarCodeMode(env) {

  override def langScope = "source.cs"

  override protected def createIndenter = new CSharpIndenter(config)

  override val commenter = new Commenter() {
    override def linePrefix  = "//"
    override def blockOpen   = "/*"
    override def blockPrefix = "*"
    override def blockClose  = "*/"
    override def docOpen     = "///"
    override def docPrefix   = "///"
  }

  // TODO: more things!
}
