package gov.nyc.doitt.model.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

@Repository
public class NodeIntersectionDAO {
    @PersistenceContext
    private EntityManager em;
    //select n.* from lion_line l, node_stname n where (n.nodeid=trim(Leading '0' from l.nodeidfrom) or n.nodeid=trim(Leading '0' from l.nodeidto)) and l.segmentID='0022428'

}
