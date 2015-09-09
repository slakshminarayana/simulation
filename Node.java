
public class Node 
{
	double time;            										// Time at which Event takes place
	String type; 
	
	Node()
	{
		time = 0.0;
		type = "";
	}

	Node(double time, String type)
	{
		this.time = time;
		this.type = type;
	}
	
	Double getTime()
	{
		return time;
	}
}