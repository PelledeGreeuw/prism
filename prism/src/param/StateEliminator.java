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

import static param.elimination.BackwardOrder.collectStatesBackward;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import param.elimination.EliminationOrderIterator;
import param.elimination.benchmark.EliminationRunGroup;
import param.elimination.benchmark.EliminationStep;
import prism.PrismComponent;

/**
 * Performs computation of reachability probabilities and rewards. This class
 * handles the computation of unbounded reachability probabilities, expected
 * accumulated rewards and expected long-run average rewards for Markov chains.
 * To handle this computation, the different states of the model are
 * "eliminated", that is of the model are modified in such a way that the value
 * of concern (probability or reward) is maintained, but the state no longer has
 * any incoming transitions, except in some cases self loops. This way, after
 * all states have been treated, the value of concern can be obtained by a
 * simple computation.
 * 
 * @author Ernst Moritz Hahn <emhahn@cs.ox.ac.uk> (University of Oxford)
 */
final class StateEliminator {
	private static final Logger logger = LogManager.getLogger(StateEliminator.class);

	/**
	 * The order in which states shall be eliminated.
	 */
	enum EliminationOrder {
		/** arbitrary */
		ARBITRARY,
		/** states close to initial states first */
		FORWARD,
		/** states close to initial states last */
		FORWARD_REVERSED,
		/** states close to target states first */
		BACKWARD,
		/** states close to target states last */
		BACKWARD_REVERSED,
		/** random */
		RANDOM;
	}

	private EliminationOrderIterator eliminationOrder;

	/** the mutable parametric Markov chain to compute values of */
	private MutablePMC pmc;

	/** order in which states are eliminated */

	/**
	 * Create a new state eliminator object.
	 * 
	 * @param pmc              parametric Markov chain to compute values of
	 * @param eliminationOrder order in which states shall be eliminated
	 */
	StateEliminator(MutablePMC pmc, EliminationOrderIterator eliminationOrder) {
		this.pmc = pmc;
		this.eliminationOrder = eliminationOrder;
	}

	/**
	 * Performs precomputation before actual state elimination. This handles cases
	 * in which all or some states can or have to be treated differently, e.g.
	 * because there are no target states at all, or some states do never reach a
	 * target state.
	 * 
	 * @return true iff state elimination is necessary to obtain a result
	 */
	private boolean precompute() {
		/*
		 * if there are no target states, the result is zero everywhere for a
		 * reachability probability analysis, so all states can be made absorbing. If we
		 * are performing analysis of accumulated rewards, the value will be infinity
		 * everywhere.
		 */
		if (!pmc.isUseTime() && !pmc.hasTargetStates()) {
			for (int state = 0; state < pmc.getNumStates(); state++) {
				pmc.makeAbsorbing(state);
				if (pmc.isUseRewards()) {
					pmc.setReward(state, pmc.getFunctionFactory().getInf());
				}
			}
			return false;
		}

		/*
		 * search for states which might never reach a target state and thus have to be
		 * assigned a reward of infinity.
		 */
		if (pmc.isUseRewards()) {
			int[] backStatesArr = collectStatesBackward(pmc, true);
			HashSet<Integer> reaching = new HashSet<Integer>();
			for (int stateNr = 0; stateNr < backStatesArr.length; stateNr++) {
				reaching.add(backStatesArr[stateNr]);
			}
			for (int state = 0; state < pmc.getNumStates(); state++) {
				if (!pmc.isUseTime() && !reaching.contains(state)) {
					pmc.setReward(state, pmc.getFunctionFactory().getInf());
				}
			}
		}
		return true;
	}

	/**
	 * Eliminate all states of the model. The order of elimination is given by
	 * {@code eliminationOrder}.
	 */
	void eliminate() {
		if (!precompute()) {
			return;
		}
		while (eliminationOrder.hasNext()) {
			eliminate(eliminationOrder.next());
		}
	}
	
	int tryEliminate(long maxCalculations) {
		if (!precompute()) {
			return -1;
		}
		while (eliminationOrder.hasNext() && EliminationRunGroup.getInstance().getCurrentRun().getCalculations() < maxCalculations) {
			eliminate(eliminationOrder.next());
		}
		if (eliminationOrder.hasNext()) {
			return eliminationOrder.next();
		}
		return -1;
	}

