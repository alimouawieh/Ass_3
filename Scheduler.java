import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;




//The clock feature has NOT been synchronized yet. declaring elapsedTime as volatile did not work
//Try synchronized? GOTO LINE 340
/*RESOURCES AND REFERENCES: 
 * http://stackoverflow.com/questions/20196211/read-input-from-text-file-in-java
 * http://stackoverflow.com/questions/10079415/splitting-a-string-with-multiple-spaces
 * 
 */

@SuppressWarnings("rawtypes")
public class Scheduler implements Runnable {
	
	private List<String> input_from_file; 
	private List<MyProcess> MyProcessList; 
	private List<ThreadHandler> ThreadHandlerList; //ThreadHandler's are created and stored here before they enter the queue system.

	private Variable[] MainMemory; 
	private List<String> input_from_mem_config;
	private List<String> commands_from_file;
	
	private boolean queueOne;
	private TreeSet<ThreadHandler> QueueOne = new TreeSet<ThreadHandler>();
	private TreeSet<ThreadHandler> QueueTwo = new TreeSet<ThreadHandler>(); 

	// volatile int elapsedTime; //volatile ensures multiple threads will see
	// the same value after it's been updated by either
	private int elapsedTime; //this will git Scheduler access to the current time as dictated by it's instance of Clock
	Clock clock;
	
	private int size_of_main_memory;
	
	PrintWriter outputFile;     //so we can print to a .txt file..ASS 2

	File hard_drive;

	Thread clockThread;        //a Thread for our Clock instance

	// private final float START_TIME;

	public Scheduler() {
		
		createTextFile();
		elapsedTime = 0;
		clock = new Clock(this);
		clockThread = new Thread(clock);

		queueOne = true; // when queueOne is true then QueueOne is activ and QueueTwo is expired, vice versa
		
		MyProcessList = new ArrayList<MyProcess>(); //stores MyProcess objects so they can become ThreadHandlers
		input_from_file = new ArrayList<String>();  //break the input from .txt file into strings, each line a string
		
		ThreadHandlerList = new ArrayList<ThreadHandler>();

		try {
			input_from_file = Files.readAllLines(Paths.get("/home/scabandari/Desktop", "boobs.txt"),
					StandardCharsets.UTF_8);
		} catch (IOException e) {

			e.printStackTrace();
		}
		try {
			input_from_mem_config = Files.readAllLines(Paths.get("/home/scabandari/Desktop", "memconfig.txt"),
					StandardCharsets.UTF_8);
		} catch (IOException e) {

			e.printStackTrace();
		}
		
		try {
			commands_from_file = Files.readAllLines(Paths.get("/home/scabandari/Desktop", "commands.txt"),
					StandardCharsets.UTF_8);
		} catch (IOException e) {

			e.printStackTrace();
		}
//		for(String s : commands_from_file) {
//			System.out.println("COMMAND: " + s);
//		}
		
		//MM is created w/ the size from memconfig.txt
		size_of_main_memory = Integer.parseInt(input_from_mem_config.get(0));
		MainMemory = new Variable[size_of_main_memory];
		
		hard_drive = new File("hardDrive.txt");
		
//		//here we read just the first line of MemConfig file to get the size of MainMemory
//		BufferedReader read = null;
//		try {
//			//no idea if this will work
//			read = new BufferedReader(new FileReader("/home/scabandari/Desktop/memconfig.txt"));
//		} catch (FileNotFoundException e1) {
//			
//			e1.printStackTrace();
//		}
//		try {
//			inputFromMemConfig = read.readLine();
//		} catch (IOException e) {
//			
//			e.printStackTrace();
//		}
//		try {
//			read.close();
//		} catch (IOException e) {
//			
//			e.printStackTrace();
//		}
		
		populateProcessList();
		printProcessList(); // just a test to see if creating MyProcess objects
		populateThreadHandlerList(); //keeps ThreadHandlers and readies them to enter the queue system

	} // end Scheduler constructor
	
