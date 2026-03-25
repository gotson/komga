export type ActuatorInfo = {
  git: ActuatorGit
  build: ActuatorBuild
}

export type ActuatorGit = {
  commit: ActuatorGitCommit
  branch: string
}

export type ActuatorGitCommit = {
  time: Date
  id: string
}

export type ActuatorBuild = {
  version: string
  artifact: string
  name: string
  group: string
  time: Date
}
