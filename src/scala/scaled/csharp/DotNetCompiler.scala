//
// Scaled C# Project Support - C# project support for Scaled project framework.
// http://github.com/scaled/csharp-project/blob/master/LICENSE

package scaled.project

import java.nio.file.{Files, Path, Paths}
import scaled._
import scaled.pacman.{Pacman, RepoId, Filez}
import scaled.util.{BufferBuilder, Chars, Errors, SubProcess}

object DotNetCompiler {
  // matches: Dir/Source.cs(LL,CC): warning xUnit2000: Text. [Dir/Project.csproj]
  val outputM = Matcher.regexp("""^([^(]+)\((\d+),(\d+)\): (warning|error)([^:]*): (.*)\[(.*)\]$""")
}

class DotNetCompiler (proj :Project) extends Compiler(proj) {
  import Compiler._
  import DotNetCompiler._

  /** Options to pass to `dotnet build`. */
  def buildOpts :SeqV[String] = Seq()

  val log = proj.metaSvc.log

  override def describeEngine = "dotnet"

  override def describeOptions (bb :BufferBuilder) :Unit = {
    bb.addKeyValue("dotnet: ", if (buildOpts.isEmpty) "<none>" else buildOpts.mkString(" "))
  }

  /** A hook called just before we initiate compilation. */
  protected def willCompile () :Unit = {}

  protected def compile (buffer :Buffer, file :Option[Path]) = {
    // now call down to the project which may copy things back into the output dir
    willCompile()

    val result = Promise[Boolean]()
    val cmd = Seq("dotnet", "build") ++ buildOpts
    SubProcess(SubProcess.Config(cmd.toArray, cwd=proj.root.path),
               proj.metaSvc.exec, buffer, result.succeed)
    result
  }

  override def nextNote (buffer :Buffer, start :Loc) = buffer.findForward(outputM, start) match {
    case Loc.None => NoMoreNotes
    case ploc => try {
      val csproj = Paths.get(outputM.group(7))
      val file = csproj.getParent.resolve(outputM.group(1))
      val eline = outputM.group(2).toInt-1
      val ecol = outputM.group(3).toInt-1
      val ekind = outputM.group(4)
      // val kextra = outputM.group(5)
      val desc = outputM.group(6)
      val pnext = ploc.nextStart
      NoteLoc(Note(Store(file), Loc(eline, ecol), Seq(desc), ekind == "error"), pnext)
    } catch {
      case e :Exception => log.log("Error parsing error buffer", e) ; NoMoreNotes
    }
  }
}
