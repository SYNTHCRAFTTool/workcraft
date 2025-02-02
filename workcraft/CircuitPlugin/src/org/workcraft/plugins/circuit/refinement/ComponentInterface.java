package org.workcraft.plugins.circuit.refinement;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ComponentInterface {

    private final Set<String> inputs;
    private final Set<String> outputs;

    public ComponentInterface(Set<String> inputs, Set<String> outputs) {
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public Set<String> getInputs() {
        return inputs == null ? null : Collections.unmodifiableSet(inputs);
    }

    public Set<String> getOutputs() {
        return outputs == null ? null : Collections.unmodifiableSet(outputs);
    }

    public Set<String> getMissingSignals(ComponentInterface that) {
        Set<String> result = that.createInputOutputSet();
        result.removeAll(this.createInputOutputSet());
        return result;
    }

    public Set<String> getExtraSignals(ComponentInterface that) {
        Set<String> result = this.createInputOutputSet();
        result.removeAll(that.createInputOutputSet());
        return result;
    }

    public Set<String> getMismatchSignals(ComponentInterface that) {
        Set<String> mismatchInputs = this.createInputSet();
        mismatchInputs.retainAll(that.createOutputSet());
        Set<String> mismatchOutputs = this.createOutputSet();
        mismatchOutputs.retainAll(that.createInputSet());
        Set<String> result = new HashSet<>();
        result.addAll(mismatchInputs);
        result.addAll(mismatchOutputs);
        return result;
    }

    private Set<String> createInputOutputSet() {
        Set<String> result = new HashSet<>();
        result.addAll(createInputSet());
        result.addAll(createOutputSet());
        return result;
    }

    private Set<String> createInputSet() {
        return inputs == null ? new HashSet<>() : new HashSet<>(inputs);
    }

    private Set<String> createOutputSet() {
        return outputs == null ? new HashSet<>() : new HashSet<>(outputs);
    }

}
