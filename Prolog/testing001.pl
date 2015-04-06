service(orderprocessing).
service(scheduling).
service(inventorychecking).
service(delivery).
microservice(manufacturing).
uses(gamma_ray_ionizer, scheduling).
uses(computationalmachine, scheduling).
uses(gamma_ray_ionizer, manufacturing).
uses(computationalmachine, manufacturing).
hasscf(scf, orderprocessing).
hasscf(scf, scheduling).
hasscf(scf, inventorychecking).
hasscf(scf, manufacturing).
hasscf(scf, delivery).
mflist( wayrad_computer_9000, [asusmotherboard, seagatehdd, areallycoolcase, hyperionram, intelprocessor]).
madefrom(asusmotherboard, wayrad_computer_9000).
madefrom(seagatehdd, wayrad_computer_9000).
madefrom(areallycoolcase, wayrad_computer_9000).
madefrom(hyperionram, wayrad_computer_9000).
madefrom(intelprocessor, wayrad_computer_9000).
hascflabel(manufacturing, 4343).
hascflabel(asusmotherboard, 43).
hascflabel(seagatehdd, 123).
hascflabel(areallycoolcase, 999).
hascflabel(hyperionram, 23).
hascflabel(intelprocessor, 12).
instanceof(mommyboard, asusmotherboard).
instanceof(superhdd, seagatehdd).
instanceof(seriouslyitsawesome, areallycoolcase).
instanceof(gottagofast, hyperionram).
instanceof(i7000, intelprocessor).
hasquantity(mommyboard, 1).
hasquantity(superhdd, 2).
hasquantity(seriouslyitsawesome, 1).
hasquantity(gottagofast, 4).
hasquantity(i7000, 1).
performedat(officebuilding, orderprocessing).
performedat(officebuilding, scheduling).
performedat(storagebuilding, inventorychecking).
performedat(officebuilding, inventorychecking).
performedat(factory, manufacturing).
performedat(storagebuilding, delivery).
hascfd(lemss, officebuilding). 
hascfd(lemss, officebuilding). 
hascfd(lemss, storagebuilding). 
hascfd(lemss, officebuilding). 
hascfd(lemss, factory). 
hascfd(lemss, storagebuilding). 
producedby(wayrad_computer_9000, manufacturing).

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
