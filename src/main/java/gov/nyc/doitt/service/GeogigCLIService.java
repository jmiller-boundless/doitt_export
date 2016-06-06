package gov.nyc.doitt.service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeogigCLIService {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Value(value = "${versionRepoPath}")
	public String versionRepoPath;
	@Value(value = "${fid}")
	public String fid;
	@Value(value="${geogigCLIExec}")
	private String geogigCLIExec;
	@Value(value="${geogigPath}")
	public String gigPath;
	

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
	
	public File getDiffShapefile(String repoPath, String newcommitId,
			String previouscommitid, String gigPath) {
		//geogig shp export-diff --nochangetype --overwrite <commit1> <commit2> <path> <shapefile>
		Path temppath;
		File shpfile = null;
		File zipout = null;
		try {
			temppath = Files.createTempDirectory("shpdifftemp");
	        File shppath = temppath.toFile();
	        shpfile = new File(temppath.toString(), "diff.shp");
			List<String>args = Arrays.asList(new String[]{"shp","export-diff","--nochangetype","--overwrite",previouscommitid,newcommitId,gigPath,shpfile.getAbsolutePath()});
			String stdout = executeCommand(new File(repoPath),geogigCLIExec,args);
			Path temppath2 = Files.createTempDirectory("diffziptemp");
			Path zipfile = Files.createTempFile(temppath2, "diff", ".zip");
			FileOutputStream fos = new FileOutputStream(zipfile.toString());
	        ZipOutputStream zip = new ZipOutputStream(fos);
	         zipDirectory(shppath, zip);
	        zip.close();
	        zipout = zipfile.toFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
		}


		return zipout;
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
			log.error(e.getLocalizedMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
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
	private void zipDirectory(File shppath, ZipOutputStream zip) throws IOException,
    FileNotFoundException {
		File[] shpdirfiles = shppath.listFiles();
		for (int i = 0; i < shpdirfiles.length; i++) {
		    byte[] buffer = new byte[1024];
		    File file = shpdirfiles[i];
		    ZipEntry e = new ZipEntry(file.getName());
		    zip.putNextEntry(e);
		    FileInputStream fis = new FileInputStream(file);
		    int length;
		    while ((length = fis.read(buffer)) > 0) {
		        zip.write(buffer, 0, length);
		    }
		    zip.closeEntry();
		    fis.close();
		}

		zip.finish();
		zip.flush();
		zip.close();
	}

}
