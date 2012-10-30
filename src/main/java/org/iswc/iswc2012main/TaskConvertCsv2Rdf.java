package org.iswc.iswc2012main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.iswc.util.ToolText2Rdf;
import org.iswc.vocabulary.DGTWC;
import org.iswc.vocabulary.SWC;
import org.iswc.vocabulary.SWRC;
import org.iswc.vocabulary.BIBO;

import com.csvreader.CsvReader;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Seq;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import sw4j.rdf.util.RDFSYNTAX;
import sw4j.rdf.util.ToolJena;
import sw4j.util.ToolHash;

public class TaskConvertCsv2Rdf {

	public static void main(String[] args){
		Model m = ModelFactory.createDefaultModel();
		m.add(createMetaModel());
		
		try {
			processConf(m);
			processEvent(m);
			processPaper(m);
			processPerson(m);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		updateModelByInverseProperty(m, SWC.isSubEventOf, SWC.isSuperEventOf );
		updateModelByTransitiveProperty(m, SWC.isSubEventOf);
		
		updateModelByInverseProperty(m, SWC.isSubEventOf, SWC.isSuperEventOf );
		updateModelByInverseProperty(m, FOAF.maker, FOAF.made );
		updateModelByInverseProperty(m, SWC.holdsRole, SWC.heldBy);
		updateModelByInverseProperty(m, SWC.hasRole, SWC.isRoleAt);
		updateModelByInverseProperty(m, SWRC.affiliation, FOAF.member);
		
		updateModelByRemoveSameSandO(m);
		
		updateGraphMeta(m);
		
		updateNsPrefix(m);
		ToolJena.printModel(m, "N3", "");
		ToolJena.printModelToFile(m, Config.FILE.iswc2012_complete.getFile());
		ToolJena.printModelToFile(m, RDFSYNTAX.TURTLE, Config.FILE.iswc2012_complete.getFile(Config.EXT.ttl), false);
	}
	private static void updateGraphMeta(Model m) {
		Resource resGraph = createResource(URI_BASE_CONF, "complete");
		m.add(resGraph, RDF.type, DGTWC.Dataset);
		m.add(resGraph, DCTerms.hasVersion, Config.VERSION);
		String timeNow = formatXSDTime(System.currentTimeMillis());
		m.add(resGraph, DCTerms.modified, m.createTypedLiteral(timeNow, XSDDatatype.XSDdateTime));
		m.add(resGraph, DCTerms.creator, createResource(URI_BASE_PERSON, "Li Ding"));
		
		//the last triple here
		m.addLiteral(resGraph, DGTWC.number_of_triples, m.size()+1);
	}
	private static void updateModelByTransitiveProperty(Model m, Property p){
		ToolJena.updateModelTranstive(m, p);
		
	}
	
	private static void updateModelByInverseProperty(Model m, Property p, Property pInverse){
		for (Statement stmt: m.listStatements(null, p, (RDFNode) null).toSet()){
			if (stmt.getObject().isLiteral())
				continue;
			m.add(stmt.getObject().asResource(), pInverse, stmt.getSubject());
		}
		for (Statement stmt: m.listStatements(null, pInverse, (RDFNode) null).toSet()){
			if (stmt.getObject().isLiteral())
				continue;
			m.add(stmt.getObject().asResource(), p, stmt.getSubject());
		}
	}
	
	private static void updateModelByRemoveSameSandO(Model m){
		StmtIterator iter = m.listStatements();
		while(iter.hasNext()){
			Statement stmt = iter.nextStatement();
			if (stmt.getSubject().equals(stmt.getObject())){
				iter.remove();
			}
		}
	}


	public static final String URI_BASE_ROOT = "http://data.semanticweb.org/";

	public static final String URI_BASE_CONF = URI_BASE_ROOT+"conference/iswc/2012";
	public static final String URI_BASE_PERSON = URI_BASE_ROOT+"person";
	public static final String URI_BASE_ORGANIZATION = URI_BASE_ROOT+"organization";
	public static final String URI_BASE_WORKSHOP= URI_BASE_ROOT+"workshop";
	
	//iswc2012 specific
	public static final String URI_BASE_PLACE = URI_BASE_CONF+"/place";
	public static final String URI_BASE_CHAIR = URI_BASE_CONF+"/chair";
	public static final String URI_BASE_SPEAKER = URI_BASE_CONF+"/speaker";
	public static final String URI_BASE_PANELLIST = URI_BASE_CONF+"/panellist";
	public static final String URI_BASE_MEDIATOR = URI_BASE_CONF+"/mediator";
	public static final String URI_NS_ICAL = "http://www.w3.org/2002/12/cal/ical#";

	public static final Resource R_CONF_VENUE = createResource(URI_BASE_PLACE ,"The Boston Park Plaza Hotel & Towers"); 

	public static Model createMetaModel(){
		Model m = ModelFactory.createDefaultModel();
		{
			Resource s = addNamedInstance(m, URI_BASE_PLACE, "The Boston Park Plaza Hotel & Towers");
			s.addProperty(RDF.type, SWC.ConferenceVenuePlace);
			s.addProperty(FOAF.homepage, ResourceFactory.createResource("http://bostonparkplaza.com/") );
			s.addProperty(SWRC.phone, "617.426.2000");
			s.addProperty(SWRC.address, "50 Park Plaza at Arlington Street, Boston, MA 02116");
			s.addProperty(SWRC.fax, "617.426.5545");
			s.addProperty(RDFS.comment, "Located in the heart of historic Back Bay, The Boston Park Plaza Hotel & Towers is one of Boston�s most recognized and renowned landmarks. The Boston Park Plaza, a member of Historic Hotels of America, was constructed in March, 1927 as part of the E.M. Statler Empire. With an unsurpassed Boston address, the hotel is located only 3 miles from Logan International Airport and only 200 yards from the nation�s first public parks, Boston Common & the Public Garden. The hotel is easily accessible to public transportation, world renowned shopping along Newbury Street, Faneuil Hall Marketplace, the Theatre & Financial Districts and most historic landmarks. Rich in history, The Boston Park Plaza Hotel & Towers has distinguished itself with classic elegance and personalized service that continues to attract travelers from all over the world who visit  Boston for business, leisure or special events.");
		}

		return m;
	}
	
	
	
	enum CsvHeader{
		xProperty(null),
		xValue(null),
		xKeyChair(null, URI_BASE_CHAIR, null, null),
		xLabelChair(null),
		xKey(URI_BASE_CONF),
		xListChair(new Property[]{SWC.heldBy}, URI_BASE_PERSON, ";", null),
		xKeyTrack(new Property[]{SWC.isRoleAt}, URI_BASE_CONF, null, null),
		
		conferenceChair ( SWC.hasRole, URI_BASE_CHAIR, SWC.heldBy, URI_BASE_PERSON, ";", 
				new RDFNode[][] {
				{RDF.type, SWC.Chair},
				} ),

		
		keyEvent (null, URI_BASE_CONF, null, null),
		typeEvent	( new Property[]{RDF.type},  SWC.getURI(), null, null),
		room (	 new Property[]{SWC.hasLocation}, URI_BASE_PLACE,  ";", new RDFNode[][] {
				{RDF.type, SWC.MeetingRoomPlace},
				{SWC.isPartOf, R_CONF_VENUE},
				}),
		label (	 new Property[]{RDFS.label}, null, null, null),
		keySuperEvent (	 new Property[]{SWC.isSubEventOf},	URI_BASE_CONF,	null, null),
		keySubEvent (	 new Property[]{SWC.isSuperEventOf},	URI_BASE_CONF,	null, null),
		homepage (	 new Property[]{FOAF.homepage}, 	null,	null, null),
		hasAcronym(	SWC.getURI() ), 
		dtStart(	URI_NS_ICAL ),
		dtEnd(	URI_NS_ICAL ),
		sessionChair ( SWC.hasRole, URI_BASE_CHAIR, SWC.heldBy, URI_BASE_PERSON, ";", 
				new RDFNode[][] {
				{RDF.type, SWC.Chair},
				{RDFS.label, ResourceFactory.createPlainLiteral("ISWC2012 Session Chair")},
				} ),
		keynoteSpeaker( SWC.hasRole, URI_BASE_SPEAKER, SWC.heldBy, URI_BASE_PERSON, ";", 
				new RDFNode[][] {
				{RDF.type, ResourceFactory.createResource(SWC.getURI() + "KeynoteSpeaker")},
				{RDFS.label, ResourceFactory.createPlainLiteral("ISWC2012 Keynote Speaker")},
				} ),
		panellist( SWC.hasRole, URI_BASE_PANELLIST, SWC.heldBy, URI_BASE_PERSON, ";", 
				new RDFNode[][] {
				{RDF.type, ResourceFactory.createResource(SWC.getURI() + "Panellist")},
				{RDFS.label, ResourceFactory.createPlainLiteral("ISWC2012 Panellist")},
				} ),
		mediator( SWC.hasRole, URI_BASE_MEDIATOR, SWC.heldBy, URI_BASE_PERSON, ";", 
				new RDFNode[][] {
				{RDF.type, ResourceFactory.createResource(SWC.getURI() + "Mediator")},
				{RDFS.label, ResourceFactory.createPlainLiteral("ISWC2012 Session Mediator")},
				} ),
		hasAbstract( new Property[]{ SWRC.abstract_java}, null, null, null ),

		authors( new Property[]{FOAF.maker, DC.creator, DCTerms.creator, SWRC.author},
				URI_BASE_PERSON, 
				";", 
				new RDFNode[][] {
				{RDF.type, FOAF.Person},
				} ),
		keywords( new Property[]{DC.subject, DCTerms.subject}, null, ",", null),
		title( new Property[]{DC.title, DCTerms.title, RDFS.label}, null, null, null),
		keyPaper(null, URI_BASE_CONF, null, new RDFNode[][] {
				{RDF.type, SWRC.InProceedings},
//				{SWC.isPartOf, createResource(URI_BASE_CONF, "proceedings")},
//				{SWC.hasTopic, ResourceFactory.createResource("http://dbpedia.org/resource/Semantic_Web")},
				}),
		keyProceedings(new Property[]{SWC.isPartOf}, 
				URI_BASE_CONF,
				null,
				null),
		nameTrack(null),
		nameGroup(null),
		sessionTimeStart(null),
		sessionTimeEnd(null),
		sessionTimeRoom(null),

				
		paperSpotlight( new Property[]{ResourceFactory.createProperty(SWRC.getURI()+"spotlight")}, null, null, null ),
		paperPdfLink( new Property[]{ResourceFactory.createProperty(SWRC.getURI()+"pdf")}, null, null, null ),						
		paperPdfLinkFile( new Property[]{ResourceFactory.createProperty(SWRC.getURI()+"pdfLocal")}, null, null, null ),						
		booktitle(  new Property[]{SWRC.booktitle}, null, null, null),
		
		idLncs( new Property[]{
				ResourceFactory.createProperty(SWRC.getURI()+"idLncs"),
				DC.identifier}, null, null, null ),
		keyTalkEvent( new Property[]{SWC.relatedToEvent}, URI_BASE_CONF, null, new RDFNode[][] {
				{RDF.type, SWC.TalkEvent},
		}),

		keySessionEvent( new Property[]{SWC.isSubEventOf}, URI_BASE_CONF, null, new RDFNode[][] {
				{RDF.type, SWC.SessionEvent},
		}),
		orderInSession( new Property[]{ ResourceFactory.createProperty(SWRC.getURI()+"orderInSession") }, null, null, null),
		
		keyPerson(null, URI_BASE_PERSON, null, new RDFNode[][] {
				{RDF.type, FOAF.Person},
				}),
		firstName( new Property[] {FOAF.firstName}, URI_BASE_PERSON, null, null ),		
		bio( new Property[] {ResourceFactory.createProperty(SWRC.getURI()+"bio") }, null , null, null ),		
		lastName( new Property[] {FOAF.surname}, URI_BASE_PERSON, null, null ),		
		organization( new Property[] {SWRC.affiliation}, URI_BASE_ORGANIZATION, ";", new RDFNode[][] {
				{RDF.type, FOAF.Organization},} ),	
		
		heldBy( new Property[]{SWC.heldBy}, URI_BASE_PERSON, ";", new RDFNode[][] {
				{RDF.type, FOAF.Person},} ),
		role( null ),
		email( null ),
		;
		Property[] listP =null;
		String uri_base = null;
		Property p1 = null;
		String uri_base1 = null;
		String deliminator = null;
		RDFNode[][] listPv1 = null;
		CsvHeader(String base_prop){
			if (null!=base_prop){
				this.listP = new Property[]{ResourceFactory.createProperty(base_prop+ name())};
			}else{
				this.listP = new Property[]{ResourceFactory.createProperty(SWC.getURI()+ name())};				
			}
		}

		CsvHeader(Property[] listProp, String base, String deliminator, RDFNode[][] listPv1){
			this.listP= listProp;
			this.uri_base =base;
			this.deliminator = deliminator;
			this.listPv1 = listPv1;
		}
		CsvHeader(Property prop, String base, Property prop1, String base1, String deliminator, RDFNode[][] listPv1){
			this (null,base, deliminator, listPv1);
			if (null!=prop)
				this.listP=new Property[]{prop};
			this.p1 =prop1;
			this.uri_base1 = base1;
			
		}		
		public String [] split(String value) {
			if (null==value || value.length()==0)
				return new String[]{};
			
			if (null==deliminator ){
				return  new String []{value};				
			}else{
				value = getPreprocessedAuthorList(this, value);
				
				return value.split(deliminator);
			}
		}
		public void updateListPv(Model m, Resource o) {
			if (null!= this.listPv1){
				for (RDFNode[] pv: this.listPv1){
					Property p1 = (Property) pv[0];
					RDFNode o1 = pv[1];
					m.add(o, p1, o1);
				}				
			}			
		}
		public void updateListP(Model m, Resource s, RDFNode o) {
			if (null!=listP){
				for (Property p: listP){
					m.add(s,p,o);
				}
			}
			
		}
	}
	

	
	
	
	
	
	
	
	
	private static String getPreprocessedAuthorList(CsvHeader key, String value){
		if (!CsvHeader.authors.equals(key))
			return value;
		
		value = value.replaceAll(" and ", ";");
		value = value.replaceAll(",",";");
		return value;
	}
	

	private static void updateProceedings(Model m, Resource sProceedings, String label, String booktitle, String editors, String month, String year, String url, String [] listId){
		m.add(sProceedings, RDF.type, SWC.Proceedings);
		m.add(sProceedings, ResourceFactory.createProperty(SWRC.getURI()+"listEditor"), editors);
		Seq editorList = m.createSeq(sProceedings.getURI()+"/editor_list");
		for (String editor: editors.split(",")){
			editor = editor.trim();
			Resource resEditor = createResource(URI_BASE_PERSON, editor);
			m.add(sProceedings, FOAF.maker, resEditor );
			m.add(sProceedings, SWRC.editor, resEditor);
			editorList.add(resEditor);
		}
		m.add(sProceedings, SWRC.booktitle, booktitle);
		m.add(sProceedings, SWRC.month, month );
		m.add(sProceedings, SWRC.year, year);
		m.add(sProceedings, RDFS.label, label);
		
		if (null!=url){
			Resource resUrl = m.createResource(url);
			m.add(sProceedings, FOAF.homepage, resUrl);
		}
		
		if (null!=listId){
			for (String id: listId){
				m.add(sProceedings, DCTerms.identifier, id);
			}
		}

		
	}
	
	public static void processConf(Model m) throws IOException{
		String szFileName = Config.FILE.data_conf.getFile().getAbsolutePath();
//		List<String> listStrEvent = pipeFileToCsvInput(szFileName);
//		for (String line: listStrEvent){
//			System.out.println(line);
//		}
		
		Resource s = createResource(URI_BASE_CONF, null);
		Resource resGraph = createResource(URI_BASE_CONF, "complete");
		m.add(s, RDF.type, SWC.ConferenceEvent);
		m.add(s, ResourceFactory.createProperty(SWC.getURI()+"hasAcronym"), "ISWC2012");
		m.add(s, ResourceFactory.createProperty(SWC.getURI()+"completeGraph"), resGraph);
		m.add(s, RDFS.seeAlso, createResource(URI_BASE_CONF, "rdf"));
		m.add(s, OWL.sameAs, ResourceFactory.createResource("http://semanticweb.org/id/ISWC2012"));
		m.add(s, FOAF.based_near, ResourceFactory.createResource("http://dbpedia.org/resource/Boston"));
		
		m.add(s, FOAF.logo, ResourceFactory.createResource("http://iswc2012.semanticweb.org/sites/default/files/iswc_logo.jpg"));


		{
			Resource sProceedings = createResource(URI_BASE_CONF, SWC.Proceedings.getLocalName());
			String editors = "Philippe Cudré-Mauroux, Jeff Heflin, Evren Sirin, Tania Tudorache, Jérôme Euzenat, Manfred Hauswirth, Josiane Xavier Parreira, Jim Hendler, Guus Schreiber, Abraham Bernstein, Eva Blomqvist";
			String label = "Proceedings of ISWC 2012";
			String booktitle = "Proceedings of 11th International Semantic Web Conference (ISWC2012), November 11 - November 15, 2012";
			String month = "Nov";
			String year = "2012";					
			String url = null;
			String [] listId = new String[]{};
			TaskConvertCsv2Rdf.updateProceedings(m, sProceedings, label, booktitle, editors, month, year, url, listId);
			m.add(s, SWC.hasRelatedDocument, sProceedings);
		}
		
		{
			Resource sProceedings = createResource(URI_BASE_CONF, "poster-demo-"+ SWC.Proceedings.getLocalName());
			String editors = "Birte Glimm, David Huynh";
			String booktitle = "Proceedings of the ISWC 2012 Posters & Demonstrations Track";
			String label = booktitle;
			String month = "Nov";
			String year = "2012";			
			String url = "http://ceur-ws.org/Vol-914/";
			String[] listId = new String [] {"urn:nbn:de:0074-914-3"};
			TaskConvertCsv2Rdf.updateProceedings(m, sProceedings, label, booktitle, editors, month, year, url, listId);
			m.add(s, SWC.hasRelatedDocument, sProceedings);
			Resource sTrack = createResource(URI_BASE_CONF, "track/poster-demo");
			m.add(sTrack, SWC.hasRelatedDocument, sProceedings);
		}

		{
			Resource sProceedings = createResource(URI_BASE_CONF, "industry-"+ SWC.Proceedings.getLocalName());
			String editors = "Tim Berners-Lee,Tom Heath, Ivan Herman";
			String booktitle = "Proceedings of the ISWC 2012 Industry Track";
			String label = booktitle;
			String month = "Nov";
			String year = "2012";
			String url = null;
			String[] listId = new String [] {};
			TaskConvertCsv2Rdf.updateProceedings(m, sProceedings , label, booktitle, editors, month, year, url, listId);
			m.add(s, SWC.hasRelatedDocument, sProceedings);
			Resource sTrack = createResource(URI_BASE_CONF, "track/industry");
			m.add(sTrack, SWC.hasRelatedDocument, sProceedings);
		}
	
		{
			Resource sProceedings= createResource(URI_BASE_CONF, "semantic-web-challenge-"+ SWC.Proceedings.getLocalName());
			String editors = "Diana Maynard, Andreas Harth";
			String booktitle = "Proceedings of the Semantic Web Challenge 2012";
			String label = booktitle;
			String month = "Nov";
			String year = "2012";
			String url = null;
			String[] listId = new String [] {};
			TaskConvertCsv2Rdf.updateProceedings(m, sProceedings, label, booktitle, editors, month, year, url, listId);
			m.add(s, SWC.hasRelatedDocument, sProceedings);
			Resource sTrack = createResource(URI_BASE_CONF, "track/semantic-web-challenge");
			m.add(sTrack, SWC.hasRelatedDocument, sProceedings);
		}

		CsvReader reader = new CsvReader(szFileName);
			reader.setSkipEmptyRecords(true);
			reader.readHeaders();
			while(reader.readRecord()){
				
				String prop = reader.get(CsvHeader.xProperty.name());
				String value = reader.get(CsvHeader.xValue.name());
				
				if (CsvHeader.label.name().equals(prop)){
					addTripleLiteral(m, s, CsvHeader.label, value);
				}else if (CsvHeader.homepage.name().equals(prop)){
					addTripleResource(m, s, CsvHeader.homepage, value);
				}else if (CsvHeader.keySubEvent.name().equals(prop)){
					addPropertyChain1(m, s, CsvHeader.keySubEvent, value);
				}else if (CsvHeader.conferenceChair.name().equals(prop)){
					
					


					String labelChair = reader.get(CsvHeader.xLabelChair. name());
					String localName = reader.get(CsvHeader.xKeyChair. name());
					if (null==localName || localName.length()==0){
						localName = ToolText2Rdf.extractLocalName(labelChair);
					}
//					addNamedInstance(m, CsvHeader.xKeyChair.uri_base, localName, SWC.ConferenceChair );
					
					labelChair = "ISWC2012 "+labelChair +" Chair"; 
					
					Resource o = createResource(CsvHeader.xKeyChair.uri_base, localName);
					m.add(s, SWC.hasRole, o);
					//addPropertyChain1(m, s, CsvHeader.conferenceChair, localName, null, null);
					m.add(o, RDFS.label, labelChair);
					m.add(o, RDF.type, SWC.Chair);
					//addTripleResource(m, o, CsvHeader.xKeyTrack, reader);
					addPropertyChain1(m, o, CsvHeader.xListChair, reader);
				} 
			}
	}
	
	private static String formatXSDTime(long time) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        String s =  simpleDateFormat.format(time);
        return  s;
	}
	
