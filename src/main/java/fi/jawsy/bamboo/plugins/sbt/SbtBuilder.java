package fi.jawsy.bamboo.plugins.sbt;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.types.Commandline;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.atlassian.bamboo.builder.AbstractBuilder;
import com.atlassian.bamboo.configuration.LabelPathMap;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.utils.error.SimpleErrorCollection;
import com.atlassian.bamboo.utils.map.FilteredMap;
import com.atlassian.bamboo.v2.build.agent.capability.Capability;
import com.atlassian.bamboo.v2.build.agent.capability.ReadOnlyCapabilitySet;
import com.atlassian.bamboo.ww2.actions.build.admin.create.BuildConfiguration;
import com.google.common.collect.Lists;

public class SbtBuilder extends AbstractBuilder {

    private static final long serialVersionUID = -5612694309491882048L;

    public static final String DEFAULT_SBT_COMMAND = "update clean test";
    public static final String PARAM_SBT_COMMAND = "command";
    public static final String PATH_HELP = "Path to SBT launcher JAR";

    private String sbtCommand = DEFAULT_SBT_COMMAND;

    public void addDefaultValues(@NotNull BuildConfiguration buildConfiguration) {
        buildConfiguration.setProperty(getKeyPrefix() + "." + PARAM_HASTESTS, false);
        buildConfiguration.setProperty(getKeyPrefix() + "." + PARAM_TEST_RESULTS_DIRECTORY, DEFAULT_TEST_REPORTS_XML);

        buildConfiguration.setProperty(getKeyPrefix() + "." + PARAM_SBT_COMMAND, DEFAULT_SBT_COMMAND);
    }

    @Override
    public String getPathHelp() {
        return PATH_HELP;
    }

    @Override
    @NotNull
    public String[] getCommandArguments(ReadOnlyCapabilitySet capabilitySet) {
        List<String> result = Lists.newArrayList();
        if (isWindowsPlatform()) {
            result.add("/c");
            result.add("call");
            result.add(Commandline.quoteArgument(getJavaExecutable(capabilitySet)));
        }

        result.add("-Dsbt.log.noformat=true");
        result.add("-Djline.terminal=jline.UnsupportedTerminal");
        result.add("-jar");
        result.add(Commandline.quoteArgument(getPath(capabilitySet)));

        result.add(sbtCommand);

        return result.toArray(new String[result.size()]);
    }

    @Override
    @NotNull
    public String getCommandExecutable(ReadOnlyCapabilitySet capabilitySet) {
        return isWindowsPlatform() ? "cmd.exe" : getJavaExecutable(capabilitySet);
    }

    @NotNull
    public String getName() {
        return "SBT";
    }

    public String getSbtCommand() {
        return sbtCommand;
    }

    @Override
    @NotNull
    public Map<String, String> getFullParams() {
        Map<String, String> result = super.getFullParams();
        result.put(getKeyPrefix() + "." + PARAM_SBT_COMMAND, getSbtCommand());
        return result;
    }

    @Override
    public Map<String, LabelPathMap> addDefaultLabelPathMaps(Map<String, LabelPathMap> labelPathMaps) {
        labelPathMaps.put("SBT", new LabelPathMap("SBT", "", "fi.jawsy.bamboo-sbt:sbt"));

        return labelPathMaps;
    }

    public boolean isPathValid(@Nullable String path) {
        if (path == null) {
            return false;
        }
        return new File(path).isFile();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void setParams(@NotNull FilteredMap filteredParams) {
        super.setParams(filteredParams);

        if (filteredParams.containsKey(PARAM_SBT_COMMAND)) {
            setSbtCommand((String) filteredParams.get(PARAM_SBT_COMMAND));
        }
    }

    public void setSbtCommand(String sbtCommand) {
        this.sbtCommand = sbtCommand;
    }

    @NotNull
    public ErrorCollection validate(@NotNull BuildConfiguration buildConfiguration) {
        ErrorCollection errorCollection = new SimpleErrorCollection();

        SubnodeConfiguration config = buildConfiguration.configurationAt(getKeyPrefix());

        if (StringUtils.isBlank(config.getString(PARAM_SBT_COMMAND))) {
            errorCollection.addError(getKeyPrefix(), PARAM_SBT_COMMAND, "Please specify the SBT command");
        }

        if (config.containsKey(PARAM_HASTESTS) && config.getBoolean(PARAM_HASTESTS) && StringUtils.isBlank(config.getString(PARAM_TEST_RESULTS_DIRECTORY))) {
            errorCollection.addError(getKeyPrefix(), PARAM_TEST_RESULTS_DIRECTORY, "Please specify the directory containing the XML test result files.");
        }

        if (StringUtils.isBlank(config.getString(PARAM_BUILDJDK))) {
            errorCollection.addError(getKeyPrefix(), PARAM_BUILDJDK, "Please specify a JDK - you need to add a JDK in the Administration pages.");
        }

        return errorCollection;
    }

    @Override
    @NotNull
    protected ErrorCollection validate(@NotNull FilteredMap<String> filteredParams) {
        ErrorCollection errorCollection = new SimpleErrorCollection();

        if (StringUtils.isEmpty((String) filteredParams.get(PARAM_SBT_COMMAND))) {
            errorCollection.addError(getKeyPrefix(), PARAM_SBT_COMMAND, "Please specify the SBT command");
        }

        if (Boolean.valueOf((String) filteredParams.get(PARAM_HASTESTS)) && StringUtils.isEmpty((String) filteredParams.get(PARAM_TEST_RESULTS_DIRECTORY))) {
            errorCollection.addError(getKeyPrefix(), PARAM_TEST_RESULTS_DIRECTORY, "Please specify the directory containing the XML test result files.");
        }

        if (StringUtils.isEmpty((String) filteredParams.get(PARAM_BUILDJDK))) {
            errorCollection.addError(getKeyPrefix(), PARAM_BUILDJDK, "Please specify a JDK - you need to add a JDK in the Administration pages.");
        }
        return errorCollection;
    }

    private String getJavaExecutable(ReadOnlyCapabilitySet capabilitySet) {
        Capability capability = capabilitySet.getCapability("system.jdk." + getBuildJdk());
        if (capability == null) {
            return null;
        }
        String jdkPath = capability.getValue();
        if (StringUtils.isBlank(jdkPath)) {
            return null;
        }

        StringBuilder result = new StringBuilder();
        result.append(jdkPath);
        if (!jdkPath.endsWith(File.separator)) {
            result.append(File.separator);
        }

        result.append("bin").append(File.separator).append("java");
        if (isWindowsPlatform()) {
            result.append(".exe");
        }

        return result.toString();
    }

}
