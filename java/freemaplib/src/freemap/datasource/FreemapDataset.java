package freemap.datasource;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.Iterator;

import freemap.data.Algorithms;
import freemap.data.POI;
import freemap.data.Way;
import freemap.data.Projection;
import freemap.data.Point;
import freemap.data.Annotation;
import freemap.jdem.DEM;

import freemap.data.*;

import java.io.PrintWriter;
import java.io.FileWriter;

import java.io.IOException;

// 120315 change FreemapDataset to store Ways as an ArrayList, not a HashMap.
// This is to account for intersections of a dataset with a bounding box producing a MultiLineString
// of multiple segments of the same way, which on a renderer e.g. Hikar we will treat as separate ways.
// As they will all have the same OSM ID, we can't use a HashMap unless we change the ID.
// The only affected methods are findWayById() (becomes much slower, doesn't seem to be used anywhere
// anyhow) and wayIterator (seems not to be used anywhere)

public class FreemapDataset implements TiledData
{
	//HashMap<Long,Way> ways;
	ArrayList<Way> ways;
	HashMap<Long,POI> pois;
	HashMap<Integer,Annotation> annotations;
	Projection proj;

	public interface POIVisitor
	{
		public void visit(POI p);
	}
	
	public interface WayVisitor 
	{
	    
		public void visit(Way w);
	}
	
	public interface AnnotationVisitor
	{
		public void visit(Annotation a);
	}
	
	public FreemapDataset()
	{
		//ways=new HashMap<Long,Way>();
		ways = new ArrayList<Way>();
		pois=new HashMap<Long,POI>();
		annotations=new HashMap<Integer,Annotation>();
	}
	
	public void setProjection(Projection proj)
	{
		this.proj=proj;
	}
	
	public void add(Way way)
	{
		way.reproject(proj);
		//ways.put(way.getId(),way);
		ways.add(way);
	}
	
	public void add(POI poi)
	{
		poi.reproject(proj);
		pois.put(poi.getId(),poi);	
	}
	
	public void add(Annotation ann)
	{
		ann.reproject(proj);
		annotations.put(ann.getId(), ann);
	}
	
	public void merge(TiledData otherData)
	{
		//ways.putAll(((FreemapDataset)otherData).ways);
		ways.addAll(((FreemapDataset)otherData).ways);
		pois.putAll(((FreemapDataset)otherData).pois);
		annotations.putAll(((FreemapDataset)otherData).annotations);
	}
	
	public String toString()
	{
		return "OSMRenderData: " + pois.toString()+"\n" + ways.toString() + "\n" + annotations.toString();
	}
	
	public void applyDEM(DEM dem)
	{
	    
	//	Set<Map.Entry<Long,Way> > waySet = ways.entrySet();
		Set<Map.Entry<Long, POI> > poiSet = pois.entrySet();
		
		
		/*
		for(Map.Entry<Long,Way> w: waySet)
		{
			
			w.getValue().applyDEM(dem);
		}
		*/
		for(Way w: ways)
			w.applyDEM(dem);
		
		for(Map.Entry<Long,POI> p: poiSet)
		{
			p.getValue().applyDEM(dem);
		}
	}
	
	public boolean isWithin(DEM dem)
	{
		//Set<Map.Entry<Long,Way> > waySet = ways.entrySet();
		Set<Map.Entry<Long, POI> > poiSet = pois.entrySet();
		
		/*
		for(Map.Entry<Long,Way> w: waySet)
		{
			if (w.getValue().isWithin(dem))
				return true;
		}
		*/
		for(Way w: ways)
		{
			if (w.isWithin(dem))
				return true;
		}
		
		for(Map.Entry<Long,POI> p: poiSet)
		{
			if(dem.pointWithin(p.getValue().getPoint(),proj))
				return true;
		}
		return true;
	}
	
	public void save(String filename) throws IOException
	{
	
	 System.out.println("save()");
	    PrintWriter pw = new PrintWriter(new FileWriter(filename));
	  
	    pw.println("<rdata>");
	    writeProjection(pw);
	    savePOIs(pw);
	    saveWays(pw);
	    saveAnnotations(pw);
	    pw.println("</rdata>");
	       
	    pw.flush();
        pw.close();
	}
	
	public void writeProjection(PrintWriter pw)
	{
		if(proj!=null)
			pw.println("<projection>" + proj.getID()+"</projection>");
	}
	
	public void saveWays(PrintWriter pw)
	{
		/*
		Set<Map.Entry<Long,Way> > waySet = ways.entrySet();
		for(Map.Entry<Long,Way> w: waySet)
		{
			w.getValue().save(pw);
		}
		*/
		for(Way w: ways)
			w.save(pw);
	}
	
	public void savePOIs(PrintWriter pw)
	{
	    System.out.println("savePOIs");
		Set<Map.Entry<Long, POI> > poiSet = pois.entrySet();
		for(Map.Entry<Long,POI> p: poiSet)
		{
			p.getValue().save(pw);
		}
	}
	
	public void saveAnnotations(PrintWriter pw)
	{
		Set<Map.Entry<Integer, Annotation> > annSet = annotations.entrySet();
		for(Map.Entry<Integer,Annotation> a: annSet)
		{
			a.getValue().save(pw);
		}
	}
	