	public static void processEvent(Model m) throws IOException{
		String szFileName = Config.FILE.data_event.getFile().getAbsolutePath();
//		List<String> listStrEvent = pipeFileToCsvInput(szFileName);
//		for (String line: listStrEvent){
//			System.out.println(line);
//		}
		

			CsvReader reader = new CsvReader(szFileName);
			reader.setSkipEmptyRecords(true);
			reader.readHeaders();
			while(reader.readRecord()){
//				for (String header: reader.getHeaders()){
//					System.out.print(String.format("%s:%s\t", header, reader.get(header)));
//				}
//				System.out.println();
				
				Resource s = null;
				{
					CsvHeader h = CsvHeader.keyEvent;					
					String key = h.name();
					String value = reader.get(key);
					if (null!=value && value.startsWith("workshop")){
						s = createResource(m, URI_BASE_ROOT.substring(0, URI_BASE_ROOT.length()-1), value);
					}else{
						s = createResource(m, h.uri_base, value);						
					}
				}
				
				addPropertyChain1(m, s, CsvHeader.room, reader);
				addPropertyChain0(m, s, CsvHeader.keySuperEvent, reader, true);
				String typeEvent = reader.get(CsvHeader.typeEvent.name());
				if (null!=typeEvent && typeEvent.length()>0)
					m.add(s, RDF.type, ResourceFactory.createResource(CsvHeader.typeEvent.uri_base + typeEvent));
				
				
				addPropertyChain2(m, s, CsvHeader.sessionChair, reader, reader.get(CsvHeader.keyEvent.name()));
				addPropertyChain2(m, s, CsvHeader.keynoteSpeaker,reader,  reader.get(CsvHeader.keyEvent.name()));
				addPropertyChain2(m, s, CsvHeader.panellist, reader, reader.get(CsvHeader.keyEvent.name()));
				addPropertyChain2(m, s, CsvHeader.mediator, reader, reader.get(CsvHeader.keyEvent.name()));
				

				addTripleLiteral(m, s, CsvHeader.label, reader);
				addTripleLiteral(m, s, CsvHeader.hasAbstract, reader);
				addTripleLiteral(m, s, CsvHeader.hasAcronym, reader);
				addTripleLiteral(m, s, CsvHeader.dtStart, reader, XSDDatatype.XSDdateTime);
				addTripleLiteral(m, s, CsvHeader.dtEnd, reader, XSDDatatype.XSDdateTime);
				addTripleLiteral(m, s, CsvHeader.keywords, reader);
				addTripleResource(m, s, CsvHeader.homepage, reader);
				
				{//shortcut
					String temp = reader.get(CsvHeader.keywords.name());
					if (null!=temp && temp.length()>0)
						m.add(s, ResourceFactory.createProperty(SWRC.getURI()+"listKeyword"), temp.trim());
				}

			}
			

	}

