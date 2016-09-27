package gov.nyc.doitt.service;

import gov.nyc.doitt.model.NodeStreetName;
import gov.nyc.doitt.model.repository.NodeIntersectionDAO;

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
import java.util.Iterator;
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
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vividsolutions.jts.geom.Geometry;

@Service
public class ProcessShapefile {
	@Autowired
	private EmailService es;
	@Autowired
	private NodeIntersectionDAO nid;
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final String fromto="fromTocl";
	private final String tofrom="toFromcl";
	private final String fromto_nodeid = "fromToNode";
	private final String tofrom_nodeid = "toFromNode";
	private final String fromto_hr = "fromToHR";
	private final String tofrom_hr = "toFromHR";
	public File processZipShape(File shapeZipIn, String tempDirName, String filename,Boolean fieldSplit){
		Path zipfile = null;
		File shpfile = null;
		try {
			 Path temppath = Files.createTempDirectory(tempDirName);
			 //Path temppath2 = Files.createTempDirectory("bikepathtemp2");
	         File shppath = temppath.toFile();
	         shpfile = new File(temppath.toString(), filename);
	         //zipfile = Files.createTempFile(temppath2, "bp", ".zip");
			 FeatureCollection<SimpleFeatureType, SimpleFeature> existing = getExistingFeatureCollection(shapeZipIn);

			SimpleFeatureStore output = getOutputDataStore(shpfile.toURI().toURL(),existing.getSchema(),existing.features(),fieldSplit);
			
			//FileOutputStream fos = new FileOutputStream(zipfile.toString());
           // ZipOutputStream zip = new ZipOutputStream(fos);
            //zipDirectory(shppath, zip);
           // zip.close();
            System.out.println(shppath);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			log.error(e.getLocalizedMessage());
			e.printStackTrace();
			es.send(e.getLocalizedMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			es.send(e.getLocalizedMessage());
		}catch (Exception e){
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			es.send(e.getLocalizedMessage());
		}
		//return zipfile.toFile();
		return shpfile;
			 
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
			log.error(e.getLocalizedMessage());
			es.send(e.getLocalizedMessage());
		}catch (Exception e){
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			es.send(e.getLocalizedMessage());
		}


		return out;
	}
	
	
	public int featureCount(File diffout) {
		int out=0;

		try {
			URL url = diffout.toURI().toURL();
			final HashMap<String, Serializable> params = new HashMap<>(3);
			final ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
			FeatureCollection<SimpleFeatureType, SimpleFeature> fcs = null;
			params.put(ShapefileDataStoreFactory.URLP.key, url);
			params.put(ShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key, Boolean.FALSE);
			params.put(ShapefileDataStoreFactory.ENABLE_SPATIAL_INDEX.key, Boolean.FALSE);
			ShapefileDataStore dataStore = (ShapefileDataStore) factory.createDataStore(params);
			String typeName = dataStore.getTypeNames()[0];
			FeatureSource<SimpleFeatureType, SimpleFeature> source= dataStore
			        .getFeatureSource(typeName);
			fcs = source.getFeatures();
			out = fcs.size();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			es.send(e.getLocalizedMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			es.send(e.getLocalizedMessage());
		}catch (Exception e){
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			es.send(e.getLocalizedMessage());
		}
		return out;
	}
	
	
	private SimpleFeatureStore getOutputDataStore(URL outurl,SimpleFeatureType existingFeatureType, FeatureIterator<SimpleFeature>existingfeatures,Boolean fieldSplit){
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
			if(fieldSplit){
				builder.add(fromto, String.class);
				builder.add(tofrom, String.class);
				builder.add(fromto_nodeid, String.class);
				builder.add(tofrom_nodeid, String.class);
				builder.add(fromto_hr, String.class);
				builder.add(tofrom_hr, String.class);
			}
            CoordinateReferenceSystem worldCRS = getTargetCRS();
            CoordinateReferenceSystem dataCRS = existingFeatureType.getCoordinateReferenceSystem();
            SimpleFeatureType reprojFeatureType = SimpleFeatureTypeBuilder.retype(existingFeatureType, worldCRS);
            for (AttributeDescriptor descriptor : reprojFeatureType.getAttributeDescriptors()) {
            	if(!descriptor.getLocalName().equalsIgnoreCase("AllClasses"))
            		builder.add(descriptor);
            }

            builder.setName(reprojFeatureType.getName());
            builder.setCRS(reprojFeatureType.getCoordinateReferenceSystem());
            reprojFeatureType = builder.buildFeatureType();
			dataStore.createSchema(reprojFeatureType);
			final String typeName = dataStore.getTypeNames()[0];
            final SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
            if (!(featureSource instanceof SimpleFeatureStore)) {
                log.error("Could not create feature store.");
                es.send("Could not create feature store.");
            }
            out = (SimpleFeatureStore) featureSource;

            SimpleFeatureBuilder fbuilder = new SimpleFeatureBuilder(reprojFeatureType);
            boolean lenient = true;
            MathTransform transform = CRS.findMathTransform(dataCRS, worldCRS, lenient);
            List<SimpleFeature> features = new ArrayList<SimpleFeature>();
            while (existingfeatures.hasNext()) {
                SimpleFeature feature = existingfeatures.next();
                for (Property property : feature.getProperties()) {
                    if (property instanceof GeometryAttribute) {
                    	Geometry geometry = (Geometry) property.getValue();
                    	Geometry geometry2 = JTS.transform(geometry, transform);
                        fbuilder.set(existingFeatureType.getGeometryDescriptor().getName(),
                                geometry2);
                    } else {
                    	
                    		
                    	if(property.getName().toString().equalsIgnoreCase("AllClasses")){
                    		String clazzfromto="";
                    		String clazztofrom="";
                    		String[]clazzes = ((String)property.getValue()).split(",");
                    		if(clazzes.length==1){
                    			clazzfromto=clazzes[0];
                    			clazztofrom=clazzes[0];
                    		}else if(clazzes.length==2){
                    			clazzfromto=clazzes[0];
                    			clazztofrom=clazzes[1];
                    		}
                    		fbuilder.set(fromto, clazzfromto);
                    		fbuilder.set(tofrom, clazztofrom);
                    			
                    	}else{
                    		fbuilder.set(property.getName(), property.getValue());
                    	}
                    }
                }//end copying attributes from existing feature
                if(fieldSplit)
                	populateIntersection(feature,fbuilder);
                Feature modifiedFeature = fbuilder.buildFeature(feature.getIdentifier().getID());
                features.add((SimpleFeature) modifiedFeature);
            }
            SimpleFeatureCollection collection = new ListFeatureCollection(reprojFeatureType, features);
            try {
                out.addFeatures(collection);
                transaction.commit();
            } catch (Exception problem) {
                problem.printStackTrace();
                transaction.rollback();
            } finally {
                transaction.close();
                existingfeatures.close();
            }

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			es.send(e.getLocalizedMessage());
		} catch (FactoryException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			es.send(e.getLocalizedMessage());
		} catch (MismatchedDimensionException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			es.send(e.getLocalizedMessage());
		} catch (TransformException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			es.send(e.getLocalizedMessage());
		}
		return out;
	}
	
