//==============================================================================
//	
//	Copyright (c) 2013-
//	Authors:
//	* Ernst Moritz Hahn <emhahn@cs.ox.ac.uk> (University of Oxford)
//	
//------------------------------------------------------------------------------
//	
//	This file is part of PRISM.
//	
//	PRISM is free software; you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation; either version 2 of the License, or
//	(at your option) any later version.
//	
//	PRISM is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with PRISM; if not, write to the Free Software Foundation,
//	Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//	
//==============================================================================

package param;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Representation of mutable parametric Markov chain. This class is intended to
 * be used in combination with the {@code StateEliminator}, which uses this
 * class to compute values of parametric Markov models.
 * 
 * @author Ernst Moritz Hahn <emhahn@cs.ox.ac.uk> (University of Oxford)
 * @see StateEliminator
 */
public final class MutablePMC implements Cloneable {
	/** function factory to which functions in this object belong */
	private FunctionFactory functionFactory;
	/** assignment of rewards to each state */
	private Function[] rewards;
	/** assignment of time to each state */
	private Function[] times;
	/** for each state, provides list of leaving transition probabilities */
	private List<LinkedList<Function>> transitionProbs;
	/** for each state, provides list of leaving transition targets */
	List<LinkedList<Integer>> transitionTargets;
	/**
	 * for each state, provides list of states which have transitions to this state
	 */
	List<LinkedList<Integer>> incoming;
	/** true iff uses a reward structure */
	private boolean useRewards;
	/** true iff uses time reward structure */
	private boolean useTime;
	/** initial states */
	private BitSet initStates;
	/** target states */
	private BitSet targetStates;
	/** total number of states */
	private int numStates;

	/**
	 * Constructs a new mutable parametric Markov chain.
	 * 
	 * @param functionFactory function factory used to maintain rational functions
	 * @param numStates       total number of states this parametric Markov chain
	 *                        shall have
	 * @param useRewards      true iff parametric Markov chain constructed shall use
	 *                        rewards
	 * @param useTime         true iff parametric Markov chain constructed needs
	 *                        time entry
	 */
	MutablePMC(FunctionFactory functionFactory, int numStates, boolean useRewards, boolean useTime) {
		this.numStates = numStates;
		this.functionFactory = functionFactory;
		transitionProbs = new ArrayList<LinkedList<Function>>(numStates);
		transitionTargets = new ArrayList<LinkedList<Integer>>(numStates);
		incoming = new ArrayList<LinkedList<Integer>>(numStates);
		for (int state = 0; state < numStates; state++) {
			getTransitionTargets().add(new LinkedList<Integer>());
			getTransitionProbs().add(new LinkedList<Function>());
			getIncoming().add(new LinkedList<Integer>());
		}
		this.useRewards = useRewards;
		this.useTime = useTime;
		initStates = new BitSet(numStates);
		targetStates = new BitSet(numStates);
		if (useRewards) {
			rewards = new Function[numStates];
			for (int i = 0; i < numStates; i++) {
				rewards[i] = functionFactory.getZero();
			}
		}
		if (useTime) {
			times = new Function[numStates];
			for (int i = 0; i < numStates; i++) {
				times[i] = functionFactory.getOne();
			}
		}
	}

