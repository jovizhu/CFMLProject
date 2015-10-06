# CFMLProject
Simplified implementation for a CFL-Ontology Model for Carbon Footprint Reasoning

# Realted Papers
Toward Ontology and Service Paradigm for Enhanced Carbon Footprint Management and Labeling
  http://ieeexplore.ieee.org/xpls/abs_all.jsp?arnumber=6649591
A CFL-ontology model for carbon footprint reasoning
  http://ieeexplore.ieee.org/xpls/abs_all.jsp?arnumber=7050810

##Things to note for the program

Main program is in CFMLReasoning.java

Service individuals names (owl file) need to match up with the name in their workflow (bpmn2 file)

The parts that the computer (or any other product) are `madeFrom` need to have `quantity`, `CFLabel`, and `name` as string data properties.
For example in the facility2.owl, we have CPU as a subclass of computer. For CPU we have an individual called `intelProcessor` with `name` of `i7000`, `CFlabel` of `12`, and `quantity` of `1`

Microservices (mService) also requires a CFLabel as a data property with a string data type.
In our example we have `Manufacturing` as a `mService` with `CFLabel` of `4343`


###known problems:
- duplication of processes that are children of two separate nodes (I.E. Two services have a shared child service). this is not a huge problem since prolog ignores duplications anyways.
- potentially some issues with workflow interpretation. Only a few simple cases were tested.
- prolog is just in the terminal, it's not saved to a seprate file or ran.
