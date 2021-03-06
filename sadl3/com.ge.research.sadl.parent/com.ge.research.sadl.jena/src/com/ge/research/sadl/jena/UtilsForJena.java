/************************************************************************
 * Copyright � 2007-2010 - General Electric Company, All Rights Reserved
 *
 * Project: SADL
 *
 * Description: The Semantic Application Design Language (SADL) is a
 * language for building semantic models and expressing rules that
 * capture additional domain knowledge. The SADL-IDE (integrated
 * development environment) is a set of Eclipse plug-ins that
 * support the editing and testing of semantic models using the
 * SADL language.
 *
 * This software is distributed "AS-IS" without ANY WARRANTIES
 * and licensed under the Eclipse Public License - v 1.0
 * which is available at http://www.eclipse.org/org/documents/epl-v10.php
 *
 ***********************************************************************/

/***********************************************************************
 * $Last revised by: crapo $
 * $Revision: 1.4 $ Last modified on   $Date: 2015/07/31 11:32:37 $
 ***********************************************************************/

package com.ge.research.sadl.jena;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.pojava.datetime.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ge.research.sadl.jena.inference.SadlJenaModelGetterPutter;
import com.ge.research.sadl.utils.ResourceManager;
import com.ge.research.sadl.utils.SadlUtils;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.ontology.CardinalityRestriction;
import com.hp.hpl.jena.ontology.MaxCardinalityRestriction;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.XSD;

public class UtilsForJena {
	protected static final Logger logger = LoggerFactory.getLogger(UtilsForJena.class);

	public static final String OWL_MODELS_FOLDER_NAME = "OwlModels";
    public static final String ONT_POLICY_FILENAME = "ont-policy.rdf";
    
    public static final String FILE_SHORT_PREFIX = "file:/";
    public static final String FILE_URL_PREFIX = "file://";
    public static final String FILE_ABS_URL_PREFIX = "file:///";
    public static final String HTTP_URL_PREFIX = "http://";
    
	// Constants used to manage mappings between model namespace (publicURI) and model file (altURL) in ont-policy.rdf file
    public static final String SADL = "SADL";
    public static final String EXTERNAL_URL = "ExternalUrl";
	protected static final String OWL_ONT_MANAGER_PUBLIC_URINS = "http://www.w3.org/2002/07/owl";
	protected static final String ONT_MANAGER_LANGUAGE = "http://jena.hpl.hp.com/schemas/2003/03/ont-manager#language";
	protected static final String ONT_MANAGER_CREATED_BY = "http://jena.hpl.hp.com/schemas/2003/03/ont-manager#createdBy";
	protected static final String ONT_MANAGER_ALT_URL = "http://jena.hpl.hp.com/schemas/2003/03/ont-manager#altURL";
	protected static final String ONT_MANAGER_PUBLIC_URI = "http://jena.hpl.hp.com/schemas/2003/03/ont-manager#publicURI";
	protected static final String ONT_MANAGER_PREFIX = "http://jena.hpl.hp.com/schemas/2003/03/ont-manager#prefix";
	protected static final String ONT_MANAGER_ONTOLOGY_SPEC = "http://jena.hpl.hp.com/schemas/2003/03/ont-manager#OntologySpec";


	protected RDFNode sadlNode = null;
	protected Property createdBy;
	protected Property altUrlProp;
	protected Property publicUrlProp;
	protected Property prefixProp;
	protected RDFNode createdBySadlLiteral;

	protected OntDocumentManager jenaDocumentMgr;
	private OntModelSpec ontModelSpec = null;


	/**
     * Call this method to convert a value (v) as a Java object to a typed 
     * Literal matching the range of the property.
     *
     * @param m
     * @param prop
     * @param v
     * @return
     * @throws JenaProcessorException
     */
    public static synchronized Literal getLiteralMatchingDataPropertyRange(OntModel m, OntProperty prop, Object v) throws JenaProcessorException {
        Literal val = null;
        String errMsg = null;
        if (prop.isAnnotationProperty()) {
        	return m.createTypedLiteral(v);
        }
        // SADL only has DoubleLiterals--if this property has range float convert v to Float.
        OntResource rng = prop.getRange();
        String rnguri = rng != null ? rng.getURI() : null;
        if (rng == null) {
            errMsg = "Range not given.";
        }
        else if (rng.isAnon()) {
            // this is a complex range--needs work. Try to do something with it....
            // If value is a String
            if (v instanceof String) {
                v = stripQuotes((String)v);
                val = m.createTypedLiteral(v);                
            }
            else {
                val = m.createTypedLiteral(v);
                if (val == null) {
                    errMsg = "Range is an unsupported complex type, failed to create a Literal value for '" + v.toString() + "'.";
                }
            }
        }
        else {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      	val = getLiteralMatchingDataPropertyRange(m, rnguri, v);
        }
        if (errMsg != null) {
        	errMsg += " (Property is '" + prop.getLocalName() + "'.)";
            throw new JenaProcessorException(errMsg);
        }
        return val;
    }
    
