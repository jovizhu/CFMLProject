
%product related code
producedBy(wayrad_computer_9000, computer_production). 

%made from code
mfList(wayrad_computer_9000, [  asusmotherboard, seagatehdd, areallycoolcase, hyperionram, intelprocessor]).

%quantity code
hasQuantity( wayrad_computer_9000, asusmotherboard, 1).
hasQuantity( wayrad_computer_9000, seagatehdd, 2).
hasQuantity( wayrad_computer_9000, areallycoolcase, 1).
hasQuantity( wayrad_computer_9000, hyperionram, 4).
hasQuantity( wayrad_computer_9000, intelprocessor, 1).

%cf label code
hasCFLabel( asusmotherboard, 43).
hasCFLabel( seagatehdd, 123).
hasCFLabel( areallycoolcase, 999).
hasCFLabel( hyperionram, 23).
hasCFLabel( intelprocessor, 12).

%Service Related Code
service( delivery).
service( analytics).
service( orderprocessing).
service( scheduling).
service( inventorychecking).
service( manufacturing).

%uses code
uses( delivery, []).
uses( analytics, []).
uses( orderprocessing, []).
uses( scheduling, [ gamma_ray_ionizer,computationalmachine]).
uses( inventorychecking, []).
uses( manufacturing, [ gamma_ray_ionizer,computationalmachine]).

%perform at code
performedAt( delivery, [ storagebuilding]).
performedAt( analytics, [ officebuilding]).
performedAt( orderprocessing, [ officebuilding]).
performedAt( scheduling, [ officebuilding]).
performedAt( inventorychecking, [ storagebuilding,officebuilding]).
performedAt( manufacturing, [ factory]).

%micro cfd code
hasMicroCFD( delivery, microcfd).
hasMicroCFD( analytics, microcfd).
hasMicroCFD( orderprocessing, microcfd).
hasMicroCFD( scheduling, microcfd).
hasMicroCFD( inventorychecking, microcfd).
hasMicroCFD( manufacturing, microcfd).

%Micro Service Related Code
microService( minventorychecking).
microService( mdelivery).
microService( morderprocessing).
microService( manalytics).
microService( mmanufacturing).
microService( mscheduling).
microService( computer_production).

%has Holistic Service Code
hasHolisticService( minventorychecking, inventorychecking).
hasHolisticService( mdelivery, delivery).
hasHolisticService( morderprocessing, orderprocessing).
hasHolisticService( manalytics, analytics).
hasHolisticService( mmanufacturing, manufacturing).
hasHolisticService( mscheduling, scheduling).
facility( storagebuilding).
facility( officebuilding).
facility( factory).

%facility code
hasCFD( storagebuilding, lemss).
hasCFD( officebuilding, lemss).
hasCFD( factory, lemss).
equipment( gamma_ray_ionizer).
equipment( computationalmachine).
hasCFD( gamma_ray_ionizer, lemss).
hasCFD( computationalmachine, lemss).
 
%workflow node
mServiceList( computer_production, [ morderprocessing,mscheduling,manalytics,minventorychecking,mdelivery,mmanufacturing,mdelivery]).


%*****************************
%***********rules**************

carbon(Obj, CF) :- hasCFLabel(Obj, CF).

carbon(Obj, CF) :-
	producedBy(Obj, PS),
	get_MicroService_CF(PS, CFmsl),
	get_MadeFrom_CF(Obj, CFmfl),
	CF is CFmsl + CFmfl.

get_MadeFrom_CF(Obj, CFmfl) :-
	mfList(Obj, MFL),
	sum_MadeFrom_CF(Obj, MFL, CFmfl).

sum_MadeFrom_CF(_, [], 0).

sum_MadeFrom_CF(Obj, [FirstMFL|LastMFL], CFmfl) :-
	carbon(FirstMFL, CFfirst),
	hasQuantity(Obj, FirstMFL, Qfirst),
	sum_MadeFrom_CF(Obj, LastMFL, CFLast),
	CFmfl is CFfirst * Qfirst + CFLast.

get_MicroService_CF(MService, CFmsl):-
	write( 'get_MicroService_CF: '), write(MService), write(CFmsl), write( '\n' ),
	mServiceList(MService, MSL),
	sum_MicroService_CF(MSL, CFmsl).

% get_CF for micro serivce list
sum_MicroService_CF([], 0).
sum_MicroService_CF([FirstMSL|LastMSL], CFmsl) :-
    write( 'sum_MicroService_CF: '), write(FirstMSL), write(LastMSL), write(CFmsl), write( '\n' ),
	get_AtomicMicroService_CF(FirstMSL, CFfirst),
	sum_MicroService_CF(LastMSL, CFLast),
	CFmsl is CFfirst + CFLast.

