:- use_module(library(jpl)).


get_cats :-
	jpl_call( 'Cats', getCats, ['tigers and bears'], C),
	(
		C == @(null)
	->	write( 'failed to getCats')
	;	write( 'Result: '), write( C), write( '\n' ) %writes out: "Cats! So many CATS! and " + (string sent in)
	).
:- get_cats.

get_tiger :-
	jpl_call( 'Cats', getTiger, [], X),
	(
		X == @(null)
	->	write( 'failed to getTiger')
	;	write( X ), assert(likes(sam, (X) )) %writes x out then asserts a fact that likes(sam, X)
	).
:- get_tiger.

%% FACTS
%likes(sam, cats).
%likes(sam,apples).
%likes(sam,games).
