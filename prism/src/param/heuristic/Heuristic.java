package param.heuristic;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import param.MutablePMC;

public abstract class Heuristic {

	public Heuristic() {
		// TODO Auto-generated constructor stub
	}

	abstract Integer getWeight(MutablePMC pmc, int state);

	public Set<Integer> getBestStates(MutablePMC pmc, Collection<Integer> states) {
		Map<Integer, Set<Integer>> results = states.stream()
				.collect(Collectors.toMap(a -> getWeight(pmc, a), a -> new HashSet<Integer>(Set.of(a)), (a, b) -> {
					a.addAll(b);
					return a;
				}));
		return results.get(Collections.min(results.keySet()));
	}
}
