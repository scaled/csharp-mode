//
// Scaled C# Mode - support for editing C# code
// https://github.com/scaled/csharp-mode/blob/master/LICENSE

package scaled.csharp

import org.junit.Assert._
import org.junit._
import scaled._
import scaled.grammar._
import scaled.impl.BufferImpl

class CSharpGrammarTest {

  // @Test def dumpGrammar () :Unit = {
  //   val plugin = new CSharpGrammarPlugin()
  //   plugin.grammar.print(System.out)
  // }

  val testCSharpCode = Seq(
    //                  1         2         3         4         5         6         7         8
    //        012345678901234567890123456789012345678901234567890123456789012345678901234567890123456
    /* 0*/ """public static class Test {""",
    /* 1*/ """  public static string Format (int arg) => $"{arg}";""",
    /* 2*/ """}""").mkString("\n")

  @Test def testStylesLink () :Unit = {
    val buffer = BufferImpl(new TextStore("Test.cs", "", testCSharpCode))
    val plugin = new CSharpGrammarPlugin()
    val scoper = Grammar.testScoper(Seq(plugin.grammar("source.cs")), buffer,
                                    List(new Selector.Processor(plugin.effacers)))
    scoper.rethinkBuffer()

    println(scoper.showMatchers(Set("#code", "#class")))
    val start = 0  ; val end = buffer.lines.length
    start until end foreach { ll =>
      println(buffer.line(ll))
      scoper.showScopes(ll) foreach { s => println(ll + ": " + s) }
    }

    // assertFalse("Whitespace before doc comment not scoped as comment",
    //            scoper.scopesAt(Loc(8, 0)).contains("comment.block.documentation.csharp"))
    // assertTrue("Open doc comment is scoped as comment",
    //            scoper.scopesAt(Loc(8, 2)).contains("comment.block.documentation.csharp"))
    // assertTrue("Doc comment content is scoped as comment",
    //            scoper.scopesAt(Loc(8, 6)).contains("comment.block.documentation.csharp"))
  }
}
