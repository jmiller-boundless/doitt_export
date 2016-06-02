package gov.nyc.doitt.service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeogigCLIService {
	@Value(value = "${versionRepoPath}")
	public String versionRepoPath;
	@Value(value = "${fid}")
	public String fid;
	@Value(value="${geogigCLIExec}")
	private String geogigCLIExec;
	

	public String loadFile(File fieldssplitShape, String repoPath, String fid) {
		String commitid="test";
		String importprocessresult = importFile(repoPath,fieldssplitShape.getAbsolutePath(),fid);
		String addfileprocessresult = addFile(repoPath);
		String commitfileprocessresult = commitFile(repoPath);

		return commitfileprocessresult;
	}
	public String getLog(String repoPath,Integer count){
		List<String>args = Arrays.asList(new String[]{"log","--oneline","\"\"-n "+count+"\"\""});
		return executeCommand(new File(repoPath),geogigCLIExec,args);
	}
	public List<String>getCommitIds(String repoPath,Integer count){
		List<String>commitids = new ArrayList<String>();
		String rawlog = getLog(repoPath,count);
		String[] separated = rawlog.split("\n");
		for(int i=0;i<separated.length;i++){
			String[]columns = separated[i].split("\\s+");
			if(columns.length>0)
				commitids.add(columns[0]);
		}
		return commitids;
	}
	public String importFile(String repoPath,String shpPath, String fidAttrib){
		//geogig shp import ne_10m_rivers_lake_centerlines.shp --fid-attrib dissolve
		String fidarg = "\"\"--fid-attrib "+fidAttrib+"\"\"";
		List<String>args = Arrays.asList(new String[]{"shp","import",shpPath,fidarg});
		//List<String>args = Arrays.asList(new String[]{});
		//String command = geogigCLIExec+" shp import "+shpPath + " --fid-attrib "+fidAttrib;
		String stdout = executeCommand(new File(repoPath),geogigCLIExec,args);
		return stdout;
	}

	public String addFile(String repoPath){
		List<String>args = Arrays.asList(new String[]{"add"});
		return executeCommand(new File(repoPath),geogigCLIExec,args);
	}
	
	public String commitFile(String repoPath){
		String fidarg = "\"\"-m \""+new Date().toString()+"\"\"\"";
		List<String>args = Arrays.asList(new String[]{"commit",fidarg});
		String stdout =  executeCommand(new File(repoPath),geogigCLIExec,args);
		return extractCommitID(stdout);
	}
	private String extractCommitID(String commitStdOut){
		//100%100%[b5e78c0986fb10ac4d6a2dd715f6cb85b6de5c48] Thu Jun 02 13:30:29 EDT 2016"Committed, counting objects...1 features added, 0 changed, 0 deleted.
		String out=null;
		Matcher m = Pattern.compile("\\[([^)]+)\\]").matcher(commitStdOut);
	     while(m.find()) {
	       out=m.group(1);    
	     }
	     return out;
	}
	private String executeCommand(File processPath, String command, List<String>arguments) {
		//PrintResultHandler resultHandler = null;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		CommandLine cmdLine = new CommandLine(command);
		Iterator<String> it = arguments.iterator();
		while (it.hasNext()){
			cmdLine.addArgument(it.next(),false);
		}
		DefaultExecutor executor = new DefaultExecutor();
		//executor.setExitValue(1);
		PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
		executor.setWorkingDirectory(processPath);
		ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
		executor.setWatchdog(watchdog);
		try {
			System.out.println(cmdLine.toString());
			executor.setStreamHandler(streamHandler);
			int exitValue = executor.execute(cmdLine);
			//resultHandler = new PrintResultHandler(exitValue);
		} catch (ExecuteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return(outputStream.toString());

	}
	
	private class PrintResultHandler extends DefaultExecuteResultHandler {
		private ExecuteWatchdog watchdog;
		public PrintResultHandler(final ExecuteWatchdog watchdog){
			this.watchdog = watchdog;
		}
		public PrintResultHandler(final int exitValue) {
			super.onProcessComplete(exitValue);
		}
		@Override
		public void onProcessComplete(final int exitValue) {
			super.onProcessComplete(exitValue);
			System.out.println("[resultHandler] The document was successfully printed ...");
		}
		public void onProcessFailed(final ExecuteException e) {
			super.onProcessFailed(e);
			if (watchdog != null && watchdog.killedProcess()) {
				System.err.println("[resultHandler] The print process timed out");
			}else{
				System.err.println("[resultHandler] The print process failed to do : " + e.getMessage());
			}
		}
	}
}