	private static void processPaper(Model m) throws IOException {
		String szFileName =Config.FILE.data_paper.getFile().getAbsolutePath();
	//		List<String> listStrEvent = pipeFileToCsvInput(szFileName);
	//		for (String line: listStrEvent){
	//			System.out.println(line);
	//		}
		
	
		CsvReader reader = new CsvReader(szFileName);
		reader.setSkipEmptyRecords(true);
		reader.readHeaders();
		while(reader.readRecord()){
//				for (String header: reader.getHeaders()){
//					System.out.print(String.format("%s:%s\t", header, reader.get(header)));
//				}
//				System.out.println();
			
			Resource s = null;
			{
				CsvHeader h = CsvHeader.keyPaper;					
				String key = h.name();
				String value = reader.get(key);
				if (null==value || value.length()==0)
					continue;
				
				s = createResource(m, h.uri_base, value);
				
				h.updateListPv(m, s);
			}
			
			addPropertyChain1(m, s, CsvHeader.authors, reader, createResource(s.getURI(),"authors"), BIBO.authorList);
			addPropertyChain0(m, s, CsvHeader.keyProceedings, reader);

			{//shortcut
				m.add(s, ResourceFactory.createProperty(SWRC.getURI()+"listAuthor"), reader.get(CsvHeader.authors.name()).trim());
				String temp = reader.get(CsvHeader.keywords.name());
				if (null!=temp && temp.length()>0)
					m.add(s, ResourceFactory.createProperty(SWRC.getURI()+"listKeyword"), temp.trim());
			}

			addTripleLiteral(m, s, CsvHeader.title, reader);
			addTripleLiteral(m, s, CsvHeader.hasAbstract, reader);
			addTripleLiteral(m, s, CsvHeader.keywords, reader);
			addTripleLiteral(m, s, CsvHeader.idLncs, reader);
			addTripleLiteral(m, s, CsvHeader.paperSpotlight, reader);

			addTripleResource(m, s, CsvHeader.paperPdfLink, reader);

			{
				CsvHeader h = CsvHeader.keyTalkEvent;					
				String key = h.name();
				String value = reader.get(key);
				if (null!=value && value.length()>0){
					addPropertyChain0(m, s, CsvHeader.keyTalkEvent, reader);

					Resource s1 = createResource(m, h.uri_base, value);
					
					addPropertyChain0(m, s1, CsvHeader.keySessionEvent, reader);
					//addTripleLiteral(m, s1, CsvHeader.title, reader);
					addTripleLiteral(m, s1, CsvHeader.orderInSession, reader, XSDDatatype.XSDinteger);
				}else{
					//for poster/swc
					CsvHeader hTemp = CsvHeader.keySessionEvent;	
					String temp = reader.get(hTemp.name());
					Resource s1 = createResource(hTemp.uri_base, temp);
					m.add( s, SWC.relatedToEvent, s1);

				}
			}
		}
	
	}

