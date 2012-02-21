/**
 * *  Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved
*
 *
 * This software is licensed under the GNU General Public License v3 or later.
 *
 * It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.cloud.network.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.IAgentControl;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.DirectNetworkUsageAnswer;
import com.cloud.agent.api.DirectNetworkUsageCommand;
import com.cloud.agent.api.MaintainAnswer;
import com.cloud.agent.api.MaintainCommand;
import com.cloud.agent.api.PingCommand;
import com.cloud.agent.api.ReadyAnswer;
import com.cloud.agent.api.ReadyCommand;
import com.cloud.agent.api.RecurringNetworkUsageAnswer;
import com.cloud.agent.api.RecurringNetworkUsageCommand;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupTrafficMonitorCommand;
import com.cloud.host.Host;
import com.cloud.resource.ServerResource;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.exception.ExecutionException;

public class TrafficSentinelResource implements ServerResource {
	
	private String _name;
    private String _zoneId;
    private String _ip;
    private String _guid;
    private String _url;
    private static Integer _numRetries;
    private static Integer _timeoutInSeconds;
	
	
	private static final Logger s_logger = Logger.getLogger(TrafficSentinelResource.class);
	
	@Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
    	try {    		    		
    		
    		_name = name;
    		
            _zoneId = (String) params.get("zone");
            if (_zoneId == null) {
                throw new ConfigurationException("Unable to find zone");
            }

            _ip = (String) params.get("ipaddress");
            if (_ip == null) {
                throw new ConfigurationException("Unable to find IP");
            }

            _guid = (String)params.get("guid");
            if (_guid == null) {
                throw new ConfigurationException("Unable to find the guid");
            }
            
            _url = (String)params.get("url");
            if (_url == null) {
                throw new ConfigurationException("Unable to find url");
            }
            
            _numRetries = NumbersUtil.parseInt((String) params.get("numRetries"), 1);
            
            _timeoutInSeconds = NumbersUtil.parseInt((String) params.get("timeoutInSeconds"), 300);

    		return true;
    	} catch (Exception e) {
    		throw new ConfigurationException(e.getMessage());
    	}
    	
    }

	@Override
    public StartupCommand[] initialize() {   
		StartupTrafficMonitorCommand cmd = new StartupTrafficMonitorCommand();
		cmd.setName(_name);
        cmd.setDataCenter(_zoneId);
        cmd.setPod("");
        cmd.setPrivateIpAddress(_ip);
        cmd.setStorageIpAddress("");
        cmd.setVersion("");
        cmd.setGuid(_guid);		
    	return new StartupCommand[]{cmd};
    }

	@Override
    public Host.Type getType() {
		return Host.Type.TrafficMonitor;
	}
	
	@Override
	public String getName() {
		return _name;
	}
	
	@Override
    public PingCommand getCurrentStatus(final long id) {
		return new PingCommand(Host.Type.TrafficMonitor, id);
    }
	
	@Override
	public boolean start() {
		return true;
	}

	@Override
	public boolean stop() {
		return true;
	}

	@Override
	public void disconnected() {
		return;
	}		

	@Override
    public IAgentControl getAgentControl() {
		return null;
	}

	@Override
    public void setAgentControl(IAgentControl agentControl) {
		return;
	}

	@Override
	public Answer executeRequest(Command cmd) {
		if (cmd instanceof ReadyCommand) {
			return execute((ReadyCommand) cmd);
		} else if (cmd instanceof MaintainCommand) {
		    return execute((MaintainCommand) cmd);
		} else if (cmd instanceof DirectNetworkUsageCommand) {
			return execute((DirectNetworkUsageCommand) cmd);
        } else if (cmd instanceof RecurringNetworkUsageCommand) {
            return execute((RecurringNetworkUsageCommand) cmd);
		} else {
			return Answer.createUnsupportedCommandAnswer(cmd);
		}
	}
	
	private Answer execute(ReadyCommand cmd) {
        return new ReadyAnswer(cmd);
    }
	
	private synchronized RecurringNetworkUsageAnswer execute(RecurringNetworkUsageCommand cmd) {
			return new RecurringNetworkUsageAnswer(cmd);
	}
	
	private synchronized DirectNetworkUsageAnswer execute(DirectNetworkUsageCommand cmd) {
        try {
            return getPublicIpBytesSentAndReceived(cmd);
        } catch (ExecutionException e) {
            return new DirectNetworkUsageAnswer(cmd, e);
        }
    }
	
	private Answer execute(MaintainCommand cmd) {
	    return new MaintainAnswer(cmd);
	}

	
	private DirectNetworkUsageAnswer getPublicIpBytesSentAndReceived(DirectNetworkUsageCommand cmd) throws ExecutionException {
	    DirectNetworkUsageAnswer answer = new DirectNetworkUsageAnswer(cmd);
		
		try {
		  //Direct Network Usage
            URL trafficSentinel;
            try {
                //Query traffic Sentinel
                trafficSentinel = new URL(_url+"/inmsf/Query?script="+URLEncoder.encode(getScript(cmd.getPublicIps(), cmd.getStart(), cmd.getEnd()),"UTF-8")
                        +"&authenticate=basic&resultFormat=txt");

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(trafficSentinel.openStream()));

                String inputLine;

                while ((inputLine = in.readLine()) != null){
                    //Parse the script output
                    StringTokenizer st = new StringTokenizer(inputLine, ",");
                    if(st.countTokens() == 3){
                        String publicIp = st.nextToken();
                        Long bytesSent = new Long(st.nextToken());
                        Long bytesRcvd = new Long(st.nextToken());
                        if(bytesSent == null || bytesRcvd == null){
                            s_logger.debug("Incorrect bytes for IP: "+publicIp);
                        }
                        long[] bytesSentAndReceived = new long[2];
                        bytesSentAndReceived[0] = bytesSent;
                        bytesSentAndReceived[1] = bytesRcvd;
                        answer.put(publicIp, bytesSentAndReceived);
                    }
                }
                in.close();
            } catch (MalformedURLException e1) {
                s_logger.info("Invalid Traffic Sentinel URL",e1);
                throw new ExecutionException(e1.getMessage());
            } catch (IOException e) {
                s_logger.debug("Error in direct network usage accounting",e);
                throw new ExecutionException(e.getMessage());
            } 
		} catch (Exception e) {
			s_logger.debug(e);
			throw new ExecutionException(e.getMessage());
		}
		return answer;
	}
	
	private String getScript(List<String> Ips, Date start, Date end){
	    String IpAddresses = "";
	    for(int i=0; i<Ips.size(); i++ ){
	        IpAddresses += Ips.get(i);
	        if(i != (Ips.size() - 1)){
	            // Append comma for all Ips except the last Ip
	            IpAddresses += ",";
	        }
	    }
	    String startDate = getDateString(start);
	    String endtDate = getDateString(end);
	    StringBuffer sb = new StringBuffer();
	    sb.append("var q = Query.topN(\"historytrmx\",");
	    sb.append("                 \"ipsource,bytes\",");
        sb.append("                 \"ipsource = "+IpAddresses+" & destinationzone = EXTERNAL\",");
        sb.append("                 \""+startDate+", "+endtDate+"\",");
	    sb.append("                 \"bytes\",");
	    sb.append("                 100000);");
	    sb.append("var totalsSent = {};");
	    sb.append("var t = q.run(");
	    sb.append("  function(row,table) {");
	    sb.append("    if(row[0]) {    ");
	    sb.append("      totalsSent[row[0]] = row[1];");
	    sb.append("    }");
	    sb.append("  });");
        sb.append("var q = Query.topN(\"historytrmx\",");
        sb.append("                 \"ipdestination,bytes\",");
        sb.append("                 \"ipdestination = "+IpAddresses+" & sourcezone = EXTERNAL\",");
        sb.append("                 \""+startDate+", "+endtDate+"\",");
        sb.append("                 \"bytes\",");
        sb.append("                 100000);");	    
	    sb.append("var totalsRcvd = {};");
	    sb.append("var t = q.run(");
	    sb.append("  function(row,table) {");
	    sb.append("    if(row[0]) {");
	    sb.append("      totalsRcvd[row[0]] = row[1];");
	    sb.append("    }");
	    sb.append("  });");
	    sb.append("for (var addr in totalsSent) {");
	    sb.append("    var TS = 0;");
        sb.append("    var TR = 0;");
        sb.append("    if(totalsSent[addr]) TS = totalsSent[addr];");
        sb.append("    if(totalsRcvd[addr]) TR = totalsRcvd[addr];");
        sb.append("    println(addr + \",\" + TS + \",\" + TR);");
        sb.append("}");
	    return sb.toString();
	}
	
	private String getDateString(Date date){
	       DateFormat dfDate = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
	       return dfDate.format(date);
	}
}