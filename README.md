# lagom-multiclient

Project to investigate issues for lagom for working a multi-tenant environment in a collection of lagom services.

Another objective is to work with a large "model" which comprises data that must be held for multiple entities. This is to simulate the effect of Optrak's Vehicle Routing Problem (VRP) model.

The general multi-tenant issues are explored in the products, orders and tenants services. The model is a simple one of a tenant having many product lines and orders which reference those lines.

The VRP model requirements are investigated in the model service. Here we have a number of features:

### "VRP" Model

The structure we are simulating with the model services has the following characteristics:

* The model is collection of orders from the tentants order set.
* Each tenant can have zero or more models.
* Models are independent and can have overlapping sets of orders.
* To simulate the VRP activities we create a number of commands to the model. These commands involve (fake) non-trivial comutation.

#### Model Commands

Commands will take an arbitrary length of time. In the real VRP system commands could invoke optimisation services and call out to additional services for geocoding and time/distance matrix calculations. Even actions that are calculated internally, such as data updates often require complex internal recalculations (the scheduling operation involves solving a multi-threaded dynamic program and adding orders to a load may involve recomputing a 3-d packing problem)

 Some of these operations are non-deterministic as the solution techniques use randomising metaheuristics or merely parallel solution techniques where alternative solutions may some up first.

 Some model commands relate to requests from the UI to do something about the future plan. This may be rejected if it breaks rules. We call these **Change Requests**. A Change Request can take a long time to recompute - several minutes for an optimisation, for example.

 Other model commands relate to things which are **Concrete Commands** - the customer has changed this order, this vehicle has reached this customer 30 minutes late, etc. Concrete Commands can usually be processed in an elapsed time of 100ms

 The model is subject to a high read-to-update ratio. The UI is a rich environment comprising maps and drill-down activity. It therefore makes sense to have a separate read model.

 The model is large - typically 1mb or so of data - internally an acyclic directed multi-graph of activities with all the associated input data such as orders, vehicles and drivers. Serializing it and passing it around a node cluster would be inefficient and costly. But much of the model is needed to process even a simple Concete Command (a 30 minute delay at a plant could affect all subsequent vehicles visiting that plant). Consequently it is highly desireable that any processing take place within the same JVM so that it is practical to pass immutable model objects to calculation processes.

The above has serveral consequences:

**Events cannot perform computations** We cannot carry out non-deterministic operations as part of the Event processsing, it would be both too time-consuming and would lead to non-deterministic data model recreation.

**We cannot process in the PersistentEntity** The long-running nature of the calculations mean that they do not fit in the standard Command->Event transformation that is typically the workload of the PersistentEntity. This would prevent any parallel computation and potentially cause timeouts (current default timeout is apparently around 5 seconds)*[]:

**Treat Commands Separately** The 2 types of commands - Change Requests and Concrete Commands must be treated differently. A Concrete Command must be dealt with immediately and as a consequence any pending Change Requests may become invalidated if there are conflicts.

**Maintain 2 Queues** We can deal with the commands by managing 2 queues, one for each type of command. As each command comes in it is added to the relevant input queue for the type of command. We don't know enough to attempt to prioritise Concrete Commands and the calculation is relatively fast. For Change Requests, first come, first served is an acceptable model. Generally there will be a single user or at most a small team, so it is reasonable to queue the requests. There will be some pre-processing of the queuing items - any independent calculations (such as requests for time-distance data) can be initiated immediately, but anything else waits for its turn (as the input data for the calculation will probably change).

 **Merging Required** It is highly likely that by the time the results of the Change Request are returned, the underlying model may have been updated by a Concrete Command. This is analagous to events in a source code control system - where emergency patches have taken place while some other longer term development was taking place.

 ### Entities
 Lagom provides only one mechanism for having an object that could reflect the model - which is the PersistentEntity

 ### Overall Approach

 The service commands will not be **Commands** in the PersistentEntity sense. Instead they will pre-processed. The PersistentEntity will farm out any input Concrete Commands and Change Requests to in-JVM Actors that will carry out the queue management and initial processing, with the PersistentEntity simply returning an "Accepted" result. These actors will eventually send the PersistentEntity Commands that contain all the calculation results. The PersistentEntity will then transform these trivially into Events that can be applied to update the internal model.

 At the same time, all events will be sent pushed out on a Kafka stream where they will be picked up by ReadModel entities - one per model. These will be managed as PersistentEntities themselves where the "Commands" are shallow wrappers around the Events that will update the model - thus allowing model code to be shared with the write-side model.

  Exactly how this can be achieved is the primary investigation of this project!