	 private void populateIntersection(SimpleFeature existingFeature,
			SimpleFeatureBuilder fbuilder) {
		 System.out.println(existingFeature.getAttribute("SegmentID"));
		 if(existingFeature.getAttribute("SegmentID")!=null){
		 String segid = (String)existingFeature.getAttribute("SegmentID");
		List<NodeStreetName>fromnodestreetnames =  nid.getNodeStreetnameBySegmentIDFrom(segid);
		List<NodeStreetName>tonodestreetnames =  nid.getNodeStreetnameBySegmentIDTo(segid);
		Iterator<NodeStreetName> itfrom = fromnodestreetnames.iterator();
		Iterator<NodeStreetName >itto = tonodestreetnames.iterator();
		String hrfrom = "";
		while(itfrom.hasNext()){
			NodeStreetName nsnfrom = itfrom.next();
			fbuilder.set(fromto_nodeid, nsnfrom.getId().getNodeid());
			//String delim = "_";
			//if(hrfrom.length()<1)
			//	delim="";
			hrfrom = hrfrom+ nsnfrom.getId().getStname();
		}
		fbuilder.set(fromto_hr, hrfrom);
		String hrto = "";
		while(itto.hasNext()){
			NodeStreetName nsnto = itto.next();			
			fbuilder.set(tofrom_nodeid, nsnto.getId().getNodeid());
			//String delim = "_";
			//if(hrto.length()<1)
			//	delim="";
			hrto = hrto+ nsnto.getId().getStname();
		}
		fbuilder.set(tofrom_hr, hrto);

		 }
	}

	private CoordinateReferenceSystem getTargetCRS() {
		 CoordinateReferenceSystem crsout = null;
		try {
			crsout=CRS.decode("EPSG:4326");
		} catch (NoSuchAuthorityCodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			es.send(e.getLocalizedMessage());
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			es.send(e.getLocalizedMessage());
		}catch (Exception e){
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			es.send(e.getLocalizedMessage());
		}
		return crsout;
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
	

}
