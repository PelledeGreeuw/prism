package param.heuristic;

import java.util.HashSet;
import java.util.Set;

import param.MutablePMC;

public class MinNeighbourInOutChangeHeuristic extends Heuristic {

	@Override
	Integer getWeight(MutablePMC pmc, int state) {
		Set<Integer> targets = new HashSet<>(pmc.getTransitionTargets().get(state));
		Set<Integer> incoming = new HashSet<>(pmc.getIncoming().get(state));
		Set<Integer> neighbours = new HashSet<>(targets);
		neighbours.addAll(incoming);
		incoming.remove(state);
		targets.remove(state);
		Integer min = Integer.MAX_VALUE;
		for (int neighbour : neighbours) {
			Set<Integer> inc = new HashSet<>(pmc.getIncoming().get(neighbour));
			Set<Integer> out = new HashSet<>(pmc.getTransitionTargets().get(neighbour));
			out.remove(state);
			inc.remove(state);
			if (incoming.contains(neighbour)) {
				out.addAll(targets);
			}
			if (targets.contains(neighbour)) {
				inc.addAll(incoming);
			}
			int after = InOutMultHeuristic.getInOutDegree(neighbour, inc, out);
			if (after != 0 && after < min) {
				min = after;
			}
		}

		return min;
	}

}
