package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.utils.ToggleUtils;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.*;
import org.workcraft.traces.Solution;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.ExecutableUtils;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.TextUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MpsatTask implements Task<MpsatOutput> {

    private static final String SOLUTIONS_FILE_PREFIX = "solutions";
    private static final String SOLUTIONS_FILE_EXTENSION = ".xml";

    private final File unfoldingFile;
    private final File netFile;
    private final VerificationParameters verificationParameters;
    private final File directory;

    public MpsatTask(File netFile, VerificationParameters verificationParameters, File directory) {
        this(null, netFile, verificationParameters, directory);
    }

    public MpsatTask(File unfoldingFile, File netFile, VerificationParameters verificationParameters, File directory) {
        this.unfoldingFile = unfoldingFile;
        this.netFile = netFile;
        this.verificationParameters = verificationParameters;
        if (directory == null) {
            // Prefix must be at least 3 symbols long.
            directory = FileUtils.createTempDirectory("mpsat-");
        }
        this.directory = directory;
    }

    @Override
    public Result<? extends MpsatOutput> run(ProgressMonitor<? super MpsatOutput> monitor) {
        ArrayList<String> command = new ArrayList<>();

        // Name of the executable
        String toolName = ExecutableUtils.getAbsoluteCommandPath(MpsatVerificationSettings.getCommand());
        command.add(toolName);

        // Built-in arguments
        command.addAll(verificationParameters.getMpsatArguments(directory));

        // Global arguments
        if (MpsatVerificationSettings.getReplicateSelfloopPlaces()) {
            command.add("-l");
        }

        // Extra arguments (should go before the file parameters)
        String extraArgs = MpsatVerificationSettings.getArgs();
        if (MpsatVerificationSettings.getAdvancedMode()) {
            String tmp = DialogUtils.showInput("Additional parameters for MPSat:", extraArgs);
            if (tmp == null) {
                return Result.cancel();
            }
            extraArgs = tmp;
        }
        command.addAll(TextUtils.splitWords(extraArgs));

        // Input file
        if (unfoldingFile != null) {
            command.add(unfoldingFile.getAbsolutePath());
        } else {
            command.add(netFile.getAbsolutePath());
        }

        // Output file
        File solutionsFile = verificationParameters.getDescriptiveFile(directory,
                SOLUTIONS_FILE_PREFIX, SOLUTIONS_FILE_EXTENSION);

        command.add(solutionsFile.getAbsolutePath());

        boolean printStdout = MpsatVerificationSettings.getPrintStdout();
        boolean printStderr = MpsatVerificationSettings.getPrintStderr();
        ExternalProcessTask task = new ExternalProcessTask(command, directory, printStdout, printStderr);
        SubtaskMonitor<? super ExternalProcessOutput> subtaskMonitor = new SubtaskMonitor<>(monitor);
        Result<? extends ExternalProcessOutput> result = task.run(subtaskMonitor);

        ExternalProcessOutput output = result.getPayload();
        if (result.isSuccess() && (output != null)) {
            int returnCode = output.getReturnCode();
            if ((returnCode == 0) || (returnCode == 1)) {
                try {
                    SolutionReader outputReader = new SolutionReader(solutionsFile);
                    if (outputReader.isSuccess()) {
                        Stg stg = StgUtils.importStg(netFile);
                        Set<String> signals = stg == null ? Collections.emptySet() : stg.getSignalReferences();
                        List<Solution> solutions = ToggleUtils.toggleSignalTransitions(outputReader.getSolutions(), signals);
                        return Result.success(new MpsatOutput(output, verificationParameters, netFile, unfoldingFile, solutions));
                    }
                    return Result.exception(outputReader.getMessage());
                } catch (ParserConfigurationException | SAXException | IOException e) {
                    return Result.exception(e);
                }
            }
            return Result.failure(new MpsatOutput(output, verificationParameters, netFile, unfoldingFile, null));
        }

        if (result.isCancel()) {
            return Result.cancel();
        }

        return Result.exception(result.getCause());
    }

}
