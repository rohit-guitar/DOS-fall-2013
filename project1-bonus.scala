import akka.actor.{Actor, Props, ActorSystem}
import com.typesafe.config.ConfigFactory
import akka.remote._
import akka.routing.RoundRobinRouter
import scala.concurrent.duration._ 
import java.util._



object NewApp3 extends App
{

     val sampleActorSystem = ActorSystem("sampleActorSystem", ConfigFactory.parseString("""
    akka {
  actor {
    provider = "akka.remote.RemoteActorRefProvider"
    deployment {
      /masterActor {
        remote = "akka://sampleActorSystem@127.0.0.1:2551"
      }
      /dispatcher {
        remote = "akka://sampleActorSystem@127.0.0.1:2552"
      }
      /dispatcher2 {
        remote = "akka://sampleActorSystem@127.0.0.1:2553"
      }
      
      /dispatcher3 {
        remote = "akka://sampleActorSystem@127.0.0.1:2554"
      }
      /dispatcher4 {
        remote = "akka://sampleActorSystem@127.0.0.1:2555"
      }
      /dispatcher5 {
        remote = "akka://sampleActorSystem@127.0.0.1:2556"
      }
      /dispatcher6 {
        remote = "akka://sampleActorSystem@127.0.0.1:2557"
      }
      /dispatcher7 {
        remote = "akka://sampleActorSystem@127.0.0.1:2558"
      }
      /dispatcher8 {
        remote = "akka://sampleActorSystem@127.0.0.1:2559"
      }
      /dispatcher9 {
        remote = "akka://sampleActorSystem@127.0.0.1:2560"
      }
      /dispatcher10 {
        remote = "akka://sampleActorSystem@127.0.0.1:2561"
      }
    }
  }
  remote {
    transport = "akka.remote.netty.NettyRemoteTransport"
    netty {
      hostname = "localhost"
      //port = 2556
    }
 }
}

  """))

    	var limit:Int = _ 
      var length:Int = _
      //var parts:Int = _
      var counter: Int = _
      
      
      start(1,100000000,nrOfMessages=100000000,20)
   
    sealed trait Message //to ensure message are passed in a controlled fashion
    case object Calculate extends Message
    case class Work(startNum:Int,endNum:Int) extends Message
    case class Result(answer: Int) extends Message
    //case class PrintPerfectSquares(counter: Int, time: Duration) extends Message

   class SampleActor2 extends Actor
  {
    def receive = {
        case x:String => 
        println(x)
    }
  }
  
  
      
    def start(nrOfWorkers: Int,limit:Int,nrOfMessages: Int,length: Int )
    {
        val sampleActorSystem2 = ActorSystem("sampleActorSystem2")
    	

    	val actor = sampleActorSystem2.actorOf(Props(new Master(nrOfWorkers,nrOfMessages,limit,length)),"masterActor")

    	
    	
    	actor ! Calculate

    	
    }
   
  class Worker extends Actor
    {
    	var sum : Int = 0
    	def doCalculation(startNum:Int, endNum:Int):Int = 
    	{
    		
    		sum = 0
    		//main logic 
    		for(i <- startNum until endNum+1)
    			{
    				 sum =sum + i*i
    			}
    		
    		
				var number = sum.toDouble
				//println((Math.sqrt(number))%1)
      	if(Math.sqrt(number) % 1== 0.0) 
      	{
      		println("StarNum: "+startNum+" EndNum "+endNum+" sum "+number)
    			return 1;
				} 
				else
				{
					//println("hola its false")
    			return 0;
				}
    	}

    	def receive = 
    	{
    		case Work(startNum,endNum) => 
    			sender ! Result(doCalculation(startNum,endNum)) //check the sequence and send back the result
    			//println(doCalculation(startNum,endNum))
    		
    		
    	}
    }

  
  
  class Master(nrOfWorkers: Int,nrOfMessages: Int,limit:Int,length: Int) extends Actor
  {
     var nrOfResults: Int = _
    	val beginTime: Long = System.currentTimeMillis
        var time: Long = 0