	private static void processPerson(Model m) throws IOException {
		String szFileName = Config.FILE.data_person.getFile().getAbsolutePath();
		CsvReader reader = new CsvReader(szFileName);
		reader.setSkipEmptyRecords(true);
		reader.readHeaders();
		while(reader.readRecord()){
//				for (String header: reader.getHeaders()){
//					System.out.print(String.format("%s:%s\t", header, reader.get(header)));
//				}
//				System.out.println();
			
			Resource s = null;
			{
				CsvHeader h = CsvHeader.keyPerson;					
				String key = h.name();
				String value = reader.get(key);
				
				if (null==value ||value.length()==0)
					continue;
				
				s = addNamedInstance(m, h.uri_base, value);
				
				h.updateListPv(m, s);
			}
			
			addTripleLiteral(m, s, CsvHeader.firstName, reader);
			addTripleLiteral(m, s, CsvHeader.lastName, reader);
			addTripleLiteral(m, s, CsvHeader.bio, reader);
			
			addTripleResource(m, s, CsvHeader.homepage, reader);
		//	addPropertyChain1(m, s, CsvHeader.keyEvent, reader);
			addPropertyChain1(m, s, CsvHeader.organization, reader);
			m.add(s, ResourceFactory.createProperty(SWRC.getURI()+"listAffiliation"), reader.get(CsvHeader.organization.name()).trim());
			

			{
				String role= reader.get(CsvHeader.role.name());
				if (null!=role && role.length()>0){
					String roleName = role + " at ISWC2012";
					String keyEvent = reader.get(CsvHeader.keyEvent.name());
	
					
					Resource resRole =null;
					Resource resEvent =null;
					if (keyEvent.startsWith("track")){
						resRole = createResource( URI_BASE_CONF, role+"/"+keyEvent.substring("track/".length()));
						resEvent = createResource( URI_BASE_CONF, keyEvent);
					}else if (keyEvent.startsWith("workshop")){
						resRole = createResource( URI_BASE_ROOT.substring(0, URI_BASE_ROOT.length()-1), keyEvent+"/"+role );
						resEvent = createResource( URI_BASE_ROOT.substring(0, URI_BASE_ROOT.length()-1), keyEvent);
					}else{	
						resRole = createResource( URI_BASE_CONF, role+"/"+keyEvent);
						resEvent = createResource( URI_BASE_CONF, keyEvent);
					}
					m.add(resEvent, SWC.hasRole, resRole);
					m.add(resRole, SWC.heldBy, s);
					Resource resRoleType = SWC.ProgrammeCommitteeMember;
					if ("chair".equals(role.toLowerCase())){
						resRoleType = SWC.Chair;
						if (keyEvent.startsWith("workshop")){
							roleName = "Workshop Chair";
						}else if (keyEvent.startsWith("tutorial")){
							roleName = "Tutorial Organizer";
						}else{
							roleName = null;
						}
						if (null!=roleName){
							m.add(resRole, RDFS.label, roleName);
						}
					}else{
						if (keyEvent.endsWith("inuse")){
							roleName += "(Semantic Web In Use Track)";
						}else if (keyEvent.endsWith("research")){
							roleName += "(Research Track)";
						}else if (keyEvent.endsWith("evaluation")){
							roleName += "(Evaluation Track)";
						}else if (keyEvent.endsWith("industry")){
							roleName += "(Industry Track)";
						}else if (keyEvent.endsWith("poster-demo")){
							roleName += "(Poster and Demo)";
						}else if (keyEvent.endsWith("doctoral-consortium")){
							roleName += "(Doctoral Consortium)";
						}
						m.add(resRole, RDFS.label, roleName);					
					}
					m.add(resRole, RDF.type, resRoleType);
				}
			}
			

			{
				String email = reader.get(CsvHeader.email.name());
				if (null!=email && email.length()>0 && email.indexOf("@")>0){
					String mbox_sha1sum = ToolHash.hash_mbox_sum_sha1(email);
					m.add( s, FOAF.mbox_sha1sum , mbox_sha1sum);
				}
			}
		}
	}

	
	public static String createUri(String uriBase, String localName){
		if (null==localName || localName.length()==0){
			return uriBase;
		}else{
			localName = ToolText2Rdf.extractLocalName(localName);
			return uriBase +"/" +localName;
		}
	}


