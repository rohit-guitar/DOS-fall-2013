import akka.actor._
import akka.routing.RoundRobinRouter //to distribute the work among the workers
import scala.concurrent.duration._    //to calculate the time of exucution in millis
import java.util._    //to calculate the sqaure root

 object project  {
      var limit:Int = _   //first argument
      var length:Int = _   //second argument
      //var parts:Int = _
      var counter: Int = _    //to count the number of feasable sequence
      
    def main(args: Array[String]) 
    {
    	//println("in main")
      limit=Integer.parseInt(args(0))   //first argument limit
      length=Integer.parseInt(args(1))  //second argument length of sequence
      //parts= limit - length+1 

      start(15000,limit,nrOfMessages=limit,length)     //no of workers set to 15,000 caculated using hit and trial until a break even was seen
    }

    

		

    sealed trait Message //to ensure message are passed in a controlled fashion
    case object Calculate extends Message      //to ask master to order the workers for caluculation
    case class Work(startNum:Int,endNum:Int) extends Message   //to ask workers to do calculation
    case class Result(answer: Int) extends Message				//to send back the result 
    case class PrintPerfectSquares(counter: Int, time: Duration) extends Message  //to ask printer to print the result

    class Worker extends Actor
    {
    	var sum : Int = 0
    	def doCalculation(startNum:Int, endNum:Int):Int = 
    	{
    		
    		sum = 0
    		//main logic 
    		for(i <- startNum until endNum+1) 				//calculating the sum of sqaures of the sequence
    			{
    				 sum =sum + i*i  
    			}
    		
    		
				var number = sum.toDouble
				//println((Math.sqrt(number))%1)
      	if(Math.sqrt(number) % 1== 0.0)      
      	{
      		//println("StarNum: "+startNum+" EndNum "+endNum+" sum "+number) 
    			return 1;        //returning true if sum is a perfect squares
				} 
				else
				{
					
    			return 0;        //returning false if the sum is not a perfect sqaure
				}
    	}

    	def receive = 
    	{
    		case Work(startNum,endNum) => 
    			sender ! Result(doCalculation(startNum,endNum)) //check the sequence and send back the result
    			//println(doCalculation(startNum,endNum))
    		
    		
    	}
    }


    class Master(nrOfWorkers: Int,nrOfMessages: Int,limit:Int,printer:ActorRef,length: Int) extends Actor
    {
    	
    	var nrOfResults: Int = _
    	val beginTime: Long = System.currentTimeMillis

    	val dispatcher = context.actorOf(Props[Worker].withRouter(RoundRobinRouter(nrOfWorkers)),name="dispathcer") //dispatching task to workers using in a roundrobin fashion

    	def receive = 
    	{
    		case Calculate =>
    			for (i <- 1 until limit+1)
    				{
    					//println("dispathing"+"\t"+i+"\t"+(i+length-1))
    					dispatcher ! Work(i,i+length-1)   //each worker recieved numbers equal to length of the sequence 
    				} 
    		  	
    		case Result(value) =>
    		//println(value)
    		
    			if(value == 1)
    				{
    					counter = counter+1      //incrementing the counter each time a perfect sqaure is observed
    					//println(counter)
    				}
    			nrOfResults += 1
    			if(nrOfResults == nrOfMessages)
    				{
    					printer ! PrintPerfectSquares(counter,time =(System.currentTimeMillis - beginTime).millis) //when result= no of task dispatched then asking printer to print the result
    					context.stop(self)
    				}
    	}
    }

    class Printer extends Actor
    {
    	def receive = 
    	{
    		case PrintPerfectSquares(counter,time) =>
    			//println(counter)
    			println("\n\tPossible Sequences: \t\t%s\n\t Time:\t%s".format(counter,time))  //printing the total perfect sqaures observed and time taken for the execution
    			context.system.shutdown()
    	}
    }

    def start(nrOfWorkers: Int,limit:Int,nrOfMessages: Int,length: Int )
    {
    	val system = ActorSystem("SquareSystem")

    	val printer = system.actorOf(Props[Printer],name = "printer")  //printer actor to print the result

    	val master = system.actorOf(Props(new Master(nrOfWorkers,nrOfMessages,limit,printer,length)),name = "master") //master actor to control the workers and command them to calculate and receive result from them
    	//println("about to tell master to calcuate")
    	//println(nrOfWorkers)
    	//println(parts)
    	//println(nrOfMessages)
    	//println(length)

    	master ! Calculate    //asking workers to calculate
    }
    

    
  }