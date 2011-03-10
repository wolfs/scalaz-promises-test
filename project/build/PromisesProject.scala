import sbt._

class PromisesProject(info: ProjectInfo) extends DefaultProject(info)
{

  case class ScalaVersion(version: String)

  class ModuleBaseVersion(module: String) {
     def ~(implicit scalaVersion: ScalaVersion): String = {
       module + "_" + scalaVersion.version
     }
  }

  implicit def string2ModuleBase(module: String) = new ModuleBaseVersion(module)

  implicit val scalaVersion = ScalaVersion("2.8.1")
  val scala28 = ScalaVersion("2.8.0")
  val scalazGroupId: String = "org.scalaz"
  val scalazVersion: String = "6.0-SNAPSHOT"
  val scalaToolsSnapshots = "Scala-Tools Maven Repository" at
          "http://scala-tools.org/repo-snapshots/"

  lazy val scalaSwing = "org.scala-lang" % "scala-swing" % scalaVersion.version withSources()
  lazy val scalaCheck = "org.scala-tools.testing" % "scalacheck".~ % "1.8" % "test->default" withSources()
  lazy val scalaTest = "org.scalatest" % "scalatest" % "1.3" % "test->default" withSources()
  lazy val scalaz = scalazGroupId % "scalaz-core".~ % scalazVersion withSources
  lazy val scalazExamples = scalazGroupId % "scalaz-example".~ % scalazVersion % "test->default" withSources
  lazy val scalazTestSuite = scalazGroupId % "scalaz-test-suite".~ % scalazVersion % "test->default" withSources

}
