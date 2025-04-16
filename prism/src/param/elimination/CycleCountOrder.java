package param.elimination;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import explicit.DTMCSimple;
import explicit.SCCComputer;
import explicit.SCCConsumer;
import param.MutablePMC;
import prism.PrismComponent;
import prism.PrismException;

public class CycleCountOrder extends OrderWithShadowIterator {
	protected List<Set<Integer>> SCCs;
	protected Set<Integer> blocked;
	protected Set<Integer> closed;

	public CycleCountOrder(MutablePMC pmc, int initialState, PrismComponent parent) {
		super(pmc, initialState, parent);
	}

	public void initialize() {
		SCCs = new ArrayList<>();
		blocked = new HashSet<>();
		closed = new HashSet<>();
	}

	public void clean() {
		blocked.clear();
		closed.clear();
		SCCs.clear();
	}

	public Set<Integer> countCycles(List<Set<Integer>> SCCs, int[] resultArray) {
		Set<Integer> starts = new HashSet<Integer>();
		for (Set<Integer> scc : SCCs) {
			if (scc.size() <= 1) {
				continue;
			}
			int start = Collections.min(scc);
			starts.add(start);
			Map<Integer, Set<Integer>> unblockChain = new HashMap<>();
			List<StackEntry> stack = new ArrayList<>();
			stack.add(new StackEntry(start,
					new ArrayList<>(pmc.getTransitionTargets().get(start).stream().filter(scc::contains).toList())));
			while (!stack.isEmpty()) {
				StackEntry current = stack.get(stack.size() - 1);
				if (!current.unvisitedNeighbours.isEmpty()) {
					int nextState = current.unvisitedNeighbours.remove(0);
					if (nextState == start) {
						recordCycle(stack, resultArray);
					} else if (!blocked.contains(nextState)) {
						stack.add(new StackEntry(nextState, new ArrayList<>(
								pmc.getTransitionTargets().get(nextState).stream().filter(scc::contains).toList())));
						closed.remove(nextState);
						blocked.add(nextState);
					}
				} else {
					if (closed.contains(current.state)) {
						Set<Integer> toUnblock = new HashSet<>();
						toUnblock.add(current.state);
						while (!toUnblock.isEmpty()) {
							int unblock = toUnblock.iterator().next();
							toUnblock.remove(unblock);
							if (blocked.contains(unblock)) {
								blocked.remove(unblock);
								Set<Integer> removed = unblockChain.remove(unblock);
								if (removed != null) {
									toUnblock.addAll(removed);
								}
							}
						}
					} else {
						pmc.getTransitionTargets().get(current.state).stream().filter(scc::contains).forEach(a -> {
							unblockChain.computeIfAbsent(a, b -> new HashSet()).add(current.state);
						});
					}
					stack.remove(stack.size() - 1);
				}
			}
		}
		return starts;
	}

	private void removeStates(Set<Integer> states, DTMCSimple<Integer> dtmc) {
		for (int i : states) {
			dtmc.clearState(i);
		}
	}

	public List<StateCountPair> johnsons() throws PrismException {
		Set<Integer> starts = new HashSet<Integer>();
		int[] resultArray = new int[pmc.getNumStates()];
		DTMCSimple<Integer> simple = convertMutablePMCToAll1(pmc);
		do {
			clean();
			removeStates(starts, simple);
			SCCComputer sccComputer = SCCComputer.createSCCComputer(parent, simple, new Consumer());
			sccComputer.computeSCCs();
			starts = countCycles(SCCs, resultArray);
		} while (!starts.isEmpty());
		List<StateCountPair> result = new ArrayList<>(pmc.getNumStates());
		for (int i = 0; i < pmc.getNumStates(); i++) {
			result.add(i, new StateCountPair(i, resultArray[i]));
		}
		return result;
	}

	protected void recordCycle(List<StackEntry> cycle, int[] cycleCounts) {
		for (StackEntry entry : cycle) {
			cycleCounts[entry.state]++;
			closed.add(entry.state);
		}
	}

	protected static class StackEntry {
		int state;
		List<Integer> unvisitedNeighbours;

		public StackEntry(int state, List<Integer> unvisitedNeighbours) {
			this.state = state;
			this.unvisitedNeighbours = unvisitedNeighbours;
		}
	}

	private class Consumer implements SCCConsumer {
		private Set<Integer> current;

		@Override
		public void notifyStartSCC() throws PrismException {
			current = new HashSet<Integer>();
		}

		@Override
		public void notifyStateInSCC(int stateIndex) throws PrismException {
			current.add(stateIndex);
		}

		@Override
		public void notifyEndSCC() throws PrismException {
			SCCs.add(current);
		}

	}

	private static class StateCountPair {
		int state;
		int count;

		public StateCountPair(int state, int count) {
			this.state = state;
			this.count = count;
		}
	}

	public static DTMCSimple<Integer> convertMutablePMCToAll1(MutablePMC pmc) {
		DTMCSimple<Integer> simple = new DTMCSimple<Integer>(pmc.getNumStates());

		for (int state = 0; state < pmc.getNumStates(); state++) {
			for (Integer toState : pmc.getTransitionTargets().get(state)) {
				simple.setProbability(state, toState, 1);
			}
		}
		return simple;
	}

	@Override
	public int[] getOrder() {
		initialize();
		try {
			List<StateCountPair> counts = johnsons();
			Iterator<StateCountPair> iterator = counts.iterator();
			while (iterator.hasNext()) {
				StateCountPair p = iterator.next();
				if (pmc.isInitState(p.state) || pmc.isTargetState(p.state)) {
					iterator.remove();
				}
			}
			counts.sort((StateCountPair a, StateCountPair b) -> Integer.compare(a.count, b.count));
			parent.getLog().print("Counted cycles");
			return counts.stream().mapToInt(a -> a.state).toArray();
		} catch (PrismException e) {
			e.printStackTrace();
			// TODO fix exception
		}
		return null;
	}

}