	private MutablePMC() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public MutablePMC clone() {
		Function[] newRewards = null;
		Function[] newTimes = null;
		if (useRewards) {
			newRewards = new Function[rewards.length];
			System.arraycopy(rewards, 0, newRewards, 0, numStates);
		}
		if (useTime) {
			newTimes = new Function[times.length];
			System.arraycopy(times, 0, newTimes, 0, numStates);
		}

		MutablePMC cloned = new MutablePMC();
		cloned.functionFactory = functionFactory;
		cloned.rewards = newRewards;
		cloned.times = newTimes;
		cloned.transitionProbs = transitionProbs.stream().map(a -> (LinkedList<Function>) a.clone())
				.collect(Collectors.toCollection(() -> new ArrayList<>(transitionProbs.size())));
		cloned.transitionTargets = transitionTargets.stream().map(a -> (LinkedList<Integer>) a.clone())
				.collect(Collectors.toCollection(() -> new ArrayList<>(transitionProbs.size())));
		cloned.incoming = incoming.stream().map(a -> (LinkedList<Integer>) a.clone())
				.collect(Collectors.toCollection(() -> new ArrayList<>(transitionProbs.size())));
		cloned.useRewards = useRewards;
		cloned.useTime = useTime;
		cloned.targetStates = (BitSet) targetStates.clone();
		cloned.initStates = (BitSet) initStates.clone();
		cloned.numStates = numStates;
		return cloned;
	}

	public BitSet getTargetStates() {
		return targetStates;
	}

	/**
	 * Returns function factory maintaining functions used in this object.
	 * 
	 * @return function factory maintaining functions
	 */
	FunctionFactory getFunctionFactory() {
		return functionFactory;
	}

	/**
	 * Adds a probabilistic transition.
	 * 
	 * @param from state transition starts from
	 * @param to   state transition leads to
	 * @param prob probability of transition
	 */
	void addTransition(int from, int to, Function prob) {
		ListIterator<Integer> toIter = getTransitionTargets().get(from).listIterator();
		ListIterator<Function> valIter = getTransitionProbs().get(from).listIterator();
		boolean alreadyThere = false;
		while (toIter.hasNext() && !alreadyThere) {
			int succ = toIter.next();
			Function succProb = valIter.next();
			if (succ == to) {
				valIter.set(succProb.add(prob));
				alreadyThere = true;
			}
		}
		if (!alreadyThere) {
			getTransitionTargets().get(from).add(to);
			getTransitionProbs().get(from).add(prob);
			getIncoming().get(to).add(from);
		}
	}

	/**
	 * Returns the probability of a given transition
	 * 
	 * @param from source state of transition
	 * @param to   target state of transition
	 * @return probability to move from given state to given state
	 */
	Function getTransProb(int from, int to) {
		Function prob = null;
		ListIterator<Integer> toIter = getTransitionTargets().get(from).listIterator();
		ListIterator<Function> valIter = getTransitionProbs().get(from).listIterator();
		while (toIter.hasNext()) {
			int succ = toIter.next();
			Function succProb = valIter.next();
			if (succ == to) {
				prob = succProb;
				break;
			}
		}

		if (prob == null) {
			prob = functionFactory.getZero();
		}

		return prob;
	}

	/**
	 * Returns probability of the self-loop in a given state.
	 * 
	 * @param state state to return self-loop probability of
	 * @return self-loop probability of given state
	 */
	Function getSelfLoopProb(int state) {
		Function loopProb = null;

		ListIterator<Integer> toIter = getTransitionTargets().get(state).listIterator();
		ListIterator<Function> valIter = getTransitionProbs().get(state).listIterator();
		while (toIter.hasNext()) {
			int to = toIter.next();
			Function val = valIter.next();
			if (to == state) {
				loopProb = val;
				break;
			}
		}
		if (loopProb == null) {
			loopProb = functionFactory.getZero();
		}

		return loopProb;
	}

	int getNumTransitions() {
		int c = 0;
		for (int i = 0; i < numStates; i++) {
			if (initStates.get(i)) {
				c += transitionProbs.get(i).size();
			} else if (!incoming.get(i).isEmpty()) {
				c += transitionProbs.get(i).size();
			}
		}
		return IntStream.range(0, numStates).filter(a -> initStates.get(a) || !incoming.get(a).isEmpty())
				.map(a -> transitionProbs.get(a).size()).sum();
	}

	public void printInitialStates() {
		printContainingStates(initStates, "initStates");
	}

	public void printTargetStates() {
		printContainingStates(targetStates, "targetStates");
	}

