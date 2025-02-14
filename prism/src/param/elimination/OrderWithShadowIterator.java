package param.elimination;

import java.util.Arrays;
import java.util.Iterator;

import param.MutablePMC;
import prism.PrismComponent;

abstract class OrderWithShadowIterator extends EliminationOrderIterator {
	private Iterator<Integer> shadowIterator;

	public OrderWithShadowIterator(MutablePMC pmc, int initialState, PrismComponent parent) {
		super(pmc, initialState, parent);
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
		int[] states = new int[preFilter.length];
		int target = 0;
		for (int i = 0; i < preFilter.length; i++) {
			if (!pmc.isTargetState(preFilter[i])) {
				states[target] = preFilter[i];
				target++;
			}
		}
		if (target != states.length) {
			int[] newStates = new int[target];
			System.arraycopy(states, 0, newStates, 0, target);
			states = newStates;
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
