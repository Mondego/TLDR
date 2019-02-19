package uci.ics.mondego.tldr.model;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import uci.ics.mondego.tldr.worker.Worker;

public class ThreadedChannel<E> {

    private ThreadPoolExecutor executor;
    
    private Class<Runnable> workerType;
    private Semaphore semaphore;
    private final int thread_count;  
    private static final Logger logger = LogManager
            .getLogger(ThreadedChannel.class);  
    
    public ThreadedChannel(int nThreads, Class clazz) {        
    	this.thread_count = nThreads;		   	    	
    	this.workerType = clazz;
    	
    	this.executor = new ThreadPoolExecutor(thread_count, // core size
    		    thread_count * 2 , // max size
    		    10*60, // idle timeout
    		    TimeUnit.SECONDS,
    		    new LinkedBlockingQueue<Runnable>(100));
    	
        this.semaphore = new Semaphore(thread_count+2);                
    }

    public int getSem(){
    	return semaphore.availablePermits();
    }

    public int getThreadCount(){
    	return thread_count;
    }
    
    public void send(E e) throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException {
    	
          long startTime = System.nanoTime();
          //final Runnable o = workerType;
          
           final Runnable o = this.workerType.getDeclaredConstructor(e.getClass()).newInstance(e);
           
	        try {           
	            semaphore.acquire();
	            
	            long estimatedTime = System.nanoTime() - startTime;          
	                               
	        } catch (InterruptedException ex) {
	            logger.error("Caught interrupted exception " + ex);
	        }

	        try {
	            executor.execute(new Runnable() {
	                public void run() {
	                    try {
	                        o.run();
	                    } finally {
	                        semaphore.release();                          
	                    }
	                }
	            });
	        } catch (RejectedExecutionException ex) {
	            semaphore.release();
	        }
    }
    
    
    public void shutdown() {
        this.executor.shutdown();
        System.out.println(workerType.getName()+" terminating");
        try {
            this.executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.error("inside catch, shutdown");
        }
    }
}
