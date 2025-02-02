package org.workcraft.plugins.circuit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.commands.*;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.circuit.utils.ScanUtils;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;

class CycleCommandTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    void testCycleTmLoopbreakerCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "cycle-tm.circuit.work");
        testLoopbreakerCommands(workName, 0, true);
    }

    @Test
    void testChargeTmLoopbreakerCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "charge-tm.circuit.work");
        testLoopbreakerCommands(workName, 3, true);
    }

    private void testLoopbreakerCommands(String workName, int breakCount, boolean pass)
            throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);

        new PathBreakerClearAllTagCommand().execute(we);
        Assertions.assertEquals(0, countPathBreaker(circuit));

        new PathBreakerAutoAppendTagCommand().execute(we);
        int count = countPathBreaker(circuit);
        Assertions.assertEquals(breakCount, count);

        if (count > 0) {
            new TestableGateInsertionCommand().execute(we);
            Assertions.assertEquals(breakCount, countPathBreaker(circuit));

            new ScanInsertionCommand().execute(we);

            MathNode scaninPort = circuit.getNodeByReference(CircuitSettings.getScaninPort());
            Assertions.assertTrue(scaninPort instanceof FunctionContact);

            MathNode scanoutPort = circuit.getNodeByReference(CircuitSettings.getScanoutPort());
            Assertions.assertTrue(scanoutPort instanceof FunctionContact);

            MathNode scanenPort = circuit.getNodeByReference(CircuitSettings.getScanenPort());
            Assertions.assertTrue(scanenPort instanceof FunctionContact);

            MathNode scanckPort = circuit.getNodeByReference(CircuitSettings.getScanckPort());
            Assertions.assertTrue(scanckPort instanceof FunctionContact);

            for (FunctionComponent component : circuit.getFunctionComponents()) {
                if (ScanUtils.hasPathBreakerOutput(component)) {
                    MathNode scanckPin = circuit.getNodeByReference(component, CircuitSettings.getScanckPin());
                    Assertions.assertTrue(scanckPin instanceof FunctionContact);
                    Assertions.assertEquals(scanckPort, CircuitUtils.findDriver(circuit, scanckPin, false));

                    MathNode scanenPin = circuit.getNodeByReference(component, CircuitSettings.getScanenPin());
                    Assertions.assertTrue(scanenPin instanceof FunctionContact);
                    Assertions.assertEquals(scanenPort, CircuitUtils.findDriver(circuit, scanenPin, false));
                }
            }
        }

        Assertions.assertEquals(pass, new CycleFreenessVerificationCommand().execute(we));

        framework.closeWork(we);
    }

    private int countPathBreaker(Circuit circuit) {
        int result = 0;
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            if (ScanUtils.hasPathBreakerOutput(component)) {
                result++;
            }
            for (Contact contact : component.getInputs()) {
                if (contact.getPathBreaker()) {
                    result++;
                }
            }
        }
        return result;
    }

}
