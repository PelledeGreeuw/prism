package param;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import param.elimination.BackwardOrder;

public class CutUnreachableStates {
	private final MutablePMC originalPMC;
	private MutablePMC reachablePMC;
	private final Map<Integer, Integer> originalToNewState = new HashMap<>();
	private int sinkState;

	public CutUnreachableStates(MutablePMC originalPMC) {
		this.originalPMC = originalPMC;
	}

	public void calculateReachableStates() {
		
		int[] reachableStatesWithoutTarget = BackwardOrder.collectStatesBackward(originalPMC, true, true);
		int[] reachableStates = new int[reachableStatesWithoutTarget.length + originalPMC.getTargetStates().cardinality()];
		System.arraycopy(reachableStatesWithoutTarget, 0, reachableStates, 0, reachableStatesWithoutTarget.length);
		int ctr = reachableStatesWithoutTarget.length;
		for (int state = 0; state < originalPMC.getNumStates(); state++) {
			if (originalPMC.isTargetState(state)) {
				reachableStates[ctr++] = state;
			}
		}
		
		reachablePMC = new MutablePMC(originalPMC.getFunctionFactory(), reachableStates.length + 1,
				originalPMC.isUseRewards(), originalPMC.isUseTime());
		sinkState = reachableStates.length;
		reachablePMC.addTransition(sinkState, sinkState, reachablePMC.getFunctionFactory().getOne());
		for (int i = 0; i < reachableStates.length; i++) {
			originalToNewState.put(reachableStates[i], i);
		}
		for (int i = 0; i < reachableStates.length; i++) {
			int oldState = reachableStates[i];
			Iterator<Integer> targetIterator = originalPMC.getTransitionTargets().get(oldState).iterator();
			Iterator<Function> probIterator = originalPMC.getTransitionProbs().get(oldState).iterator();
			while (targetIterator.hasNext()) {
				int target = targetIterator.next();
				int newTarget = originalToNewState.getOrDefault(target, sinkState);
				reachablePMC.addTransition(i, newTarget, probIterator.next());
			}
			if (originalPMC.isUseRewards()) {
				reachablePMC.setReward(i, originalPMC.getReward(oldState));
			}
			reachablePMC.setTargetState(i, originalPMC.isTargetState(oldState));
			reachablePMC.setInitState(i, originalPMC.isInitState(oldState));
		}
	}

	public MutablePMC getReachableStatesPMC() {
		if (reachablePMC == null) {
			calculateReachableStates();
		}
		return reachablePMC;
	}

	public int getNewStateNR(int originalStateNR) {
		return originalToNewState.getOrDefault(originalStateNR, sinkState);
	}

}