	public static Resource createResource(String uriBase, String localName){
		return ResourceFactory.createResource(createUri(uriBase, localName));
	}
	public static Resource createResource(Model m, String uriBase, String localName){
		return m.createResource(createUri(uriBase, localName));
	}
	private static void addPropertyChain0(Model m, Resource s, CsvHeader h, CsvReader reader) throws IOException{
		addPropertyChain0 (m, s, h, reader, false);
	}
	private static void addPropertyChain0(Model m, Resource s, CsvHeader h, CsvReader reader, boolean bUseDefault) throws IOException{
		String key = h.name();
		String value = reader.get(key);
		addPropertyChain0(m, s, h, value, bUseDefault);
	}
	private static void addPropertyChain0(Model m, Resource s, CsvHeader h, String value, boolean bUseDefault) {
		
		if (null==value || value.length()==0){
			if (bUseDefault){
				Resource o = ResourceFactory.createResource(h.uri_base);
				h.updateListP(m, s, o);
			}
			return;
		}
		
		Resource o = createResource(h.uri_base , value);
		h.updateListP(m, s, o);
	}
	
	private static void addPropertyChain1(Model m, Resource s,  CsvHeader h, CsvReader reader) throws IOException{
		 addPropertyChain1(m, s, h, reader, null, null);
	}
	private static void addPropertyChain1(Model m, Resource s,  CsvHeader h, CsvReader reader, Resource sList, Property pRdfList) throws IOException{
		String key = h.name();
		String value = reader.get(key);
		addPropertyChain1(m, s, h, value, sList, pRdfList);
	}
	private static void addPropertyChain1(Model m, Resource s,  CsvHeader h, String value)throws IOException{
		addPropertyChain1(m, s, h, value, null, null);
	}
	
