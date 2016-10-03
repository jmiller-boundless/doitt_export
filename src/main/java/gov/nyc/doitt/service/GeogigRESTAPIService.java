package gov.nyc.doitt.service;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private EmailService es;
	@Value(value = "${geoserverURL}")
	public String geoserverURL;
	public final static String geogigPluginRepoPath = "/geogig/repos";
	public final static String geogigPluginTaskPath = "/geogig/tasks";
	@Value(value = "${repoIDBikePath}")
	private String repoID;
	@Value(value = "${repoIDBuilding}")
	public String repoIDBuilding;
	@Value(value = "${geoshapePath}")
	public String path;
	@Value(value = "${geoshapePathBuilding}")
	public String pathBuilding;
	@Value(value = "${fid}")
	public String fid;
	@Value(value = "${fidBuilding}")
	public String fidBuilding;
	@Value(value="${author}")
	public String author;
	@Value(value="${email}")
	public String email;
	@Value(value="${importMonitorPauseTimeSeconds}")
	public int importMonitorPauseTimeSeconds;
	@Value(value="${maxNumberOfImportMonitor}")
	public int maxNumberOfImportMonitor;
	
	public String removeFeature(String geoserverURL,String repoID,String treeName, String fid){
		String transactionID = startTransaction(geoserverURL,repoID);
		log.info("transactionID: "+transactionID);
		RestTemplate restTemplate = new RestTemplate();
		String url = geoserverURL+geogigPluginRepoPath+"/"+repoID+"/remove?path="+treeName+"/"+fid+"&transactionId="+transactionID;
		String response = restTemplate.getForObject(url, String.class);
		log.info(response);
		commit(geoserverURL,repoID,transactionID);
		String transactionEndResponse = endTransaction(geoserverURL,repoID,transactionID);
		log.info(transactionEndResponse);
		return response;
	}
	
	public String commit(String geoserverURL, String repoID, String transactionID){
		RestTemplate restTemplate = new RestTemplate();
		String url = geoserverURL+geogigPluginRepoPath+"/"+repoID+"/commit?authorName=nyc&authorEmail=nycadmin@nyc.gov&message=removing&all=true"+"&transactionId="+transactionID;
		String response = restTemplate.getForObject(url, String.class);
		log.info(response);
		return response;
	}
	
	public void removeFeatures(List<String> removed,String geoserverURL,String repoID,String treeName) {
		Iterator<String>it = removed.iterator();
		while(it.hasNext()){
			String fid = it.next();
			removeFeature(geoserverURL,repoID,treeName,fid);
		}
		
	}
	
	public String importZip(File zip,String geoserverURL,String repoID2,String fid,String path,String author,String email,String message){
		String transactionID = startTransaction(geoserverURL,repoID);
		log.info("transactionID: "+transactionID);
		Resource resource = new FileSystemResource(zip);
		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
		parts.add("Content-Type", "multipart/form-data");
		parts.add("fileUpload", resource);
		String url = geoserverURL+geogigPluginRepoPath+"/"+repoID2+"/import.xml?format=zip"
				+ "&add=true"
				//+ "&forceFeatureType=true"
				+ "&fidAttribute="+fid
				//+ "&dest="+path
				+ "&authorName="+author
				+ "&authorEmail="+email
				+ "&message="+message
				+ "&transactionId="+transactionID;
		log.info("url: "+url);
		log.info("repoID2:"+repoID2);
		log.info("repoID:"+repoID);
		RestTemplate restTemplate = new RestTemplate();
		String response =  restTemplate.exchange(url, HttpMethod.POST,
	            new HttpEntity<MultiValueMap<String, Object>>(parts),
	            String.class).getBody();
		String jobID = getJobID(response);
		log.info("jobID: "+jobID);
		int i=0;
		String status=jobStatus(geoserverURL,jobID);
		while(!status.equalsIgnoreCase("FINISHED")&&i<=maxNumberOfImportMonitor){
			try {
				Thread.sleep(importMonitorPauseTimeSeconds*1000);
				status =jobStatus(geoserverURL,jobID);
				i++;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.error(e.getLocalizedMessage());
				es.send(e.getLocalizedMessage());
			}

		}
		if(!status.equalsIgnoreCase("FINISHED"))
			response = response + "; \n Import process did not complete during the time alloted.  The final status was " + status;
		else
			response = "Job "+jobID+ " returned with status of "+status + " at "+new Date();
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
			log.error(e.getLocalizedMessage());
			es.send(e.getLocalizedMessage());
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			es.send(e.getLocalizedMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			es.send(e.getLocalizedMessage());
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
			log.error(e.getLocalizedMessage());
			es.send(e.getLocalizedMessage());
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			es.send(e.getLocalizedMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			es.send(e.getLocalizedMessage());
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
			log.error(e.getLocalizedMessage());
			es.send(e.getLocalizedMessage());
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			es.send(e.getLocalizedMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			es.send(e.getLocalizedMessage());
		}
		return out;

	}

	public String getRepoID() {
		return repoID;
	}

	public void setRepoID(String repoID) {
		this.repoID = repoID;
	}



}
