package com.ra1ph.getpic.image;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.location.Location;
import android.media.ExifInterface;

public class EXIFProcessor {

	private File exifFile; // It's the file passed from constructor
	private String exifFilePath; // file in Real Path format
	private ExifInterface exifInterface;

	private String exifFilePath_withoutext;
	private String ext;
	private Boolean exifValid = false;

	public EXIFProcessor(String fileString) {
		exifFile = new File(fileString);
		exifFilePath = fileString;
		PrepareExif();
	}
	
	public EXIFProcessor(File file) {
		exifFile = file;
		exifFilePath = file.getPath();
		PrepareExif();
	}

	private void PrepareExif() {

		int dotposition = exifFilePath.lastIndexOf(".");
		//exifFilePath_withoutext = exifFilePath.substring(0, dotposition);
		//ext = exifFilePath.substring(dotposition + 1, exifFilePath.length());

		//if (ext.equalsIgnoreCase("jpg")) {
		if(true){
			try {
				exifInterface = new ExifInterface(exifFilePath);
				exifValid = true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	
	public void UpdateGeoTag(double latitude, double longitude){
		 //with dummy data
		
		double lat = latitude;
        double alat = Math.abs(lat);
        String dms = Location.convert(alat, Location.FORMAT_SECONDS);
        String[] splits = dms.split(":");
        String[] secnds = (splits[2]).split("\\.");
        String seconds;
        if(secnds.length==0)
        {
            seconds = splits[2];
        }
        else
        {
            seconds = secnds[0];
        }

        String latitudeStr = splits[0] + "/1," + splits[1] + "/1," + seconds + "/1";    
        
        double lon = longitude;
        double alon = Math.abs(lon);


        dms = Location.convert(alon, Location.FORMAT_SECONDS);
        splits = dms.split(":");
        secnds = (splits[2]).split("\\.");

        if(secnds.length==0)
        {
            seconds = splits[2];
        }
        else
        {
            seconds = secnds[0];
        }
        String longitudeStr = splits[0] + "/1," + splits[1] + "/1," + seconds + "/1";
		
        
        exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE, latitudeStr);
        exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, lat>0?"N":"S");
        
        
        exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, longitudeStr);
        exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, lon>0?"E":"W");
        
		 try {
		  exifInterface.saveAttributes();
		 } catch (IOException e) {
		  // TODO Auto-generated catch block
		  e.printStackTrace();
		 }


		}
	
	private Float convertToDegree(String stringDMS){
		 Float result = null;
		 String[] DMS = stringDMS.split(",", 3);

		 String[] stringD = DMS[0].split("/", 2);
		    Double D0 = new Double(stringD[0]);
		    Double D1 = new Double(stringD[1]);
		    Double FloatD = D0/D1;

		 String[] stringM = DMS[1].split("/", 2);
		 Double M0 = new Double(stringM[0]);
		 Double M1 = new Double(stringM[1]);
		 Double FloatM = M0/M1;
		  
		 String[] stringS = DMS[2].split("/", 2);
		 Double S0 = new Double(stringS[0]);
		 Double S1 = new Double(stringS[1]);
		 Double FloatS = S0/S1;
		  
		    result = new Float(FloatD + (FloatM/60) + (FloatS/3600));
		  
		 return result;


		};
		
		public Double getLatitude(){
			boolean valid=false;
			double Latitude=0;
			String attrLATITUDE = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
			 String attrLATITUDE_REF = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);

			 if((attrLATITUDE !=null)
			   && (attrLATITUDE_REF !=null))
			 {
			  valid = true;
			 
			  if(attrLATITUDE_REF.equals("N")){
			   Latitude = convertToDegree(attrLATITUDE);
			  }
			  else{
			   Latitude = 0 - convertToDegree(attrLATITUDE);
			  }
			 
			 }
			return Latitude;			
		}
		
		public Double getLongitude(){
			boolean valid=false;
			double Longitude=0;
			 String attrLONGITUDE = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
			 String attrLONGITUDE_REF = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);

			 if((attrLONGITUDE != null)
			   && (attrLONGITUDE_REF !=null))
			 {
			  valid = true;
			 
			  if(attrLONGITUDE_REF.equals("E")){
			   Longitude = convertToDegree(attrLONGITUDE);
			  }
			  else{
			   Longitude = 0 - convertToDegree(attrLONGITUDE);
			  }
			 
			 }
			return Longitude;			
		}
}
