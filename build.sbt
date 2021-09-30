name := "pps-lid"

version := "1.0"

scalaVersion := "2.13.6"

// scalatest dependencies
libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.9"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % "test"

// junit dependencies
libraryDependencies += "junit" % "junit" % "4.12" % "test"
libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"

libraryDependencies += "it.unibo.alice.tuprolog" % "tuprolog" % "3.3.0"

// testing execution policy
parallelExecution in Test := false

Compile / mainClass := Some("main.Main")
assembly / mainClass := Some("main.Main")

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

// sbt compile output options
scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-language:implicitConversions"
)