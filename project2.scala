import scala.math._
import scala.util.Random
import akka.actor.Actor
import akka.actor.Props
import akka.actor._
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import scala.reflect._
import scala.concurrent.duration._ 

 //sealed trait Message //to ensure message are passed in a controlled fashion
     case class NodeInitialize(number:Int,nodes:List[ActorRef],neighbors:List[Int],tracker:ActorRef,stopRumor:Int,distribute:Int)
     case class TrackerInitialize(system:ActorSystem, numNodes:Int, stopRumor:Int, algo:String)
     case class UpdateGossip(id:Int,Counter:Int)
     case class UpdatePushSum(id:Int)
     case class SpreadRumorPushSum(s:Double,w:Double)

     sealed trait message
     case object SpreadRumorGossip extends message
     
     

    

     
object project2 {
 
	def main(args: Array[String]) : Unit = {
		println("object intitialized")
	val system = ActorSystem("MessageSpreading")
	  val tracker:ActorRef = system.actorOf(Props[Master])
      var nodes:List[ActorRef] = Nil
      var numNodes:Int = 0;
	  var topology:String = null
	  var algo:String = null
	  var stopRumor:Int = 0;
	  var distribute:Int = 0;
	  var pushsumlength = 10;
	  

	  if(args.length == 0 || args.length != 3){
      println("Invalid Arguments")
    }else if(args.length == 3){
		numNodes = args(0).toInt
		topology = args(1)
		algo = args(2)
		stopRumor = 10
		distribute = 2
    }
		
	  if(topology.equalsIgnoreCase("2D") || topology.equalsIgnoreCase("imp2D")) {
	    var temp:Int = numNodes
	     while(math.sqrt(temp.toDouble)%1!=0){
	    	 temp+=1
	     }
      	numNodes=temp
	  }	
	   val nodesPointer = math.sqrt(numNodes.toDouble);	
	  
	  
	  var i:Int = 0
		while(i<numNodes){
          nodes ::= system.actorOf(Props[Worker])
          i += 1
          }
			println("intitialized finished")
			
		
		

		if(topology.equalsIgnoreCase("full"))
		{
			println("topology check")
			var i:Int = 0
			while(i<nodes.length)
			{
				var neighbors:List[Int] = Nil   //initialising neighbors of each element 

				for(j <- 0 until nodes.length)
					{
						if(j!=i)
						{
							neighbors ::= j
						}
					}
					nodes(i) ! NodeInitialize(i,nodes,neighbors,tracker,stopRumor,distribute)
					i+=1;
			}
			
		}


		if(topology.equalsIgnoreCase("line")){
          var i:Int = 0;
          while(i<nodes.length){
            var neighbors:List[Int] = Nil
            
            if(i>0) neighbors ::= (i-1)
            if(i<nodes.length-1) neighbors ::= (i+1)
            
            nodes(i) ! NodeInitialize(i, nodes, neighbors, tracker, stopRumor, distribute)
            
            i += 1
          }
        }
		
		
		if(topology.equalsIgnoreCase("2D")){
          var i:Int = 0
          while(i<nodes.length){
        	 var neighbors :List[Int]= neighborList2D(i) 
            nodes(i) ! NodeInitialize(i, nodes, neighbors, tracker, stopRumor, distribute)
            
            i += 1
          }
        }
		
		 if(topology.equalsIgnoreCase("imp2D")){
          var i:Int = 0
          var neighbors:List[Int]=Nil
          while(i<nodes.length){
           neighbors = neighborListImp2D(i)
            nodes(i) ! NodeInitialize(i, nodes, neighbors, tracker, stopRumor, distribute)
            
            i += 1
          }
        }
		
	
		tracker ! TrackerInitialize(system,numNodes,stopRumor,algo)
		if(algo.equalsIgnoreCase("gossip"))
			{
				nodes(0) ! SpreadRumorGossip
			}
		else
			{
				if(numNodes<pushsumlength)
					{
						pushsumlength = numNodes
					}

					for(i <-0 until pushsumlength)
					{
						nodes(Random.nextInt(nodes.length)) ! SpreadRumorPushSum(0,1)
					}
			}
		
	def checkUpperNode (temp:Int, length:Int):Int = {
      if(temp.toDouble<math.sqrt(length.toDouble)) return 1
      return 0;
    }
		
    def checkLowerNode(temp:Int,length:Int):Int = {
      if(temp.toDouble>=(length.toDouble - math.sqrt(length.toDouble))) return 1
      return 0;
    }
    def checkLeftNode(temp:Int,length:Int):Int = {
      if(temp.toDouble % math.sqrt(length.toDouble)==0) return 1
      return 0;
    }
    def checkRightNode(temp:Int,length:Int):Int = {
      if((temp.toDouble+1) % math.sqrt(length.toDouble)==0) return 1
      return 0;
    }

    def neighborList2D(index:Int) : List[Int] = {
    		var neighbors:List[Int] = Nil
            //Initializing neighbours list here 
            if(checkLowerNode(index, nodes.length)==0){ 
              neighbors ::= (index+nodesPointer.toInt)
            }
            if(checkUpperNode(index, nodes.length)==0)
              {
              neighbors ::= (index-nodesPointer.toInt)
              }
            if(checkLeftNode(index, nodes.length)==0) 
              {
              neighbors ::= (index-1)
              }
            if(checkRightNode(index, nodes.length)==0) 
              {
              neighbors ::= (index+1)
              }
            return neighbors
      
    }
    
    def neighborListImp2D(index:Int) : List[Int] ={
			var neighbors:List[Int] = Nil
            var randomList:List[Int] = Nil
            
            if(checkLowerNode(index, nodes.length)==0) 
            { 
              neighbors ::= (index+nodesPointer.toInt); 
              randomList ::= (index+nodesPointer.toInt); 
             }
            if(checkUpperNode(index, nodes.length)==0) 
            { 
              neighbors ::= (index-nodesPointer.toInt); 
              randomList ::= (index-nodesPointer.toInt); 
            }
            if(checkLeftNode(index, nodes.length)==0)
            { 
              neighbors ::= (index-1);
              randomList ::= (index-1); 
             }
            if(checkRightNode(index, nodes.length)==0)
            { 
              neighbors ::= (index+1); 
              randomList ::= (index+1); 
            }
            
            var temp:Int = -1;
            do{
              temp = Random.nextInt(nodes.length)
              for(k<-randomList){
                if(temp == k) temp = -1
              }   
            }while(temp == -1)
            
            neighbors ::= (temp)
            return neighbors
      
    }

        

		
	}	
			
}
		class Master extends Actor
		{

			var localStopRumorGossip:Int = 0
			var time:Long = 0
			var localNumNodes = 0
			var localSys:ActorSystem = null
			var tracking:List[UpdateGossip] = Nil
			time = System.currentTimeMillis


			def receive = 
				{
					case TrackerInitialize(system:ActorSystem, numNodes:Int, stopRumor:Int, algo:String) =>
					{
					time = System.currentTimeMillis
					localSys = system;
					localNumNodes = numNodes;
					localStopRumorGossip = stopRumor;
					//if(algo.equalsIgnoreCase("gossip")) shouldWork = true;
					}


					case UpdateGossip(id:Int, counter:Int) => 
					{
					var temp:List[UpdateGossip] = Nil
							var i:Int = 0
							while(i<tracking.length){
            
								if(tracking(i).id!=id) 
								{
									temp ::= tracking(i)
								} 
								i += 1
							}
					tracking = temp
					tracking = tracking ::: List(new UpdateGossip(id:Int, counter:Int))
          
          
					if((tracking.length.toDouble/localNumNodes.toDouble)>0.90){
						localSys.shutdown;
					}
					}

					case UpdatePushSum(id:Int) =>
						{
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
			var localStopRumorGossip:Int = 0
			var localStopRumorPushSum:Double = math.pow(10, -10)
			var counter:Int=0
			var localNodes:List[ActorRef] = Nil
			var localNeighbors:List[Int] = Nil
			var localTracker:ActorRef = null
			var dist:Int = 0
			var random:Int = 0
			var oldRatio:Double = 0
			var newRatio:Double = 0
      var localSum:Double=0
      var localWeight:Double=0
      var stabaliser:Int = 0



    
			 def stabilityCheck(oldRatio:Double,newRatio:Double,localSum:Double,localWeight:Double)
			 	{
			 		if(counter==1 || Math.abs((oldRatio-newRatio))>localStopRumorPushSum) {
        	  stabaliser=0;
        	  
	          for(c<- 1 until dist)
	          {
	            localNodes(localNeighbors(Random.nextInt(localNeighbors.length))) ! SpreadRumorPushSum(localSum,localWeight)
	          }

          }
          else
          {
            stabaliser+=1;
            if(stabaliser>3) {
              
              
              localTracker ! UpdatePushSum(id); 
	            self ! PoisonPill
	        }
	        else{
	          for(c<- 1 until dist){
	            
	            
	            localNodes(localNeighbors(Random.nextInt(localNeighbors.length))) ! SpreadRumorPushSum(localSum,localWeight)
	          }
	        }
          }
			 	}
			
			
		def receive = 
			{
				case NodeInitialize(number:Int,nodes:List[ActorRef],neighbors:List[Int],tracker:ActorRef,stopRumor:Int,distribute:Int) => 
					{
						localNeighbors = localNeighbors ::: neighbors
						localNodes = nodes
						id = number
						localTracker = tracker
						localStopRumorGossip = stopRumor
						dist = distribute
						
					}

					case SpreadRumorGossip =>
					{
						if(counter<localStopRumorGossip)
							{
								counter += 1
								localTracker ! UpdateGossip(id,counter)

								for(i <- 0 until dist)
									{
										random = Random.nextInt(localNeighbors.length)
										localNodes(localNeighbors(random)) ! SpreadRumorGossip
									}
							}

							
					}

					case SpreadRumorPushSum(s:Double,w:Double) =>
						counter +=1
						dist = 2
						oldRatio = s/w;
						localSum = localSum + s;
						localWeight = localWeight +w;
						newRatio = localSum/localWeight;


						stabilityCheck(oldRatio,newRatio,localSum,localWeight)

			 }

		}

