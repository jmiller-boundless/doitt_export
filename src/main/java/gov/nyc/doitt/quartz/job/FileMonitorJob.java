package gov.nyc.doitt.quartz.job;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.amazonaws.services.securitytoken.model.GetSessionTokenRequest;
import com.amazonaws.services.securitytoken.model.GetSessionTokenResult;

import gov.nyc.doitt.service.FileMetadataService;


public class FileMonitorJob implements Job {
	@Autowired
	private FileMetadataService fms;

	@Value("${s3.bucketname}")
	private String bucketname;
	@Value("${s3.filekey}")
	private String filekey;

	@Override
	public void execute(JobExecutionContext jobExecutionContext) {
		System.out.println("Quartz Job");
		//AWSSecurityTokenServiceClient sts_client = new AWSSecurityTokenServiceClient();
		//GetSessionTokenRequest session_token_request = new GetSessionTokenRequest();
		//session_token_request.setRequestCredentials(credentials);
		//GetSessionTokenResult session_token_result = sts_client.getSessionToken(session_token_request);
		//Credentials session_creds = session_token_result.getCredentials();
		AWSCredentials credentials = new ProfileCredentialsProvider().getCredentials();
		//BasicSessionCredentials basic_session_creds = new BasicSessionCredentials(
		//		session_creds.getAccessKeyId(),
		//		session_creds.getSecretAccessKey(),
		//		session_creds.getSessionToken()
				
		//		);
		AmazonS3 s3client = new AmazonS3Client(credentials);
		ObjectListing ol = s3client.listObjects(bucketname);
		List<S3ObjectSummary> summaries = ol.getObjectSummaries();
		Iterator<S3ObjectSummary> it = summaries.iterator();
		while(it.hasNext()){
			S3ObjectSummary sos = it.next();
			System.out.println(sos.getKey());
		}

		ObjectMetadata om = s3client.getObjectMetadata(bucketname, filekey);
		if(fms.isNewestRev(om.getLastModified(), filekey)){
			fms.saveRev(om.getLastModified(), filekey);
		}
	}
}