	private static void addPropertyChain1(Model m, Resource s,  CsvHeader h, String value, Resource sList, Property pRdfList) throws IOException{		
		if (null==value || value.length()==0)
			return;

		List<RDFNode> listO1 = new ArrayList<RDFNode>();
		for (String v: h.split(value)){			
			if (null==v || v.length()==0)
				continue;
			v = v.trim();
			Resource o = addNamedInstance(m, h.uri_base, v);
			h.updateListPv(m, o);
			h.updateListP(m, s, o);
			listO1.add(o);
		}
		
		if (null!=pRdfList){
			Seq seq = m.createSeq(sList.getURI());
			for (RDFNode node: listO1){
				seq.add(node);				
			}			
			m.add(s, pRdfList, seq);
		}
	}
	
	private static void addPropertyChain2(Model m, Resource s,  CsvHeader h, CsvReader reader, String v1) throws IOException{

		String key = h.name();
		String value = reader.get(key);
		if (null==value || value.length()==0)
			return;

		Resource o = createResource(h.uri_base, v1);
		h.updateListP(m, s, o);
		h.updateListPv(m, o);
		
		for (String v: h.split(value)){		
			if (null==v || v.length()==0)
				continue;
			v = v.trim();
			Resource o1 = addNamedInstance(m, h.uri_base1, v);
			m.add(o, h.p1, o1);	
		}
		
		
		
	}
	