    public static synchronized Literal getLiteralMatchingDataPropertyRange(OntModel m, String rnguri, Object v) throws JenaProcessorException {
        Literal val = null;
        String errMsg = null;
        RDFDatatype rdftype = TypeMapper.getInstance().getSafeTypeByName(rnguri);
        if (rdftype != null && !rdftype.getURI().equals(XSD.xboolean.getURI()) && 
        		!rdftype.getURI().equals(XSD.date.getURI()) && 
        		!rdftype.getURI().equals(XSD.dateTime.getURI())) {
        	if (rdftype.isValidValue(v)) {
        		try {
        			val = m.createTypedLiteral(v, rdftype);
        		}
        		catch(Throwable e) {
        			e.printStackTrace();
        		}
        	}
        	if (val != null) {
        		return val;
        	}
        }
    	if (rnguri != null) {
	        if (rnguri.contains("float")) {
	        	if (v instanceof String) {
	       			v = Double.parseDouble(stripQuotes((String)v));
	         	}
	            if (v instanceof Double) {
	                v = new Float(((Double)v).floatValue());
	                val = m.createTypedLiteral(v);
	            }
	            else if (v instanceof Float){
	                val = m.createTypedLiteral(v);
	            }
	            else if (v instanceof Integer) {
	                v = new Float(((Integer)v).floatValue());
	                val = m.createTypedLiteral(v);
	            }
	            else {
	                errMsg = "Unexpected value '" + v.toString() + "' (" + v.getClass().getSimpleName() + ") doesn't match range float";
	            }
	        }
	        else if (rnguri.contains("double")) {
	        	if (v instanceof String) {
	       			v = Double.parseDouble(stripQuotes((String)v));
	         	}
	            if (v instanceof Double) {
	                val = m.createTypedLiteral(v);
	            }
	            else if (v instanceof Float){
	                v = new Double(((Float)v).doubleValue());
	                val = m.createTypedLiteral(v);
	            }
	            else if (v instanceof Integer) {
	                v = new Double(((Integer)v).doubleValue());
	                val = m.createTypedLiteral(v);
	            }
	            else {
	                errMsg = "Unexpected value '" + v.toString() + "' (" + v.getClass().getSimpleName() + ") doesn't match range double";
	            }
	        }
	        else if (rnguri.contains("decimal")) {
	        	if (v instanceof String) {
	       			v = Double.parseDouble(stripQuotes((String)v));
	         	}
	            if (v instanceof Double) {
	                v= new BigDecimal(((Double)v).doubleValue());
	            }
	            else if (v instanceof Float){
	                v= new BigDecimal(((Float)v).doubleValue());
	            }
	            else if (v instanceof Integer) {
	                v= new BigDecimal(((Integer)v).doubleValue());
	            }
	            else {
	                errMsg = "Unexpected value '" + v.toString() + "' (" + v.getClass().getSimpleName() + ") doesn't match range decimal";
	            }
	            val = m.createTypedLiteral(v);
	        }
	        else if (rnguri.contains("int")) {
	        	if (v instanceof String) {
	       			v = Integer.parseInt(stripQuotes((String)v));
	         	}
	            if (v instanceof Integer) {
	                val = m.createTypedLiteral(v);
	            }
	            else {
	                errMsg = "Unexpected value '" + v.toString() + "' (" + v.getClass().getSimpleName() + ") doesn't match range int";
	            }
	        }
	        else if (rnguri.contains("long")) {
	        	if (v instanceof String) {
	       			v = Long.parseLong(stripQuotes((String)v));
	         	}
	            if (v instanceof Long) {
	                val = m.createTypedLiteral(v);
	            }
	            else if (v instanceof Integer) {
	            	val = m.createTypedLiteral(new Long(((Integer)v).longValue()));
	            }
	            else {
	                errMsg = "Unexpected value '" + v.toString() + "' (" + v.getClass().getSimpleName() + ") doesn't match range long";
	            }
	        }
	        else if (rnguri.contains("string")) {
	            if (v instanceof String) {
	                v = stripQuotes((String)v);
	                val = m.createTypedLiteral(v);
	            }
	            else {
	                errMsg = "Unexpected value '" + v.toString() + "' (" + v.getClass().getSimpleName() + ") doesn't match range string";
	            }
	        }
	        else if (rnguri.endsWith("date")) {
	            if (v instanceof String) {
	                v = stripQuotes((String)v);
					DateTime dt = new DateTime((String)v);
					String xsdFormat = "yyyy-MM-dd";
					String modifiedV = dt.toString(xsdFormat);
	                val = m.createTypedLiteral(modifiedV, rnguri);
	            }
	            else {
	                errMsg = "Unexpected value '" + v.toString() + "' (" + v.getClass().getSimpleName() + ") doesn't match range date/dateTime/time";
	            }
	        }
	        else if (rnguri.endsWith("dateTime")) {
	            if (v instanceof String) {
	                v = stripQuotes((String)v);
	                if (v != null && ((String) v).length() > 0) {
						DateTime dt = new DateTime((String)v);
						String xsdFormat = "yyyy-MM-dd'T'HH:mm:ssZZ";
						String modifiedV = dt.toString(xsdFormat);
		                val = m.createTypedLiteral(modifiedV, rnguri);
	                }
	            }
	            else {
	                errMsg = "Unexpected value '" + v.toString() + "' (" + v.getClass().getSimpleName() + ") doesn't match range date/dateTime/time";
	            }
	        }
	        else if (rnguri.endsWith("time")) {
	            if (v instanceof String) {
	                v = stripQuotes((String)v);
	                val = m.createTypedLiteral(v, rnguri);
	            }
	            else {
	                errMsg = "Unexpected value '" + v.toString() + "' (" + v.getClass().getSimpleName() + ") doesn't match range date/dateTime/time";
	            }
	        }
	        else if (rnguri.endsWith("boolean")) {
	        	if (v instanceof String) {
	       			v = Boolean.parseBoolean(stripQuotes((String)v));
	         	}
	            if (v instanceof Boolean) {
	                val = m.createTypedLiteral(v);
	            }
	            else {
	                errMsg = "Unexpected value '" + v.toString() + "' (" + v.getClass().getSimpleName() + ") doesn't match range boolean";
	            }
	        }
	        else {
	            errMsg = "Unhandled range " + rnguri;
	        }
    	}
    	else {
    		errMsg = "Range should not be null.";
    	}
        if (errMsg != null) {
            throw new JenaProcessorException(errMsg);
        }
        return val;
    }

