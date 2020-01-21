package uci.ics.mondego.tldr.worker;

/**
 * Base class for Workers. Each module in the pipeline is associated with a 
 * Worker. For each module the {@link run} method is implemented.
 * 
 * @author demigorgan
 *
 */
public abstract class Worker<E> implements Runnable{
	private final String name;
	
	public Worker(){
		this.name = null;
	}
	
	public Worker(String n){
		this.name = n;
	}
	
	public String getName() {
        return this.name;
    }	
	
	public abstract void run();
}
