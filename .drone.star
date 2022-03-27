def main(ctx):
  return {
    "kind": "pipeline",
    "name": "Test",
    "steps": [
      {
        "name": "build",
        "image": "gradle:7.3.1-jdk11",
        "commands": [
            "./gradlew linkDebugTestLinuxX64"
        ]
      }
    ]
  }