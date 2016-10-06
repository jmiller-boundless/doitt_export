package gov.nyc.doitt.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.nyc.doitt.model.FileMetadata;
import gov.nyc.doitt.model.repository.FileMetadataRepository;
@Service
public class FileMetadataService {
	@Autowired
	private FileMetadataRepository fmr;
	
	public Boolean isNewestRev(Date fileRev,String filename){
		FileMetadata fm = fmr.findTopByFileNameOrderByLastChangedDesc(filename);
		if(fm==null)
			return true;
		return fileRev.compareTo(fm.getLastChanged())>0;
	}
	public Long saveRev(Date fileRev,String filename){
		FileMetadata fm = new FileMetadata();
		fm.setLastChanged(fileRev);
		fm.setFileName(filename);
		return fmr.saveAndFlush(fm).getId();
	}

}