    /**
     * Call this method to remove double quotes from the beginning and end of a string so quoted.
     * @param quotedString -- the string from which quotes are to be removed
     */
    protected static String stripQuotes(String quotedString) {
        if (quotedString != null && !quotedString.isEmpty()) {
            while (quotedString.charAt(0) == '\"') {
                quotedString = quotedString.substring(1);
            }
            while (quotedString.length() > 0 && quotedString.charAt(quotedString.length() - 1) == '\"') {
                quotedString = quotedString.substring(0, quotedString.length() - 1);
            }
        }
        return quotedString;
    }

    /**
     * Method to check a URI to see if it is valid in the context of RDF.
     * 
     * @param uri -- URI (as String) to validate
     * @return -- null if valid else an error description if not valid
     */
	public static synchronized String validateRdfUri(String uri) {
		int lbsign = uri.indexOf('#');
		if (lbsign >= 0) {
			String ns = uri.substring(0, uri.indexOf('#'));
			String nsregex = "^(http)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@/%=~_|]";
			if (!ns.matches(nsregex)) {
				return "invalid namespace '" + ns + "'";
			}
		}
		String fragment = lbsign >= 0 ? uri.substring(uri.lastIndexOf('#') + 1) : uri;
		if (fragment == null) {
			return "missing '#' followed by fragment";
		}
		String fragmentregex = "[a-zA-Z]+[a-zA-Z\\._\\-0-9]*";
		if (! fragment.matches(fragmentregex)) {
			return "invalid fragment '" + fragment + "'";
		}
		return null;
	}

	/**
	 * Method to generate a unique URI string in a given ontology model (OntModel), also given a baseUri. 
	 * If the baseUri ends with a series of digits (a number without decimal or any character following),
	 * then the number will be extracted and used as a base "counter" and incremented by 1 until a unique
	 * URI is obtained. If the baseUri does not end with a number then a number will be found, with the
	 * search starting with 1, which when added to the baseUri gives a unique URI.
	 * 
	 * @param ontModel -- the model in which the URI is to be unique
	 * @param baseUri -- the base URI to which the counter is to be added and incremented until unique
	 * @return -- the unique URI string
	 */
	public static synchronized String getUniqueOntUri(OntModel ontModel, String baseUri) {
		long cntr = 0;
		int numDigitsAtEnd = 0;
		String cntrStr = "";
		for (int i = baseUri.length() - 1; i >= 0; i--) {
			int exp = baseUri.length() - (i + 1);
			char c = baseUri.charAt(i);
			if (Character.isDigit(c)) {
				int cint = c - 48;
				long mplier = (long) Math.pow(10, exp);
				cntr += cint * mplier;
				numDigitsAtEnd++;
			}
			else {
				break;
			}
		}
		if (numDigitsAtEnd > 0) {
			baseUri = baseUri.substring(0, baseUri.length() - numDigitsAtEnd);
		}
		else {
			cntr = 1;
		}
		String uri = baseUri + cntr;
		while (ontModel.getOntResource(uri) != null) {
			uri = baseUri + ++cntr;
		}
		return uri;
	}

