/**
 * This Java Class is to Verify the Mainframe JCL member from PDS and Submit the Job to Jes2
 * if the JCL Member not found and it notifies to the User that Member does not exist or wrong member name
 */
package com.ca.jesftp;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.*;

import com.ca.auto.JesJob;



/**
 * @author pansr01
 *
 */
public class SubmitJcltoJes {
	String serverName 	= 	"";
    String userName 	= 	"";
	 String password 	= 	"";
	 String jobfile 	= 	"";
	 StringBuffer strbuf = new StringBuffer();
	 String jobNumber 	= 	"";
	 String jesJobno    =   "";
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

	 public FTPClient ftp;
	 /**
	  * @throws java.lang.Exception
	  */
	public SubmitJcltoJes() {
		
			ftp = new FTPClient(); 
			
		}
	/**
	 * @throws java.lang.Exception
	 * Logon to Mainframe with Credentials
	 */
	 public void login(String user, String passwd, String server) throws Exception {
		 	serverName = server;
			userName   = user;
			password   = passwd;
			
			ftp.connect (serverName) ; 
	        replyText =ftp.getReplyString()  ; 
	        System.out.println(replyText) ; 
	        
	        ftp.login (userName, password) ; 
	        replyText = ftp.getReplyString() ; 
	        System.out.println(replyText);
	 }//
	 
	 /**
	   * @throws java.lang.Exception
	   * Set the mainframe JCL dataset and JCL Member name 
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
			 ftp.changeWorkingDirectory(dsn);
			 FTPFile[] result = ftp.listFiles();
			  for (int i = 0; i < result.length; i++) {
				  //System.out.println(result[i].getName());
				  if (result[i].getName().equalsIgnoreCase(pdsMember))
					  memexists = true;
			  }
			
			  return memexists;
		 }//
	 
		/**
		 * @throws java.lang.Exception
		 */
		public void SubmitJCLtoJes() throws Exception {
		        String jclmember =  "'" + dsname + "("+pdsMember +")"+"'";
		        System.out.println(jclmember);
				InputStream inputStream = ftp.retrieveFileStream(jclmember);
				BufferedReader bfStream = new BufferedReader(new InputStreamReader(inputStream));
				String line;
				while ((line = bfStream.readLine()) != null) {
						line.trim();
						//System.out.println(line);
						strbuf.append(line);strbuf.append("\n");
				}
				inputStream.close();
				bfStream.close();
								
				ftp.site("filetype=jes") ;
				System.out.println(ftp.getReplyString());
				FTPFile[] result = ftp.listFiles();
				
				ByteArrayInputStream sbis = new ByteArrayInputStream(strbuf.toString().getBytes("UTF-8"));
				ftp.storeFile(serverName,sbis);
				replyText = ftp.getReplyString() ; 
				System.out.println(replyText);
				sbis.close();
							    				
			  //find out the Correct job submitted on mainframe
			    jobNumber 	= parseJobnumber(replyText).trim();
			    System.out.println("job number is "+jobNumber);
			    String site = "filetype=jes jesjobname=* jesowner="+getUserName();
			    ftp.site(site);
		} 
	 
		/**
		 * @throws java.lang.Exception
		 */
		public void checkJobStatus() throws Exception {
			jobcond 	= true;
			counter 	= 0;
			while(jobcond){
				
				FTPFile[] result = ftp.listFiles("*");
				  for (int i = 0; i < result.length; i++) {
					  
					  String jobline = result[i].getRawListing();
					  int idx = jobline.indexOf(jobNumber);
					  System.out.println(jobline);
					  System.out.println("Job index is ..  "+idx);
					  if ((result[i].getRawListing()).matches(jobNumber))
					    parseJoblog(result[i].getRawListing());
				}//
				  
				  System.out.println("Job status is ..  "+jobStatus);
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
		}//
		
		public String getJobStatus() {
			String jobstatus = jobNumber + " " + jobStatus + "   " + ReturnCode + "\n";
			return jobstatus;
		}
		public String getDsname() {
				return dsname;
			}
		public void setDsname(String dsname) {
			this.dsname = dsname;
		}
		public String getPdsMember() {
			return pdsMember;
		}
		public void setPdsMember(String pdsMember) {
			this.pdsMember = pdsMember;
		}
		public String getDataset() {
			return Dataset;
		}
		public void setDataset(String dataset) {
			Dataset = dataset;
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
		 * Parse the Reply String to Find the Job NUmber ftp has submitted
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
		  * Parse the JES log and get the details of jobnumber and its status and Return Code information
		  */
		 private void parseJoblog(String line ){
			 System.out.println(line);
			 String sRemainder = "";
			 Pattern p = Pattern.compile("(\\S+)\\s+(\\S+)\\s+(.*)");
			 Matcher matcher = p.matcher(line);
			 if (matcher.find()) {
				 jobName = matcher.group(1).trim();
				 jesJobno = matcher.group(2).trim();
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
				System.out.println(jobName + " "+ jesJobno +" "+jobOwner+" "+jobStatus +" "+jobClass);
		 }//
}//
