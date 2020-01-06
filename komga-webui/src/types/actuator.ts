interface ActuatorInfo {
  git: ActuatorGit,
  build: ActuatorBuild
}

interface ActuatorGit {
  commit: ActuatorGitCommit,
  branch: string
}

interface ActuatorGitCommit {
  time: Date,
  id: string
}

interface ActuatorBuild {
  version: string,
  artifact: string,
  name: string,
  group: string,
  time: Date
}