% get_CF for micro service
get_AtomicMicroService_CF(MService, CFms) :-
	write( 'get_AtomicMicroService_CF: '), write(MService), write(CFms), write( '\n' ),
	hasHolisticService(MService, Service),
	get_Service_CF(Service, ServiceCF),
	hasMicroCFD(Service, MicroCFD),
	calAtomicMicroSerivceCFLabel(Service,ServiceCF, MService,MicroCFD,CFms).

get_AtomicMicroService_CF(MService, CFms) :-
	write( 'get_AtomicMicroService_CF: '), write(MService), write(CFms), write( '\n' ),
	microService(MService),
	hasCFLabel(MService, CFms).
	
get_Service_CF(Service, CFservice) :-
	write( 'get_Service_CF: '), write(Service), write(CFservice), write( '\n' ),
	service(Service),
	hasCFLabel(Service, CFservice).

get_Service_CF(Service, CFservice) :-
	write( 'get_Service_CF: '), write(Service), write(CFservice), write( '\n' ),
	service(Service),
	performedAt(Service, FacilityList),
	sum_Service_CF_from_Facility(FacilityList, Service, CFservicefromFacility),
	uses(Service, EquipmentList),
	sum_Service_CF_from_Equipment(EquipmentList, Service, CFServicefromEquipment),
	CFservice is CFservicefromFacility + CFServicefromEquipment.
	
% sum_Service_CF_from_Facility for facility list
sum_Service_CF_from_Facility([], _,0).

sum_Service_CF_from_Facility([FirstFacility|LastFacilityList], Service, CFservice) :-
    write( 'sum_Service_CF_from_Facility: '), write(FirstFacility), write(LastFacilityList), write(Service), write(CFservice), write( '\n' ),
	get_Service_CF_from_Facility(FirstFacility, Service, CFfirst),
	sum_Service_CF_from_Facility(LastFacilityList, Service, CFLast),
	CFservice is CFfirst + CFLast.
	
% sum_Service_CF_from_Facility for equipment list
sum_Service_CF_from_Equipment([], _,0).

sum_Service_CF_from_Equipment([FirstEquipment|LastEquipmentList], Service, CFservice) :-
    write( 'sum_Service_CF_from_Equipment: '), write(FirstEquipment), write(LastEquipmentList), write(Service), write(CFservice), write( '\n' ),
	get_Service_CF_from_Equipment(FirstEquipment, Service, CFfirst),
	sum_Service_CF_from_Equipment(LastEquipmentList, Service, CFLast),
	CFservice is CFfirst + CFLast.
	
get_Service_CF_from_Facility(Facility, Service, CFservice) :-
	write( 'get_Service_CF_from_Facility: '), write(Facility), write(Service), write(CFservice), write( '\n' ),
	hasCFD(Facility, CFD),
	calAtomicServiceCFfromFacility(Service, Facility, CFD, CFservice).
	
get_Service_CF_from_Equipment(Equipment, Service, CFservice) :-
	write( 'get_Service_CF_from_Equipment: '), write(Equipment), write(Service), write(CFservice), write( '\n' ),
	hasCFD(Equipment, CFD),
	calAtomicServiceCFfromEquipment(Service, Equipment, CFD, CFservice).
	
calAtomicMicroSerivceCFLabel(Service, CFservice, MService, MicroCFD, CFms) :-
	write( 'calAtomicMicroSerivceCFLabel: '), write(Service), write(CFservice), write(MService), write(MicroCFD), write( '\n' ),
	jpl_call( 'MicroCFD', getMCFD, [Service, CFservice, MicroCFD, MService], CFms).
	
calAtomicServiceCFfromFacility(Service, Facility, CFD, CFservice) :-
	write( 'calAtomicServiceCFfromFacility: '), write(Service), write(Facility), write(CFD), write(CFservice), write( '\n' ),
	jpl_call('FacilityCFD', getServiceCF, [Facility, CFD, Service], CFservice).
	
calAtomicServiceCFfromEquipment(Service, Equipment, CFD, CFservice) :-
	write( 'calAtomicServiceCFfromEquipment: '), write(Service), write(Equipment), write(CFD), write(CFservice), write( '\n' ),
	jpl_call('EquipmentCFD', getServiceCF, [Equipment, CFD, Service], CFservice).
