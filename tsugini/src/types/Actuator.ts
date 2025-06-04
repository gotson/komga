export interface ActuatorInfo {
  git: ActuatorGit
  build: ActuatorBuild
}

export interface ActuatorGit {
  commit: ActuatorGitCommit
  branch: string
}

export interface ActuatorGitCommit {
  time: Date
  id: string
}

export interface ActuatorBuild {
  version: string
  artifact: string
  name: string
  group: string
  time: Date
}
