name := "pps-lid"

version := "0.1"

scalaVersion := "2.13.6"

// scalatest dependencies
libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.9"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % "test"

// junit dependencies
libraryDependencies += "junit" % "junit" % "4.12" % "test"
libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"

// testing execution policy
parallelExecution in Test := false

// sbt compile output options
scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-language:implicitConversions"
)