package org.workcraft.plugins.dfs.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.plugins.dfs.Dfs;
import org.workcraft.plugins.dfs.tasks.OutputPersistencyCheckTask;
import org.workcraft.plugins.mpsat_verification.tasks.VerificationChainResultHandlingMonitor;
import org.workcraft.plugins.mpsat_verification.utils.MpsatUtils;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class OutputPersisitencyVerificationCommand extends AbstractVerificationCommand
        implements ScriptableCommand<Boolean> {

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public String getDisplayName() {
        return "Output persistency [MPSat]";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Dfs.class);
    }

    @Override
    public void run(WorkspaceEntry we) {
        queueTask(we);
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        VerificationChainResultHandlingMonitor monitor = queueTask(we);
        monitor.setInteractive(false);
        return monitor.waitForHandledResult();
    }

    private VerificationChainResultHandlingMonitor queueTask(WorkspaceEntry we) {
        VerificationChainResultHandlingMonitor monitor = new VerificationChainResultHandlingMonitor(we);
        if (!isApplicableTo(we)) {
            monitor.isFinished(Result.cancel());
        } else {
            Framework framework = Framework.getInstance();
            TaskManager manager = framework.getTaskManager();
            OutputPersistencyCheckTask task = new OutputPersistencyCheckTask(we);
            String description = MpsatUtils.getToolchainDescription(we.getTitle());
            manager.queue(task, description, monitor);
        }
        return monitor;
    }

}
