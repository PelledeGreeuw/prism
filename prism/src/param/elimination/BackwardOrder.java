package param.elimination;

import java.util.BitSet;
import java.util.HashSet;

import param.MutablePMC;
import prism.PrismComponent;

public class BackwardOrder extends OrderWithShadowIterator {
	private boolean onlyStatesReachingTarget;

	public BackwardOrder(MutablePMC pmc, int initialState, PrismComponent parent, boolean onlyStatesReachingTarget) {
		super(pmc, initialState, parent);
		this.onlyStatesReachingTarget = onlyStatesReachingTarget;
	}

	/**
	 * Orders states so that states near target states are eliminated first. States
	 * which do not reach target states are eliminated last. In case there are no
	 * target states, the order is arbitrary <br>
	 * If {@code onlyStatesReachingTarget} is true, only the states that can reach
	 * the target states will be returned.
	 * 
	 * @return list of states in requested order
	 */
	@Override
	public int[] getOrder() {
		return collectStatesBackward(pmc, onlyStatesReachingTarget, false);
	}

	public static int[] collectStatesBackward(MutablePMC pmc, boolean onlyStatesReachingTarget, boolean includeInitialStates) {
		int[] states = new int[pmc.getNumStates() - pmc.getTargetStates().cardinality() - (includeInitialStates ? 0 : pmc.getInitialStateNumbers().size())];
		BitSet seen = new BitSet(pmc.getNumStates());
		HashSet<Integer> current = new HashSet<Integer>();
		int nextStateNr = 0;
		for (int state = 0; state < pmc.getNumStates(); state++) {
			if (pmc.isTargetState(state)) {
				current.add(state);
				seen.set(state);
			}
			if (!includeInitialStates && pmc.isInitState(state)) {
				seen.set(state);
			}
		}
		while (!current.isEmpty()) {
			HashSet<Integer> next = new HashSet<Integer>();
			for (int state : current) {
				for (int succState : pmc.getIncoming().get(state)) {
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

		if (onlyStatesReachingTarget) {
			int[] reachingTarget = new int[nextStateNr];
			System.arraycopy(states, 0, reachingTarget, 0, nextStateNr);
			return reachingTarget;
		}

		/*
		 * might not find all states when doing as above, so add missing ones
		 */
		for (int state = 0; state < pmc.getNumStates(); state++) {
			if (!seen.get(state) && pmc.getTransitionProbs().get(state).size() == 1) {
				current.add(state);
				states[nextStateNr] = state;
				seen.set(state, true);
				nextStateNr++;
			}
		}
		while (!current.isEmpty()) {
			HashSet<Integer> next = new HashSet<Integer>();
			for (int state : current) {
				for (int succState : pmc.getIncoming().get(state)) {
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

		return states;
	}

}
