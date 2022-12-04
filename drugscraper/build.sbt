val scala3Version = "3.2.1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "drug-scrape",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test,
    libraryDependencies += "net.ruippeixotog" %% "scala-scraper" % "3.0.0",
    libraryDependencies ++= Seq(
      ("org.apache.spark" %% "spark-sql" % "3.2.1" % "provided")
        .cross(CrossVersion.for3Use2_13)
    ),
    libraryDependencies += "io.github.vincenzobaz" %% "spark-scala3" % "0.1.3",
    Compile / run := Defaults
      .runTask(
        Compile / fullClasspath,
        Compile / run / mainClass,
        Compile / run / runner
      )
      .evaluated
  )