    	val dispatcher = context.actorOf(Props[Worker].withRouter(RoundRobinRouter(nrOfWorkers)),name="dispatcher")
        val dispatcher2 = context.actorOf(Props[Worker].withRouter(RoundRobinRouter(nrOfWorkers)),name="dispatcher2")
        val dispatcher3 = context.actorOf(Props[Worker].withRouter(RoundRobinRouter(nrOfWorkers)),name="dispatcher3")
        val dispatcher4 = context.actorOf(Props[Worker].withRouter(RoundRobinRouter(nrOfWorkers)),name="dispatcher4")
        val dispatcher5 = context.actorOf(Props[Worker].withRouter(RoundRobinRouter(nrOfWorkers)),name="dispatcher5")
        val dispatcher6 = context.actorOf(Props[Worker].withRouter(RoundRobinRouter(nrOfWorkers)),name="dispatcher6")
        val dispatcher7 = context.actorOf(Props[Worker].withRouter(RoundRobinRouter(nrOfWorkers)),name="dispatcher7")
        val dispatcher8 = context.actorOf(Props[Worker].withRouter(RoundRobinRouter(nrOfWorkers)),name="dispatcher8")
        val dispatcher9 = context.actorOf(Props[Worker].withRouter(RoundRobinRouter(nrOfWorkers)),name="dispatcher9")
        val dispatcher10 = context.actorOf(Props[Worker].withRouter(RoundRobinRouter(nrOfWorkers)),name="dispatcher10")


    	def receive = 
    	{
    		case Calculate =>
    			for (i <- 1 until (limit+1)/10)
    				{
    					//println("dispathing"+"\t"+i+"\t"+(i+length-1))
    					dispatcher ! Work(i,i+length-1)
    				} 
                    
                for (i <-(limit+1)/10 until (2*(limit+1))/10)
        			{
    					//println("dispathing"+"\t"+i+"\t"+(i+length-1))
    					dispatcher2 ! Work(i,i+length-1)
    				} 
                    
                for (i <-(2*(limit+1))/10 until (3*(limit+1))/10)
            		{
    					//println("dispathing"+"\t"+i+"\t"+(i+length-1))
    					dispatcher3 ! Work(i,i+length-1)
    				} 
                    
                for (i <-(3*(limit+1))/10 until (4*(limit+1))/10)
            		{
    					//println("dispathing"+"\t"+i+"\t"+(i+length-1))
    					dispatcher4 ! Work(i,i+length-1)
    				} 
                    
                for (i <-(4*(limit+1))/10 until (5*(limit+1))/10)
            		{
    					//println("dispathing"+"\t"+i+"\t"+(i+length-1))
    					dispatcher5 ! Work(i,i+length-1)
    				} 
                    
                for (i <-(5*(limit+1))/10 until (6*(limit+1))/10)
            		{
    					//println("dispathing"+"\t"+i+"\t"+(i+length-1))
    					dispatcher6 ! Work(i,i+length-1)
    				} 
                    
                for (i <-(6*(limit+1))/10 until (7*(limit+1))/10)
            		{
    					//println("dispathing"+"\t"+i+"\t"+(i+length-1))
    					dispatcher7 ! Work(i,i+length-1)
    				} 
                    
                    
                for (i <-(7*(limit+1))/10 until (8*(limit+1))/10)
            		{
    					//println("dispathing"+"\t"+i+"\t"+(i+length-1))
    					dispatcher8 ! Work(i,i+length-1)
    				} 
                    
                for (i <-(8*(limit+1))/10 until (9*(limit+1))/10)
            		{
    					//println("dispathing"+"\t"+i+"\t"+(i+length-1))
    					dispatcher9 ! Work(i,i+length-1)
    				} 
                    
                for (i <-(9*(limit+1))/10 until limit+1)
            		{
    					//println("dispathing"+"\t"+i+"\t"+(i+length-1))
    					dispatcher10 ! Work(i,i+length-1)
    				} 
    		  	
    		case Result(value) =>
    		//println(value)
    		
    			if(value == 1)
    				{
    					counter = counter+1
    					//println(counter)
    				}
    			nrOfResults += 1
    			if(nrOfResults == nrOfMessages)
    				{
    					//printer ! PrintPerfectSquares(counter,time =(System.currentTimeMillis - beginTime).millis)
    					//context.stop(self)
                        println(counter)
                        println(((System.currentTimeMillis - beginTime).millis).toString())
    				}
    	}
  }
      
  

  
  
  
      
    


}
    

    
  
 
  
