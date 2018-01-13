def langs = ['golang', 'java-spring', 'kotlin-spring']
def stepsForParallel = [:]
for (lang in langs) {
    stepsForParallel["$lang"] = {
      node("docker") {
        stage("Checkout: $lang") {
            checkout([
                    $class                           : 'GitSCM',
                    branches                         : scm.branches,
                    doGenerateSubmoduleConfigurations: scm.doGenerateSubmoduleConfigurations,
                    extensions                       : scm.extensions + [
                            [$class: 'CleanCheckout'],
                    ],
                    userRemoteConfigs                : scm.userRemoteConfigs
            ])
        }
        stage("Build: $lang") {
            sh """
docker build -t tech-db-hello-$lang -f Dockerfile.$lang .
"""
        }
      }
    }
}
stepsForParallel.failFast = false
parallel stepsForParallel
