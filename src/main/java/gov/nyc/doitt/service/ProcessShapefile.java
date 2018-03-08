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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.FilenameUtils;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.vividsolutions.jts.geom.Geometry;

@Service
public class ProcessShapefile {
	@Autowired
	private EmailService es;
	@Autowired
	private NodeIntersectionDAO nid;
	 @Value("#{'${requiredColumns}'.split(',')}") 
	 private List<String> requiredColumns;
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final String fromto="fromTocl";
	private final String tofrom="toFromcl";
	//private final String fromto_nodeid = "fromToNode";
	//private final String tofrom_nodeid = "toFromNode";
	private final String fromto_hr = "fromToHR";
	private final String tofrom_hr = "toFromHR";
	public File processZipShape(File shapeZipIn, String tempDirName, String filename,Boolean fieldsplit, Boolean populateintersection){
		Path zipfile = null;
		File shpfile = null;
		try {
			 Path temppath = Files.createTempDirectory(tempDirName);
			 //Path temppath2 = Files.createTempDirectory("bikepathtemp2");
	         File shppath = temppath.toFile();
	         shpfile = new File(temppath.toString(), filename);
	         //zipfile = Files.createTempFile(temppath2, "bp", ".zip");
			 FeatureCollection<SimpleFeatureType, SimpleFeature> existing = getExistingFeatureCollection(shapeZipIn);

			SimpleFeatureStore output = getOutputDataStore(shpfile.toURI().toURL(),existing.getSchema(),existing.features(),fieldsplit,populateintersection);
			
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
			//Call ogr2Ogr here
			URL reprojShp = reprojWithOgr(unzippedShp);
			//end call to ogr2Ogr
			params.put(ShapefileDataStoreFactory.URLP.key, reprojShp);
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

	private URL reprojWithOgr(URL unzippedShp) throws MalformedURLException {
		//ogr2ogr -t_srs EPSG:3857 /out/path/bikepath.shp /in/path/bikepath.shp
		String outpath = FilenameUtils.getPath(unzippedShp.getFile());
		String outfile = outpath+"bikepathreproj.shp";
		String[] cmd = {"-t_srs", "EPSG:3857", outfile, unzippedShp.getFile()};

		ogr2ogr.main(cmd);
		File ofile = new File(outfile);
		return ofile.toURI().toURL();
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
	
	
	private SimpleFeatureStore getOutputDataStore(URL outurl,SimpleFeatureType existingFeatureType, FeatureIterator<SimpleFeature>existingfeatures,Boolean fieldSplit, Boolean popintersection){
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
				//builder.add(fromto_nodeid, String.class);
				//builder.add(tofrom_nodeid, String.class);
				//builder.add(fromto_hr, String.class);
				//builder.add(tofrom_hr, String.class);
			}
            CoordinateReferenceSystem worldCRS = getTargetCRS();
            CoordinateReferenceSystem dataCRS = existingFeatureType.getCoordinateReferenceSystem();
            //SimpleFeatureType reprojFeatureType = SimpleFeatureTypeBuilder.retype(existingFeatureType, worldCRS);
            for (AttributeDescriptor descriptor : existingFeatureType.getAttributeDescriptors()) {
            	if(!descriptor.getLocalName().equalsIgnoreCase("BikeLane"))
            		builder.add(descriptor);
            }

            builder.setName(existingFeatureType.getName());
            builder.setCRS(existingFeatureType.getCoordinateReferenceSystem());
            SimpleFeatureType reprojFeatureType = builder.buildFeatureType();
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
                    	//Geometry geometry = (Geometry) property.getValue();
                    	//Geometry geometry2 = JTS.transform(geometry, transform);
                        //fbuilder.set(existingFeatureType.getGeometryDescriptor().getName(),
                        //        geometry2);
                    } else {
                    	
                    		
                    	if(property.getName().toString().equalsIgnoreCase("BikeLane")){

                    		if(property.getValue()!=null&&feature.getProperty("BIKE_TRAFDIR")!=null){
	                    		String[]clazzes = getPathClasses((String)property.getValue(),(String)feature.getProperty("BIKE_TRAFDIR").getValue());
	                    		fbuilder.set(fromto, clazzes[0]);
	                    		fbuilder.set(tofrom, clazzes[1]);
                    		}
                    			
                    	}else{
                    		fbuilder.set(property.getName(), property.getValue());
                    	}
                    }
                }//end copying attributes from existing feature
                if(popintersection)
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
		} 
 /* catch (TransformException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			es.send(e.getLocalizedMessage());
		}*/
		return out;
	}
	
