//
// Scaled C# Mode - a Scaled major mode for editing C# code
// http://github.com/scaled/csharp-mode/blob/master/LICENSE

package scaled.project

import java.nio.file.{Files, Path}
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
      println(s"Looking for .sln in $rootPath")
      findSln(rootPath).foreach { sln =>
        println(s"Processing $sln in $rootPath")
        val projName = rootPath.getFileName.toString // TOOD: read from sln?

        val sb = Ignorer.stockIgnores
        sb += Ignorer.ignoreName("bin")
        sb += Ignorer.ignoreName("obj")

        // if this appears to be a Unity project, ignore Unity stuff as well
        if (Files.exists(rootPath.resolve("Assets"))) {
          sb += Ignorer.ignoreName("bin~")
          sb += Ignorer.ignoreName("obj~")
          sb += Ignorer.ignoreName("Library")
          sb += Ignorer.ignoreName("Temp")

          // add a sources component with our source directories
          val sources = new Sources(
            Seq(rootPath.resolve("Assets").resolve("Scripts"))
          )
          project.addComponent(classOf[Sources], sources)

        }

        project.addComponent(classOf[Filer], new DirectoryFiler(project, sb))

        // add a compiler that runs 'dotnet build' and parses the output
        project.addComponent(classOf[Compiler], new DotNetCompiler(project))

        // // TODO: bower doesn't define source directories, so we hack some stuff
        // val sourceDirs = Seq("src", "test").map(rootPath.resolve(_))
        // project.addComponent(classOf[Sources], new Sources(sourceDirs))

        val oldMeta = project.metaV()
        project.metaV() = oldMeta.copy(name = projName)
      }
    }
  }
}
