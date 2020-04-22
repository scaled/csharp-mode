//
// Scaled C# Mode - a Scaled major mode for editing C# code
// http://github.com/scaled/csharp-mode/blob/master/LICENSE

package scaled.csharp

import scaled._
import scaled.code.{Commenter, Indenter, BlockIndenter}
import scaled.grammar.GrammarCodeMode

object CSharpConfig extends Config.Defs {

  @Var("If true, cases inside switch blocks are indented one step.")
  val indentCase = key(false)
}

@Major(name="csharp",
       tags=Array("code", "project", "csharp"),
       pats=Array(".*\\.cs"),
       desc="A major editing mode for the C# language.")
class CSharpMode (env :Env) extends GrammarCodeMode(env) {

  override def langScope = "source.cs"
  override def configDefs = CSharpConfig :: super.configDefs

  override protected def createIndenter = new CSharpIndenter(config)

  override val commenter = new Commenter() {
    override def linePrefix  = "//"
    override def blockOpen   = "/*"
    override def blockPrefix = "*"
    override def blockClose  = "*/"
    override def docOpen     = "///"
    override def docPrefix   = "///"
  }
}

class CSharpIndenter (config :Config) extends BlockIndenter(config, Std.seq(
  // bump `extends`/`implements` in two indentation levels
  BlockIndenter.adjustIndentWhenMatchStart(Matcher.regexp("(extends|implements)\\b"), 2),
  // bump `where` in two indentation levels
  BlockIndenter.adjustIndentWhenMatchStart(Matcher.regexp("(where)\\b"), 1),
  // align chained method calls under their dot
  new BlockIndenter.AlignUnderDotRule(),
  // handle indenting switch statements properly
  new BlockIndenter.SwitchRule() {
    override def indentCaseBlocks = config(CSharpConfig.indentCase)
  },
  // handle continued statements, with some special sauce for : after case
  new BlockIndenter.CLikeContStmtRule()
)) {
  import Indenter._
  import BlockIndenter._

  override protected def openBlock (line :LineV, open :Char, close :Char, col :Int, state :State) =
    super.openBlock(line, open, close, col, state) match {
      case bstate :BlockIndenter.BlockS if (col >= lastNonWS(line)) =>
        if (line.matches(namespaceM)) new I0BlockS(close, -1, bstate.next)
        else bstate
      case ostate => ostate
    }

  private class I0BlockS (close :Char, col :Int, next :Indenter.State)
      extends BlockS(close, col, next) {
    override protected def indentWidth (config :Config) :Int = 0
    override protected def show :String = s"I0BlockS($close, $col)"
  }

  private val namespaceM = Matcher.regexp("\\s*namespace ")
}
