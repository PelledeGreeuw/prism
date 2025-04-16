package param.elimination;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import param.MutablePMC;
import prism.PrismComponent;
import prism.PrismException;

public class MinimizeOverallInOutDegree extends EliminationOrderIterator {
	private BitSet eliminatedStates;

	public MinimizeOverallInOutDegree(MutablePMC pmc, int initialState, PrismComponent parent) {
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
		Map<Integer, List<Integer>> results = IntStream.range(0, pmc.getNumStates()).boxed()
				.filter(a -> !eliminatedStates.get(a))
				.collect(Collectors.toMap(
						a -> (getInOutDegree(a, pmc.getIncoming().get(a), pmc.getTransitionTargets().get(a))),
						b -> new ArrayList<Integer>() {
							{
								add(b);
							}
						}, (a, b) -> {
							a.addAll(b);
							return a;
						}));
		List<Integer> lowestMult = results.get(Collections.min(results.keySet()));
		int next = -1;
		if (lowestMult.size() > 1) {
			Integer min = null;
			for (int state : lowestMult) {
				int change = getNeightbourChange(state);
				if (min == null || min > change) {
					min = change;
					next = state;
				}
			}
		} else {
			next = lowestMult.get(0);
		}

		if (next == -1) {
			throw new RuntimeException("Next cannot be -1");
		}
		eliminatedStates.set(next);
		return next;
	}

	public int getNeightbourChange(int state) {
		Set<Integer> targets = new HashSet<>(pmc.getTransitionTargets().get(state));
		Set<Integer> incoming = new HashSet<>(pmc.getIncoming().get(state));
		Set<Integer> neighbours = new HashSet<>(targets);
		neighbours.addAll(incoming); 
		int change = -getInOutDegree(state, incoming, targets);
		incoming.remove(state);
		targets.remove(state);

		for (int neighbour : neighbours) {
			Set<Integer> inc = new HashSet<>(pmc.getIncoming().get(neighbour));
			Set<Integer> out = new HashSet<>(pmc.getTransitionTargets().get(neighbour));
			out.remove(state);
			inc.remove(state);
			int before = getInOutDegree(neighbour, inc, out);
			if (incoming.contains(neighbour)) {
				out.addAll(targets);
			}
			if (targets.contains(neighbour)) {
				inc.addAll(incoming);
			}
			int after = getInOutDegree(neighbour, inc, out);
			change += after - before;
		}
		
		return change;
	}

	int getInOutDegree(int state, Collection<Integer> incoming, Collection<Integer> targets) {
		int degree = targets.size() * incoming.size();
		if (incoming.contains(state)) {
			if (targets.size() <= incoming.size()) {
				degree = (targets.size() - 1) * incoming.size();
			} else {
				degree = targets.size() * (incoming.size() - 1);
			}
		}
		return degree;
	}
}