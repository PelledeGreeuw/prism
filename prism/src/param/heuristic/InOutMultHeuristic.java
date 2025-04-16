package param.heuristic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import param.MutablePMC;

public class InOutMultHeuristic extends Heuristic {

	@Override
	Integer getWeight(MutablePMC pmc, int state) {
		Set<Integer> targets = new HashSet<>(pmc.getTransitionTargets().get(state));
		Set<Integer> incoming = new HashSet<>(pmc.getIncoming().get(state));
		return getInOutDegree(state, incoming, targets);
	}
	
	public static int getInOutDegree(int state, Collection<Integer> incoming, Collection<Integer> targets) {
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