	/**
	 * Stores a transition which shall be added to the model later.
	 */
	class NewTransition {
		/** source state of transition */
		final int fromState;
		/** target state of transition */
		final int toState;
		/** probability of transition */
		final Function prob;

		/**
		 * Creates a new transition to be added later on.
		 * 
		 * @param fromState source state of transition
		 * @param toState   target state of transition
		 * @param prob      probability of transition
		 */
		public NewTransition(int fromState, int toState, Function prob) {
			this.fromState = fromState;
			this.toState = toState;
			this.prob = prob;
		}

	}

	/**
	 * Eliminates a given state
	 * 
	 * @param midState state to eliminate
	 */
	private void eliminate(int midState) {
		logger.trace("Eliminating state {}", midState);
		Function loopProb = pmc.getSelfLoopProb(midState);
		/* states with only a self-loop require no further treatment */
		if (loopProb.equals(pmc.getFunctionFactory().getOne())) {
			if (EliminationRunGroup.getInstance().isRecordData()) {
				EliminationRunGroup.getInstance().getCurrentRun().addStep(
						new EliminationStep(EliminationRunGroup.getInstance().getCurrentRun().getSteps().size(), 
								midState, 0, pmc.getNumTransitions()));
			}
			return;
		}
		/* slStar = 1/(1-x), where x is the self-loop probability */
		Function slStar = loopProb.star();

		/*
		 * adapt rewards and time spent in state accordingly. The new values correspond
		 * to adding the expected reward/time obtained from moving to the midState from
		 * one of its predecessors, times the probability of moving.
		 */
		if (pmc.isUseRewards()) {
			pmc.setReward(midState, pmc.getReward(midState).multiply(slStar));
			for (int from : pmc.getIncoming().get(midState)) {
				if (from != midState) {
					pmc.setReward(from, pmc.getReward(from)
							.add(pmc.getTransProb(from, midState).multiply(pmc.getReward(midState))));
				}
			}
		}
		if (pmc.isUseTime()) {
			pmc.setTime(midState, pmc.getTime(midState).multiply(slStar));
			for (int from : pmc.getIncoming().get(midState)) {
				if (from != midState) {
					pmc.setTime(from,
							pmc.getTime(from).add(pmc.getTransProb(from, midState).multiply(pmc.getTime(midState))));
				}
			}
		}

		/*
		 * redirect transitions of predecessors of midState. Redirection is done such
		 * that some state fromState will have a probability of moving to a successor
		 * state toState of midState with probability (<fromState-to-midState-prob> *
		 * <midState-to-toState-prob) / (1-<self-loop-prob>). (If there already was a
		 * transition from fromState to toState, probabilities will be added up.). All
		 * transitions to midState will be removed.
		 */
		ArrayList<NewTransition> newTransitions = new ArrayList<NewTransition>();
		for (int fromState : pmc.getIncoming().get(midState)) {
			if (fromState != midState) {
				Function fromToMid = pmc.getTransProb(fromState, midState);
				ListIterator<Integer> toStateIter = pmc.getTransitionTargets().get(midState).listIterator();
				ListIterator<Function> toProbIter = pmc.getTransitionProbs().get(midState).listIterator();
				while (toStateIter.hasNext()) {
					int toState = toStateIter.next();
					Function midToTo = toProbIter.next();
					if (toState != midState) {
						Function fromToToAdd = fromToMid.multiply(slStar.multiply(midToTo));
						newTransitions.add(new NewTransition(fromState, toState, fromToToAdd));
					}
				}
			}
		}
		for (int fromState : pmc.getIncoming().get(midState)) {
			ListIterator<Integer> toStateIter = pmc.getTransitionTargets().get(fromState).listIterator();
			ListIterator<Function> toProbIter = pmc.getTransitionProbs().get(fromState).listIterator();
			while (toStateIter.hasNext()) {
				int state = toStateIter.next();
				toProbIter.next();
				if (state == midState) {
					toStateIter.remove();
					toProbIter.remove();
					break;
				}
			}
		}
		for (NewTransition newTransition : newTransitions) {
			pmc.addTransition(newTransition.fromState, newTransition.toState, newTransition.prob);
		}

		/*
		 * remove self loop from state and set outgoing probabilities to <out-prob> /
		 * (1-<self-loop-prob>). This corresponds to the probability to eventually
		 * leaving midState to a specific successor state, after executing any number of
		 * self loops.
		 */
		ListIterator<Integer> toStateIter = pmc.getTransitionTargets().get(midState).listIterator();
		ListIterator<Function> toProbIter = pmc.getTransitionProbs().get(midState).listIterator();
		while (toStateIter.hasNext()) {
			int toState = toStateIter.next();
			Function toProb = toProbIter.next();
			if (midState != toState) {
				toProbIter.set(slStar.multiply(toProb));
			}
		}
		toStateIter = pmc.getTransitionTargets().get(midState).listIterator();
		toProbIter = pmc.getTransitionProbs().get(midState).listIterator();
		while (toStateIter.hasNext()) {
			int toState = toStateIter.next();
			toProbIter.next();
			if (midState == toState) {
				toStateIter.remove();
				toProbIter.remove();
				break;
			}
		}
		pmc.getIncoming().get(midState).clear();
		if (EliminationRunGroup.getInstance().isRecordData()) {
			EliminationRunGroup.getInstance().getCurrentRun().addStep(
					new EliminationStep(EliminationRunGroup.getInstance().getCurrentRun().getSteps().size(), 
							midState, newTransitions.size(), pmc.getNumTransitions()));
		}
	}

