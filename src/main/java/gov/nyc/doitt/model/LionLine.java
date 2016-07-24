package gov.nyc.doitt.model;

import java.io.Serializable;

import javax.persistence.*;

import org.hibernate.annotations.Type;

import com.vividsolutions.jts.geom.Geometry;

import java.math.BigDecimal;


/**
 * The persistent class for the lion_line database table.
 * 
 */
@Entity
@Table(name="lion_line")
@NamedQuery(name="LionLine.findAll", query="SELECT l FROM LionLine l")
public class LionLine implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private Integer gid;

	private BigDecimal arccenterx;

	private BigDecimal arccentery;

	private String bikelane;

	@Column(name="boe_lgc")
	private String boeLgc;

	private String borobndry;

	private String conparity;

	private String curveflag;

	private String facecode;

	private String fcc;

	private String fdnyid;

	private String featuretyp;

	private BigDecimal fromleft;

	private BigDecimal fromright;

	private BigDecimal genericid;

    @Column(name = "geom", columnDefinition = "Geometry")
    @Type(type = "org.hibernate.spatial.GeometryType")
    private Geometry geom;

	private String incexflag;

	@Column(name="join_id")
	private String joinId;

	@Column(name="l_cd")
	private String lCd;

	private String lassmdist;

	private String latomicpol;

	private String lblockface;

	private BigDecimal lboro;

	private String lcb2000;

	private String lcb2000suf;

	private String lcb2010;

	private String lcb2010suf;

	private String lct1990;

	private String lct1990suf;

	private String lct2000;

	private String lct2000suf;

	private String lct2010;

	private String lct2010suf;

	private String legacyid;

	private String lelectdist;

	private String lgc1;

	private String lgc2;

	private String lgc3;

	private String lgc4;

	private String lgc5;

	private String lgc6;

	private String lgc7;

	private String lgc8;

	private String lgc9;

	@Column(name="lhi_hyphen")
	private String lhiHyphen;

	@Column(name="llo_hyphen")
	private String lloHyphen;

	private String locstatus;

	private String lschldist;

	private String lsubsect;

	private String lzip;

	private String mapfrom;

	private String mapto;

	@Column(name="mh_ri_flag")
	private String mhRiFlag;

	private String nodeidfrom;

	private String nodeidto;

	private String nodelevelf;

	private String nodelevelt;

	private String nonped;

	private String nypdid;

	private BigDecimal physicalid;

	@Column(name="r_cd")
	private String rCd;

	private BigDecimal radius;

	private String rassmdist;

	private String ratomicpol;

	@Column(name="rb_layer")
	private String rbLayer;

	private String rblockface;

	private BigDecimal rboro;

	private String rcb2000;

	private String rcb2000suf;

	private String rcb2010;

	private String rcb2010suf;

	private String rct1990;

	private String rct1990suf;

	private String rct2000;

	private String rct2000suf;

	private String rct2010;

	private String rct2010suf;

	private String relectdist;

	@Column(name="rhi_hyphen")
	private String rhiHyphen;

	@Column(name="rlo_hyphen")
	private String rloHyphen;

	@Column(name="row_type")
	private String rowType;

	private String rschldist;

	private String rsubsect;

	@Column(name="rw_type")
	private String rwType;

	private String rzip;

	private String safstreetc;

	private String safstreetn;

	private String sandistind;

	private String segcount;

	private String segmentid;

	private String segmenttyp;

	private String seqnum;

	@Column(name="shape_leng")
	private BigDecimal shapeLeng;

	@Column(name="snow_prior")
	private String snowPrior;

	private String specaddr;

	private String splitelect;

	private String splitschl;

	private String status;

	private String street;

	private String streetcode;

	@Column(name="streetwi_1")
	private String streetwi1;

	private BigDecimal streetwidt;

	private BigDecimal toleft;

	private BigDecimal toright;

	private String trafdir;

	private String trafsrc;

	private String twisted;

	private BigDecimal xfrom;

	private BigDecimal xto;

	private BigDecimal yfrom;

	private BigDecimal yto;

	public LionLine() {
	}

	public Integer getGid() {
		return this.gid;
	}

	public void setGid(Integer gid) {
		this.gid = gid;
	}

	public BigDecimal getArccenterx() {
		return this.arccenterx;
	}

	public void setArccenterx(BigDecimal arccenterx) {
		this.arccenterx = arccenterx;
	}

	public BigDecimal getArccentery() {
		return this.arccentery;
	}

	public void setArccentery(BigDecimal arccentery) {
		this.arccentery = arccentery;
	}

	public String getBikelane() {
		return this.bikelane;
	}

	public void setBikelane(String bikelane) {
		this.bikelane = bikelane;
	}

	public String getBoeLgc() {
		return this.boeLgc;
	}

	public void setBoeLgc(String boeLgc) {
		this.boeLgc = boeLgc;
	}

	public String getBorobndry() {
		return this.borobndry;
	}

	public void setBorobndry(String borobndry) {
		this.borobndry = borobndry;
	}

	public String getConparity() {
		return this.conparity;
	}

	public void setConparity(String conparity) {
		this.conparity = conparity;
	}

	public String getCurveflag() {
		return this.curveflag;
	}

	public void setCurveflag(String curveflag) {
		this.curveflag = curveflag;
	}

	public String getFacecode() {
		return this.facecode;
	}

	public void setFacecode(String facecode) {
		this.facecode = facecode;
	}

	public String getFcc() {
		return this.fcc;
	}

	public void setFcc(String fcc) {
		this.fcc = fcc;
	}

	public String getFdnyid() {
		return this.fdnyid;
	}

	public void setFdnyid(String fdnyid) {
		this.fdnyid = fdnyid;
	}

	public String getFeaturetyp() {
		return this.featuretyp;
	}

	public void setFeaturetyp(String featuretyp) {
		this.featuretyp = featuretyp;
	}

	public BigDecimal getFromleft() {
		return this.fromleft;
	}

	public void setFromleft(BigDecimal fromleft) {
		this.fromleft = fromleft;
	}

	public BigDecimal getFromright() {
		return this.fromright;
	}

	public void setFromright(BigDecimal fromright) {
		this.fromright = fromright;
	}

	public BigDecimal getGenericid() {
		return this.genericid;
	}

	public void setGenericid(BigDecimal genericid) {
		this.genericid = genericid;
	}



	public Geometry getGeom() {
		return geom;
	}

	public void setGeom(Geometry geom) {
		this.geom = geom;
	}

	public String getIncexflag() {
		return this.incexflag;
	}

	public void setIncexflag(String incexflag) {
		this.incexflag = incexflag;
	}

	public String getJoinId() {
		return this.joinId;
	}

	public void setJoinId(String joinId) {
		this.joinId = joinId;
	}

	public String getLCd() {
		return this.lCd;
	}

	public void setLCd(String lCd) {
		this.lCd = lCd;
	}

	public String getLassmdist() {
		return this.lassmdist;
	}

	public void setLassmdist(String lassmdist) {
		this.lassmdist = lassmdist;
	}

	public String getLatomicpol() {
		return this.latomicpol;
	}

	public void setLatomicpol(String latomicpol) {
		this.latomicpol = latomicpol;
	}

	public String getLblockface() {
		return this.lblockface;
	}

	public void setLblockface(String lblockface) {
		this.lblockface = lblockface;
	}

	public BigDecimal getLboro() {
		return this.lboro;
	}

	public void setLboro(BigDecimal lboro) {
		this.lboro = lboro;
	}

	public String getLcb2000() {
		return this.lcb2000;
	}

	public void setLcb2000(String lcb2000) {
		this.lcb2000 = lcb2000;
	}

	public String getLcb2000suf() {
		return this.lcb2000suf;
	}

	public void setLcb2000suf(String lcb2000suf) {
		this.lcb2000suf = lcb2000suf;
	}

	public String getLcb2010() {
		return this.lcb2010;
	}

	public void setLcb2010(String lcb2010) {
		this.lcb2010 = lcb2010;
	}

	public String getLcb2010suf() {
		return this.lcb2010suf;
	}

	public void setLcb2010suf(String lcb2010suf) {
		this.lcb2010suf = lcb2010suf;
	}

	public String getLct1990() {
		return this.lct1990;
	}

	public void setLct1990(String lct1990) {
		this.lct1990 = lct1990;
	}

	public String getLct1990suf() {
		return this.lct1990suf;
	}

	public void setLct1990suf(String lct1990suf) {
		this.lct1990suf = lct1990suf;
	}

	public String getLct2000() {
		return this.lct2000;
	}

	public void setLct2000(String lct2000) {
		this.lct2000 = lct2000;
	}

	public String getLct2000suf() {
		return this.lct2000suf;
	}

	public void setLct2000suf(String lct2000suf) {
		this.lct2000suf = lct2000suf;
	}

	public String getLct2010() {
		return this.lct2010;
	}

	public void setLct2010(String lct2010) {
		this.lct2010 = lct2010;
	}

	public String getLct2010suf() {
		return this.lct2010suf;
	}

	public void setLct2010suf(String lct2010suf) {
		this.lct2010suf = lct2010suf;
	}

	public String getLegacyid() {
		return this.legacyid;
	}

	public void setLegacyid(String legacyid) {
		this.legacyid = legacyid;
	}

	public String getLelectdist() {
		return this.lelectdist;
	}

	public void setLelectdist(String lelectdist) {
		this.lelectdist = lelectdist;
	}

	public String getLgc1() {
		return this.lgc1;
	}

	public void setLgc1(String lgc1) {
		this.lgc1 = lgc1;
	}

	public String getLgc2() {
		return this.lgc2;
	}

	public void setLgc2(String lgc2) {
		this.lgc2 = lgc2;
	}

	public String getLgc3() {
		return this.lgc3;
	}

	public void setLgc3(String lgc3) {
		this.lgc3 = lgc3;
	}

	public String getLgc4() {
		return this.lgc4;
	}

	public void setLgc4(String lgc4) {
		this.lgc4 = lgc4;
	}

	public String getLgc5() {
		return this.lgc5;
	}

	public void setLgc5(String lgc5) {
		this.lgc5 = lgc5;
	}

	public String getLgc6() {
		return this.lgc6;
	}

	public void setLgc6(String lgc6) {
		this.lgc6 = lgc6;
	}

	public String getLgc7() {
		return this.lgc7;
	}

	public void setLgc7(String lgc7) {
		this.lgc7 = lgc7;
	}

	public String getLgc8() {
		return this.lgc8;
	}

	public void setLgc8(String lgc8) {
		this.lgc8 = lgc8;
	}

	public String getLgc9() {
		return this.lgc9;
	}

	public void setLgc9(String lgc9) {
		this.lgc9 = lgc9;
	}

	public String getLhiHyphen() {
		return this.lhiHyphen;
	}

	public void setLhiHyphen(String lhiHyphen) {
		this.lhiHyphen = lhiHyphen;
	}

	public String getLloHyphen() {
		return this.lloHyphen;
	}

	public void setLloHyphen(String lloHyphen) {
		this.lloHyphen = lloHyphen;
	}

	public String getLocstatus() {
		return this.locstatus;
	}

	public void setLocstatus(String locstatus) {
		this.locstatus = locstatus;
	}

	public String getLschldist() {
		return this.lschldist;
	}

	public void setLschldist(String lschldist) {
		this.lschldist = lschldist;
	}

	public String getLsubsect() {
		return this.lsubsect;
	}

	public void setLsubsect(String lsubsect) {
		this.lsubsect = lsubsect;
	}

	public String getLzip() {
		return this.lzip;
	}

	public void setLzip(String lzip) {
		this.lzip = lzip;
	}

	public String getMapfrom() {
		return this.mapfrom;
	}

	public void setMapfrom(String mapfrom) {
		this.mapfrom = mapfrom;
	}

	public String getMapto() {
		return this.mapto;
	}

	public void setMapto(String mapto) {
		this.mapto = mapto;
	}

	public String getMhRiFlag() {
		return this.mhRiFlag;
	}

	public void setMhRiFlag(String mhRiFlag) {
		this.mhRiFlag = mhRiFlag;
	}

	public String getNodeidfrom() {
		return this.nodeidfrom;
	}

	public void setNodeidfrom(String nodeidfrom) {
		this.nodeidfrom = nodeidfrom;
	}

	public String getNodeidto() {
		return this.nodeidto;
	}

	public void setNodeidto(String nodeidto) {
		this.nodeidto = nodeidto;
	}

	public String getNodelevelf() {
		return this.nodelevelf;
	}

	public void setNodelevelf(String nodelevelf) {
		this.nodelevelf = nodelevelf;
	}

	public String getNodelevelt() {
		return this.nodelevelt;
	}

	public void setNodelevelt(String nodelevelt) {
		this.nodelevelt = nodelevelt;
	}

	public String getNonped() {
		return this.nonped;
	}

	public void setNonped(String nonped) {
		this.nonped = nonped;
	}

	public String getNypdid() {
		return this.nypdid;
	}

	public void setNypdid(String nypdid) {
		this.nypdid = nypdid;
	}

	public BigDecimal getPhysicalid() {
		return this.physicalid;
	}

	public void setPhysicalid(BigDecimal physicalid) {
		this.physicalid = physicalid;
	}

	public String getRCd() {
		return this.rCd;
	}

	public void setRCd(String rCd) {
		this.rCd = rCd;
	}

	public BigDecimal getRadius() {
		return this.radius;
	}

	public void setRadius(BigDecimal radius) {
		this.radius = radius;
	}

	public String getRassmdist() {
		return this.rassmdist;
	}

	public void setRassmdist(String rassmdist) {
		this.rassmdist = rassmdist;
	}

	public String getRatomicpol() {
		return this.ratomicpol;
	}

	public void setRatomicpol(String ratomicpol) {
		this.ratomicpol = ratomicpol;
	}

	public String getRbLayer() {
		return this.rbLayer;
	}

	public void setRbLayer(String rbLayer) {
		this.rbLayer = rbLayer;
	}

	public String getRblockface() {
		return this.rblockface;
	}

	public void setRblockface(String rblockface) {
		this.rblockface = rblockface;
	}

	public BigDecimal getRboro() {
		return this.rboro;
	}

	public void setRboro(BigDecimal rboro) {
		this.rboro = rboro;
	}

	public String getRcb2000() {
		return this.rcb2000;
	}

	public void setRcb2000(String rcb2000) {
		this.rcb2000 = rcb2000;
	}

	public String getRcb2000suf() {
		return this.rcb2000suf;
	}

	public void setRcb2000suf(String rcb2000suf) {
		this.rcb2000suf = rcb2000suf;
	}

	public String getRcb2010() {
		return this.rcb2010;
	}

	public void setRcb2010(String rcb2010) {
		this.rcb2010 = rcb2010;
	}

	public String getRcb2010suf() {
		return this.rcb2010suf;
	}

	public void setRcb2010suf(String rcb2010suf) {
		this.rcb2010suf = rcb2010suf;
	}

	public String getRct1990() {
		return this.rct1990;
	}

	public void setRct1990(String rct1990) {
		this.rct1990 = rct1990;
	}

	public String getRct1990suf() {
		return this.rct1990suf;
	}

	public void setRct1990suf(String rct1990suf) {
		this.rct1990suf = rct1990suf;
	}

	public String getRct2000() {
		return this.rct2000;
	}

	public void setRct2000(String rct2000) {
		this.rct2000 = rct2000;
	}

	public String getRct2000suf() {
		return this.rct2000suf;
	}

	public void setRct2000suf(String rct2000suf) {
		this.rct2000suf = rct2000suf;
	}

	public String getRct2010() {
		return this.rct2010;
	}

	public void setRct2010(String rct2010) {
		this.rct2010 = rct2010;
	}

	public String getRct2010suf() {
		return this.rct2010suf;
	}

	public void setRct2010suf(String rct2010suf) {
		this.rct2010suf = rct2010suf;
	}

	public String getRelectdist() {
		return this.relectdist;
	}

	public void setRelectdist(String relectdist) {
		this.relectdist = relectdist;
	}

	public String getRhiHyphen() {
		return this.rhiHyphen;
	}

	public void setRhiHyphen(String rhiHyphen) {
		this.rhiHyphen = rhiHyphen;
	}

	public String getRloHyphen() {
		return this.rloHyphen;
	}

	public void setRloHyphen(String rloHyphen) {
		this.rloHyphen = rloHyphen;
	}

	public String getRowType() {
		return this.rowType;
	}

	public void setRowType(String rowType) {
		this.rowType = rowType;
	}

	public String getRschldist() {
		return this.rschldist;
	}

	public void setRschldist(String rschldist) {
		this.rschldist = rschldist;
	}

	public String getRsubsect() {
		return this.rsubsect;
	}

	public void setRsubsect(String rsubsect) {
		this.rsubsect = rsubsect;
	}

	public String getRwType() {
		return this.rwType;
	}

	public void setRwType(String rwType) {
		this.rwType = rwType;
	}

	public String getRzip() {
		return this.rzip;
	}

	public void setRzip(String rzip) {
		this.rzip = rzip;
	}

	public String getSafstreetc() {
		return this.safstreetc;
	}

	public void setSafstreetc(String safstreetc) {
		this.safstreetc = safstreetc;
	}

	public String getSafstreetn() {
		return this.safstreetn;
	}

	public void setSafstreetn(String safstreetn) {
		this.safstreetn = safstreetn;
	}

	public String getSandistind() {
		return this.sandistind;
	}

	public void setSandistind(String sandistind) {
		this.sandistind = sandistind;
	}

	public String getSegcount() {
		return this.segcount;
	}

	public void setSegcount(String segcount) {
		this.segcount = segcount;
	}

	public String getSegmentid() {
		return this.segmentid;
	}

	public void setSegmentid(String segmentid) {
		this.segmentid = segmentid;
	}

	public String getSegmenttyp() {
		return this.segmenttyp;
	}

	public void setSegmenttyp(String segmenttyp) {
		this.segmenttyp = segmenttyp;
	}

	public String getSeqnum() {
		return this.seqnum;
	}

	public void setSeqnum(String seqnum) {
		this.seqnum = seqnum;
	}

	public BigDecimal getShapeLeng() {
		return this.shapeLeng;
	}

	public void setShapeLeng(BigDecimal shapeLeng) {
		this.shapeLeng = shapeLeng;
	}

	public String getSnowPrior() {
		return this.snowPrior;
	}

	public void setSnowPrior(String snowPrior) {
		this.snowPrior = snowPrior;
	}

	public String getSpecaddr() {
		return this.specaddr;
	}

	public void setSpecaddr(String specaddr) {
		this.specaddr = specaddr;
	}

	public String getSplitelect() {
		return this.splitelect;
	}

	public void setSplitelect(String splitelect) {
		this.splitelect = splitelect;
	}

	public String getSplitschl() {
		return this.splitschl;
	}

	public void setSplitschl(String splitschl) {
		this.splitschl = splitschl;
	}

	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStreet() {
		return this.street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getStreetcode() {
		return this.streetcode;
	}

	public void setStreetcode(String streetcode) {
		this.streetcode = streetcode;
	}

	public String getStreetwi1() {
		return this.streetwi1;
	}

	public void setStreetwi1(String streetwi1) {
		this.streetwi1 = streetwi1;
	}

	public BigDecimal getStreetwidt() {
		return this.streetwidt;
	}

	public void setStreetwidt(BigDecimal streetwidt) {
		this.streetwidt = streetwidt;
	}

	public BigDecimal getToleft() {
		return this.toleft;
	}

	public void setToleft(BigDecimal toleft) {
		this.toleft = toleft;
	}

	public BigDecimal getToright() {
		return this.toright;
	}

	public void setToright(BigDecimal toright) {
		this.toright = toright;
	}

	public String getTrafdir() {
		return this.trafdir;
	}

	public void setTrafdir(String trafdir) {
		this.trafdir = trafdir;
	}

	public String getTrafsrc() {
		return this.trafsrc;
	}

	public void setTrafsrc(String trafsrc) {
		this.trafsrc = trafsrc;
	}

	public String getTwisted() {
		return this.twisted;
	}

	public void setTwisted(String twisted) {
		this.twisted = twisted;
	}

	public BigDecimal getXfrom() {
		return this.xfrom;
	}

	public void setXfrom(BigDecimal xfrom) {
		this.xfrom = xfrom;
	}

	public BigDecimal getXto() {
		return this.xto;
	}

	public void setXto(BigDecimal xto) {
		this.xto = xto;
	}

	public BigDecimal getYfrom() {
		return this.yfrom;
	}

	public void setYfrom(BigDecimal yfrom) {
		this.yfrom = yfrom;
	}

	public BigDecimal getYto() {
		return this.yto;
	}

	public void setYto(BigDecimal yto) {
		this.yto = yto;
	}

}