	public static synchronized boolean isSingleValued(OntClass cls, OntProperty prop, String rngString) {
		if (prop.isFunctionalProperty()) {
			return true;
		}
		if (cls != null) {
			ExtendedIterator<OntClass> eitr = cls.listSuperClasses(false);
			while (eitr.hasNext()) {
				OntClass supercls = eitr.next();
				if (supercls.isRestriction()) {
					Restriction rstrct = supercls.asRestriction();
					if (rstrct.isMaxCardinalityRestriction()) {
						MaxCardinalityRestriction mxcr = rstrct.asMaxCardinalityRestriction();
						if (mxcr.getOnProperty().equals(prop) && mxcr.getMaxCardinality() == 1) {
							return true;
						}
					}
					else if (rstrct.isCardinalityRestriction()) {
						if (rstrct.isCardinalityRestriction()) {
							CardinalityRestriction cr = rstrct.asCardinalityRestriction();
							if (cr.getOnProperty().equals(prop) && cr.getCardinality() == 1) {
								return true;
							}
						}
					}
					else {
						if (rstrct.hasProperty(OWL2.maxQualifiedCardinality)) {
							if (rstrct.getOnProperty().equals(prop) && rstrct.getProperty(OWL2.maxQualifiedCardinality).getInt() == 1) {
								// check class
								if (rstrct.getProperty(OWL2.onClass).getResource().toString().equals(rngString)) {
									return true;
								}
							}
						}
						else if (rstrct.hasProperty(OWL2.qualifiedCardinality)) {
							if (rstrct.getOnProperty().equals(prop) && rstrct.getProperty(OWL2.qualifiedCardinality).getInt() == 1) {
								// check class
								if (rstrct.getProperty(OWL2.onClass).getResource().toString().equals(rngString)) {
									return true;
								}
							}							
						}
//						StmtIterator siter = rstrct.listProperties();
//						while (siter.hasNext()) {
//							System.out.println(siter.nextStatement().toString());
//						}
					}
				}
			}
		}
		return false;
	}
	
	public String addMappingToPolicyFile(String content, String publicUri, String altUrl, String globalAlias, String source) throws JenaProcessorException {
		// read content into a Model
        Model m = ModelFactory.createDefaultModel();
        m.read(new ByteArrayInputStream(content.getBytes()), null);
        
        // add/update the model with the specified mapping
        initializePolicyConcepts(m);
		Resource pubv = m.createResource(publicUri);
		Resource altv = m.createResource(altUrl);
		Literal pref = null;
		if (globalAlias != null) {
			pref = m.createTypedLiteral(globalAlias);
		}
		addMapping(m, altv, pubv, pref, false, source);
        
        // prepare the new content and return it
		String pfBase = "http://jena.hpl.hp.com/schemas/2003/03/ont-manager";
		String format = "RDF/XML-ABBREV";
		RDFWriter w = m.getWriter(format);
		w.setProperty("xmlbase", pfBase);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		w.write(m, out, pfBase);
		Charset charset = Charset.forName("UTF-8"); 
		CharSequence seq = new String(out.toByteArray(), charset);
		return seq.toString();
	}

	public boolean removeMappingFromPolicyFile(String modelFolder, String publicUri) {
		
		return true;
	}
	
	public String getMinimalPolicyFileContent() throws IOException, URISyntaxException {
        File source = ResourceManager.getAbsoluteBundlePath("Models", ONT_POLICY_FILENAME);
        return new SadlUtils().fileToString(source);
	}

	private boolean initializePolicyConcepts(Model m) {
    	if (sadlNode == null) {
    		sadlNode = m.createTypedLiteral(SADL);
    		createdBy = m.createProperty(ONT_MANAGER_CREATED_BY);
    		altUrlProp = m.createProperty(ONT_MANAGER_ALT_URL);
    		publicUrlProp = m.createProperty(ONT_MANAGER_PUBLIC_URI);
    		prefixProp = m.createProperty(ONT_MANAGER_PREFIX);
    		return true;
    	}
    	return false;
	}