	public void printContainingStates(BitSet set, String name) {
		List<String> states = new ArrayList<>();
		for (int i = 0; i < numStates; i++) {
			if (set.get(i)) {
				states.add(String.valueOf(i));
			}
		}
		System.out.println(MessageFormat.format("{0}: [{1}]", name, String.join(",", states)));
	}

	/**
	 * Makes a given state absorbing. This means removing all leaving transitions
	 * and adding a self-loop with probability one.
	 * 
	 * @param state state to make absorbing
	 */
	void makeAbsorbing(int state) {
		LinkedList<Integer> loop = new LinkedList<Integer>();
		loop.add(state);
		LinkedList<Function> one = new LinkedList<Function>();
		one.add(functionFactory.getOne());
		getTransitionTargets().set(state, loop);
		getTransitionProbs().set(state, one);
	}

	/**
	 * Sets whether given state shall be an initial state
	 * 
	 * @param state       state which shall or shall not be an initial state
	 * @param targetState true iff given state shall be an initial state
	 */
	void setInitState(int state, boolean targetState) {
		initStates.set(state, targetState);
	}

	/**
	 * Sets whether given state shall be a target state
	 * 
	 * @param state       state which shall or shall not be a target state
	 * @param targetState true iff given state shall be a target state
	 */
	void setTargetState(int state, boolean targetState) {
		targetStates.set(state, targetState);
	}

	/**
	 * Set reward of a given state.
	 * 
	 * @param state  state to set reward of
	 * @param reward reward to set for given state
	 */
	void setReward(int state, Function reward) {
		rewards[state] = reward;
	}

	/**
	 * Set time of a given state.
	 * 
	 * @param state state to set reward of
	 * @param time  time to set for given state
	 */
	void setTime(int state, Function time) {
		times[state] = time;
	}

	/**
	 * Get time of a given state.
	 * 
	 * @param state state to get time of
	 * @return time of given state
	 */
	Function getTime(int state) {
		return times[state];
	}

	/**
	 * Checks whether given state is a target state.
	 * 
	 * @param state state to check whether it is a target state
	 * @return true iff given state is a target state
	 */
	public boolean isTargetState(int state) {
		return targetStates.get(state);
	}

	/**
	 * Checks whether there are some target states
	 * 
	 * @return true iff there are some target states
	 */
	public boolean hasTargetStates() {
		return targetStates.cardinality() != 0;
	}

	/**
	 * Checks whether given state is an initial state.
	 * 
	 * @param state state to check whether it is an initial state
	 * @return true iff given state is an initial state
	 */
	public boolean isInitState(int state) {
		return initStates.get(state);
	}

	/**
	 * Gets the reward of a given state.
	 * 
	 * @param state state to get reward of
	 * @return reward of given state
	 */
	Function getReward(int state) {
		return rewards[state];
	}

	/**
	 * Returns number of states.
	 * 
	 * @return number of states
	 */
	public int getNumStates() {
		return numStates;
	}

	/**
	 * Returns whether pmc uses rewards.
	 * 
	 * @return true iff rewards are used
	 */
	boolean isUseRewards() {
		return useRewards;
	}

	/**
	 * Returns whether pmc uses time.
	 * 
	 * @return true iff uses time
	 */
	boolean isUseTime() {
		return useTime;
	}

	public List<LinkedList<Function>> getTransitionProbs() {
		return transitionProbs;
	}

	public List<LinkedList<Integer>> getTransitionTargets() {
		return transitionTargets;
	}

	public List<LinkedList<Integer>> getIncoming() {
		return incoming;
	}

	/*
	 * @Override public String toString() { StringBuilder result = new
	 * StringBuilder();
	 * 
	 * for (int state = 0; state < numStates; state++) { result.append("state " +
	 * state); if (useRewards) { result.append(" " + rewards[state]); } if (useTime)
	 * { result.append(" " + times[state]); } result.append("\n"); }
	 * 
	 * return result.toString(); }
	 */
}