	private static Resource addNamedInstance(Model m, String uri_base_s, String label_s, Resource type_s){
		Resource s = addNamedInstance(m, uri_base_s, label_s);
		m.add(s, RDF.type, type_s);
		return s;
	}
	
	private static Resource addNamedInstance(Model m, String uri_base_s, String label_s){
		if (null==label_s || label_s.length()==0)
			return null;

		String localName_s = ToolText2Rdf.extractLocalName(label_s);
		Resource s = createResource(m, uri_base_s, localName_s);
		m.add(s, RDFS.label, label_s);
		return s;
	}
	
	private static void addTripleLiteral(Model m, Resource s, CsvHeader h, CsvReader reader) throws IOException{
		addTripleLiteral(m, s, h, reader, null);
	}
	
	private static void addTripleLiteral(Model m, Resource s, CsvHeader h, CsvReader reader, XSDDatatype xsdDataType) throws IOException{
		String key = h.name();
		String value = reader.get(key);
		addTripleLiteral(m, s, h, value, xsdDataType);
	}
	private static void addTripleLiteral(Model m, Resource s, CsvHeader h, String value) {
		addTripleLiteral(m, s, h, value, null);	
	}

	private static void addTripleLiteral(Model m, Resource s, CsvHeader h, String value, XSDDatatype xsdDataType) {

		if (null==value || value.length()==0)
			return;
		
		for (String v: h.split(value)){
			Literal o = null;
			if (null!= xsdDataType){
				o = m.createTypedLiteral(v, xsdDataType);
				h.updateListP(m, s, o);
			}else{
				o =ResourceFactory.createPlainLiteral(v);
			}
			h.updateListP(m, s, o);
		}
	}	
	
