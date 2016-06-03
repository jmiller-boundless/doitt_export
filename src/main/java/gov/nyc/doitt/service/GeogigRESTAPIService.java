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
	
	public String importZip(File zip,String geoserverURL,String repoID,String fid,String path,String author,String email,String message){
		String transactionID = startTransaction(geoserverURL,repoID);
		Resource resource = new FileSystemResource(zip);
		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
		parts.add("Content-Type", "multipart/form-data");
		parts.add("fileUpload", resource);
		String url = geoserverURL+"/"+repoID+"/import.xml?format=zip"
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
		while(!jobComplete(jobID)){
			Thread.sleep(10000);
		}
		String transactionEndResponse = endTransaction(geoserverURL,repoID,transactionID);
		return response;
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
		String url = geoserverURL +"/"+repoID+ "/endTransaction?cancel=false&transactionId="+transactionID;
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.getForObject(url, String.class);
	}

	public String startTransaction(String geoserverURL,String repoID){
		String out=null;
		String url = geoserverURL +"/"+repoID+ "/beginTransaction";
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
