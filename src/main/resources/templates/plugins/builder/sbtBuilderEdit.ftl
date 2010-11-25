[@ww.textfield labelKey='builder.sbt.command' name='builder.${sbt.key}.command' required='true' /]
[@ww.textfield labelKey='builder.common.env' name='builder.${sbt.key}.environmentVariables' /]
[@ww.textfield labelKey='builder.common.sub' name='builder.${sbt.key}.workingSubDirectory' helpUri='working-directory.ftl' /]

[#assign addJdkLink][@ui.displayAddJdkInline /][/#assign]
[@ww.select labelKey='builder.common.jdk' name='builder.${sbt.key}.buildJdk'
            cssClass="builderJdk"
            list=availableJdks required='true'
            extraUtility=addJdkLink]
[/@ww.select]

[@ui.bambooSection titleKey='builder.common.tests.directory.description']
    [@ww.checkbox labelKey='builder.common.tests.exists' name='builder.${sbt.key}.testChecked' toggle='true'/]

    [@ui.bambooSection dependsOn='builder.${sbt.key}.testChecked' showOn='true']
        [@ww.textfield labelKey='builder.common.tests.directory.custom' name='builder.${sbt.key}.testResultsDirectory' /]
    [/@ui.bambooSection]
[/@ui.bambooSection]