	private static void addTripleResource(Model m, Resource s, CsvHeader h, CsvReader reader) throws IOException{
		String key = h.name();
		String value = reader.get(key);
		addTripleResource(m, s, h, value);
	}
	
	private static void addTripleResource(Model m, Resource s, CsvHeader h, String value){
	
		if (null==value || value.length()==0)
			return;
		
		for (String v: h.split(value)){
			RDFNode o = ResourceFactory.createResource(v);
			h.updateListP(m, s, o);
		}
	}	


	private static List<String> pipeFileToCsvInput(String szFileName){
		ArrayList<String> ret = new ArrayList<String>();
		try{
			BufferedReader in = new BufferedReader(new FileReader(szFileName));
			
			String line;
			while (null!=(line= in.readLine())){
				if (null==line)
					continue;
				line = line.trim();
				
				if (line.length()==0)
					continue;
				
				if (line.startsWith("#"))
					continue;
				if (line.startsWith(","))
					continue;
				
				ret.add(line);
			}
		}catch(IOException e){
			e.printStackTrace();
		}
		
		return ret;
	}
	
	

	private static void updateNsPrefix(Model m){
		m.setNsPrefix( OWL.class.getSimpleName().toLowerCase(), OWL.getURI());
		m.setNsPrefix( RDF.class.getSimpleName().toLowerCase(), RDF.getURI());
		m.setNsPrefix( RDFS.class.getSimpleName().toLowerCase(), RDFS.getURI());
		m.setNsPrefix( FOAF.class.getSimpleName().toLowerCase(), FOAF.getURI());
		m.setNsPrefix( SWC.class.getSimpleName().toLowerCase(), SWC.getURI());
		m.setNsPrefix( SWRC.class.getSimpleName().toLowerCase(), SWRC.getURI());
		m.setNsPrefix( DC.class.getSimpleName().toLowerCase(), DC.getURI());
		m.setNsPrefix( DCTerms.class.getSimpleName().toLowerCase(), DCTerms.getURI());
		m.setNsPrefix( BIBO.class.getSimpleName().toLowerCase(), BIBO.getURI());
		m.setNsPrefix( DGTWC.class.getSimpleName().toLowerCase(), DGTWC.getURI());
		m.setNsPrefix( "iCal", URI_NS_ICAL);
		m.setNsPrefix( "iswc2012place", URI_BASE_PLACE+"/");
		m.setNsPrefix( "person", URI_BASE_PERSON+"/");
		m.setNsPrefix( "organization", URI_BASE_ORGANIZATION+"/");
		m.setNsPrefix( "workshop", URI_BASE_WORKSHOP+"/");
		m.setNsPrefix( "iswc2012track", URI_BASE_CONF+"/track/");
		m.setNsPrefix( "iswc2012session", URI_BASE_CONF+"/session/");
		m.setNsPrefix( "iswc2012talk", URI_BASE_CONF+"/talk/");
		m.setNsPrefix( "iswc2012special", URI_BASE_CONF+"/special/");
		m.setNsPrefix( "iswc2012social", URI_BASE_CONF+"/social/");
		m.setNsPrefix( "iswc2012tutorial", URI_BASE_CONF+"/tutorial/");
		m.setNsPrefix( "iswc2012panel", URI_BASE_CONF+"/panel/");
		m.setNsPrefix( "iswc2012paper", URI_BASE_CONF+"/paper/");
		m.setNsPrefix( "iswc2012chair", URI_BASE_CONF+"/chair/");
		m.setNsPrefix( "iswc2012pc", URI_BASE_CONF+"/pc-member/");
		m.setNsPrefix( "iswc2012spc", URI_BASE_CONF+"/senior-pc-member/");
		m.setNsPrefix( "iswc2012sessionchair", URI_BASE_CONF+"/chair/session/");
		m.setNsPrefix( "iswc2012", URI_BASE_CONF+"/");
	}

	
}
