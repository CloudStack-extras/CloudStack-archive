package com.cloud.agent.api;

import java.util.List;

public class HostUpdatesAnswer extends Answer {
	protected List<String> list;
	
	protected HostUpdatesAnswer(){
		
	}
	
	public HostUpdatesAnswer(HostUpdatesCommand cmd, List<String> result){
		super(cmd);	
		this.list = result;
	}
	
	public HostUpdatesAnswer(HostUpdatesCommand cmd, Throwable t){
		
	}
	public List<String> getAppliedPatchList(){
		return list;
	}
}
