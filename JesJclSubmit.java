/**
 * 
 */
package com.ca.jesftp;

import java.io.InputStream;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



import com.enterprisedt.net.ftp.*;
import com.enterprisedt.net.ftp.FTPReply;

/**
 * @author PANSR01
 *
 */
public class JesJclSubmit {
	 String serverName 	= 	"";
     String userName 	= 	"";
	 String password 	= 	"";
	 String jobfile 	= 	"QAWEBSET.jcl";
	 StringBuffer strbuf = new StringBuffer();
	 String jobNumber 	= 	"";
	 String jobName 	= 	"";
	 String jobOwner	=	"";
	 String jobStatus   =   "";
	 String jobClass	=	"";
	 String ReturnCode  = 	"";
	 String replyText	=	"";
	 String replyJobMsg =   "";
	 String dsname      = "";
	 String pdsMember   = "";
	 String Dataset     = "";
	 int jobind			=	0;
	 long timeout 		= 3*60*60*1000;
	 long duration 		= 10*1000;
	 long counter 		= 0;
	 boolean jobcond 	= true;
	 boolean found      = true;
	 
	 public FTPReply reply;
	 public FTPClient ftp;
	 
	 /**
	  * @throws java.lang.Exception
	  */
	public JesJclSubmit() {
		
			ftp = new FTPClient(); 
//			serverName = getServerName();
//			userName   = getUserId();
//			password   = getPassword();
			
		}
	/**
	 * @throws java.lang.Exception
	 */
	 public void login(String user, String passwd, String server) throws Exception {
		 	serverName = server;
			userName   = user;
			password   = passwd;
			ftp.setRemoteHost(serverName);
			ftp.connect() ;
			ftp.login (userName, password);
	 }
	 /**
	   * @throws java.lang.Exception
	   */
	 public void setJcldataset(String pds, String member) throws Exception{
		 dsname = pds;
		 pdsMember = member.trim();
	 }
	 	/**
		 * @throws java.lang.Exception
		 */
		 public boolean checkPdsMemer() throws Exception {
			 boolean memexists = false;
			 String dsn = "'" + dsname + "'";
			 ftp.chdir(dsn);
			 String[] result = ftp.dir(dsn);
			  for (int i = 0; i < result.length; i++) {
				  System.out.println(result[i]);
				  if (result[i].equalsIgnoreCase(pdsMember))
					  memexists = true;
			  }
			
			  return memexists;
		 }//
	/**
	 * @throws java.lang.Exception
	 */
	 public String submitJob(String jobname) throws Exception {
		    
			ftp.quote("quote site filetype=jes");
			ftp.site("filetype=jes");
			reply = ftp.getLastReply(); 
			System.out.println(reply.getReplyText());
			InputStream inputStream =    this.getClass().getResourceAsStream(jobname);
			ftp.put(inputStream, serverName);
			inputStream.close();
			reply = ftp.getLastReply(); 
			System.out.println(reply.getReplyText());
			replyJobMsg =reply.getReplyText();
			jobNumber = parseJobnumber(reply.getReplyText());
			return reply.getReplyText();
			
		}
	 /**
		 * @throws java.lang.Exception
		 */
		public void checkJobStatus() throws Exception {
			String site 		= "filetype=jes jesjobname=* jesowner="+userName;
			ftp.quote("quote site " + site);
			ftp.site(site);
			jobcond 	= true;
			counter 	= 0;
			while(jobcond){
				String[] s1 = ftp.dir(jobNumber,true);
				 for(int i=0;i<s1.length;i++)
				 {
					if ( s1[i].contains(jobNumber))
						parseJoblog(s1[i]);
					 System.out.println(s1[i]);
				 }
				//Checking the status of the job	
					if(jobStatus.contentEquals("OUTPUT")){
						System.out.println(jobNumber + " " +jobName + " Job Ended Succesfully");
						jobcond = false;
					}
					else
					{
						if (timeout==counter)
						{	
							System.out.println("Job Waited for 3 Hours and did not Completed .......");
							jobcond = false;
						}
						else
						{
							System.out.println("Waiting for Job "+ jobNumber + " " + jobName + " Execution to complete .......");
							Thread.sleep(duration);
							counter = counter + duration;
						}// end of inner if loop
					}//end of outer if loop
			}
		}
		
		public String getJobStatus() {
			String jobstatus = jobNumber + " " + jobStatus + "   " + ReturnCode + "\n";
			return jobstatus;
		}
		public String getJobNumStatus() {
			
			return jobStatus;
		}
		public void setJobStatus(String jobStatus) {
			this.jobStatus = jobStatus;
		}
		public String getReplyJobMsg() {
			return replyJobMsg;
		}
		public void setReplyJobMsg(String replyJobMsg) {
			this.replyJobMsg = replyJobMsg;
		}
		public String getServerName() {
			return serverName;
		}
		public void setServerName(String serverName) {
			this.serverName = serverName;
		}
		public String getUserName() {
			return userName;
		}
		public void setUserName(String userName) {
			this.userName = userName;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
	 /**
	  * @throws java.lang.Exception
	  */
	 private String parseJobnumber(String line ){
			StringTokenizer st1 = new StringTokenizer(line, " ");
			String jobnum = "";
			
			//iterate through tokens
			   while(st1.hasMoreTokens()){
			    String token = st1.nextToken();
				   if (token.startsWith("JOB"))
			    	jobnum = token.trim();
		  
			   	}
			return jobnum;   
		}
		
	 /**
	  * @throws java.lang.Exception
	  */
	 private void parseJoblog(String line ){
		 String sRemainder = "";
		 Pattern p = Pattern.compile("(\\S+)\\s+(\\S+)\\s+(.*)");
		 Matcher matcher = p.matcher(line);
		 if (matcher.find()) {
			 jobName = matcher.group(1).trim();
			 jobNumber = matcher.group(2).trim();
			 sRemainder = matcher.group(3);
		 }	
		 
		 Pattern p2 = Pattern.compile("(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(.*)");
		 Matcher matcher2 = p2.matcher(sRemainder);
		
			if (matcher2.find()) {
				jobOwner = matcher2.group(1).trim();
				jobStatus = matcher2.group(2).trim();
				jobClass = matcher2.group(3).trim();
				String remainder = matcher2.group(4);

				if (remainder.startsWith("RC=")) {
					ReturnCode = remainder.substring(0, 8);
				}
				if (remainder.startsWith("ABEND=")) {
					ReturnCode = "S" + remainder.substring(0, 10);
				}
				if (remainder.startsWith("(JCL error)")) {
					ReturnCode = "JCL error";
				}
			}
//		 StringTokenizer st1 = new StringTokenizer(line, " ");
//			String job = "";
//			
//			//iterate through tokens
//			   while(st1.hasMoreTokens()){
//			    String token = st1.nextToken().trim();
//				   if (token.equals(jobNumber))
//			    	{
//					   job = token;
//			    	
//			    	}
//		  
//			   	}
//			return job;   
		}
}//
