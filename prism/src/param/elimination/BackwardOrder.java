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
		return removeTargetStates(collectStatesBackward(pmc, onlyStatesReachingTarget));
	}

	public static int[] collectStatesBackward(MutablePMC pmc, boolean onlyStatesReachingTarget) {
		int[] states = new int[pmc.getNumStates()];
		BitSet seen = new BitSet(pmc.getNumStates());
		HashSet<Integer> current = new HashSet<Integer>();
		int nextStateNr = 0;
		for (int state = 0; state < pmc.getNumStates(); state++) {
			if (pmc.isTargetState(state)) {
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

		if (onlyStatesReachingTarget) {
			return states;
		}

		/*
		 * might not find all states when doing as above, so add missing ones
		 */
		HashSet<Integer> allStates = new HashSet<Integer>();
		for (int stateNr = 0; stateNr < states.length; stateNr++) {
			int state = states[stateNr];
			allStates.add(state);
		}
		for (int state = 0; state < pmc.getNumStates(); state++) {
			if (!allStates.contains(state)) {
				states[nextStateNr] = state;
				nextStateNr++;
			}
		}

		return states;
	}

}