	public void reproject(Projection newProj)
	{
		//Set<Map.Entry<Long,Way> > waySet = ways.entrySet();
		Set<Map.Entry<Long, POI> > poiSet = pois.entrySet();
		Set<Map.Entry<Integer, Annotation> > annotationSet = annotations.entrySet();
		
		/*
		for(Map.Entry<Long,Way> w: waySet)
		{
			ways.get(w.getKey()).reproject(newProj);
		}
		*/
		for(Way w: ways)
			w.reproject(newProj);
		
		for(Map.Entry<Long,POI> p: poiSet)
		{
			pois.get(p.getKey()).reproject(newProj);
		}
		for(Map.Entry<Integer,Annotation> a: annotationSet)
		{
			annotations.get(a.getKey()).reproject(newProj);
		}
		proj=newProj;
	}
	
	public void operateOnWays(WayVisitor visitor)
	{
		/*
		Set<Map.Entry<Long,Way> > waySet = ways.entrySet();
		
		for(Map.Entry<Long,Way> w: waySet)
			visitor.visit(w.getValue());
		*/
		for(Way w: ways)
			visitor.visit(w);
	}
	
	public void operateOnPOIs (POIVisitor visitor)
	{
		Set<Map.Entry<Long, POI>> poiSet = pois.entrySet();
		for(Map.Entry<Long, POI> p: poiSet)
			visitor.visit(p.getValue());
	}
	
	public void operateOnAnnotations(AnnotationVisitor visitor)
	{
		Set<Map.Entry<Integer,Annotation> > annSet = annotations.entrySet();
		
		for(Map.Entry<Integer,Annotation> a: annSet)
			visitor.visit(a.getValue());
	}
	
	public void operateOnNearbyWays(WayVisitor visitor, Point point, double distance)
	{
		
		//Set<Map.Entry<Long,Way> > waySet = ways.entrySet();
		
		/*
		for(Map.Entry<Long,Way> w: waySet)
		{
			way=w.getValue();
			if(way.distanceTo(point)<=distance)
				visitor.visit(way);
		}
		*/
		for(Way w: ways)
		{
			// IMPORTANT!!! 130715 changed to haversineDistanceTo(). I don't think this breaks anything!
			double d = w.haversineDistanceTo(point);
		
			if(w.haversineDistanceTo(point)<=distance)
				visitor.visit(w);
		}
		
	}
	
	public void operateOnNearbyPOIs (POIVisitor visitor, Point pointLL, double distanceMetres)
	{
		Set<Map.Entry<Long, POI>> poiSet = pois.entrySet();
		
		for(Map.Entry<Long, POI> p: poiSet)
		{
			Point unproj = (proj==null) ? p.getValue().getPoint(): proj.unproject(p.getValue().getPoint());
		
			if(Algorithms.haversineDist(pointLL.x, pointLL.y, unproj.x, unproj.y) <= distanceMetres)
				visitor.visit(p.getValue());
		}
	}
	
	public ArrayList<POI> getPOIsByKey(String key)
	{
		return getPOIsByType(key,"*");
	}
	
	public ArrayList<POI> getPOIsByType(String key,String val)
	{
		ArrayList<POI> poiReturned=new ArrayList<POI>();
		Set<Map.Entry<Long,POI> > poiSet = pois.entrySet();
		for(Map.Entry<Long,POI> p: poiSet)
		{
			if(p.getValue().containsKey(key) && (val.equals("*") || p.getValue().getValue(key).equals(val)))
				poiReturned.add(p.getValue());
		}
		return poiReturned;
	}
	
	public ArrayList<Annotation> getAnnotations()
	{
		ArrayList<Annotation> anns=new ArrayList<Annotation>();
		Set<Map.Entry<Integer, Annotation> > annSet = annotations.entrySet();
		for(Map.Entry<Integer,Annotation> a: annSet)
		{
			anns.add(a.getValue());
		}
		return anns;
	}
	
	public POI getPOIById(long id)
	{
		return pois.get(id);
	}
	
	
	public Way getWayById(long id)
	{
		//return ways.get(id);
		for(Way w: ways)
			if(w.getId()==id)
				return w;
		return null;
	}
	
	public Annotation getAnnotationById(int id)
	{
		return annotations.get(id);
	}
	
	// limit is in whatever the units the projection uses
	public Annotation findNearestAnnotation(Point inPoint, double limit, Projection inProj)
	{
		if (inProj!=null)
			inPoint = inProj.unproject(inPoint);
		if(proj != null)
			inPoint = proj.project(inPoint);
		
	
		Annotation found = null;
		double lastDist = Double.MAX_VALUE, d = 0.0;
		for(Map.Entry<Integer, Annotation> e : annotations.entrySet())
		{
			d = inPoint.distanceTo(e.getValue().getPoint());
			
			if(d <= limit && d < lastDist)
			{
				lastDist = d;
				found= e.getValue();
			}
			
		}
		return found;
	}
	
	public Iterator<Long> poiIterator()
	{
	    return pois.keySet().iterator();
	}
	
	/*
	public Iterator<Long> wayIterator()
	{
	    return ways.keySet().iterator();
    }
	*/
	
	public Iterator<Way> wayIterator()
	{
		return ways.iterator();
	}
	
	public int nPOIs()
	{
	    return pois.size();
	}
	
	public int nWays()
	{
	    return ways.size();
	}
}