	public synchronized boolean addMapping(Model m, Resource altv, Resource pubv, Literal prefix, boolean bKeepPrefix, String source) {
		boolean bChanged = false;
		boolean mappingFound = false;
		List<Statement> pendingDeletions = null;
		// Get all the statements that have this public URI
		StmtIterator pubitr = m.listStatements(null,
				publicUrlProp, pubv);
		if (pubitr.hasNext()) {
			mappingFound = true;
			int cntr = 0;
			while (pubitr.hasNext()) {
				Statement s = pubitr.nextStatement();
				if (cntr > 0) {
					// there are multiple entries for this public URI
					if (pendingDeletions == null) {
						pendingDeletions = new ArrayList<Statement>();
					}
					pendingDeletions.add(s);
				} else {
					Resource subj = s.getSubject();
					// find the corresponding altURL
					Statement s2 = subj.getProperty(altUrlProp);
					if (s2 != null) {
						// Is the old and the new actual URL the same? If not
						// then change the statement for the actual URL
						if (!s2.getObject().equals(altv)) {
							if (pendingDeletions == null) {
								pendingDeletions = new ArrayList<Statement>();
							}
							pendingDeletions.add(s2);
							subj.addProperty(altUrlProp, altv);
							bChanged = true;
						}
					} else {
						subj.addProperty(altUrlProp, altv);
						bChanged = true;
					}
					Statement s3 = subj.getProperty(prefixProp);
					if (s3 != null) {
						// there is already a prefix in the model
						if (prefix != null) {
							// we have another which is not null
							if (!s3.getObject().equals(prefix)) {
								if (!bKeepPrefix) {
									// only make the change if not keeping old prefix (when the new prefix is null)
									if (pendingDeletions == null) {
										pendingDeletions = new ArrayList<Statement>();
									}
									pendingDeletions.add(s3);
								}
								if (prefix != null) {
									subj.addProperty(prefixProp, prefix);
								}
								bChanged = true;
							}
						}
					} else if (prefix != null) {
						subj.addProperty(prefixProp, prefix);
						bChanged = true;
					}
				}
				cntr++;
			}
		}
		StmtIterator altitr = m.listStatements(null,
				altUrlProp, altv);
		if (altitr.hasNext()) {
			mappingFound = true;
			int cntr = 0;
			while (altitr.hasNext()) {
				Statement s = altitr.nextStatement();
				if (cntr > 0) {
					// there are mulitiple statements for this alt URL
					if (pendingDeletions == null) {
						pendingDeletions = new ArrayList<Statement>();
					}
					pendingDeletions.add(s);
				} else {
					if (!bChanged) {
						// if bChanged is true then we must have already fixed
						// the one mapping in the section above--no need to do
						// it again
						Resource subj = s.getSubject();
						// find the corresponding publicUri
						Statement s2 = subj.getProperty(publicUrlProp);
						if (s2 != null) {
							// is the old and the new public URI the same? If
							// not then change the statement for the new public
							// URI
							if (!s2.getObject().equals(pubv)) {
								if (pendingDeletions == null) {
									pendingDeletions = new ArrayList<Statement>();
								}
								pendingDeletions.add(s2);
								subj.addProperty(publicUrlProp, pubv);
								bChanged = true;
							}
						}
						subj.addProperty(publicUrlProp, pubv);
						bChanged = true;

						Statement s3 = subj.getProperty(prefixProp);
						if (s3 != null) {
							// there is already a prefix in the model
							if (prefix != null) {
								// we have another which is not null
								if (!s3.getObject().equals(prefix)) {
									if (!bKeepPrefix) {
										// only make the change if not keeping old prefix (when the new prefix is null)
										if (pendingDeletions == null) {
											pendingDeletions = new ArrayList<Statement>();
										}
										pendingDeletions.add(s3);
									}
									if (prefix != null) {
										subj.addProperty(prefixProp, prefix);
									}
									bChanged = true;
								}
							}
						} else if (prefix != null) {
							subj.addProperty(prefixProp, prefix);
							bChanged = true;
						}
					}
				}
				cntr++;
			}
		}

		// remove extra and obsolete entries
		if (pendingDeletions != null && pendingDeletions.size() > 0) {
			for (int i = 0; i < pendingDeletions.size(); i++) {
				Statement s = pendingDeletions.get(i);
				m.remove(s);
				bChanged = true;
			}
		}

		if (!mappingFound) {
			com.hp.hpl.jena.rdf.model.Resource type = m
					.createResource(ONT_MANAGER_ONTOLOGY_SPEC);
			com.hp.hpl.jena.rdf.model.Resource newOntSpec = m
					.createResource(type);
			Property langp = m
					.getProperty(ONT_MANAGER_LANGUAGE);
			RDFNode langv = m.createResource(
					OWL_ONT_MANAGER_PUBLIC_URINS);
			m.add(newOntSpec, publicUrlProp, pubv);
			m.add(newOntSpec, altUrlProp, altv);
			m.add(newOntSpec, langp, langv);
			if (source != null && !source.equalsIgnoreCase(SADL)) {
				m.add(newOntSpec, createdBy, m.createTypedLiteral(source));
			} else {
				m.add(newOntSpec, createdBy, SADL);
			}
			if (prefix != null) {
				m.add(newOntSpec, prefixProp, prefix);
			}
			logger.debug("Created new mapping for '" + pubv.toString() + "', '"
					+ altv.toString() + "'");
			bChanged = true;
		}
//		try {
//			// add mapping to Jena OntDocumentManager
//			if (addJenaMapping(pubv.getURI().toString(), altv.getURI()
//					.toString())) {
//				bChanged = true;
//			}
//		} catch (URISyntaxException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ConfigurationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		if (bChanged) {
//			setMappingChanged(true);
//			logger.debug("Modified mapping for '" + pubv.toString() + "', '"
//					+ altv.toString() + "'");
//		}
//		if (this.mappings == null) {
//			mappings = new HashMap<String, String>();
//		}
//		mappings.put(rdfNodeToString(pubv), rdfNodeToString(altv));
		return bChanged;
	}
	