	// virtual memory manager starts here
	//Store (string variableId, unsigned int value): This instruction stores the given variableId
	//and its value in the first unassigned spot in the memory. 
	public void store(String varId, int value) {
		
		BufferedReader read = null;
		BufferedWriter write = null;
		try {
			read = new BufferedReader(new FileReader(hard_drive));
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		}
		try {
			write = new BufferedWriter(new FileWriter(hard_drive));
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		//http://docs.oracle.com/javase/tutorial/essential/concurrency/locksync.html
		//this provides the object on which the lock is aquired
		Variable newVar = new Variable(varId, value );
		boolean flag = false; 
		synchronized(this) {
			for(int i = 0; i < size_of_main_memory; i++) {
				// when releasing we must always set MainMemory = null
				if(MainMemory[i] == null) { //add variable to first available place
					flag = true;
					MainMemory[i] = newVar;
				}
			}
			
			if(!flag) {
				//here i would store newVar in txt file hard_drive
			
			}
		}
		
		try {
			read.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		try {
			write.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	public void release(String varId) {
		//http://docs.oracle.com/javase/tutorial/essential/concurrency/locksync.html
		//this provides the object on which the lock is aquired
		boolean flag = false; 
		synchronized(this) {
			for(int i = 0; i < size_of_main_memory; i++) {
				if(MainMemory[i].getVarID() == varId) {
					flag = true;
					MainMemory[i] = null; //release the variable
				}
			}
			
			if(!flag) {
				//here i would store newVar in txt file
			}
		}
	}
	
	
	//flushing is good. closing is bad, unless you're sure you're done
	public void printToFile(String input) {
		outputFile.println(input);
		outputFile.flush();
//		outputFile.close();
		
	}
	
	
	//http://stackoverflow.com/questions/2885173/how-do-i-create-a-file-and-write-to-it-in-java
	public void createTextFile() {
		
//		System.out.println("SHOULD BE Creating a .txt file named Output.txt BUT NOTHING HAPPENS");
			try {
				outputFile = new PrintWriter("Output.txt", "UTF-8");
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
			
				e.printStackTrace();
			}
		

	}

	public int getElapsedTime() {
		return elapsedTime;
	}

	public void setElapsedTime(int elapsedTime) {
		this.elapsedTime = elapsedTime;
	}


	//As long as ThreadHandlerList is not empty check if the next available ThreadHandler is ready for the big show
	public void checkForArrival() {
		if (!getThreadHandlerList().isEmpty()) {
			//if the get(0) of thread handler list's arrival time is <= current time
			if (threadArrives(getThreadHandlerList().get(0))) {
				
				System.out.println("Before a ThreadHandler has been removed,  ThreadHandlerList is: " );
				printThreadHandlerList();
				ThreadHandler tempHandler = getThreadHandlerList().get(0);
				getThreadHandlerList().remove(0);
				System.out.println("After a ThreadHandler has been removed,  ThreadHandlerList is: " );
				printThreadHandlerList();
				
	
				System.out.println("Time " + getElapsedTime() + ", "
						+ tempHandler.getPID() + " Arrived");

				tempHandler.incrementTimeSlot();
				
				printToFile("Time " + getElapsedTime() + ", "
						+ tempHandler.getPID() + " Arrived");

				tempHandler.setTimeThreadEntersQueue(elapsedTime);
				getExpiredQueue().add(tempHandler);
	
				

			}
		}
	}
	
	//this function came in hamdy during testing. I didn't have the heart to get rid of it
	public void printExpiredQueue() {
		if(getExpiredQueue() == QueueOne ){
			System.out.println("QueueOne");
		} else if(getExpiredQueue() == QueueTwo) {
			System.out.println("QueueTwo");
		} else System.out.println("That shit didn't work");
			
	
	}
	// if this returns false then Scheduler should check the Queues repeatedly
	// for MyThread instance
	// until one is available since MyThreadList is not empty therefore some
	// Processes have not arrived yet
	// or the queueus are not empty in which case a Process should be available
	// for execution
	public boolean checkIfFinnished() {

		if (ThreadHandlerList.isEmpty()) {
		//	if (QueueOne.isEmpty() && QueueTwo.isEmpty()) {
			if (getActiveQueue().isEmpty() && getExpiredQueue().isEmpty()) {
				return true;
			}

		}
		return false;
	}

	// I need a funntion to return the ThreadHandler list
	public List<ThreadHandler> getThreadHandlerList() {
		return ThreadHandlerList;
	}


	// a Function that compares the next Thread Handler objects arrival time to
	// elapsed time
	// if this funciton returns true then the ThreadHandler instance has been
	// added to the expired Queue
	// therefore the next ThreadHandler in the list should be readied and passed
	// to this function
	public boolean threadArrives(ThreadHandler th) {
		if (th.getArrival_time() <= getElapsedTime()) {
			// function to put a Thread Handler in the correct queue
			getExpiredQueue().add(th);
			return true;
		}
		return false;
	}

	// in order to switch queues a function just needs to flip the boolean
	// queueOne
	public TreeSet<ThreadHandler> getExpiredQueue() {
		if (queueOne)
			return QueueTwo;
		else
			return QueueOne;
	}

	public TreeSet<ThreadHandler> getActiveQueue() {
		if (queueOne)
			return QueueOne;
		else
			return QueueTwo;
	}

	public void flipQueues() {
	//	printBothQueues();
		if (queueOne == true){
			System.out.println("Flipping: Queue 2 is now active ");
			queueOne = false;
		}
		else {
			System.out.println("Flipping: Queue 1 is now active ");
			queueOne = true;
		}
	}

	//sometimes it's nice to know what time it is
	public void printElapsedTime() {
		System.out.println(elapsedTime);
	}


	public int getNumberOfProcessesInFile() {
		int number_of_Processes = 0;

		// take the ArrayList input_from_file and retrieve it's first element
		// which defines the number of MyProcesses in the input file
		number_of_Processes = Integer.parseInt(input_from_file.get(0));
		return number_of_Processes;
	}

	// this returns an array list with each line from the input file as an
	// element
	public ArrayList<String> getInputFromFile() {
		return (ArrayList<String>) input_from_file;
	}

	// uses input from .txt file --> input_from_file to populate the ArrayList
	// of MyProcess MyProcessList
	// from there will will be able to supply ThreadHandlerList with ThreadHandlers

	public void populateProcessList() {

		String input_from_file_row = "";
		String pid = "";
		int time_of_arrival = 0;
		int time_of_burst = 0;
		int Process_priority = 0;

		for (int i = 1; i < input_from_file.size(); i++) {

			input_from_file_row = input_from_file.get(i);
			String[] listOfInput = input_from_file_row.split(" \\s+");

			try {

				pid = listOfInput[0];
				time_of_arrival = Integer.parseInt(listOfInput[1]);
				// time_of_arrival = Integer.valueOf(listOfInput[2]);
				// Integer.parseInt(listOfInput[1]);
				time_of_burst = Integer.parseInt(listOfInput[2]);
				// time_of_burst = Integer.parseInt(listOfInput[2]);
				Process_priority = Integer.parseInt(listOfInput[3]);

			} catch (Exception e) {
				e.printStackTrace();
			}

			MyProcess newMyProcess = new MyProcess(pid, time_of_arrival, time_of_burst, Process_priority, this);
			MyProcessList.add(newMyProcess);
			// this next line is just a test to make sure MyProcess objects are
			// created properly

		}
	}

	public void printProcessList() {
		for (MyProcess p : MyProcessList) {
			System.out.println();
			p.printProcess();

		}
	}

	// Every ThreadHandler object is passed a MyProcess instance via constructor
	// Threads are created but not .start() yet
	public void populateThreadHandlerList() {
		for (MyProcess p : MyProcessList) {
			// take every MyProcess instance from list, create ThreadHandler
			// instance and add to list
			ThreadHandlerList.add(new ThreadHandler(p));
		}
	}

	// function verifies that ThreadHandlerList contains correct ThreadHandler
	// objects by printing them
	public void printThreadHandlerList() {

		System.out.println("\nPrinting MyProcess objects from ThreadHandlerList.\n");
		if(ThreadHandlerList.isEmpty()) {
			System.out.println("ThreadHanlderList is empty.");
			return;
		}
		for (ThreadHandler handle : ThreadHandlerList) {
			handle.getMyProcess().printProcess();
		}
		
	
	}

//this was used in testing but the output became messy cuz multple threads outputing to console
	public void printBothQueues() {
		System.out.println("Printing contents of Queue One: \n");
		for(ThreadHandler th : QueueOne) {
			th.getMyProcess().printProcess();
		}
		
		System.out.println("Printing contents of Queue Two: \n");
		for(ThreadHandler th : QueueTwo) {
			th.getMyProcess().printProcess();
		}
		
	}
	
	//make way for mr. important
	public void updatePriority(ThreadHandler handler) {
		int waitingTime;
		waitingTime = handler.getSumOfWaitTimes();
		int bonus;
		double calculation = elapsedTime - handler.getArrival_time();
		if(!(calculation == 0)){
			bonus = (int) Math.floor( (double)(10*waitingTime/(calculation )));
		} else {
			bonus = 0;
		}
		
		int newPriority;
		int min;
		int max;
		if((handler.getPriority() - bonus +5) < 139){
			min = handler.getPriority() - bonus +5;
		} else {
			min = 139;
		}
		
		if(100 > min) {
			newPriority = 100;
		} else {
			newPriority = min;
		}
		
		handler.setPriority(newPriority);
		
	}
		
	/*NOTE
	 * the scheduler needs to keep information on each MyProcess object such as
	 * priority which influences the threads time slice on the CPU, how much
	 * burst time it has left. This is info it needs to get updated from the
	 * Runnable object BUT it needs to interact with the Thread object by
	 * calling .start(), .suspend(), .resume(). Also either the thread object or
	 * the Runnable object needs to be able to be put into one of 2 priority
	 * queues and taken out on it's turn to run on the CPU.
	 */

	//for an instance that implements Runnable well the run function is where the magic happens
	public void run() {

		// int time_slice;
		clockThread.start();
		
		
		try {
			Thread.sleep(1000);
			
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} // nothing happens before 1 second mark


		//not finished if any of List<ThreadHandler> or the 2 Queues<ThreadHandler> are non empty
		while (!checkIfFinnished()) {
			// if we're not done yet but active queue
		    // empty, flip queues and try again

			if (!(getActiveQueue().isEmpty())) {
				// remove the ThreadHandler from the active queue and either
				// .start() or .resume()
				
				ThreadHandler th = getActiveQueue().pollFirst(); 
				if (!th.getStartedYet()) {
			
					th.getThread().start();
					th.setStartedYet(true);
					// now sheduler waits until this process's quantum is up
					// REMEMBER TO ADD A COUNTER THAT COUNTS TO 2 FOR
					
					if (th.getBurst_time() > th.getQuantum()) { // Process
																// should
																// execute for a
																// full Quantum
						System.out.println("Executing Proces " + th.getPID() + " Thread not started and burst time > Quantum" );
						printToFile("Time " + elapsedTime + ", " + th.getPID() + ", Started, Granted " + th.getQuantum());
	
						try {

							Thread.sleep(th.getQuantum()); // executes for a
															// quantum
						} catch (InterruptedException e) {

							e.printStackTrace();
						}

						System.out.println("Time " + elapsedTime + ", " + th.getPID() + ", Paused.");
						printToFile("Time " + elapsedTime + ", " + th.getPID() + ", Paused.");
						
						th.getThread().suspend(); // we suspend the Thread
						th.updateBurstTime(); // update it's burst time

	
						// now add th back into the expired queue becuase it's
						// burst_time is non zeros
						
//if incrementTimeSlot() returns false then priority needs to be updated
						if(!th.incrementTimeSlot()) {
							
							updatePriority(th);
							th.resetQueueTime();
							System.out.println("Updating Process " + th.getMyProcess().getPID() + "'s priority to " + th.getPriority());
							printToFile("Time " + elapsedTime + ", Updating " + th.getMyProcess().getPID() + " priority updated to " + th.getPriority());
						}
						//grab the time the ThreadHandler enters the queue
						th.setTimeThreadEntersQueue(elapsedTime);
						getExpiredQueue().add(th);
						System.out.println("Just added process " + th.getMyProcess().getPID() + " to expired queue");
						
					} else { // else burst time <= quantum and the
								// Process will finish before executing a full
								// quantum or during 1 complete quantum
						System.out.println("Executing Proces " + th.getPID() + " Thread not started and burst <= Quantum") ;
				
						printToFile("Time " + elapsedTime + ", " + th.getPID() + ", Started, Granted " + th.getBurst_time());						try {
							Thread.sleep(th.getBurst_time());
						} catch (InterruptedException e) {

							e.printStackTrace();
						}

						System.out.println("Time " + elapsedTime + ", " + th.getPID() + ", Terminated.");
						printToFile("Time " + elapsedTime + ", " + th.getPID() + ", Terminated.");
						
						//is it necessary to .join() here???
						th.getThread().interrupt(); // we interrupt the Thread
						// and it will quickly
						// finish it's run()
						try {
							th.getThread().join();
						} catch (InterruptedException e) {
							
							e.printStackTrace();
						}
				
						// This Thread should now terminate and is not put back into any queue
						//to be run again
					}
					
                  //else the Process has been started and now must resume
				} else {
					// THIS MUST BE PRINTED TO OUTPUT FILE!!!!!!!!!!!!1
					th.setTimeThreadExitsQueue(elapsedTime); 
					th.updateSumOfWaitTimes();  //this function updates the ThreadHandlers time spent in queue
					th.getThread().resume();
					
					//if there's enough burst time it will execute a full quantum and return to a queue
					if (th.getBurst_time() > th.getQuantum()) { // Process
						// should
						// execute for a
						// full Quantum
						System.out.println("Time " + elapsedTime + ", " + th.getPID() + " Resumed, Granted " + th.getQuantum());
						printToFile("Time " + elapsedTime + ", " + th.getPID() + " Resumed, Granted " + th.getQuantum());
						try {

							Thread.sleep(th.getQuantum()); // executes for a
							// quantum
						} catch (InterruptedException e) {

							e.printStackTrace();
						}

						// THIS MUST BE PRINTED TO OUTPUT
						// FILE!!!!!!!!!!!!!!!!!!11
						System.out.println("Time " + elapsedTime + ", " + th.getPID() + ", Paused.");
//						outputFile.println("Time " + elapsedTime + ", " + th.getPID() + ", Paused.");
						printToFile("Time " + elapsedTime + ", " + th.getPID() + ", Paused.");
						th.getThread().suspend(); // we suspend the Thread
						th.updateBurstTime(); // update it's burst time

						// now add th back into the expired queue becuase it's
						// burst_time is non zeros
//						printBothQueues();
						if(!th.incrementTimeSlot()) {
							
							updatePriority(th); 
							System.out.println("Updating Process " + th.getMyProcess().getPID() + "'s priority to " + th.getPriority());
							printToFile("Time " + elapsedTime + ", Updating " + th.getMyProcess().getPID() + "'s priority to " + th.getPriority());
						}
						getExpiredQueue().add(th);
						System.out.println("Just added " +th.getMyProcess().getPID() + " to the expired queue");
						
					} else { // else burst time <= quantum and the
						// Process will finish before executing a full
						// quantum or during 1 complete quantum
						System.out.println("Executing Proces " + th.getPID() + " Thread already started and burst <= Quantum" );
	
						printToFile("Time " + elapsedTime + ", " + th.getPID() + " Resumed, Granted " + th.getBurst_time());
						try {
							Thread.sleep(th.getBurst_time());
						} catch (InterruptedException e) {

							e.printStackTrace();
						}

		
						System.out.println("Time " + elapsedTime + ", " + th.getPID() + ", Terminated.");

						printToFile("Time " + elapsedTime + ", " + th.getPID() + ", Terminated.");
						th.getThread().interrupt();

						// is it necessary to .join() here???
						try {
							th.getThread().join();
						} catch (InterruptedException e) {

							e.printStackTrace();
						}
						 // we interrupt the Thread
													// and it will quickly
													// finish it's run()
						// This Thread should now terminate and is not put back
						// into any queue
						// never to be run again
					}
					
					
					
				}

			} else {
				flipQueues(); // if the active queue is empty flip queues and
								// continue while loop
			}

		}
		outputFile.close();
		
		////////////////////////////////////////////////////////////////////
		// Before run() closes must .join() & .interrupt() all threads
		/////////////////////////////////////////////////////////////////////
	
		//	ThreadHandler's Thread data members should have been closed by now via the .interrupt()
	
		try {
			clockThread.join();
		} catch (InterruptedException e) {

			e.printStackTrace();
		}

		clockThread.interrupt(); // this should cause this Threads run

									// function to end
		System.out.println("Scheduler Thread is about to finish");
		outputFile.close();

	}
/*
 * 
 * The problem was that once thread objects were created from MyProcess objects it was
 * hard to keep track of which Myprocess went with which thread, updating burst time and 
 * making other changes was not possible. Then we implemented ThreadHandler that has both a 
 * Thread and a MyProcess instance as a data member. This we can affect the thread via 
 * getter as a handle and also update info particular to the MyProcess instance that went into that 
 * Threads creation. It was the only solution we could find. 
 */

	private class ThreadHandler implements Comparable {
		MyProcess p; // will have a MyProcess instance p
		Thread thread; // a Thread instance will be created w/ MyProcess
						// instance
		int priority; // priority is passed from MyProcess to this data member
		int quantum; // quantum is the time slice this Thread gets on the CPU
		private String PID;
		private int arrival_time;
		private int burst_time;
		private int sumOfWaitTimes;  //all the time spent in either queue will be added to this
		private int timeThreadEntersQueue;  //these next 2 data members help determine wait times
		private int timeThreadExitsQueue;

		// Scheduler needs to know whether to call .start() or .resume()
		private boolean started_yet;
		
		//every time a ThreadHandler instance enters a Queue this gets incremented
		//when ThreadHandler thread is PAUSED if numberOfTimeSlots == 2 then update priority and reset to zero
		private int numberOfTimeSlots;  

		public ThreadHandler(MyProcess process) {
			started_yet = false;
			sumOfWaitTimes = 0;
			timeThreadEntersQueue = 0;
			timeThreadExitsQueue = 0;
			numberOfTimeSlots = 0;
			p = process;
			thread = new Thread(p); // Create a Thread from MyProcess object
									// passed via constructor
			// thread is not started yet
			priority = process.getPriority();
			quantum = process.getQuantum();
			PID = process.getPID();
			arrival_time = process.getArrivalTime();
			burst_time = process.getBurstTime();
		}

		public void updateSumOfWaitTimes() {
			sumOfWaitTimes += (getTimeThreadExitsQueue() - getTimeThreadEntersQueue());
		}
		
		
		public void resetQueueTime() {
			sumOfWaitTimes = 0;
			timeThreadEntersQueue = 0;
			timeThreadExitsQueue = 0;
		}
		public int getTimeThreadEntersQueue() {
			return timeThreadEntersQueue;
		}

		public void setTimeThreadEntersQueue(int timeThreadEntersQueue) {
			this.timeThreadEntersQueue = timeThreadEntersQueue;
		}

		public int getTimeThreadExitsQueue() {
			return timeThreadExitsQueue;
		}

		public void setTimeThreadExitsQueue(int timeThreadExitsQueue) {
			this.timeThreadExitsQueue = timeThreadExitsQueue;
		}

		public int getSumOfWaitTimes() {
			return sumOfWaitTimes;
		}

		public void setSumOfWaitTimes(int sumOfWaitTimes) {
			this.sumOfWaitTimes = sumOfWaitTimes;
		}

		// if this funciton returns false then i must update this ThreadHandler
		// instance's priority
		public boolean incrementTimeSlot() {
			
			if (numberOfTimeSlots < 2) {
				numberOfTimeSlots++;
				return true;
			} else {
				numberOfTimeSlots = 0;
				return false;
			}
		}
		
	

		public boolean getStartedYet() {
			return started_yet;
		}

		public void setStartedYet(boolean started_yet) {
			this.started_yet = started_yet;
		}

		public MyProcess getMyProcess() {
			return p;
		}

		public void setMyProcess(MyProcess p) {
			this.p = p;
		}

		public Thread getThread() {
			return thread;
		}

		public void setThread(Thread thread) {		this.thread = thread;
		}
		
		

		public int getPriority() {
			return priority;
		}

		public void setPriority(int priority) {
			this.priority = priority;
		}

		public int getQuantum() {
			return quantum;
		}

		public void setQuantum(int quantum) {
			this.quantum = quantum;
		}

		public String getPID() {
			return PID;
		}

		public void setPID(String pID) {
			PID = pID;
		}

		public int getArrival_time() {
			return arrival_time;
		}

		public void setArrival_time(int arrival_time) {
			this.arrival_time = arrival_time;
		}

		public int getBurst_time() {
			return burst_time;
		}

		public void setBurst_time(int burst_time) {
			this.burst_time = burst_time;
		}

		public void updateBurstTime() {
			burst_time -= quantum;
		}

		//ThreadHandler must implement comparable so that it can be used in data structure TreeSet<>
		@Override
		public int compareTo(Object object) {
			if (priority < ((ThreadHandler) object).getPriority()) {
				return -1;
			} else if (priority > ((ThreadHandler) object).getPriority()) {
				return 1;
			}

			return 0;
		}
	}

	// the Scheduler will keep track of time using a Clock instance in it's own
	// thread
	// 
	// NOTE!!!!! THIS CLOCK RUNS FOREVER AND MUST BE STOPPED BY A CALL FROM
	// SCHEDULER OR INFINITE LOOP
	// that call should be interupt() then .join or vice versa not sure which but if i interupt then run() should exit anyway
	private class Clock implements Runnable {

		int clockValue;
		Scheduler s;     //we need access to the datamembers of the Scheduler instance a Clock is created in

		public Clock(Scheduler s) {

			this.s = s;
		}

		// i may need to change how long to Thread.sleep() here, how accurate
		// must my clock be??
		@Override
		public void run() {
			while (!Thread.interrupted()) {
				try {
					Thread.sleep(10); // wait 10 milliseconds
				} catch (InterruptedException e) {

					e.printStackTrace();
				}

				s.setElapsedTime(s.getElapsedTime() + 10);
				 // increment clock by 10 millis

				//this function will check to see if the next available ThreadHandler's time for arrival in ThreadHandlerList 
				// if it's time to arrive then that ThreadHandler is removed and put into the queue system
				s.checkForArrival();
				
			}

		}

	}

	public void executeNextCommand() {
		
		String[] tokens;
		String command;
		synchronized(this) {
			command = commands_from_file.remove(0);
		}
		
		tokens = command.split("\\s");
		
		switch(tokens[0]) {
			
		case "Store":
			store(tokens[1], Integer.parseInt(tokens[2]));
			break;
	/*	case "Lookup":
			Lookup(tokens[1], Integer.parseInt(tokens[2]));   DON'T HAVE THIS YET
			break;
			*/
		case "Release":
			release(tokens[1]);
			break;
			
			default:
				System.out.println("Command not found. problem in function executeNextCommand() in class Scheduler");
		}	
		
	}

}
