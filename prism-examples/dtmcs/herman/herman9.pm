dtmc

formula num_tokens = (x1=x2?1:0)+(x2=x3?1:0)+(x3=x4?1:0)+(x4=x5?1:0)+(x5=x6?1:0)+(x6=x7?1:0)+(x7=x8?1:0)+(x8=x9?1:0)+(x9=x1?1:0);

label "stable" = (((x1=x2?1:0)+(x2=x3?1:0)+(x3=x4?1:0)+(x4=x5?1:0)+(x5=x6?1:0)+(x6=x7?1:0)+(x7=x8?1:0)+(x8=x9?1:0)+(x9=x1?1:0)))=1;

const double p = 0.5;

module process1

	x1 : [0..1];

	[step] (x1=x9) -> p : (x1'=0) + 1-p : (x1'=1);
	[step] !(x1=x9) -> (x1'=x9);

endmodule

module process2

	x2 : [0..1];

	[step] (x2=x1) -> p : (x2'=0) + 1-p : (x2'=1);
	[step] !(x2=x1) -> (x2'=x1);

endmodule

module process3

	x3 : [0..1];

	[step] (x3=x2) -> p : (x3'=0) + 1-p : (x3'=1);
	[step] !(x3=x2) -> (x3'=x2);

endmodule

module process4

	x4 : [0..1];

	[step] (x4=x3) -> p : (x4'=0) + 1-p : (x4'=1);
	[step] !(x4=x3) -> (x4'=x3);

endmodule

module process5

	x5 : [0..1];

	[step] (x5=x4) -> p : (x5'=0) + 1-p : (x5'=1);
	[step] !(x5=x4) -> (x5'=x4);

endmodule

module process6

	x6 : [0..1];

	[step] (x6=x5) -> p : (x6'=0) + 1-p : (x6'=1);
	[step] !(x6=x5) -> (x6'=x5);

endmodule

module process7

	x7 : [0..1];

	[step] (x7=x6) -> p : (x7'=0) + 1-p : (x7'=1);
	[step] !(x7=x6) -> (x7'=x6);

endmodule

module process8

	x8 : [0..1];

	[step] (x8=x7) -> p : (x8'=0) + 1-p : (x8'=1);
	[step] !(x8=x7) -> (x8'=x7);

endmodule

module process9

	x9 : [0..1];

	[step] (x9=x8) -> p : (x9'=0) + 1-p : (x9'=1);
	[step] !(x9=x8) -> (x9'=x8);

endmodule

rewards "steps" 

	true : 1;

endrewards

init true endinit
