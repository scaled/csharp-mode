//
// Scaled C# Mode - a Scaled major mode for editing C# code
// http://github.com/scaled/csharp-mode/blob/master/LICENSE

package scaled.project

import java.nio.file.{Files, Path}
import scala.jdk.CollectionConverters._
import scala.util.matching._
import scaled._

/** Plugins to extract project metadata from `.sln` files. */
object CSharpPlugins {

  def findSln (root :Path) :Option[Path] = Option.from(
    Files.list(root).filter(p => p.getFileName.toString.endsWith(".sln")).findAny())

  @Plugin(tag="project-root")
  class SlnRootPlugin extends RootPlugin {
    def checkRoot (root :Path) = if (findSln(root).isDefined) 1 else -1
  }

  @Plugin(tag="project-resolver")
  class SlnResolverPlugin extends ResolverPlugin {

    override def metaFiles (root :Project.Root) = findSln(root.path).toSeq

    def addComponents (project :Project) :Unit = {
      val rootPath = project.root.path
      val projName = rootPath.getFileName.toString // TOOD: read from sln?
      val ignores = Ignorer.stockIgnores
      ignores += Ignorer.ignoreName("bin")
      ignores += Ignorer.ignoreName("obj")
      // TODO: only ignore these if we detect Unity
      ignores += Ignorer.ignoreName("bin~")
      ignores += Ignorer.ignoreName("obj~")
      ignores += Ignorer.ignoreRegex(".*\\.meta")

      // TODO: we should probably do this in stockIgnores?
      ignores ++= Ignorer.gitIgnores(rootPath)

      val sources = SeqBuffer[Path]()
      def addProject (pdir :Path) :Unit = {
        // if this appears to be a Unity project, ignore Unity stuff as well
        if (Files.exists(pdir.resolve("Assets"))) {
          ignores += Ignorer.ignorePath(pdir.resolve("Library"), rootPath)
          ignores += Ignorer.ignorePath(pdir.resolve("Temp"), rootPath)
          ignores += Ignorer.ignorePath(pdir.resolve("Logs"), rootPath)
        }

        sources += pdir
      }

      for (sln <- findSln(rootPath) ;
           slnLine <- Files.newBufferedReader(sln).lines.iterator.asScala) slnLine match {
        case projRe(proj, path) if (path endsWith ".csproj") =>
          val csproj = rootPath.resolve(path.replace('\\', '/'))
          if (Files.exists(csproj)) addProject(csproj.getParent)
          else println("Invalid .csproj file? " + csproj)
          // case projRe(proj, path) => println("NOPE " + proj + " // " + path)
        case line if (line startsWith "Project") => println("NOMATCH " + line)
        case _ => // ignore
      }

      // add a sources component with our source directories
      project.addComponent(classOf[Sources], new Sources(sources.toSeq))

      project.addComponent(classOf[Filer], new DirectoryFiler(project, ignores))

      // add a compiler that runs 'dotnet build' and parses the output
      // project.addComponent(classOf[Compiler], new DotNetCompiler(project, sln))

      val oldMeta = project.metaV()
      project.metaV() = oldMeta.copy(name = projName)
    }
  }

  val projRe = raw"""Project\("\{.*\}"\) = "(.*)", "(.*)", "\{.*\}"""".r
}
