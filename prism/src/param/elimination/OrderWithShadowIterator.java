package param.elimination;

import java.util.Arrays;
import java.util.Iterator;

import param.MutablePMC;

abstract class OrderWithShadowIterator extends EliminationOrderIterator {
	private Iterator<Integer> shadowIterator;

	public OrderWithShadowIterator(MutablePMC pmc, int initialState) {
		super(pmc, initialState);
	}

	public Iterator<Integer> createIterator() {
		return Arrays.stream(getOrder()).iterator();
	}

	public abstract int[] getOrder();

	public Iterator<Integer> getShadowIterator() {
		if (shadowIterator == null) {
			shadowIterator = createIterator();
		}
		return shadowIterator;
	}

	public int[] removeTargetStates(int[] preFilter) {
		int[] states = new int[pmc.getNumStates() - pmc.getTargetStates().cardinality()];
		int target = 0;
		for (int i = 0; i < preFilter.length; i++) {
			if (!pmc.isTargetState(preFilter[i])) {
				states[target] = preFilter[i];
				target++;
			}
		}
		return states;
	}

	@Override
	public boolean hasNext() {
		return getShadowIterator().hasNext();
	}

	@Override
	public Integer next() {
		return getShadowIterator().next();
	}

}
