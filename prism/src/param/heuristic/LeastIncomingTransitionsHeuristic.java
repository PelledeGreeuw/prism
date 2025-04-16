package param.heuristic;

import param.MutablePMC;

public class LeastIncomingTransitionsHeuristic extends Heuristic {
	@Override
	Integer getWeight(MutablePMC pmc, int state) {
		return pmc.getIncoming().get(state).size();
	}
}