private String[] getPathClasses(String value, String trafdir) {
		
		String[]out = new String[2];
		if(trafdir==null||trafdir.trim().equals("")){
			out[0]="";
			 out[1]="";
			 return out;
		}
		 try{
			 Integer clazz = Integer.valueOf(value);
			 if(trafdir.equalsIgnoreCase("TW")){
				 if(clazz==1){
					 out[0]="1";
					 out[1]="1";
				 }else if (clazz==2){
					 out[0]="2";
					 out[1]="2";
				 }else if (clazz==3){
					 out[0]="3";
					 out[1]="3";
				 }else if (clazz==4){
					 out[0]="4";
					 out[1]="4";
				 }else if (clazz==5){
					 out[0]="1";
					 out[1]="2";
				 }else if (clazz==6){
					 out[0]="2";
					 out[1]="3";
				 }else if (clazz==7){
					 out[0]="7";
					 out[1]="7";
				 }else if (clazz==8){
					 out[0]="1";
					 out[1]="3";
				 }else if (clazz==9){
					 out[0]="2";
					 out[1]="1";
				 }else if (clazz==10){
					 out[0]="3";
					 out[1]="1";
				 }else if (clazz==11){
					 out[0]="3";
					 out[1]="2";
				 }
			 }else if(trafdir.equalsIgnoreCase("FT")){
				 if(clazz==1){
					 out[0]="1";
					 out[1]="";
				 }else if (clazz==2){
					 out[0]="2";
					 out[1]="";
				 }else if (clazz==3){
					 out[0]="3";
					 out[1]="";
				 }else if (clazz==4){
					 out[0]="4";
					 out[1]="";
				 }else if (clazz==5){
					 out[0]="1";
					 out[1]="2";
				 }else if (clazz==6){
					 out[0]="2";
					 out[1]="3";
				 }else if (clazz==7){
					 out[0]="7";
					 out[1]="7";
				 }else if (clazz==8){
					 out[0]="1";
					 out[1]="3";
				 }else if (clazz==9){
					 out[0]="2";
					 out[1]="1";
				 }else if (clazz==10){
					 out[0]="3";
					 out[1]="1";
				 }else if (clazz==11){
					 out[0]="3";
					 out[1]="2";
				 }
			 }else if(trafdir.equalsIgnoreCase("TF")){
					 if(clazz==1){
						 out[0]="";
						 out[1]="1";
					 }else if (clazz==2){
						 out[0]="";
						 out[1]="2";
					 }else if (clazz==3){
						 out[0]="";
						 out[1]="3";
					 }else if (clazz==4){
						 out[0]="";
						 out[1]="4";
					 }else if (clazz==5){
						 out[0]="1";
						 out[1]="2";
					 }else if (clazz==6){
						 out[0]="2";
						 out[1]="3";
					 }else if (clazz==7){
						 out[0]="7";
						 out[1]="7";
					 }else if (clazz==8){
						 out[0]="1";
						 out[1]="3";
					 }else if (clazz==9){
						 out[0]="2";
						 out[1]="1";
					 }else if (clazz==10){
						 out[0]="3";
						 out[1]="1";
					 }else if (clazz==11){
						 out[0]="3";
						 out[1]="2";
					 }
			 }else{
				 out[0]="";
				 out[1]="";
			 }
		 }catch(NumberFormatException nfe){
			 out[0]="";
			 out[1]="";
		 }
		
		return out;
	}

	private void populateIntersection(SimpleFeature existingFeature,
			SimpleFeatureBuilder fbuilder) {
		 //System.out.println(existingFeature.getAttribute("SegmentID"));
		 //if(existingFeature.getAttribute("SegmentID")!=null){
			//String segid = (String)existingFeature.getAttribute("SegmentID");
		if(existingFeature.getAttribute("BikeLane")!=null&&!((String)existingFeature.getAttribute("BikeLane")).equals("")){
			String NodeIDFrom="";
			String NodeIDTo="";
			if(existingFeature.getAttribute("NodeIDFrom")!=null)
			 	NodeIDFrom = (String) existingFeature.getAttribute("NodeIDFrom");
			if(existingFeature.getAttribute("NodeIDTo")!=null)
		 	NodeIDTo = (String) existingFeature.getAttribute("NodeIDTo");
			List<String>fromnodestreetnames =  nid.getNodeStreetnameBySegmentIDFrom(NodeIDFrom);
			List<String>tonodestreetnames =  nid.getNodeStreetnameBySegmentIDTo(NodeIDTo);
			Iterator<String> itfrom = fromnodestreetnames.iterator();
			Iterator<String >itto = tonodestreetnames.iterator();
			String hrfrom = "";
			while(itfrom.hasNext()){
				String nsnfrom = itfrom.next();
				//fbuilder.set(fromto_nodeid, nsnfrom.getId().getNodeid());
				String delim = "&";
				if(hrfrom.length()<1)
					delim="";
				hrfrom = hrfrom+delim+nsnfrom;
			}
			fbuilder.set(fromto_hr, hrfrom);
			String hrto = "";
			while(itto.hasNext()){
				String nsnto = itto.next();			
				//fbuilder.set(tofrom_nodeid, nsnto);
				String delim = "&";
				if(hrto.length()<1)
					delim="";
				hrto = hrto+delim+nsnto;
			}
			fbuilder.set(tofrom_hr, hrto);
		}
		// }
	}

	private CoordinateReferenceSystem getTargetCRS() {
		 CoordinateReferenceSystem crsout = null;
		try {
			crsout=CRS.decode("EPSG:3857");
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

	public boolean shapefileValidated(File fieldscombinedZip) {
		boolean out = true;
		final HashMap<String, Serializable> params = new HashMap<>(3);
		final ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
		try {
			URL unzippedShp = unzipShapeFile(fieldscombinedZip);
			//Call ogr2Ogr here
			URL reprojShp = reprojWithOgr(unzippedShp);
			//end call to ogr2Ogr
			params.put(ShapefileDataStoreFactory.URLP.key, reprojShp);
			params.put(ShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key, Boolean.FALSE);
			params.put(ShapefileDataStoreFactory.ENABLE_SPATIAL_INDEX.key, Boolean.FALSE);
			ShapefileDataStore dataStore = (ShapefileDataStore) factory.createDataStore(params);
			String typeName = dataStore.getTypeNames()[0];
			SimpleFeatureType sft = dataStore.getSchema();
			List<String>additionalColumns = containsAllRequired(sft,requiredColumns);
			List<String>missingColumns = containsOnlyRequired(sft,requiredColumns);
			if(additionalColumns.size()!=0) {
				out=false;
				es.send("The following columns were found that do not match the schema: "+StringUtils.join(additionalColumns, ','));
			}
			if(missingColumns.size()!=0) {
				out=false;
				es.send("The following required columns are missing: "+StringUtils.join(missingColumns, ','));
			}

		} catch (IOException e) {
			out=false;
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			es.send(e.getLocalizedMessage());
		}catch (Exception e){
			out=false;
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			es.send(e.getLocalizedMessage());
		}

		return out;

	}

	private List<String> containsAllRequired(SimpleFeatureType sft, List<String> requiredColumns2) {
		List<String>out = new ArrayList<String>();
		for(String reqcol:requiredColumns2) {
			if(sft.getDescriptor(reqcol)==null)
				out.add(reqcol);
		}
		
		return out;


	}

	private List<String> containsOnlyRequired(SimpleFeatureType sft, List<String> requiredColumns2) {
		List<String>out = new ArrayList<String>();
		List<AttributeDescriptor>shapefileatts = sft.getAttributeDescriptors();
		for(AttributeDescriptor att:shapefileatts) {
			if(!requiredColumns2.contains(att.getLocalName()))
				out.add(att.getLocalName());
			
		}
		return out;
	}
	

}
