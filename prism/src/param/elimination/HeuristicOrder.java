package param.elimination;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import param.MutablePMC;
import param.heuristic.Heuristic;
import prism.PrismComponent;

public class HeuristicOrder extends EliminationOrderIterator {
	final List<Heuristic> heuristics;
	BitSet eliminatedStates;

	public HeuristicOrder(MutablePMC pmc, int initialState, PrismComponent parent, Heuristic... heuristics) {
		super(pmc, initialState, parent);
		eliminatedStates = (BitSet) pmc.getTargetStates().clone();
		eliminatedStates.or(pmc.getInitStates());
		this.heuristics = new ArrayList<>();
		for (Heuristic heuristic : heuristics) {
			this.heuristics.add(heuristic);
		}
	}

	@Override
	public boolean hasNext() {
		return eliminatedStates.cardinality() < pmc.getNumStates();
	}

	@Override
	public Integer next() {
		Set<Integer> states = new HashSet<>();
		for (int i = 0; i < pmc.getNumStates(); i++) {
			if (!eliminatedStates.get(i)) {
				states.add(i);
			}
		}
		for (Heuristic heuristic : heuristics) {
			states = heuristic.getBestStates(pmc, states);
		}
		int next = states.iterator().next();
		eliminatedStates.set(next);
		return next;
	}

}
