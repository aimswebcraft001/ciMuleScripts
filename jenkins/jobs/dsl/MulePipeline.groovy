// Folders
def projectFolderName = "Mulesoft01"
def projectFolder = folder(projectFolderName)

// Variables

def mulesoftAppGitRepoUrl = "https://github.com/aimswebcraft001/ciMuleProject.git"
def mulesoftScriptsGitRepoUrl = "https://github.com/aimswebcraft001/ciMuleScripts.git"

// ** The logrotator variables should be changed to meet your build archive requirements
def logRotatorDaysToKeep = 30
def logRotatorBuildNumToKeep = 30
def logRotatorArtifactsNumDaysToKeep = 30
def logRotatorArtifactsNumToKeep = 30

// Jobs
def buildJob = freeStyleJob(projectFolderName + "/Mulesoft_Build")
def unitTestJob = freeStyleJob(projectFolderName + "/Mulesoft_Unit_Test")
def codeAnalysisJob = freeStyleJob(projectFolderName + "/Mulesoft_Code_Analysis")
def publishJob = freeStyleJob(projectFolderName + "/Mulesoft_Publish")
def deployJob = freeStyleJob(projectFolderName + "/Mulesoft_Deploy")

// Views
def pipelineView = buildPipelineView(projectFolderName + "/Delivery_Pipeline")

pipelineView.with{
    title('MuleSoft Delivery Pipeline')
    displayedBuilds(1)
    selectedJob(projectFolderName + "/Mulesoft_Build")
    showPipelineDefinitionHeader()
    refreshFrequency(5)
}

// Build job definition
buildJob.with{
  description("Mulesoft application build job.")
  logRotator {
    daysToKeep(logRotatorDaysToKeep)
    numToKeep(logRotatorBuildNumToKeep)
    artifactDaysToKeep(logRotatorArtifactsNumDaysToKeep)
    artifactNumToKeep(logRotatorArtifactsNumToKeep)
  }
  scm {
        git {
            remote {
                url(mulesoftAppGitRepoUrl)
            }
            branch("*/master")
        }
  }
  triggers{
      scm('@hourly')
  }
  wrappers {
    preBuildCleanup()
    timestamps()
  }
  steps {
    maven {
      goals('clean install')
      property('skipTests','true')
    }
  }
  publishers{
    archiveArtifacts("**/*.zip")
    downstreamParameterized{
      trigger(projectFolderName + "/Mulesoft_Unit_Test"){
        condition("UNSTABLE_OR_BETTER")
        parameters{
          predefinedProp("B",'${BUILD_NUMBER}')
          predefinedProp("PARENT_BUILD", '${JOB_NAME}')
        }
      }
    }
  }
}

// Unit Test job definition
unitTestJob.with{
  description("This job executes the unit testcases related to Mulesoft application.")
  parameters{
    stringParam("B",'',"Parent build number")
    stringParam("PARENT_BUILD","Mulesoft_Build","Parent build name")
  }
  logRotator {
    daysToKeep(logRotatorDaysToKeep)
    numToKeep(logRotatorBuildNumToKeep)
    artifactDaysToKeep(logRotatorArtifactsNumDaysToKeep)
    artifactNumToKeep(logRotatorArtifactsNumToKeep)
  }
  scm {
        git {
            remote {
                url(mulesoftAppGitRepoUrl)
            }
            branch("*/master")
        }
  }
  wrappers {
    preBuildCleanup()
    timestamps()
  }
  steps {
    maven {
      goals('clean test')
    }
  }
  publishers{
    downstreamParameterized{
      trigger(projectFolderName + "/Mulesoft_Code_Analysis"){
        condition("UNSTABLE_OR_BETTER")
        parameters{
          predefinedProp("B",'${B}')
          predefinedProp("PARENT_BUILD", '${PARENT_BUILD}')
        }
      }
    }
  }
}

