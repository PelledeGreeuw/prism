package param.elimination;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashSet;

import param.MutablePMC;
import param.elimination.benchmark.DOTExport;
import prism.PrismComponent;

public class ForwardOrder extends OrderWithShadowIterator {
	public ForwardOrder(MutablePMC pmc, int initialState, PrismComponent parent) {
		super(pmc, initialState, parent);
		pmc.printInitialStates();
		try (FileOutputStream fileOutputStream = new FileOutputStream(
				"/mnt/c/dev/master-thesis/master-scripts/dotfiles/model.dot")) {
			DOTExport.exportModel(pmc, fileOutputStream);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		if (nextStateNr != states.length) {
			int[] newStates = new int[nextStateNr];
			System.arraycopy(states, 0, newStates, 0, nextStateNr);
			states = newStates;
		}
		return removeTargetStates(states);
	}
}
