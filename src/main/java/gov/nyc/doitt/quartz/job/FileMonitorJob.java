package gov.nyc.doitt.quartz.job;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;









import org.apache.commons.io.FileUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.amazonaws.services.securitytoken.model.GetSessionTokenRequest;
import com.amazonaws.services.securitytoken.model.GetSessionTokenResult;

import gov.nyc.doitt.service.EmailService;
import gov.nyc.doitt.service.FileMetadataService;
import gov.nyc.doitt.service.GeogigCLIService;
import gov.nyc.doitt.service.GeogigRESTAPIService;
import gov.nyc.doitt.service.ProcessShapefile;


public class FileMonitorJob implements Job {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private FileMetadataService fms;
	@Autowired
	private ProcessShapefile psf;
	@Autowired
	private GeogigCLIService gcs;
	@Autowired 
	private GeogigRESTAPIService gras;
	@Autowired
	private EmailService es;

	@Value("${s3.bucketname}")
	private String bucketname;
	@Value("${s3.filekey}")
	private String filekey;
	
	@Value("${s3.filekeyBuilding}")
	private String filekeyBuilding;
	
	private AmazonS3 s3client;
	
	private Path temppath;

	@Override
	public void execute(JobExecutionContext jobExecutionContext) {
		System.out.println("Quartz Job");
		log.info("repo id from gras get: "+gras.getRepoID());

		//AWSCredentials credentials = new ProfileCredentialsProvider().getCredentials();

		//AWSCredentials credentials = new InstanceProfileCredentialsProvider().getCredentials();
		//s3client = new AmazonS3Client(credentials);
		s3client = AmazonS3ClientBuilder.defaultClient();
		ObjectListing ol = s3client.listObjects(bucketname);
		List<S3ObjectSummary> summaries = ol.getObjectSummaries();
		Iterator<S3ObjectSummary> it = summaries.iterator();
		while(it.hasNext()){
			S3ObjectSummary sos = it.next();
			log.info(sos.getKey());
		}

		//bikepath find and process start
		ObjectMetadata om = s3client.getObjectMetadata(bucketname, filekey);
		if(fms.isNewestRev(om.getLastModified(), filekey)){
			fms.saveRev(om.getLastModified(), filekey);
			log.info("New file found with last modified "+om.getLastModified());
			File fieldscombinedZip = downloadS3File(s3client,bucketname,filekey);
			if(psf.shapefileValidated(fieldscombinedZip)) {
				File fieldssplitShape = psf.processZipShape(fieldscombinedZip,"bikepathtemp","bikepath.shp",true,false);
				String newcommitId = gcs.loadFile(fieldssplitShape,gcs.getVersionRepoPath(),gcs.fid);
				List<String>commitids = gcs.getCommitIds(gcs.getVersionRepoPath(),2);
				if(commitids.size()>1){
					try{
						String previouscommitid = commitids.get(1);
						List<String>removed = gcs.getRemovedFeatureIds(gcs.getVersionRepoPath(),newcommitId,previouscommitid,gcs.gigPath);
						gras.removeFeatures(removed,gras.geoserverURL,gras.getRepoID(),gras.path);
						File diffout = gcs.getDiffShapefile(gcs.getVersionRepoPath(),newcommitId,previouscommitid,gcs.gigPath,"bikepath");
						String importout="";
						if(diffout!=null){
							importout = gras.importZip(diffout,gras.geoserverURL,gras.getRepoID(),gras.fid,gras.path,gras.author,gras.email,"diff");
						}
						else{
							importout = "No bikepath feature changes found or only features removed";
						}
						es.send(importout);
					}finally{
						//gcs.deleteLock(gcs.getVersionRepoPath() + File.separator+".geogig"+File.separator+"objects"+File.separator+"je.lck");
					}
				}else{
					es.send("Only " +commitids.size() + " commits found, not enough to run difference");
				}
		}
		}
		//bikepath find and process end
		
		//building find and process start
		ObjectMetadata ombuilding = s3client.getObjectMetadata(bucketname, filekeyBuilding);
		if(fms.isNewestRev(ombuilding.getLastModified(), filekeyBuilding)){
			fms.saveRev(ombuilding.getLastModified(), filekeyBuilding);
			log.info("New building file found with last modified "+ombuilding.getLastModified());
			File buildingZip = downloadS3File(s3client,bucketname,filekeyBuilding);
			File buildingReprojShape = psf.processZipShape(buildingZip,"buildingtemp","building.shp",false,false);
			String newcommitId = gcs.loadFile(buildingReprojShape,gcs.versionRepoPathBuilding,gcs.fidBuilding);
			List<String>commitids = gcs.getCommitIds(gcs.versionRepoPathBuilding,2);
			if(commitids.size()>1){
				String previouscommitid = commitids.get(1);
				List<String>removed = gcs.getRemovedFeatureIds(gcs.versionRepoPathBuilding,newcommitId,previouscommitid,gcs.gigPathBuilding);
				gras.removeFeatures(removed,gras.geoserverURL,gras.repoIDBuilding,gras.pathBuilding);
				File diffout = gcs.getDiffShapefile(gcs.versionRepoPathBuilding,newcommitId,previouscommitid,gcs.gigPathBuilding,"building");
				String importout="";
				if(diffout!=null){
					importout = gras.importZip(diffout,gras.geoserverURL,gras.repoIDBuilding,gras.fidBuilding,gras.pathBuilding,gras.author,gras.email,"building_diff");
				}else{
					importout = "No building feature changes found or only features removed";
				}
				es.send(importout);
			}else{
				es.send("Only " +commitids.size() + " building commits found, not enough to run difference");
			}
		}
		//building find and process end
		
	}

	private File downloadS3File(AmazonS3 s3client, String bucketname2, String filekey2) {
		S3Object object = s3client.getObject(
                new GetObjectRequest(bucketname2, filekey2));
		InputStream objectData = object.getObjectContent();
		try {
			
			temppath  = Files.createTempDirectory("tempfiles");
		} catch (IOException e) {
			log.error(e.getLocalizedMessage());
			e.printStackTrace();
			es.send(e.getLocalizedMessage());
		}
		if(temppath!=null)
			return inputStreamToFile(temppath, objectData);
		else
			return null;
	}
	
	private File inputStreamToFile(Path temppath, InputStream objectData){

		File targetFile=null;
		try {
			targetFile = Files.createTempFile(temppath,"froms3", ".zip").toFile();
			 
		    FileUtils.copyInputStreamToFile(objectData, targetFile);



		} catch (IOException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			es.send(e.getLocalizedMessage());
		} finally {
			if (objectData != null) {
				try {
					objectData.close();
				} catch (IOException e) {
					e.printStackTrace();
					es.send(e.getLocalizedMessage());
					log.error(e.getLocalizedMessage());
				}
			}

		}
	    return targetFile;
	    }
		
	
}
