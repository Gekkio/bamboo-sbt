[#import "/lib/build.ftl" as bd]

[@ww.label labelKey='builder.sbt.command' name='build.buildDefinition.builder.command' /]
[@bd.showJdk /]
[@ww.label labelKey='builder.common.env' name='build.buildDefinition.builder.environmentVariables' hideOnNull='true' /]
[@ww.label labelKey='builder.common.sub' name='build.buildDefinition.builder.workingSubDirectory' hideOnNull='true' /]
[#if build.buildDefinition.builder.hasTests()]
    [@ww.label labelKey='builder.common.tests.directory' name='build.buildDefinition.builder.testResultsDirectory' hideOnNull='true' /]
[/#if]
