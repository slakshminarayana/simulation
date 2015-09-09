import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Scanner;

/*
 * author: Shreyas Lakshminarayana
 * 
 * Simulates an M/M/m/K queuing system.  The simulation terminates once 100000 customers depart from the system.
 */

public class Assgn4
{
	static LinkedList<Node> Elist = new LinkedList<Node>();             					// Create event list
	static double lambda = 0.0;           													// Arrival rate of first machine
	static double gamma = 0.0;             													// Arrival rate of second machine
	static double mu = 0.0;                													// Service rate
	static double clock = 0.0;             													// System clock
	static int N = 0;                      													// Number of customers in system
	static int Ndep;                   														// Number of departures from system
	static double EN = 0.0;                													// For calculating E[N]
	static boolean done = false; 
	static int m = 0;																		// For defining number of workers
	static int K = 0;
	static double utilizatn = 0.0;
	
	public static double utilization(double clock, double prev, int N, int m)
	{
		double util = 0.0;
		if(N > 0 && N < m)
		{
			util = (N/m) * (clock-prev);
		}
		else if(N >= m)
		{ 
			util = clock-prev;
		}
		return util;
	}

	static class TimeComparator implements Comparator<Node> 
	{
		@Override
		public int compare(Node o1, Node o2) 
		{
			double time1 = o1.getTime();
			double time2 = o2.getTime();
			return Double.compare(time1,time2);
		}
	}

	public static void main(String[] args) 
	{
		RV rand = new RV();
		Scanner sc = new Scanner(System.in);
		System.out.print("Enter the rate at which the first machine produces components(gamma): ");
		gamma = sc.nextDouble();
		System.out.print("\nEnter the number of worker(m): ");
		m = sc.nextInt();
		System.out.print("\nEnter the rate at which the worker packages the component(mu): ");
		mu = sc.nextDouble();
		int count = 0, attempts = 3;
		while(count < 3 && K < 2)
		{
			System.out.print("\nEnter the number of components after which the second machine's components are discarded(K): ");
			K = sc.nextInt();
			count++;
			if(K < 2)
			{
				System.out.print("\nThe value of K should be more than 2. Please try again and you have " + (attempts-count) + " attempts left");
			}
		}
		if(count >= 3)
			System.exit(1);
		sc.close();

		for(Double rho = 0.1; rho <= 1.0; rho += 0.1)
		{
			rho = new BigDecimal(rho).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
			lambda = rho * mu * m;
			lambda = new BigDecimal(lambda).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
			EN = 0.0;
			N = 0;
			Ndep = 0;
			clock = 0.0;
			done = false;
			double lambda_blkd = 0.0;
			int no_arrival = 0;
			utilizatn = 0.0;

			Node first_lambda= new Node(rand.exp_rv(lambda), "ARR_Lambda");
			Node first_gamma = new Node(rand.exp_rv(gamma), "ARR_Gamma");

			Elist.add(first_lambda);
			Elist.add(first_gamma);
			
			while (!done)
			{
				Collections.sort(Elist, new TimeComparator());
				Node CurrentEvent = Elist.getFirst();               							// Get next Event from list
				Elist.removeFirst();
				double prev = clock;                      										// Store old clock value
				clock = CurrentEvent.time;                 										// Update system clock 
				Node newEvent = null;
				
				if(CurrentEvent.type.equals("ARR_Gamma")) 
				{
					EN += N*(clock-prev);                   									//  update system statistics
					if(N < 2)
					{
						utilizatn += utilization(clock, prev, N, m);
						N++;                                    								//  update system size
						no_arrival++;
						newEvent = new Node(clock + rand.exp_rv(gamma), "ARR_Gamma");			//  generate next arrival
						Elist.add(newEvent); 
					}
					if(N <= 2)
					{
						if(N==2)
						{
							utilizatn += utilization(clock, prev, N, m);
							N++;
						}
						Node newDep = new Node(clock + rand.exp_rv(mu), "DEP");
						Elist.add(newDep);
					}
				}
				else if(CurrentEvent.type.equals("ARR_Lambda")) 
				{
					EN += N*(clock-prev);                   								//  update system statistics
					no_arrival++;
					newEvent = new Node(clock + rand.exp_rv(lambda), "ARR_Lambda");
					Elist.add(newEvent);
					utilizatn += utilization(clock, prev, N, m);
					if (N < K)
					{
						N++;  
						if (N <= m) 
						{                             											// If there are less than or equal to m customers
							Node newDep = new Node(clock + rand.exp_rv(mu), "DEP");
							Elist.add(newDep);   												//  generate its departure event
						}
					}
					else
					{
						lambda_blkd++;
					}
					
				}
				else																			// If departure
				{
					EN += N*(clock-prev);                   									//  update system statistics
					utilizatn += utilization(clock, prev, N, m);
					if(N > 0)
						N--;                                    									//  decrement system size
					Ndep++;                                 									//  increment number of departures
					if (N >= m) 
					{                            												// If customers remain
						Node newDep = new Node(clock + rand.exp_rv(mu), "DEP");
						Elist.add(newDep);  													//  generate next departure
					} 
				}
				if (Ndep > 100000) 
					done = true;        															// End condition
			}
			System.out.println("\nThe results of the simulation for the value of rho: " + rho + " as follows");
			System.out.println("The expected number of components as per simulation is: " + (double) (EN/clock));
			System.out.println("Total utilization of the system is: "+ (double) (utilizatn/clock));
			System.out.println("The expected time spent packaging the component as per simulation is: " + (double) (EN/Ndep));
			System.out.println("Blocking probability of second machine with production rate of lambda: " + (lambda_blkd/no_arrival) + "\n\n");
			Elist.clear();
		}
	}
}

