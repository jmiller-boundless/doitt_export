package gov.nyc.doitt.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import gov.nyc.doitt.model.FileMetadata;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
	public FileMetadata findTopByFileNameOrderByLastChangedDesc(String fileName);

}
