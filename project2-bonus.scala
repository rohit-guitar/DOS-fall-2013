package slash

import scala.math._
import scala.util.Random
import akka.actor.Actor
import akka.actor.Terminated
import akka.actor.Props
import akka.actor._
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import scala.reflect._
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy._
import akka.util.duration._
import scala.collection.immutable.HashMap



 //sealed trait Message //to ensure message are passed in a controlled fashion
     case class NodeInitialize(number:Int,nodes:List[ActorRef],neighbors:List[Int],tracker:ActorRef,stopRumor:Int,distribute:Int)
     case class TrackerInitialize(system:ActorSystem, numNodes:Int, stopRumor:Int, algo:String)
     case class Update(id:Int,Counter:Int)
     case class PushSumalgo(sum:Double, weight:Double)
     case class PushSumUpdate(number:Int)
     case class initWorker(numNodes:Int,topo:String,algo:String) extends message
     case class RegisterWorker(val worker: ActorRef, val supervisor: ActorRef)
     case class DeadWorker()
     case class init(k:Int)


     sealed trait message
     case object SpreadRumor extends message
     

    

     
object project2bonus {
 
	def main(args: Array[String]) : Unit = {
		
		println("object intitialized")
		val system = ActorSystem("MessageSpreading")
		var numNodes:Int = 0;
		var topology:String = null
		var algo:String = null
		var stopRumor:Int = 0;
		var distribute:Int = 0;
	  
			if(args.length == 0 || args.length != 3){
					println("Arguments are not proper.")
			}
			else if(args.length == 3){
					numNodes = args(0).toInt
					topology = args(1)
					algo = args(2)
					stopRumor = 10
					distribute = 2
			}
			
	val supervisor = system.actorOf(Props[SupervisorActor], name = "supervisor")
			supervisor ! initWorker(numNodes,topology,algo)
			
	}	
			
}
		class Master extends Actor
		{

			var localStopRumor:Int = 0
			var time:Long = 0
			var localNumNodes = 0
			var localSys:ActorSystem = null
			var tracking:List[Update] = Nil
			time = System.currentTimeMillis


			def receive = 
				{
					case TrackerInitialize(system:ActorSystem, numNodes:Int, stopRumor:Int, algo:String) =>
					{
					time = System.currentTimeMillis
					localSys = system;
					localNumNodes = numNodes;
					localStopRumor = stopRumor;
					//if(algo.equalsIgnoreCase("gossip")) shouldWork = true;
					}


					case Update(id:Int, counter:Int) => 
					{
					var temp:List[Update] = Nil
							var i:Int = 0
							while(i<tracking.length){
            
								if(tracking(i).id!=id) 
								{
									temp ::= tracking(i)
								} 
								i += 1
							}
					tracking = temp
					tracking = tracking ::: List(new Update(id:Int, counter:Int))
          
          
					if((tracking.length.toDouble/localNumNodes.toDouble)>0.90){
						localSys.shutdown;
					}
					}
				
			
					case PushSumUpdate(number:Int) => {
						localSys.shutdown;
					}
				}
					
					override def postStop(){
					  time = System.currentTimeMillis-time
								println("\nTotal time = "+time)
					  
					}
				
			
		}


		class Worker extends Actor
		{

			var id:Int = 0
			var localStopRumor:Int = 0
			var counter:Int=0
			var localNodes:List[ActorRef] = Nil
			var localNeighbors:List[Int] = Nil
			var localTracker:ActorRef = null
			var dist:Int = 0
			var random:Int = 0
			var PushSumCounter:Int = 0;

			var s:Double=0;
			var w:Double=0;
			
			def getId():Int = {return id}
			
		def receive = 
			{
				case NodeInitialize(number:Int,nodes:List[ActorRef],neighbors:List[Int],tracker:ActorRef,stopRumor:Int,distribute:Int) => 
					{
						localNeighbors = localNeighbors ::: neighbors
						localNodes = nodes
						id = number
						localTracker = tracker
						localStopRumor = stopRumor
						dist = distribute
						
						s=number
						
					}

					case SpreadRumor =>
					{
						if(counter==2){     
						  counter+=1;
						  var temp= new index()
						  temp.init(id)
						   context.stop(self)
						    println("here")						  	
						}
					  if(counter<localStopRumor)
							{
								counter += 1
								localTracker ! Update(id,counter)

								for(i <- 0 until dist)
									{
										random = Random.nextInt(localNeighbors.length)
										localNodes(localNeighbors(random)) ! SpreadRumor
									}
							}

							
					}
					
					case PushSumalgo(sum:Double, weight:Double) => {
							counter += 1;
							dist = 2
							var oldTemp:Double = s/w;
							s+=sum;
							w+=weight;
							s = s/dist;
							w = w/dist;
							var newTemp:Double = s/w;

	    
							if(counter==1 || Math.abs((oldTemp-newTemp))>localStopRumor) {
									PushSumCounter=0;
						
									for(i<- 1 until dist){
											var randomPlayer = Random.nextInt(localNeighbors.length);
											localNodes(localNeighbors(randomPlayer)) ! PushSumalgo(s,w)
									}

							}
				
							else{
								PushSumCounter+=1;
								
								if(PushSumCounter>3) {
									localTracker ! PushSumUpdate(id); 
									self ! PoisonPill
								}
								
								else{
									for(i<- 1 until dist){
										var randomPlayer = Random.nextInt(localNeighbors.length);
										localNodes(localNeighbors(randomPlayer)) ! PushSumalgo(s,w)
									}
								}
							}
					}
			}

}	

		class SupervisorActor extends Actor with ActorLogging {
				var system= ActorSystem("BonusProblem");
				var nodes:List[ActorRef] = Nil
				val tracker:ActorRef = system.actorOf(Props[Master])
				val monitor = context.system.actorOf(Props[MonitorActor], name = "monitor")

				val stopRumor:Int=10
				val distribute:Int=2
				
				def makeNeighborList(index:Int):List[Int] ={
				  var neighbors:List[Int] = Nil   //initialising neighbors of each element 
						for(j <- 0 until nodes.length)
						{
							if(j!=index)
							{
							neighbors ::= j
							}
						}
				  return neighbors
				}
				
				def initialize(numNodes:Int,topology:String,algo:String) ={
						var i:Int = 0
						while(i<numNodes){
							nodes ::= system.actorOf(Props[Worker])
							i += 1
						}
			
				println("intitialized finished")
				println("neighbor list starts here")
				if(topology.equalsIgnoreCase("full"))
				{
					var i:Int = 0
					var neighbors:List[Int] = Nil
					while(i<nodes.length)
					{
						   //initialising neighbors of each element 
						neighbors=makeNeighborList(i)
						nodes(i) ! NodeInitialize(i,nodes,neighbors,tracker,stopRumor,distribute)
						monitor ! new RegisterWorker(nodes(i), self) 
						i+=1;
					}
					println("topology built --------->")
					
					tracker ! TrackerInitialize(system,numNodes,stopRumor,algo)
					if(algo.equalsIgnoreCase("gossip")){
							nodes(0) ! SpreadRumor
						}
					}
				
				
				
				}
				
				
  
  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 10 seconds) {

    case _: ArithmeticException => Resume
    case _: IllegalArgumentException => Stop
    
  }
  
  
  def receive = {
    case initWorker(numNodes:Int,topology:String,algo:String) =>initialize(numNodes,topology,algo) 
    case DeadWorker() =>
      log.info("Got a DeadWorker message, restarting the worker")
      println("actor restart")

      var y= new index()
      var temp1= system.actorOf(Props[Worker])
      var neighbors:List[Int] = makeNeighborList(y.get)
      temp1 ! NodeInitialize(y.get,nodes,neighbors,tracker,stopRumor,distribute)
	  monitor ! new RegisterWorker(temp1, self)
	}
}

		
			
class MonitorActor extends Actor with ActorLogging {

  var monitoredActors = new HashMap[ActorRef, ActorRef]

  def receive: Receive = {
    case t: Terminated =>
      	println("actor terminate new")
      if (monitoredActors.contains(t.actor)) {
        log.info("Received Worker Actor Termination Message -> "
          + t.actor.path)
        log.info("Sending message to Supervisor")
        val value: Option[ActorRef] = monitoredActors.get(t.actor)
        value.get ! new DeadWorker()
      }

    case msg: RegisterWorker =>
       println("actor registered")
      context.watch(msg.worker)
      monitoredActors += msg.worker -> msg.supervisor
  }
}		
		
class index {
  var temp:Int =0
  def init(k:Int)= temp=k
  def get() :Int= return temp
 }
		
	
