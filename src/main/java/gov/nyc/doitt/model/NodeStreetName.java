package gov.nyc.doitt.model;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "node_stname")
public class NodeStreetName {
	@EmbeddedId
	private NodeStreetNameID id;
	

	public NodeStreetNameID getId() {
		return id;
	}


	public void setId(NodeStreetNameID id) {
		this.id = id;
	}


	@Embeddable
	public static class NodeStreetNameID implements Serializable {
	/**
		 * 
		 */
		private static final long serialVersionUID = -3325435067085633227L;
	private String nodeid;
	private String stname;
	public String getNodeid() {
		return nodeid;
	}
	public void setNodeid(String nodeid) {
		this.nodeid = nodeid;
	}
	public String getStname() {
		return stname;
	}
	public void setStname(String stname) {
		this.stname = stname;
	}

	}
}
