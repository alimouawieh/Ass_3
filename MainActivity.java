

public class MainActivity {

	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub

/*		try{
		    PrintWriter writer = new PrintWriter("wawanesa.txt", "UTF-8"); //name of the file
		    
		    writer.println("The first line");
		    writer.println("The second line");
		    writer.close();
		    System.out.println("Output file created?");
		} catch (IOException e) {
		   System.out.println("Could not create file"); 
		}
	*/	
		
		Scheduler sched = new Scheduler();
		/*
		 * for(String s : myApp.getInputFromFile()) { System.out.println(s); }
		 */

//		sched.populateProcessList();
		Thread t1 = new Thread(sched);
		t1.start();
		
		t1.join();
//		sched.printProcessList();

	}
}