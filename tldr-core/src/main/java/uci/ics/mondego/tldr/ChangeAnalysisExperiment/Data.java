package uci.ics.mondego.tldr.ChangeAnalysisExperiment;

import io.netty.util.internal.ConcurrentSet;

public class Data {
	private int total;
	private ConcurrentSet<String> changed;
	private ConcurrentSet<String> new_;
	private ConcurrentSet<String> deleted;
	
	public Data() {
		total = 0;
		this.changed = new ConcurrentSet<String>();
		this.new_ = new ConcurrentSet<String>();
		this.deleted = new ConcurrentSet<String>();
	}
	
	public void addChanged(String string) {
		changed.add(string);
	}
	
	public void addNew(String string) {
		new_.add(string);
	}
	
	public void addDeleted(String string) {
		deleted.add(string);
	}
	
	public int getChangedCount() {
		return changed.size();
	}
	
	public int getNewCount() {
		return new_.size();
	}
	
	public int getDeletedCount() {
		return deleted.size();
	}
	
	public void incCount() {
		total++;
	}
	
	public int getTotalCount() {
		return total;
	}
}
