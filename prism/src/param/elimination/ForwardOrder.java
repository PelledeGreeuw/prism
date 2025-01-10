package param.elimination;

import java.util.BitSet;
import java.util.HashSet;

import param.MutablePMC;

public class ForwardOrder extends OrderWithShadowIterator {
	public ForwardOrder(MutablePMC pmc, int initialState) {
		super(pmc, initialState);
	}

	/**
	 * Orders states so that states near initial states are eliminated first.
	 * 
	 * @return list of states in requested order
	 */
	@Override
	public int[] getOrder() {
		int[] states = new int[pmc.getNumStates()];
		BitSet seen = new BitSet(pmc.getNumStates());
		HashSet<Integer> current = new HashSet<Integer>();
		int nextStateNr = 0;
		/* put initial states in queue */
		for (int state = 0; state < pmc.getNumStates(); state++) {
			if (pmc.isInitState(state)) {
				states[nextStateNr] = state;
				seen.set(state, true);
				current.add(state);
				nextStateNr++;
			}
		}
		/* perform breadth-first search */
		while (!current.isEmpty()) {
			HashSet<Integer> next = new HashSet<Integer>();
			for (int state : current) {
				for (int succState : pmc.getTransitionTargets().get(state)) {
					if (!seen.get(succState)) {
						seen.set(succState, true);
						next.add(succState);
						states[nextStateNr] = succState;
						nextStateNr++;
					}
				}
			}
			current = next;
		}
		return removeTargetStates(states);
	}
}