	public static String getPolicyFilePathForProject(String projectPath) throws ConfigurationException {
		if (projectPath.startsWith("file:/")) {
			projectPath = projectPath.substring(6);
		}
		else if (projectPath.startsWith("file:")) {
			projectPath = projectPath.substring(5);
		}
		File prjFile = new File(projectPath);
		if (!prjFile.exists()) {
			throw new ConfigurationException("Project path '" + projectPath + "' does not exist.");
		}
		if (!prjFile.isDirectory()) {
			throw new ConfigurationException("Project path '" + projectPath + "' is not a folder.");
		}
     	return projectPath + "/" + OWL_MODELS_FOLDER_NAME + "/" + ONT_POLICY_FILENAME;
	}

	public OntDocumentManager loadMappings(File pf) throws IOException {
		if (!pf.exists()) {
			// reconstruct the policy file, including externals
		}
		else if (!pf.isFile()) {
			throw new IOException("'" + pf.getCanonicalPath() + "' is not a valid policy file identifier");
		}
		// load mapping info from file
		Model mappingModel = ModelFactory.createDefaultModel();
	    InputStream in = FileManager.get().open(pf.getCanonicalPath());
	    if (in == null) {
	    	throw new IllegalArgumentException("File: " + pf.getCanonicalPath() + " not found");
	    }
		try {
			mappingModel.read(in, "");
		}
		catch (Throwable t) {
			t.printStackTrace();
			logger.error("Failed to read mapping file '" + pf.getCanonicalPath() + "': " + t.getLocalizedMessage());
		}
		
		boolean needFileLocator = false;
        RDFNode pubv;
        RDFNode altv;
//        createdBySadlLiteral = mappingModel.createLiteral("SADL");
        String fileName = new String();
        String actualFilePath = new String();
        initializePolicyConcepts(mappingModel);
        StmtIterator sitr = mappingModel.listStatements(null, altUrlProp, (RDFNode)null);
        
        while (sitr.hasNext()) {
        	fileName = null;
        	actualFilePath = null;
            Statement s = sitr.nextStatement();
            com.hp.hpl.jena.rdf.model.Resource subj = s.getSubject();	
            Statement salt = subj.getProperty(altUrlProp);
            Statement spub = subj.getProperty(publicUrlProp);
            Statement sprefix = subj.getProperty(prefixProp);
            if (salt != null && spub != null) {
	            altv = salt.getObject();
            	String strAltv = rdfNodeToString(altv);
	            pubv = spub.getObject();
	            Statement isSadlStmt = subj.getProperty(createdBy);
	            if (isSadlStmt != null && isSadlStmt.getObject().asLiteral().getLexicalForm().equals(sadlNode.asLiteral().getLexicalForm())) {         
	            	// this mapping was created by SADL
		            StringTokenizer st1 = new StringTokenizer(strAltv, "/");
		 			while(st1.hasMoreTokens()) {
		            	fileName = st1.nextToken();
		 			}
	            	actualFilePath = getActualUrl(pf.getParent(), fileName);
	            }
	            else {
	            	// this handles mappings that are not created by SADL
	            	//  1) if the mapping is of type "file:" and the file exists assume it is correct
	            	//	2) else if their is a file of that name in the same folder as the policy file assume that file is the correct one
	            	//	3) else if there is a sibling folder to the folder of the policy file that contains a file of that name assume it is the correct one
	            	if (strAltv.startsWith(FILE_SHORT_PREFIX)) {
			            StringTokenizer st1 = new StringTokenizer(strAltv, "/");
			            String lastToken = null;
			 			while(st1.hasMoreTokens()) {
			 				lastToken = fileName;
			            	fileName = st1.nextToken();
			 			}
			 			String testName = strAltv;
		 				try {
				 			File testFile = new File(fileUrlToFileName(testName));
				 			if (testFile.exists()) {
				 				// the actualUrl exists as is so use it
				 				actualFilePath = testName;
				 			}
				 			else {
				 				testName =  getActualUrl(pf.getParent(), fileName);
								testFile = new File(fileUrlToFileName(testName));
				 				if (testFile.exists()) {
				 					// the actualUrl adjusted to have the relative location of the models folder exists so use it
				 					actualFilePath = testName;
				 				}
				 				else {
				 					String siblingName = siblingFolderUrl(pf.getParent(), fileName, lastToken);
				 					boolean siblingFound = false;
				 					if (siblingName != null) {
				 						File sibling = new File(fileUrlToFileName(siblingName));
				 						if (sibling.exists()) {
				 							// the named file exists in a sibling directory; use it
				 							siblingFound = true;
				 							actualFilePath = siblingName;
				 						}
				 					}
				 					if (!siblingFound) {
				 						if (pf.getParent() != null) {
								 			String folderPath = fileNameToFileUrl(pf.getParent());
						 					testName = folderPath.substring(0, folderPath.length() - (1 + lastToken.length())) + "/" + fileName;
						 					testFile = new File(fileUrlToFileName(testName));
						 					if (testFile.exists()) {
						 						// folder above??
						 						actualFilePath = testName;
						 					}
						 					else {
						 						logger.warn("Mapping file has actual URL '" + testName + "' but it does not appear to exist and could not be found in adjacent folders.");
						 					}
				 						}
				 						else {
				 							actualFilePath = testName;
				 							if (!actualFilePath.startsWith("http:")) {
				 								logger.warn("Mapping file '" + strAltv + "'; using '" + actualFilePath + "'");
				 							}
				 						}
				 					}
				 				}
				 			}
		 				}
						catch (MalformedURLException e) {
							// oh well, we tried
						}
	            	}
	            }
	            if (actualFilePath == null) {
	            	actualFilePath = strAltv;
	            }
	            String publicUri = rdfNodeToString(pubv);
	            logger.debug("Found mapping from public URI '" + publicUri + "' to alternative URL '" + actualFilePath + "'");
	            if (!actualFilePath.equals(publicUri)) {
	            	getJenaDocumentMgr(mappingModel).addAltEntry(publicUri, actualFilePath);
					if (actualFilePath != null && actualFilePath.startsWith(FILE_SHORT_PREFIX)) {
						needFileLocator = true;
					}
	            }
           }
            
         } // end while
		
		if (needFileLocator) {
			setupJenaFileManager(pf.getParent(), mappingModel);
		}
		return getJenaDocumentMgr(mappingModel);
	}

