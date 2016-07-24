package gov.nyc.doitt.model.repository;

import gov.nyc.doitt.model.NodeStreetName;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

@Repository
public class NodeIntersectionDAO {
    @PersistenceContext
    private EntityManager em;
    //select n.* from lion_line l, node_stname n where (n.nodeid=trim(Leading '0' from l.nodeidfrom) or n.nodeid=trim(Leading '0' from l.nodeidto)) and l.segmentID='0022428'
    public List<NodeStreetName> getNodeStreetnameBySegmentID(String segmentID){
        Query q = em.createNativeQuery(
        	    "select n.nodeid,n.stname from lion_line l, node_stname n where (n.nodeid=trim(Leading '0' from l.nodeidfrom) or n.nodeid=trim(Leading '0' from l.nodeidto)) and l.segmentID=:segmentid",
                NodeStreetName.class);

        q.setParameter("segmentid", segmentID);
        return q.getResultList();
    	
    }
    @Cacheable("from")
    public List<NodeStreetName> getNodeStreetnameBySegmentIDFrom(String segmentID){
    	Query q = em.createNativeQuery(
        	    "select n.nodeid,n.stname from lion_line l, node_stname n where n.nodeid=trim(Leading '0' from l.nodeidfrom) and l.segmentID=:segmentid",
                NodeStreetName.class);

        q.setParameter("segmentid", segmentID);
        return q.getResultList();
    	
    }
    @Cacheable("to")
    public List<NodeStreetName> getNodeStreetnameBySegmentIDTo(String segmentID){
    	Query q = em.createNativeQuery(
        	    "select n.nodeid,n.stname from lion_line l, node_stname n where n.nodeid=trim(Leading '0' from l.nodeidto) and l.segmentID=:segmentid",
                NodeStreetName.class);

        q.setParameter("segmentid", segmentID);
        return q.getResultList();
    	
    }
}
