package uci.ics.mondego.tldr.worker;


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
	
}
