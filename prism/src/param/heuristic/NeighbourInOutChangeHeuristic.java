package param.heuristic;

import java.util.HashSet;
import java.util.Set;

import param.MutablePMC;

public class NeighbourInOutChangeHeuristic extends Heuristic {

	@Override
	Integer getWeight(MutablePMC pmc, int state) {
		Set<Integer> targets = new HashSet<>(pmc.getTransitionTargets().get(state));
		Set<Integer> incoming = new HashSet<>(pmc.getIncoming().get(state));
		Set<Integer> neighbours = new HashSet<>(targets);
		neighbours.addAll(incoming);
		int change = -InOutMultHeuristic.getInOutDegree(state, incoming, targets);
		incoming.remove(state);
		targets.remove(state);

		for (int neighbour : neighbours) {
			Set<Integer> inc = new HashSet<>(pmc.getIncoming().get(neighbour));
			Set<Integer> out = new HashSet<>(pmc.getTransitionTargets().get(neighbour));
			int before = InOutMultHeuristic.getInOutDegree(neighbour, inc, out);
			
			out.remove(state);
			inc.remove(state);
			if (incoming.contains(neighbour)) {
				out.addAll(targets);
			}
			if (targets.contains(neighbour)) {
				inc.addAll(incoming);
			}
			int after = InOutMultHeuristic.getInOutDegree(neighbour, inc, out);
			change += after - before;
		}

		return change;
	}

}
