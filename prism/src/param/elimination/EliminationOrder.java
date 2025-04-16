package param.elimination;

public enum EliminationOrder {
	/** arbitrary */
	BENCHMARK,
	ARBITRARY,
	/** states close to initial states first */
	FORWARD,
	/** states close to initial states last */
	FORWARD_REVERSED,
	/** states close to target states first */
	BACKWARD,
	/** states close to target states last */
	BACKWARD_REVERSED,
	TRANSITION_SUM,
	TRANSITION_MULT,
	SIMPLE_CYCLE_COUNT,
	FIXED_CYCLE_3,
	MIN_IN_OUT_DEGREE,
	HEURISTIC_1,
	HEURISTIC_2,
	HEURISTIC_3,
	HEURISTIC_4,
	/** random */
	RANDOM;
}
