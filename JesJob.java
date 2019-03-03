/**
 * 
 */
package com.ca.jesftp;

/**
 * @author pansr01
 *
 */
import org.apache.commons.net.ftp.FTPFile;
import java.io.Serializable;
/**
 * The JesJob class extends the FTPFile class. This 
 * allows for <code>JES</code> specific information to
 * be maintained, in addition to the standard <code>FTPFile</code>
 * information.
 * <p>This allows information for the spool files such as:
 * <ul>
 * <li>job name</li>
 * <li>job id</li>
 * <li>job owner</li>
 * <li>job status</li>
 * <li>job class</li>
 * <li>job return code</li>
 * </ul>
 */public class JesJob extends FTPFile {

	//private static final long serialVersionUID = 1L;

	private String sJobName;
	private String sOwner;
	private String sStatus;
	private String sJobClass;
	private String sReturnCode;
	private String sNumFiles;

	public JesJob() {
		super();
		sJobName = "";
		sOwner = "";
		sStatus = "";
		sJobClass = "";
		sReturnCode = "";
		sNumFiles = "";
	}

	public String getNumFiles() {
		return sNumFiles;
	}

	public void setsNumFiles(String NumFiles) {
		sNumFiles = NumFiles;
	}

	public String getReturnCode() {
		return sReturnCode;
	}

	public void setReturnCode(String ReturnCode) {
		this.sReturnCode = ReturnCode;
	}

	public String getJobClass() {
		return sJobClass;
	}

	public void setJobClass(String JobClass) {
		this.sJobClass = JobClass;
	}

	public String getStatus() {
		return sStatus;
	}

	public void setStatus(String Status) {
		 sStatus = Status;
	}

	public String getOwner() {
		return sOwner;
	}

	public void setOwner(String Owner) {
		sOwner = Owner;
	}

	public String getJobName() {
		return sJobName;
	}

	public void setJobName(String jobname) {
		sJobName = jobname;
	}
	
}
