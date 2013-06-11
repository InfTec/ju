Maven targets:
  - versions:set
    - Run on ch.inftec.ju (root project)
    - Applies a new version to the project and all dependent modules. e.g.:
        versions:set -DnewVersion=3.0-SNAPSHOT
        
Profiles:
  - swisscom-oracle: Include Oracle tests in Swisscom-Infrastructure