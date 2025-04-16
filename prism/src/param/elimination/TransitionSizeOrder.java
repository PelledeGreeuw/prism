package param.elimination;

import java.util.BitSet;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import param.MutablePMC;
import prism.PrismComponent;

public class TransitionSizeOrder extends EliminationOrderIterator {
	private BitSet eliminatedStates;

	public TransitionSizeOrder(MutablePMC pmc, int initialState, PrismComponent parent) {
		super(pmc, initialState, parent);
		eliminatedStates = (BitSet) pmc.getTargetStates().clone();
		eliminatedStates.or(pmc.getInitStates());
	}

	@Override
	public boolean hasNext() {
		return eliminatedStates.cardinality() < pmc.getNumStates();
	}

	@Override
	public Integer next() {
		Map<Integer, Integer> results = IntStream.range(0, pmc.getNumStates()).boxed().filter(a -> !eliminatedStates.get(a))
				.collect(Collectors.toMap(
						a -> (pmc.getIncoming().get(a).size() + pmc.getTransitionTargets().get(a).size()),
						Function.identity(), (a, b) -> a));
		int next = -1;
		if (!results.isEmpty()) {
			next = results.get(Collections.min(results.keySet()));
		}
		if (next == -1 ) {
			throw new RuntimeException("Next cannot be -1");
		}
		eliminatedStates.set(next);
		return next;
	}
}