// Publish job definition
codeAnalysisJob.with{
  description("This job performs the code analysis on the Mulesoft application code.")
  parameters{
    stringParam("B",'',"Parent build number")
    stringParam("PARENT_BUILD","Mulesoft_Build","Parent build name")
  }
  logRotator {
    daysToKeep(logRotatorDaysToKeep)
    numToKeep(logRotatorBuildNumToKeep)
    artifactDaysToKeep(logRotatorArtifactsNumDaysToKeep)
    artifactNumToKeep(logRotatorArtifactsNumToKeep)
  }
  scm {
    git {
      remote {
        url(mulesoftAppGitRepoUrl)
      }
      branch("*/master")
    }
  }
  wrappers {
    preBuildCleanup()
    timestamps()
  }
  steps {
    shell('''set +x
      |# STUB
      |echo "THIS IS A STUB"
      |set -x'''.stripMargin()
    )
  }
  publishers{
    downstreamParameterized{
      trigger(projectFolderName + "/Mulesoft_Publish"){
        condition("UNSTABLE_OR_BETTER")
        parameters{
          predefinedProp("B",'${B}')
          predefinedProp("PARENT_BUILD", '${PARENT_BUILD}')
        }
      }
    }
  }
}

// Publish job definition
publishJob.with{
  description("This job publishes the Mulesoft application build package to Nexus.")
  parameters{
    stringParam("B",'',"Parent build number")
    stringParam("PARENT_BUILD","Mulesoft_Build","Parent build name")
  }
  logRotator {
    daysToKeep(logRotatorDaysToKeep)
    numToKeep(logRotatorBuildNumToKeep)
    artifactDaysToKeep(logRotatorArtifactsNumDaysToKeep)
    artifactNumToKeep(logRotatorArtifactsNumToKeep)
  }
  wrappers {
    preBuildCleanup()
    timestamps()
  }
  steps {
    copyArtifacts("Mulesoft_Build") {
      buildSelector {
        buildNumber('${B}')
      }
      flatten(true)
      includePatterns('**/*.zip')
    }
    shell('''set +x
      |# STUB
      |echo "THIS IS A STUB"
      |set -x'''.stripMargin()
    )
  }
  publishers{
    downstreamParameterized{
      trigger(projectFolderName + "/Mulesoft_Deploy"){
        condition("UNSTABLE_OR_BETTER")
        parameters{
          predefinedProp("B",'${B}')
          predefinedProp("PARENT_BUILD", '${PARENT_BUILD}')
        }
      }
    }
  }
}

// Deploy job definition
deployJob.with{
  description("This job deploys the Mulesoft application to the SANDBOX environment")
  parameters{
    stringParam("B",'',"Parent build number")
    stringParam("PARENT_BUILD","Mulesoft_Build","Parent build name")
    stringParam("APPLICATION_NAME","helloworld","Mule Application Name.")
    stringParam("RELEASE_VERSION","1.0","Release version to be deployed.")
  }
  logRotator {
    daysToKeep(logRotatorDaysToKeep)
    numToKeep(logRotatorBuildNumToKeep)
    artifactDaysToKeep(logRotatorArtifactsNumDaysToKeep)
    artifactNumToKeep(logRotatorArtifactsNumToKeep)
  }
  wrappers {
    preBuildCleanup()
    timestamps()
  }
  scm {
    git {
      remote {
        url(mulesoftAppGitRepoUrl)
      }
      branch("*/master")
    }
  }
  steps {
    copyArtifacts("Mulesoft_Build") {
      buildSelector {
        buildNumber('${B}')
      }
      flatten(true)
      includePatterns('**/*.zip')
    }
    maven {
      goals('org.mule.tools.maven:mule-maven-plugin:deploy')
      property('mule.home','/home/ubuntu/tools/mule-enterprise-standalone-3.9.1')
      property('mule.application.name','${APPLICATION_NAME}')
      property('mule.application','${APPLICATION_NAME}-${RELEASE_VERSION}-SNAPSHOT.zip')
    }
    shell('''set +x
      |# STUB
      |echo "THIS IS A STUB"
      |set -x'''.stripMargin()
    )
  }
}
