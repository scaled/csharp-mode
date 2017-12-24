//
// Scaled C# Mode - a Scaled major mode for editing C# code
// http://github.com/scaled/csharp-mode/blob/master/LICENSE

package scaled.csharp

import scaled._
import scaled.code.CodeConfig
import scaled.grammar._

@Plugin(tag="textmate-grammar")
class CSharpGrammarPlugin extends GrammarPlugin {
  import CodeConfig._

  override def grammars = Map("source.cs" -> "CSharp.ndf")

  override def effacers = List(
    effacer("comment.line", commentStyle),
    effacer("comment.block", docStyle),
    effacer("constant", constantStyle),
    effacer("invalid", invalidStyle),
    effacer("keyword", keywordStyle),
    effacer("string", stringStyle),

    effacer("entity.name.package", moduleStyle),
    effacer("entity.name.type", typeStyle),
    effacer("entity.name.tag", constantStyle),
    effacer("entity.other.inherited-class", typeStyle),
    effacer("entity.name.function", functionStyle),
    effacer("entity.name.val-declaration", variableStyle),

    effacer("meta.method.annotation", preprocessorStyle),

    effacer("storage.modifier", keywordStyle),
    effacer("storage.type", typeStyle),

    effacer("variable.package", moduleStyle),
    effacer("variable.import", typeStyle),
    effacer("variable.language", constantStyle),
    // effacer("variable.parameter", variableStyle), // leave params white
    effacer("variable.other.type", variableStyle)
  )

  override def syntaxers = List(
    syntaxer("comment.line", Syntax.LineComment),
    syntaxer("comment.block", Syntax.DocComment),
    syntaxer("constant", Syntax.OtherLiteral),
    syntaxer("string.quoted.double", Syntax.StringLiteral)
  )
}