	/**
	 * Obtain result for a given state. Before calling this method, all states must
	 * have been eliminated.
	 * 
	 * @param state state to obtain result for
	 * @return result for given state
	 */
	Function getResult(int state) {
		/*
		 * due to state elimination, at this point each state: A) either has only a
		 * self-loop, or B) has no self loop and only transitions to one or more states
		 * of the form A.
		 */

		if (pmc.isUseRewards() && !pmc.isUseTime()) {
			/*
			 * states which do not reach target states with probability one are assigned a
			 * reward of infinity. Target states have a reward of zero and only self-loops.
			 * Because of this, and from the state elimination (see above), we can read the
			 * reward directly from the according reward structure.
			 */

			return pmc.getReward(state);
		} else if (pmc.isUseRewards() && pmc.isUseTime()) {
			/*
			 * due to state elimination, each state either: A) has a self loop and or: B)
			 * does not have a self-loop and only transitions to states of the form A. The
			 * long-run average probability for states of the form A is then just
			 * reward(state) / time(state). For all states of both the form A and B, the
			 * long-run average is the probability to move to a state of form A times the
			 * long-run average value of that A state.
			 */
			ListIterator<Integer> toStateIter = pmc.getTransitionTargets().get(state).listIterator();
			ListIterator<Function> toProbIter = pmc.getTransitionProbs().get(state).listIterator();
			Function result = pmc.getFunctionFactory().getZero();
			while (toStateIter.hasNext()) {
				int toState = toStateIter.next();
				Function toProb = toProbIter.next();
				result = result.add(toProb.multiply(pmc.getReward(toState)).divide(pmc.getTime(toState)));
			}
			return result;
		} else {
			/*
			 * due to state elimination, each state either: A) has a self loop and then is a
			 * target state or cannot reach a target state at all, or: B) is not a target
			 * state or a state which cannot reach target states, and then does not have a
			 * self-loop and only transitions to states of the form A. Because of this, to
			 * obtain reachability probabilities, we just have to add up the one-step
			 * probabilities to target states.
			 */
			ListIterator<Integer> toStateIter = pmc.getTransitionTargets().get(state).listIterator();
			ListIterator<Function> toProbIter = pmc.getTransitionProbs().get(state).listIterator();
			Function result = pmc.getFunctionFactory().getZero();
			while (toStateIter.hasNext()) {
				int toState = toStateIter.next();
				Function toProb = toProbIter.next();
				if (pmc.isTargetState(toState)) {
					result = result.add(toProb);
				}
			}
			return result;
		}
	}
}
