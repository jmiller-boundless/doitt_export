package gov.nyc.doitt.service;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Service
public class GeogigRESTAPIService {
	@Value(value = "${geoserverURL}")
	public String geoserverURL;
	public final static String geogigPluginRepoPath = "/geogig/repos";
	public final static String geogigPluginTaskPath = "/geogig/tasks";
	@Value(value = "${repoID}")
	public String repoID;
	@Value(value = "${geogigPath}")
	public String path;
	@Value(value = "${fid}")
	public String fid;
	@Value(value="${author}")
	public String author;
	@Value(value="${email}")
	public String email;
	@Value(value="${importMonitorPauseTimeSeconds}")
	public int importMonitorPauseTimeSeconds;
	@Value(value="${maxNumberOfImportMonitor}")
	public int maxNumberOfImportMonitor;
	
	public String importZip(File zip,String geoserverURL,String repoID,String fid,String path,String author,String email,String message){
		String transactionID = startTransaction(geoserverURL,repoID);
		Resource resource = new FileSystemResource(zip);
		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
		parts.add("Content-Type", "multipart/form-data");
		parts.add("fileUpload", resource);
		String url = geoserverURL+geogigPluginRepoPath+"/"+repoID+"/import.xml?format=zip"
				+ "&add=true"
				+ "&fidAttribute="+fid
				+ "&dest="+path
				+ "&authorName="+author
				+ "&authorEmail="+email
				+ "&message="+message
				+ "&transactionId="+transactionID;
		RestTemplate restTemplate = new RestTemplate();
		String response =  restTemplate.exchange(url, HttpMethod.POST,
	            new HttpEntity<MultiValueMap<String, Object>>(parts),
	            String.class).getBody();
		String jobID = getJobID(response);
		int i=0;
		String status=jobStatus(geoserverURL,jobID);
		while(!status.equalsIgnoreCase("FINISHED")||i<=maxNumberOfImportMonitor){
			try {
				Thread.sleep(importMonitorPauseTimeSeconds*1000);
				status =jobStatus(geoserverURL,jobID);
				i++;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//return last status as email
		}
		String transactionEndResponse = endTransaction(geoserverURL,repoID,transactionID);
		return response;
	}
	
	private String jobStatus(String geoserverURL,String jobID) {
		//http://localhost:8080/geoserver/geogig/tasks/3.xml
		//<task>
		//<id>3</id>
		//<status>FINISHED</status>
		//or status could be FAILED
		//or status could be RUNNING
		String out = "";
		String url = geoserverURL+geogigPluginTaskPath+"/"+jobID+".xml";
		RestTemplate restTemplate = new RestTemplate();
		String fullresponse = restTemplate.getForObject(url, String.class);
		DocumentBuilderFactory factory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(fullresponse));
			Document doc = builder.parse(is);
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("status");
			if(nList.getLength()>0){
				out = nList.item(0).getTextContent();;
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out;
	}

	public String getJobID(String response) {
		String out=null;
		DocumentBuilderFactory factory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(response));
			Document doc = builder.parse(is);
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("id");
			if(nList.getLength()>0){
				out = nList.item(0).getTextContent();
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out;
	}

	public String endTransaction(String geoserverURL, String repoID,
			String transactionID) {
		String url = geoserverURL +geogigPluginRepoPath+"/"+repoID+ "/endTransaction?cancel=false&transactionId="+transactionID;
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.getForObject(url, String.class);
	}

	public String startTransaction(String geoserverURL,String repoID){
		String out=null;
		String url = geoserverURL +geogigPluginRepoPath+"/"+repoID+ "/beginTransaction";
		RestTemplate restTemplate = new RestTemplate();
		String fullresponse = restTemplate.getForObject(url, String.class);
		DocumentBuilderFactory factory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(fullresponse));
			Document doc = builder.parse(is);
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("ID");
			if(nList.getLength()>0){
				out = nList.item(0).getTextContent();
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out;

	}

}
