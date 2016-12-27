package org.workcraft.plugins.stg.serialisation;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.exceptions.FormatException;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.StgPlace;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.serialisation.Format;
import org.workcraft.serialisation.ModelSerialiser;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.util.LogUtils;

public class DotGSerialiser implements ModelSerialiser {

    class ReferenceResolver implements ReferenceProducer {
        HashMap<Object, String> refMap = new HashMap<>();

        @Override
        public String getReference(Object obj) {
            return refMap.get(obj);
        }
    }

    private void writeSignalsHeader(PrintWriter out, Collection<String> signalNames, String header) {
        if (!signalNames.isEmpty()) {
            LinkedList<String> sortedNames = new LinkedList<>(signalNames);
            Collections.sort(sortedNames);
            out.print(header);
            for (String s : sortedNames) {
                out.print(" ");
                out.print(NamespaceHelper.hierarchicalToFlatName(s));
            }
            out.print("\n");
        }
    }

    private Iterable<Node> sortNodes(Collection<? extends Node> nodes, final Model model) {
        ArrayList<Node> list = new ArrayList<>(nodes);
        Collections.sort(list, new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                return model.getNodeReference(o1).compareTo(model.getNodeReference(o2));
            }
        });
        return list;
    }

    private void writeGraphEntry(PrintWriter out, Model model, Node node) {
        if (node instanceof StgPlace) {
            if (((StgPlace) node).isImplicit()) {
                return;
            }
        }
        if (model.getPostset(node).size() > 0) {
            out.write(NamespaceHelper.hierarchicalToFlatName(model.getNodeReference(node)));

            for (Node n : sortNodes(model.getPostset(node), model)) {
                if (n instanceof StgPlace) {
                    if (((StgPlace) n).isImplicit()) {
                        Collection<Node> postset = model.getPostset(n);
                        if (postset.size() > 1) {
                            throw new FormatException("Implicit place cannot have more than one node in postset");
                        }
                        out.write(" " + NamespaceHelper.hierarchicalToFlatName(model.getNodeReference(postset.iterator().next())));
                    } else {
                        out.write(" " + NamespaceHelper.hierarchicalToFlatName(model.getNodeReference(n)));
                    }
                } else {
                    out.write(" " + NamespaceHelper.hierarchicalToFlatName(model.getNodeReference(n)));
                }
            }
            out.write("\n");
        }
    }

    @Override
    public ReferenceProducer serialise(Model model, OutputStream out, ReferenceProducer refs) {
        PrintWriter writer = new PrintWriter(out);
        writer.println("# STG file generated by Workcraft -- http://workcraft.org/");
        writer.println(".model " + getClearTitle(model));
        ReferenceResolver resolver = new ReferenceResolver();
        if (model instanceof StgModel) {
            writeSTG((StgModel) model, writer);
        } else if (model instanceof PetriNetModel) {
            writePN((PetriNetModel) model, writer);
        } else {
            throw new ArgumentException("Model class not supported: " + model.getClass().getName());
        }
        writer.print(".end\n");
        writer.close();
        return resolver;
    }

    public String getClearTitle(Model model) {
        String title = model.getTitle();
        // Non-empty model name must be present in .model line of .g file.
        // Otherwise Petrify will use the full file name (possibly with bad characters) as a Verilog module name.
        if ((title == null) || title.isEmpty()) {
            title = "Untitled";
        }
        // If the title start with a number then prepend it with an underscore.
        if (Character.isDigit(title.charAt(0))) {
            title = "_" + title;
        }
        // Petrify does not allow spaces and special symbols in the model name, so replace them with underscores.
        String result = title.replaceAll("[^A-Za-z0-9_]", "_");
        if (!result.equals(model.getTitle())) {
            LogUtils.logWarningLine("Model title was exported as '" + result + "'.");
        }
        return result;
    }

    private void writeSTG(StgModel stg, PrintWriter out) {
        writeSignalsHeader(out, stg.getSignalReferences(Type.INTERNAL), ".internal");
        writeSignalsHeader(out, stg.getSignalReferences(Type.INPUT), ".inputs");
        writeSignalsHeader(out, stg.getSignalReferences(Type.OUTPUT), ".outputs");
        writeSignalsHeader(out, stg.getDummyReferences(), ".dummy");

        out.print(".graph\n");
        for (Node n : sortNodes(stg.getSignalTransitions(), stg)) {
            writeGraphEntry(out, stg, n);
        }
        for (Node n : sortNodes(stg.getDummyTransitions(), stg)) {
            writeGraphEntry(out, stg, n);
        }
        for (Node n : sortNodes(stg.getPlaces(), stg)) {
            writeGraphEntry(out, stg, n);
        }
        writeMarking(stg, stg.getPlaces(), out);
    }

    private void writeMarking(Model model, Collection<Place> places, PrintWriter out) {
        ArrayList<String> markingEntries = new ArrayList<>();
        for (Place p: places) {
            final int tokens = p.getTokens();
            final String reference;
            if (p instanceof StgPlace) {
                if (((StgPlace) p).isImplicit()) {
                    Node predNode = model.getPreset(p).iterator().next();
                    String predFlatName = NamespaceHelper.hierarchicalToFlatName(model.getNodeReference(predNode));
                    Node succNode = model.getPostset(p).iterator().next();
                    String succFlatName = NamespaceHelper.hierarchicalToFlatName(model.getNodeReference(succNode));
                    reference = "<" + predFlatName + "," + succFlatName + ">";
                } else {
                    reference = NamespaceHelper.hierarchicalToFlatName(model.getNodeReference(p));
                }
            } else {
                reference = NamespaceHelper.hierarchicalToFlatName(model.getNodeReference(p));
            }
            if (tokens == 1) {
                markingEntries.add(reference);
            } else if (tokens > 1) {
                markingEntries.add(reference + "=" + tokens);
            }
        }
        Collections.sort(markingEntries);
        out.print(".marking {");
        boolean first = true;
        for (String m : markingEntries) {
            if (!first) {
                out.print(" ");
            } else {
                first = false;
            }
            out.print(m);
        }
        out.print("}\n");
        StringBuilder capacity = new StringBuilder();
        for (Place p : places) {
            if (p instanceof StgPlace) {
                StgPlace stgPlace = (StgPlace) p;
                if (stgPlace.getCapacity() != 1) {
                    String flatName = NamespaceHelper.hierarchicalToFlatName(model.getNodeReference(p));
                    capacity.append(" " + flatName + "=" + stgPlace.getCapacity());
                }
            }
        }
        if (capacity.length() > 0) {
            out.print(".capacity" + capacity + "\n");
        }
    }

    private void writePN(PetriNetModel net, PrintWriter out) {
        LinkedList<String> transitions = new LinkedList<>();
        for (Transition t : net.getTransitions()) {
            String flatName = NamespaceHelper.hierarchicalToFlatName(net.getNodeReference(t));
            transitions.add(flatName);
        }
        writeSignalsHeader(out, transitions, ".dummy");
        out.print(".graph\n");
        for (Transition t : net.getTransitions()) {
            writeGraphEntry(out, net, t);
        }
        for (Place p : net.getPlaces()) {
            writeGraphEntry(out, net, p);
        }
        writeMarking(net, net.getPlaces(), out);
    }

    @Override
    public boolean isApplicableTo(Model model) {
        return (model instanceof StgModel) || (model instanceof PetriNetModel);
    }

    @Override
    public String getDescription() {
        return "Workcraft STG serialiser";
    }

    @Override
    public String getExtension() {
        return ".g";
    }

    @Override
    public UUID getFormatUUID() {
        return Format.STG;
    }

}
