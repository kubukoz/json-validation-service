val composeUp = taskKey[Unit]("run docker-compose up")
val composeDown = taskKey[Unit]("run docker-compose down")

composeUp := {
  import sys.process._

  "docker-compose up --force-recreate -d".!!
}

composeDown := {
  import sys.process._

  "docker-compose down".!!
}