    protected String rdfNodeToString(RDFNode node) {
    	if (node != null) {
			if (node instanceof Literal) {
				return ((Literal)node).getValue().toString();
			}
			return node.toString();
    	}
    	return null;
	}

	private String getActualUrl(String modelFolder, String fileName) {
		String rawPath = modelFolder + "/" + fileName;
		return fileNameToFileUrl(rawPath);
	}

	/**
     * This method converts an OS filename (e.g., "C:\\folder\file.ext")
     * to a file URL
     *
     * @param fileName
     * @return
     */
    public String fileNameToFileUrl(String fileName) {
    	URI fileUri = null;
        if (fileName.startsWith("http:") || fileName.startsWith("file:")) {
            fileUri = URI.create(fileName);
        }
        else {
        	File file = new File(fileName);
        	fileUri = file.toURI();
        	try {
				return fileUri.toURL().toExternalForm();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return FILE_ABS_URL_PREFIX + fileName.replace("\\", "/");
			}
        }
        return fileUri.toString();
    }

    /**
     * This method converts the string form of a file URL to an
     * OS filename
     *
     * @param urlstr
     * @return
     * @throws MalformedURLException 
     */
    public String fileUrlToFileName(String urlstr) throws MalformedURLException {
        URI fileUri = URI.create(urlstr);
        if (fileUri != null) {
        	return fileUri.toURL().getPath();
        }
        throw new MalformedURLException("Unable to convert '" + urlstr + "' to a file name");
    }

	private String siblingFolderUrl(String modelFolder, String fileName, String lastToken) {
		if (modelFolder != null) {
			File folder = new File(modelFolder);
			File parent = folder.getParentFile();
			File[] siblings = parent.listFiles();
			for (int i = 0; i < siblings.length; i++) {
				if (siblings[i].getName().equals(lastToken)) {
					return fileNameToFileUrl(siblings[i].getAbsolutePath() + File.separator + fileName);
				}
			}
		}
		return null;
	}
	
