package gov.nyc.doitt.service;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.geotools.data.DataStore;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProcessShapefile {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final String testdummy="test";
	public File processZipShape(File shapeZipIn){
		Path zipfile = null;
		try {
			 Path temppath = Files.createTempDirectory("bikepathtemp");
			 Path temppath2 = Files.createTempDirectory("bikepathtemp2");
	         File shppath = temppath.toFile();
	         File shpfile = new File(temppath.toString(), "bikepath.shp");
	         zipfile = Files.createTempFile(temppath2, "bp", ".zip");
			 FeatureCollection<SimpleFeatureType, SimpleFeature> existing = getExistingFeatureCollection(shapeZipIn);

			SimpleFeatureStore output = getOutputDataStore(shpfile.toURI().toURL(),existing.getSchema(),existing.features());
			
			FileOutputStream fos = new FileOutputStream(zipfile.toString());
            ZipOutputStream zip = new ZipOutputStream(fos);
            zipDirectory(shppath, zip);
            zip.close();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return zipfile.toFile();
			 
	}

	private FeatureCollection<SimpleFeatureType, SimpleFeature> getExistingFeatureCollection(File shapeZipIn) {
		final HashMap<String, Serializable> params = new HashMap<>(3);
		final ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
		FeatureCollection<SimpleFeatureType, SimpleFeature> out = null;
		try {
			URL unzippedShp = unzipShapeFile(shapeZipIn);
			params.put(ShapefileDataStoreFactory.URLP.key, unzippedShp);
			params.put(ShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key, Boolean.FALSE);
			params.put(ShapefileDataStoreFactory.ENABLE_SPATIAL_INDEX.key, Boolean.FALSE);
			ShapefileDataStore dataStore = (ShapefileDataStore) factory.createDataStore(params);
			String typeName = dataStore.getTypeNames()[0];
			FeatureSource<SimpleFeatureType, SimpleFeature> source= dataStore
			        .getFeatureSource(typeName);
			out = source.getFeatures();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return out;
	}
	
	private SimpleFeatureStore getOutputDataStore(URL outurl,SimpleFeatureType existingFeatureType, FeatureIterator<SimpleFeature>existingfeatures){
		final Transaction transaction = new DefaultTransaction("create");
		SimpleFeatureStore out =null;
		final HashMap<String, Serializable> params = new HashMap<>(3);
		final ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
		params.put(ShapefileDataStoreFactory.URLP.key, outurl);
		params.put(ShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key, Boolean.FALSE);
		params.put(ShapefileDataStoreFactory.ENABLE_SPATIAL_INDEX.key, Boolean.FALSE);
		try {
			ShapefileDataStore dataStore = (ShapefileDataStore) factory.createDataStore(params);
			SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
			builder.add(testdummy, String.class);
            for (AttributeDescriptor descriptor : existingFeatureType.getAttributeDescriptors()) {
                builder.add(descriptor);
            }
            builder.setName(existingFeatureType.getName());
            builder.setCRS(existingFeatureType.getCoordinateReferenceSystem());
            existingFeatureType = builder.buildFeatureType();
			dataStore.createSchema(existingFeatureType);
			final String typeName = dataStore.getTypeNames()[0];
            final SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
            if (!(featureSource instanceof SimpleFeatureStore)) {
                log.error("Could not create feature store.");
            }
            out = (SimpleFeatureStore) featureSource;
            SimpleFeatureBuilder fbuilder = new SimpleFeatureBuilder(existingFeatureType);
            List<SimpleFeature> features = new ArrayList<SimpleFeature>();
            while (existingfeatures.hasNext()) {
                SimpleFeature feature = existingfeatures.next();
                fbuilder.set(testdummy, "meh");
                for (Property property : feature.getProperties()) {
                    if (property instanceof GeometryAttribute) {
                        fbuilder.set(existingFeatureType.getGeometryDescriptor().getName(),
                                property.getValue());
                    } else {
                        fbuilder.set(property.getName(), property.getValue());
                    }
                }
                Feature modifiedFeature = fbuilder.buildFeature(feature.getIdentifier().getID());
                features.add((SimpleFeature) modifiedFeature);
            }
            SimpleFeatureCollection collection = new ListFeatureCollection(existingFeatureType, features);
            try {
                out.addFeatures(collection);
                transaction.commit();
            } catch (Exception problem) {
                problem.printStackTrace();
                transaction.rollback();
            } finally {
                transaction.close();
            }

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out;
	}
	
	 private URL unzipShapeFile(File zipFile) throws IOException {
         URL out = null;
         byte[] buffer = new byte[1024];
         ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
         ZipEntry ze = zis.getNextEntry();
         while (ze != null) {

             String fileName = ze.getName();
             File newFile = new File(zipFile.getParent() + File.separator + fileName);

             if ("shp".equalsIgnoreCase(com.google.common.io.Files.getFileExtension(newFile
                     .getAbsolutePath()))) {
                 out = newFile.toURI().toURL();
             }

             System.out.println("file unzip : " + newFile.getAbsoluteFile());

             // create all non exists folders
             // else you will hit FileNotFoundException for compressed folder
             new File(newFile.getParent()).mkdirs();

             FileOutputStream fos = new FileOutputStream(newFile);

             int len;
             while ((len = zis.read(buffer)) > 0) {
                 fos.write(buffer, 0, len);
             }

             fos.close();
             ze = zis.getNextEntry();
         }

         zis.closeEntry();
         zis.close();

         return out;

     }


	private SimpleFeatureType addChangeTypeAttribute(SimpleFeatureType featureType) {
	    SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
	    builder.add("placeholder", String.class);
	    for (AttributeDescriptor descriptor : featureType.getAttributeDescriptors()) {
	        builder.add(descriptor);
	    }
	    builder.setName(featureType.getName());
	    builder.setCRS(featureType.getCoordinateReferenceSystem());
	    featureType = builder.buildFeatureType();
	    return featureType;
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
