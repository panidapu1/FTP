package com.ca.jesftp;

/**
 * @author pansr01
 *
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;

public class JesFileParser implements FTPFileEntryParser  {

	/** Parses a single line of text, and returns an FTPFile.
	 * <p>Typically, the text would be something like this:  
	 * <pre>
	 * ISIELW   TSU00807 ISIELW   OUTPUT TSU      ABEND=522 3 spool files
	 * jobname  jobid    owner    status type     result   
	 * </pre> 
	 * 
	 * @see org.apache.commons.net.ftp.FTPFileEntryParser#parseFTPEntry(java.lang.String)
	 */
	public FTPFile parseFTPEntry(String arg0) {

		JesJob f = new JesJob();

		String sOwner = "";
		String sStatus = "";
		String sType = "";
		String sReturnCode = "";
		
		// Use regular expressions to break into words ...
		// remembering that:
		// - the first backslash is the Java String escape mechanism
		//   so that \\S is really just \S in regexp terms.
		// - \S    means any non-whitespace character
		// - \S+   means a bunch of them
		// - (\S+) means a bunch of them - as a group 
		// - \s+   means some whitespace

		Pattern p = Pattern.compile("(\\S+)\\s+(\\S+)\\s+(.*)");
		Matcher matcher = p.matcher(arg0);
		if (matcher.find()) {
			String sJobname = matcher.group(1);
			String sJobid = matcher.group(2);
			String sRemainder = matcher.group(3);
			//System.out.println(" Matched String1 : "+matcher.toString());
			if (!sRemainder.equals("")) {

				Pattern p2 = Pattern.compile("(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(.*)");
				Matcher matcher2 = p2.matcher(sRemainder);
				//System.out.println(" Matched String2 : "+matcher2.toString());
				if (matcher2.find()) {
					sOwner = matcher2.group(1);
					sStatus = matcher2.group(2);
					sType = matcher2.group(3);
					String remainder = matcher2.group(4);

					if (remainder.startsWith("RC=")) {
						sReturnCode = remainder.substring(3, 8);
					}
					if (remainder.startsWith("ABEND=")) {
						sReturnCode = "S" + remainder.substring(6, 10);
					}
					if (remainder.startsWith("(JCL error)")) {
						sReturnCode = "JCL error";
					}
				}
			}
			f.setName(sJobid);
			f.setJobName(sJobname);
			f.setOwner(sOwner);
			f.setStatus(sStatus);
			f.setJobClass(sType);
			f.setReturnCode(sReturnCode);
			f.setType(FTPFile.DIRECTORY_TYPE);
		}
		return f;
	}

	/** Filter a list that contains the strings from a list of 
	 *  file entries.
	 *  This is an opportunity to remove any superfluous lines;
	 *  that is, strings that do not describe a real file. A common
	 *  example is headings.  
	 *  
	 * @see org.apache.commons.net.ftp.FTPFileEntryParser#preParse(java.util.List)
	 */
	public List preParse(List arg0) {

		Iterator it = arg0.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof String) {
				String s = (String) o;
				String sSub = s.substring(9, 14);
				if (sSub.equals("JOBID")) {
					it.remove();
				}
			}
		}
		return arg0;
	}

	public String readNextEntry(BufferedReader arg0) throws IOException {
		String s = arg0.readLine();
		return s;
	}
}