	private void setupJenaFileManager(String modelFolder, Model mappingModel) throws IOException {
		getJenaDocumentMgr(mappingModel).getFileManager().addLocatorFile(modelFolder);
		getJenaDocumentMgr(mappingModel).getFileManager().addLocatorURL();
		SadlReadFailureHandler rfHandler = new SadlReadFailureHandler(logger );	
//		rfHandler.setSadlConfigMgr(this);
		getJenaDocumentMgr(mappingModel).setReadFailureHandler(rfHandler);
	}
	
	public OntDocumentManager getJenaDocumentMgr(Model mappingModel) {
		if (jenaDocumentMgr == null) {
			if (mappingModel != null) {
				setJenaDocumentMgr(new OntDocumentManager());
				if (ontModelSpec != null) {
					ontModelSpec.setDocumentManager(getJenaDocumentMgr(mappingModel));
				}
			}
			else {
				setJenaDocumentMgr(OntDocumentManager.getInstance());
			}
		}
		return jenaDocumentMgr;
	}

	private void setJenaDocumentMgr(OntDocumentManager docmgr) {
		jenaDocumentMgr = docmgr;
	}
	
	public String getPolicyFilename(org.eclipse.emf.ecore.resource.Resource somerojectResource) {
		org.eclipse.emf.common.util.URI prjUri = ResourceManager.getProjectUri(somerojectResource.getURI());
		org.eclipse.emf.common.util.URI uri = prjUri.appendSegment(OWL_MODELS_FOLDER_NAME);
		uri = uri.appendSegment(ONT_POLICY_FILENAME);
		return uri.toString();
	}

	public String getNewPolicyFileContent(String policyFilename) throws IOException, URISyntaxException, JenaProcessorException {
		StringBuilder sb = new StringBuilder(getMinimalPolicyFileContent());
		sb = addExternalMappings(sb, new File(policyFilename).getParentFile().getParent());
		return sb.toString();
	}
	
	public OntModel createAndInitializeJenaModel(String policyFilename, OntModelSpec omSpec, boolean loadImports) 
			throws IOException, ConfigurationException, URISyntaxException, JenaProcessorException {
		File pf;
		if (policyFilename != null && policyFilename.length() > 0) {
			pf = new File(policyFilename);
			if (!pf.exists()) {
				String pfContent = getNewPolicyFileContent(policyFilename);
				new SadlUtils().stringToFile(pf, pfContent, false);
			}
		}
		else {
			throw new IOException("Policy file name is invalid");
		}
		String modelFolder = pf.getParent();
		OntDocumentManager owlDocMgr = loadMappings(pf);
		OntModelSpec spec = new OntModelSpec(omSpec);
		spec.setImportModelGetter(new SadlJenaModelGetterPutter(spec, modelFolder));
		spec.setDocumentManager(owlDocMgr);
		owlDocMgr.setProcessImports(loadImports);
		OntModel theModel = ModelFactory.createOntologyModel(spec);
		return theModel;
	}

	private StringBuilder addExternalMappings(StringBuilder sb, String prjDir) throws IOException, JenaProcessorException {
		// find all .url files
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory()) {
					return true;
				}
				return pathname.isFile() && pathname.getName().endsWith(".url");
			}
		};
		List<File> urlFiles = findUrlFiles(new ArrayList<File>(), new File(prjDir), filter);
		if (urlFiles != null && urlFiles.size() > 0) {
			SadlUtils su = new SadlUtils();
			for (int i = 0; urlFiles != null && i < urlFiles.size(); i++) {
				File urlFile = urlFiles.get(i);
				String content = su.fileToString(urlFile);
				List<String>[] unp = su.getUrlsAndPrefixesFromExternalUrlContent(content);
				if (unp != null) {
					List<String> urls = unp[0];
					List<String> prefixes = unp[1];
					for (int j = 0; j < urls.size(); j++) {
						String url = urls.get(j);
						String prefix = (prefixes != null && prefixes.size() > j) ? prefixes.get(j) : null;
						String urlFilename = su.getExternalModelRootFromUrlFilename(urlFile);
						String altUrl = fileNameToFileUrl(prjDir + File.separator + urlFilename + File.separator + su.externalUrlToRelativePath(url));
						String pubUri = su.getPublicUriFromUrl(url, altUrl);
						System.out.println("Found external mapping: " + prefix  + " -> " + url + " at " + altUrl);
						sb.replace(0, sb.length(), addMappingToPolicyFile(sb.toString(), url, altUrl, prefix, EXTERNAL_URL));
					}
				}
			}
		}
		return sb;
	}

	private List<File> findUrlFiles(List<File> found, File folder, FileFilter filter) {
		if (folder.isDirectory()) {
			File[] matchingContents = folder.listFiles(filter);
			for (int i = 0; i < matchingContents.length; i++) {
				File mf = matchingContents[i];
				if (mf.isDirectory()) {
					found = findUrlFiles(found, mf, filter);
				}
				else {
					found.add(mf);
				}
			}
		}
		return found;
	}

}
