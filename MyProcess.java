
import java.util.Random;
@SuppressWarnings("rawtypes")
//Scheduler will know how long each Process has been run and get update 
//the Process's burst time accordingly using updateBurtsTime()

//not sure if this needs to be comparable now that I have ThreadHandler class!!!!!


//MYProcess objects will model the processes created from the data in the supplied tesxt file
//needs to be aware of the time it's being executed to 
class MyProcess implements Runnable { 

//alt + shift s to bring up fast override menu
	Scheduler s;
	
	private String PID;
	private int arrival_time;
	private int burst_time;
	private int priority;
	int quantum;              //quatum is the same as time slice on the CPU
	
	
	public MyProcess(String pid, int time, int burst, int prio, Scheduler s) {
		
		PID = pid;
		arrival_time = time;
		burst_time = burst;
		priority = prio;
		setQuantum(prio);
		this.s = s;
	
	}
	
	//setters and getters
	public void SetPID(String pid) { PID = pid; } //won't likely need this setter
	public void setArrivalTime(int time) { arrival_time = time; }
	public void setInitialPriority(int prio) { priority = prio; }
	public void setBurstTime(int time) { burst_time = time; }
	
	public String getPID() { return PID; }
	public int getArrivalTime() { return arrival_time; }
	public int getBurstTime() { return burst_time; }
	public int getPriority() { return priority; }
	public int getQuantum() { return quantum; }
	
	//not sure MyProcess even needs to implement this now that I have ThreadHandler objects

	//when a threads run function returns the thread will end
	public void run() { //scheduler updates the time of the process
		//all code goes here
//		int i = 0; 
			//THIS RUNS UNTIL SCHEDULER CALLS .interupt() on it
			while(!Thread.interrupted()) {
		/*		try {
					i++;
				if (i % 100 == 0) {
					System.out.println("Process : " + PID + " is executing on the CPU");
					//s.executeNextInstruction
				}
					Thread.sleep(1); 
					//I think it's getting interupted during it's sleep cycle
				} catch (InterruptedException e) {
		//			http://stackoverflow.com/questions/1087475/when-does-javas-thread-sleep-throw-interruptedexception
					Thread.currentThread().interrupt();
				//	e.printStackTrace();
				
				 */
				
				Random ran = new Random();
				int executionTime = ran.nextInt(200);
				//this function reads the next command from ArrayList commands_from_file and executes the command
				s.executeNextCommand(); 
				try {
					Thread.sleep(executionTime);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				}
			
		}
	
	
	
	
	public void setQuantum(int prior) {
		if(prior < 100) {
			quantum = (140-prior)*20;
		}
		else 
			quantum = (140-prior)*5;
	}
	
	public void printProcess() {
		System.out.println(getPID() + "   "+ getArrivalTime() + "   " + getBurstTime() + "   " + getPriority() );
	}
	
	//update burst time must be called by scheduler, unless the process has run out of burst time
	public void updateBurstTime() {
		burst_time -= quantum;
		
	}
	
	//from class App we'll check if the process is done and if not it gets added to the TreeSet that acts as our  queue that's not being actively pulled from

	public boolean enoughBurstTime() {
		if(burst_time >= quantum)
			return true;
		else 
			return false;
	}
	//if the above function returns false we need to return to the scheduler how long in millis the current process should run
	//ie getBurstTime()
	

}