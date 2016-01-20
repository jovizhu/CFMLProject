hasextendedquantity(X, Y) :- instanceof(A, X), hasquantity(A, Y).
hosts(X,Y) :- performedat(Y,X).
carbon(Obj, CF) :- hascflabel(Obj, CF).
carbon(Obj, CF) :- producedby(Obj, PS),
	hascflabel(PS, CFps),
	mflist(Obj, MFL),
	get_CF(MFL, CFmfl),
	CF is CFps + CFmfl.
get_CF([], 0).
get_CF([FirstMFL|LastMFL], CFmfl) :-
	hascflabel(FirstMFL, CFfirst),
	hasextendedquantity(FirstMFL, Qfirst),
	get_CF(LastMFL, CFLast),
	CFmfl is CFfirst * Qfirst + CFLast.
