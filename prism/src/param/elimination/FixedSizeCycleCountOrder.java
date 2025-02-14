package param.elimination;

import java.util.List;

import param.MutablePMC;
import param.elimination.CycleCountOrder.StackEntry;
import prism.PrismComponent;

public class FixedSizeCycleCountOrder extends CycleCountOrder {
	private final int cycleSize;

	public FixedSizeCycleCountOrder(MutablePMC pmc, int initialState, PrismComponent parent, int cycleSize) {
		super(pmc, initialState, parent);
		this.cycleSize = cycleSize;
	}

	@Override
	protected void recordCycle(List<StackEntry> cycle, int[] cycleCounts) {
		for (StackEntry entry : cycle) {
			if (cycle.size() == cycleSize) {
				cycleCounts[entry.state]++;
			}
			closed.add(entry.state);
		}
	}

}
