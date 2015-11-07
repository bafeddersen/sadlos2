package com.ge.research.sadl.jena;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.generator.IFileSystemAccess2;
import org.eclipse.xtext.util.CancelIndicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ge.research.sadl.model.DeclarationExtensions;
import com.ge.research.sadl.model.OntConceptType;
import com.ge.research.sadl.processing.ISadlModelProcessor;
import com.ge.research.sadl.processing.ValidationAcceptor;
import com.ge.research.sadl.sADL.SadlAllValuesCondition;
import com.ge.research.sadl.sADL.SadlAnnotation;
import com.ge.research.sadl.sADL.SadlBooleanLiteral;
import com.ge.research.sadl.sADL.SadlCardinalityCondition;
import com.ge.research.sadl.sADL.SadlClassOrPropertyDeclaration;
import com.ge.research.sadl.sADL.SadlCondition;
import com.ge.research.sadl.sADL.SadlConstantLiteral;
import com.ge.research.sadl.sADL.SadlDataType;
import com.ge.research.sadl.sADL.SadlDataTypeFacet;
import com.ge.research.sadl.sADL.SadlDifferentFrom;
import com.ge.research.sadl.sADL.SadlDisjointClasses;
import com.ge.research.sadl.sADL.SadlExplicitValue;
import com.ge.research.sadl.sADL.SadlHasValueCondition;
import com.ge.research.sadl.sADL.SadlImport;
import com.ge.research.sadl.sADL.SadlInstance;
import com.ge.research.sadl.sADL.SadlIntersectionType;
import com.ge.research.sadl.sADL.SadlIsAnnotation;
import com.ge.research.sadl.sADL.SadlModel;
import com.ge.research.sadl.sADL.SadlModelElement;
import com.ge.research.sadl.sADL.SadlNecessaryAndSufficient;
import com.ge.research.sadl.sADL.SadlNumberLiteral;
import com.ge.research.sadl.sADL.SadlPrimitiveDataType;
import com.ge.research.sadl.sADL.SadlProperty;
import com.ge.research.sadl.sADL.SadlPropertyCondition;
import com.ge.research.sadl.sADL.SadlPropertyRestriction;
import com.ge.research.sadl.sADL.SadlRangeRestriction;
import com.ge.research.sadl.sADL.SadlResource;
import com.ge.research.sadl.sADL.SadlSameAs;
import com.ge.research.sadl.sADL.SadlSimpleTypeReference;
import com.ge.research.sadl.sADL.SadlStringLiteral;
import com.ge.research.sadl.sADL.SadlTypeAssociation;
import com.ge.research.sadl.sADL.SadlTypeReference;
import com.ge.research.sadl.sADL.SadlUnionType;
import com.ge.research.sadl.sADL.SadlValueList;
import com.google.inject.Inject;
import com.hp.hpl.jena.ontology.AllValuesFromRestriction;
import com.hp.hpl.jena.ontology.AnnotationProperty;
import com.hp.hpl.jena.ontology.CardinalityRestriction;
import com.hp.hpl.jena.ontology.ComplementClass;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.HasValueRestriction;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.IntersectionClass;
import com.hp.hpl.jena.ontology.MaxCardinalityRestriction;
import com.hp.hpl.jena.ontology.MinCardinalityRestriction;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.ontology.SomeValuesFromRestriction;
import com.hp.hpl.jena.ontology.UnionClass;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.XSD;

public class JenaBasedSadlModelProcessor implements ISadlModelProcessor {
	private static final Logger logger = LoggerFactory.getLogger(JenaBasedSadlModelProcessor.class);

	private OntModel theJenaModel;
	
	enum AnnType {ALIAS, NOTE}
	public enum RangeValueType {CLASS_OR_DT, LIST, LISTS}
	
	@Inject DeclarationExtensions declarationExtensions;
	private String modelNamespace;
	private OntDocumentManager jenaDocumentMgr;
	private static final String LIST_RANGE_ANNOTATION_PROPERTY = "http://sadl.org/range/annotation/listtype";
	
	/**
	 * For TESTING
	 * @return
	 */
	public OntModel getTheJenaModel() {
		return theJenaModel;
	}
	
	@Override
	public void onGenerate(Resource resource, IFileSystemAccess2 fsa, CancelIndicator cancelIndicator) {
		// save the model
		int i = 0;
	}
	
	@Override
	public void onValidate(Resource resource, ValidationAcceptor issueAcceptor, CancelIndicator cancelIndicator) {
		SadlModel model = (SadlModel) resource.getContents().get(0);
		String modelActualUrl =resource.getURI().toFileString();
		// directly create the Jena Model here!
		theJenaModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		String modelName = model.getBaseUri();
		setModelNamespace(assureNamespaceEndsWithHash(modelName));
		String modelAlias = model.getAlias();
		if (modelAlias == null) {
			modelAlias = "";
		}
		getTheJenaModel().setNsPrefix(modelAlias, getModelNamespace());
		Ontology modelOntology = getTheJenaModel().createOntology(modelName);
		logger.debug("Ontology '" + modelName + "' created");
		modelOntology.addComment("This ontology was created from a SADL file '"
				+ modelActualUrl + "' and should not be directly edited.", "en");
		
		String modelVersion = model.getVersion();
		if (modelVersion != null) {
			modelOntology.addVersionInfo(modelVersion);
		}

		EList<SadlAnnotation> anns = model.getAnnotations();
		Iterator<SadlAnnotation> iter = anns.iterator();
		while (iter.hasNext()) {
			SadlAnnotation ann = iter.next();
			String anntype = ann.getType();
			EList<String> annContents = ann.getContents();
			Iterator<String> anniter = annContents.iterator();
			while (anniter.hasNext()) {
				String annContent = anniter.next();
				if (anntype.equals(AnnType.ALIAS)) {
					modelOntology.addLabel(annContent, "en");
				}
				else if (anntype.equals(AnnType.NOTE)) {
					modelOntology.addComment(annContent, "en");
				}
			}
		}
		
		EList<SadlImport> implist = model.getImports();
		Iterator<SadlImport> impitr = implist.iterator();
		while (impitr.hasNext()) {
			SadlImport simport = impitr.next();
			SadlModel importedResource = simport.getImportedResource();
			String importUri = importedResource.getBaseUri();
			String importPrefix = simport.getAlias();
//			theJenaModel.addImport(simport.getImportURI(), simport.getAlias());
	    	if (importUri.equals(modelName)) {
	    		// don't import to self
//	    		generate error marker--can't import self
	    	}
	    	else {
		    	// Now add import model (with setCachedModels false so that the actual imports are not loaded. If then need to be loaded 
	    		//	at some point to do semantic validation, they will be loaded then.
	    		if (importPrefix == null) {
// TODO	    		// need to get the prefix from the global prefix in the imported model, if SADL, else from the ont-policy.rdf file if external
//	    			OntologyModel importedModel;//  = simport.getImportedNamespace().getURI();	// this will not work until the grammar treats the import as a reference.
	    		}
	    		if (importPrefix != null) {
	    			getTheJenaModel().setNsPrefix(importPrefix, assureNamespaceEndsWithHash(importUri));
	    		}
		    	com.hp.hpl.jena.rdf.model.Resource importedOntology = getTheJenaModel().createResource(importUri);
		    	logger.debug("Imported ontology resource '" + importUri + "' created.");
		    	modelOntology.addImport(importedOntology);
	    	}
	    	
// TODO	Should the imported model actually be loaded by Jena? The OWL model, whether from SADL or external, will potenatially 
//	    	contain information that is necessary for validation. The only information that will be potentially needed for
//	    	processing the rest of the parse tree is the type of the imported concepts. For imports of SADL models, this type
//	    	information is known in the ResourceSet. Likewise for external OWL imports? 
	    	
//	    	this.getJenaDocumentMgr().setCacheModels(true);
//	   		this.getJenaDocumentMgr().setProcessImports(true);
//	   		ReadFailureHandler rfh = this.getJenaDocumentMgr().getReadFailureHandler();
//	   		if (rfh instanceof SadlReadFailureHandler) {
//	   			((SadlReadFailureHandler)rfh).setSadlConfigMgr(this);
//	   		}
//	    	Conclusion: if checking is needed that requires Jena imported models, then it happens here. 
//	    		Otherwise it happens in the validator.
		}
		
		// process rest of parse tree
		List<SadlModelElement> elements = model.getElements();
		if (elements != null) {
			Iterator<SadlModelElement> elitr = elements.iterator();
			while (elitr.hasNext()) {
				// check for cancelation from time to time
				if (cancelIndicator.isCanceled()) {
					throw new OperationCanceledException();
				}
				SadlModelElement element = elitr.next();
				try {
					if (element instanceof SadlClassOrPropertyDeclaration) {
						processSadlClassOrPropertyDeclaration((SadlClassOrPropertyDeclaration) element, issueAcceptor, cancelIndicator);	
					}
					else if (element instanceof SadlProperty) {
						processSadlProperty((SadlProperty) element, issueAcceptor, cancelIndicator);
					}
					else if (element instanceof SadlNecessaryAndSufficient) {
						processSadlNecessaryAndSufficient((SadlNecessaryAndSufficient)element, issueAcceptor, cancelIndicator);
					}
					else if (element instanceof SadlDifferentFrom) {
						processSadlDifferentFrom((SadlDifferentFrom)element, issueAcceptor, cancelIndicator);
					}
					else if (element instanceof SadlInstance) {
						processSadlInstance((SadlInstance) element, issueAcceptor, cancelIndicator);
					}
					else if (element instanceof SadlDisjointClasses) {
						processSadlDisjointClasses((SadlDisjointClasses)element, issueAcceptor, cancelIndicator);
					}
					else if (element instanceof SadlSameAs) {
						processSadlSameAs((SadlSameAs)element, issueAcceptor, cancelIndicator);
					}
					else {
						System.out.println("onValidate for element of type '" + element.getClass().getCanonicalName() + "' not implemented");
					}
				}
				catch (JenaProcessorException e) {
					issueAcceptor.addError(e.getMessage(), element);
				}
			}
		}
	}

	private void processSadlSameAs(SadlSameAs element,
			ValidationAcceptor issueAcceptor, CancelIndicator cancelIndicator) throws JenaProcessorException {
		SadlResource sr = element.getNameOrRef();
		String nm = declarationExtensions.getConcreteName(sr);
		OntResource rsrc = getTheJenaModel().getOntResource(validateUri(nm));
		SadlTypeReference smas = element.getSameAs();
		OntConceptType sameAsType;
		if (rsrc == null) {
			// concept does not exist--try to get the type from the sameAs
			sameAsType = getSadlTypeReferenceType(smas);
			
		}
		else {
			sameAsType = declarationExtensions.getOntConceptType(sr);
		}
		if (sameAsType.equals(OntConceptType.CLASS)) {
			OntClass smasCls = sadlTypeReferenceToOntResource(smas).asClass();
			// this is a class axiom
			OntClass cls = getTheJenaModel().getOntClass(validateUri(nm));
			if (cls == null) {
				// this is OK--create class
				cls = createOntClass(modelNamespace, nm, (String)null);
			}
			if (element.isComplement()) {
				ComplementClass cc = getTheJenaModel().createComplementClass(cls.getURI(), smasCls);
				logger.debug("New complement class '" + cls.getURI() + "' created");
			}
			else {
				cls.addEquivalentClass(smasCls);
				logger.debug("Class '" + cls.toString() + "' given equivalent class '" + smasCls.toString() + "'");
			}
		}
		else if (sameAsType.equals(OntConceptType.INSTANCE)) {
			OntResource smasInst = sadlTypeReferenceToOntResource(smas);
			rsrc.addSameAs(smasInst);
			logger.debug("Instance '" + rsrc.toString() + "' declared same as '" + smas.toString() + "'");
		}
		else {
			throw new JenaProcessorException("Unexpected concept type for same as statement: " + sameAsType.toString());
		}
	}

	private List<OntResource> processSadlClassOrPropertyDeclaration(SadlClassOrPropertyDeclaration element, ValidationAcceptor issueAcceptor, CancelIndicator cancelIndicator) throws JenaProcessorException {
		// Get the names of the declared concepts and store in a list
		List<String> newNames = new ArrayList<String>();
		EList<SadlResource> clses = element.getClassOrProperty();
		if (clses != null) {
			Iterator<SadlResource> citer = clses.iterator();
			while (citer.hasNext()) {
				SadlResource sr = citer.next();
				String nm = declarationExtensions.getConcreteName(sr);
				newNames.add(nm);
			}
		}
		
		if (newNames.size() < 1) {
			throw new JenaProcessorException("No names passed to processSadlClassOrPropertyDeclaration");
		}
		List<OntResource> rsrcList = new ArrayList<OntResource>();
		// The declared concept(s) will be of type class, property, or datatype. 
		//	Determining which will depend on the structure, including the superElement....
		// 	Get the superElement
		SadlTypeReference superElement = element.getSuperElement();
		//		1) if superElement is null then it is a top-level class declaration
		if (superElement == null) {
			OntClass cls = createOntClass(getModelNamespace(), newNames.get(0), (OntClass)null);
			rsrcList.add(cls);
		}
		//  	2) if superElement is not null then the type of the new concept is the same as the type of the superElement
		// 			the superElement can be:
		// 				a) a SadlSimpleTypeReference
		else if (superElement instanceof SadlSimpleTypeReference) {
			//TODO need to get the type (class or property or datatype declaration) from the ResourceSet--how?
			SadlResource superSR = ((SadlSimpleTypeReference)superElement).getType();
// TODO Is this just the local name or is it the URI?
			String superSRUri = declarationExtensions.getConcreteName(superSR);	
			SadlResource decl = declarationExtensions.getDeclaration(superSR);
// TODO how do I get the URI and the type from the ResourceSet?	
			OntConceptType superElementType = declarationExtensions.getOntConceptType(superSR);
			if (superElementType.equals(OntConceptType.CLASS)) {
				for (int i = 0; i < newNames.size(); i++) {
					rsrcList.add(createOntClass(getModelNamespace(), newNames.get(i), superSRUri));
				}
			}
			else if (superElementType.equals(OntConceptType.CLASS_PROPERTY)) {
				for (int i = 0; i < newNames.size(); i++) {
					rsrcList.add(createObjectProperty(getModelNamespace(), newNames.get(i), superSRUri));
				}
			}
			else if (superElementType.equals(OntConceptType.DATATYPE_PROPERTY)) {
				for (int i = 0; i < newNames.size(); i++) {
					rsrcList.add(createDatatypeProperty(getModelNamespace(), newNames.get(i), superSRUri));
				}
			}
			else if (superElementType.equals(OntConceptType.ANNOTATION)) {
				for (int i = 0; i < newNames.size(); i++) {
					rsrcList.add(createAnnotationProperty(getModelNamespace(), newNames.get(i)));
				}
			}
		}
		//				b) a SadlPrimitiveDataType
		else if (superElement instanceof SadlPrimitiveDataType) {
			SadlDataTypeFacet dtf = ((SadlClassOrPropertyDeclaration)element).getFacet();
			SadlDataType sdt = ((SadlPrimitiveDataType)superElement).getPrimitiveType();
			// pull out facets, create rdfs:Datatype
		}
		//				c) a SadlPropertyCondition
		else if (superElement instanceof SadlPropertyCondition) {
			OntClass propCond = processSadlPropertyCondition((SadlPropertyCondition) superElement);
			rsrcList.add(propCond);
		}
		//				d) a SadlTypeReference
		else if (superElement instanceof SadlTypeReference) {
			// this can only be a class; can't create a property as a SadlTypeReference
			try {
				OntClass superCls = sadlTypeReferenceToOntResource(superElement).asClass();
				if (superCls != null) {
					rsrcList.add(createOntClass(modelNamespace, newNames.get(0), superCls));
				}
			} catch (JenaProcessorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (int i = 0; i < rsrcList.size(); i++) {
			Iterator<SadlProperty> dbiter = element.getDescribedBy().iterator();
			while (dbiter.hasNext()) {
				SadlProperty sp = dbiter.next();
				OntProperty prop = processSadlProperty(sp, issueAcceptor, cancelIndicator);
				addPropertyDomain(prop, rsrcList.get(i));
			}

		}
		return rsrcList;
	}

	private OntProperty processSadlProperty(SadlProperty element, ValidationAcceptor issueAcceptor, CancelIndicator cancelIndicator) throws JenaProcessorException {
		// this has two forms:
		//	1) <name> is a property...
		//	2) relationship of <Domain> to <Range> is <name>
		SadlResource sr = sadlResourceFromSadlProperty(element);
		String propName = declarationExtensions.getConcreteName(sr);
		OntConceptType propType = declarationExtensions.getOntConceptType(sr);
		
		
		Iterator<SadlPropertyRestriction> spitr = element.getRestrictions().iterator();
		while (spitr.hasNext()) {
			SadlPropertyRestriction spr = spitr.next();
			if (spr instanceof SadlRangeRestriction) {
				SadlTypeReference rng = ((SadlRangeRestriction)spr).getRange();
				if (rng instanceof SadlPrimitiveDataType) {
					String rngName = ((SadlPrimitiveDataType)rng).getPrimitiveType().getName();
					RDFNode rngNode = primitiveDatatypeToRDFNode(rngName);
					DatatypeProperty prop = null;
					if (!checkForExistingCompatibleDatatypeProperty(validateUri(propName), rngNode)) {
						prop = createDatatypeProperty(modelNamespace, propName, null);
						addPropertyRange(prop, rngNode);
					}
					else {
						prop = getTheJenaModel().getDatatypeProperty(validateUri(propName));
					}
					return prop;
				}
				else {				
					OntResource rngRsrc = sadlTypeReferenceToOntResource(rng);
					if (rngRsrc == null) {
						throw new JenaProcessorException("Range failed to resolve to a class");
					}
					OntClass rngCls = rngRsrc.asClass();
					ObjectProperty prop = getOrCreateObjectProperty(propName);
					addPropertyRange(prop, rngCls);
					return prop;
				}
			}
			else if (spr instanceof SadlCondition) {
				if (propType.equals(OntConceptType.CLASS_PROPERTY)) {
					ObjectProperty prop = getOrCreateObjectProperty(validateUri(propName));
					OntClass condCls = processSadlCondition((SadlCondition) spr, prop, propType);
					addPropertyRange(prop, condCls);
					return prop;
				}
				else if (propType.equals(OntConceptType.DATATYPE_PROPERTY)) {
					throw new JenaProcessorException("SadlCondition on data type property not handled");
				}
				else {
					throw new JenaProcessorException("Invalid property type: " + propType.toString());
				}
			}
			else if (spr instanceof SadlTypeAssociation) {
				SadlTypeReference domain = ((SadlTypeAssociation)spr).getDomain();
				OntResource domainrsrc = sadlTypeReferenceToOntResource(domain);
				ObjectProperty prop = getOrCreateObjectProperty(validateUri(propName));
				addPropertyDomain(prop, domainrsrc);
				SadlTypeReference from = element.getFrom();
				if (from != null) {
					OntResource fromrsrc = sadlTypeReferenceToOntResource(from);
					int i =0;
				}
				SadlTypeReference to = element.getTo();
				if (to != null) {
					OntResource torsrc = sadlTypeReferenceToOntResource(to);
					int i =0;
				}
			}
			else if (spr instanceof SadlIsAnnotation) {
				return getTheJenaModel().createAnnotationProperty(validateUri(propName));
			}
			else {
				throw new JenaProcessorException("Unhandled SadlPropertyRestriction type: " + spr.getClass().getCanonicalName());
			}
		} // end while

		Iterator<SadlPropertyRestriction> itr = element.getRestrictions().iterator();
		if (itr.hasNext()) {
			while (itr.hasNext()) {
				SadlPropertyRestriction rest = itr.next();
				if (rest instanceof SadlIsAnnotation) {
					return createAnnotationProperty(getModelNamespace(), propName);
				}
			}
		}
		else {
			OntProperty ontProp = createObjectProperty(getModelNamespace(), propName, null);
			Iterator<SadlPropertyRestriction> restiter = element.getRestrictions().iterator();
			while (restiter.hasNext()) {
				int i = 0;
			}
			return ontProp;
		}
		return null;
	}

	private SadlResource sadlResourceFromSadlProperty(SadlProperty element) {
		SadlResource sr = element.getNameOrRef();
		if (sr == null) {
			sr = element.getProperty();
		}
		if (sr == null) {
			sr = element.getNameDeclarations().iterator().next();
		}
		return sr;
	}

	private void addPropertyRange(OntProperty prop, RDFNode rngNode) throws JenaProcessorException {
		OntResource existingRange = prop.getRange();
		if (existingRange != null) {
			if (rngNode.equals(existingRange)) {
				// do nothing
				return;
			}
			if (prop.isObjectProperty()) {
				// is the new range a subclass of the existing range, or vice versa?
				if (rngNode.isResource() && rngNode.asResource().canAs(OntClass.class)) {
					OntClass newRngCls = rngNode.asResource().as(OntClass.class);
					RangeValueType rngType = RangeValueType.CLASS_OR_DT;	// default
					updateObjectPropertyRange(prop, newRngCls, rngType);
				}
				else {
					throw new JenaProcessorException("Unable to convert object property range to a class");
				}
			}
			else {
				// TODO issue warning--datatype property range mismatch		
			}
		}
		else {
			RangeValueType rngType = RangeValueType.CLASS_OR_DT;	// default
			prop.addRange(rngNode.asResource());
			addRangeListAnnotationToProperty(prop, rngType);
			logger.debug("Range '" + rngNode.toString() + "' given to property '" + prop.toString() + "'");
		}
	}

	private boolean updatePropertyDomain(int argIdx, OntProperty prop, OntResource domainCls) {
		boolean retval = false;
		OntResource existingDomain = prop.getDomain();
		OntProperty testProp = getTheJenaModel().getOntProperty(prop.getURI());
		if (testProp != null) {
			OntResource testDomain = testProp.getDomain();
			if (testDomain != null) {
				existingDomain = testDomain;
			}
		}
		if (domainCls != null) {
			if (existingDomain != null && !existingDomain.equals(domainCls)) {
				// there's already a domain specified
				if (!prop.getNameSpace().equals(getModelNamespace())) {
					// this is changing the domain of a property defined in a different model
					if (classIsSubclassOf((OntClass) domainCls, existingDomain, true)) {
						addWarning("The domain is a subclass of the domain of property '" + prop.getURI() + "' which is defined in an imported model; perhaps you mean an 'only has values of type' restricion?");						
					}
					else {
						addWarning("This changes the domain of property '" + prop.getURI() + "' which is defined in an imported model; are you sure that is what you want to do?");
					}
				}
				domainCls = addClassToUnionClass(existingDomain, (OntClass) domainCls);
				if (!prop.getNameSpace().equals(getModelNamespace())) {
					prop = createObjectPropertyInCurrentModel(prop);
				}
				else {
					prop.removeDomain(existingDomain);
				}
				prop.addDomain(domainCls);
				retval = true; // return true if it was already the domain
			} else {
				// this is the first domain class given
				if (!prop.getNameSpace().equals(getModelNamespace())) {
					prop = createObjectPropertyInCurrentModel(prop);
				}
				prop.addDomain(domainCls);
				retval = true;
			}
		} else {
			addError("Unable to convert domain (" + domainCls.toString() + ") to a class.");
		}
		return retval;
	}

	private boolean updateObjectPropertyRange(OntProperty prop, OntResource rangeCls, RangeValueType rngType) {
		boolean retval = false;
		OntResource existingRange = prop.getRange();
		OntProperty testProp = getTheJenaModel().getOntProperty(prop.getURI());
		if (testProp != null) {
			OntResource testRange = testProp.getRange();
			if (testRange != null) {
				existingRange = testRange;
			}
		}
		if (rangeCls != null) {
			if (existingRange != null && !existingRange.equals(rangeCls)) {
				// there's already a range class specified
				if (!prop.getNameSpace().equals(getModelNamespace())) {
					// this is changing the range of a property defined in a different model
					if (classIsSubclassOf((OntClass) rangeCls, existingRange, true)) {
						addWarning("The range is a subclass of the range of property '" + prop.getURI() + "' which is defined in an imported model; perhaps you mean an 'only has values of type' restricion?");						
					}
					else {
						addWarning("This changes the range of property '" + prop.getURI() + "' which is defined in an imported model; are you sure that's what you want to do?");
					}
				}
				OntResource newRange = addClassToUnionClass(existingRange, rangeCls);
				if (!prop.getNameSpace().equals(getModelNamespace())) {
					prop = createObjectPropertyInCurrentModel(prop);
				}
				else {
					prop.removeRange(existingRange);
				}
				prop.addRange(newRange); 
				addRangeListAnnotationToProperty(prop, rngType);
				retval = true;
			} else {
				// this is the first range class given
				if (!prop.getNameSpace().equals(getModelNamespace())) {
					prop = createObjectPropertyInCurrentModel(prop);
				}
				prop.addRange(rangeCls);
				addRangeListAnnotationToProperty(prop, rngType);
				retval = true;
			}
		} else {
			addError("Range not found.");
		}
		return retval;
	}

	private void addError(String msg) {
		// TODO Auto-generated method stub
		System.err.println(msg);
	}

	private void addRangeListAnnotationToProperty(OntProperty prop,	RangeValueType rngType) {
		if (rngType.equals(RangeValueType.LIST)) {
			AnnotationProperty annprop = getTheJenaModel().createAnnotationProperty(LIST_RANGE_ANNOTATION_PROPERTY);
			prop.addProperty(annprop, RangeValueType.LIST.toString());
		}
		else if (rngType.equals(RangeValueType.LISTS)) {
			AnnotationProperty annprop = getTheJenaModel().createAnnotationProperty(LIST_RANGE_ANNOTATION_PROPERTY);
			prop.addProperty(annprop, RangeValueType.LISTS.toString());
		}
		
	}

	private OntProperty createObjectPropertyInCurrentModel(OntProperty prop) {
		// TODO Auto-generated method stub
		return null;
	}

	private void addWarning(String msg) {
		// TODO Auto-generated method stub
		System.out.println(msg);
	}

	private OntResource addClassToUnionClass(OntResource existingCls,
			OntResource cls) {
		if (existingCls != null && !existingCls.equals(cls)) {
			if (existingCls.canAs(OntClass.class) && classIsSubclassOf(existingCls.as(OntClass.class), cls, true)) {
				return cls;
			}
			else if (cls.canAs(OntClass.class) && classIsSubclassOf(cls.as(OntClass.class), existingCls, true)) {
				return existingCls;
			}
			else {
				RDFList classes = null;
				if (existingCls.canAs(UnionClass.class)) {
					try {
						 UnionClass ucls = existingCls.as(UnionClass.class);
						 ucls.addOperand(cls);
						 return ucls;
					} catch (Exception e) {
						// don't know why this is happening
						logger.error("Union class error that hasn't been resolved or understood.");
						return cls;
					}
				} else {
					if (cls.equals(existingCls)) {
						return existingCls;
					}
					classes = getTheJenaModel().createList();
					OntResource inCurrentModel = null;
					if (existingCls.isURIResource()) {
						inCurrentModel = getTheJenaModel().getOntResource(existingCls.getURI());
					}
					if (inCurrentModel != null) {
						classes = classes.with(inCurrentModel);
					}
					else {
						classes = classes.with(existingCls);
					}
					classes = classes.with(cls);
				}
				OntResource unionClass = getTheJenaModel().createUnionClass(null,
						classes);
				return unionClass;
			}
		} else {
			return cls;
		}
	}

	private void processSadlNecessaryAndSufficient(SadlNecessaryAndSufficient element,
			ValidationAcceptor issueAcceptor, CancelIndicator cancelIndicator) throws JenaProcessorException {
		OntClass supercls = sadlTypeReferenceToOntResource(element.getSubject()).asClass();
		OntClass rolecls = getOrCreateOntClass(modelNamespace, declarationExtensions.getConcreteName(element.getObject()));
		Iterator<SadlPropertyCondition> itr = element.getPropConditions().iterator();
		List<OntClass> conditionClasses = new ArrayList<OntClass>();
		while (itr.hasNext()) {
			SadlPropertyCondition nxt = itr.next();
			conditionClasses.add(processSadlPropertyCondition(nxt));
		}
		// we have all the parts--create the equivalence class
		if (conditionClasses.size() == 1) {
			if (supercls != null && conditionClasses.get(0) != null) {
				IntersectionClass eqcls = createIntersectionClass(supercls, conditionClasses.get(0));
				rolecls.setEquivalentClass(eqcls);
				logger.debug("New intersection class created as equivalent of '" + rolecls.getURI() + "'");
			} else if (conditionClasses.get(0) != null) {
				rolecls.setEquivalentClass(conditionClasses.get(0));
				logger.debug("Equivalent class set for '" + rolecls.getURI() + "'");
			}
			else {
				throw new JenaProcessorException("Necessary and sufficient conditions appears to have invalid input.");
			}
		}
		else {
			int base = supercls != null ? 1 : 0;
			RDFNode[] memberList = new RDFNode[base + conditionClasses.size()];
			if (base > 0) {
				memberList[0] = supercls;
			}
			for (int i = 0; i < conditionClasses.size(); i++) {
				memberList[base + i] = conditionClasses.get(i);
			}
			IntersectionClass eqcls = createIntersectionClass(memberList);
			rolecls.setEquivalentClass(eqcls);
			logger.debug("New intersection class created as equivalent of '" + rolecls.getURI() + "'");
		}
	}

	private void processSadlDifferentFrom(SadlDifferentFrom element,
			ValidationAcceptor issueAcceptor, CancelIndicator cancelIndicator) throws JenaProcessorException {
		List<Individual> differentFrom = new ArrayList<Individual>();
		Iterator<SadlClassOrPropertyDeclaration> dcitr = element.getTypes().iterator();
		while(dcitr.hasNext()) {
			SadlClassOrPropertyDeclaration decl = dcitr.next();
			Iterator<SadlResource> djitr = decl.getClassOrProperty().iterator();
			while (djitr.hasNext()) {
				SadlResource sr = djitr.next();
				String declUri = declarationExtensions.getConcreteName(sr);
				Individual inst = getTheJenaModel().getIndividual(validateUri(declUri));
				differentFrom.add(inst);
			}
		}
		SadlTypeReference nsas = element.getNotTheSameAs();
		if (nsas != null) {
			OntResource nsasrsrc = sadlTypeReferenceToOntResource(nsas);
			differentFrom.add(nsasrsrc.asIndividual());
			SadlResource sr = element.getNameOrRef();
			Individual otherInst = getTheJenaModel().getIndividual(validateUri(declarationExtensions.getConcreteName(sr)));
			differentFrom.add(otherInst);
		}
		RDFNode[] nodeArray = null;
		if (differentFrom.size() > 0) {
			nodeArray = differentFrom.toArray(new Individual[differentFrom.size()]);
		}
		else {
			throw new JenaProcessorException("Unexpect empty array in processSadlDifferentFrom");
		}
		RDFList differentMembers = getTheJenaModel().createList(nodeArray);
		getTheJenaModel().createAllDifferent(differentMembers);
		logger.debug("New all different from created");
	}

	private Individual processSadlInstance(SadlInstance element, ValidationAcceptor issueAcceptor, CancelIndicator cancelIndicator) {
		// this has two forms:
		//	1) <name> is a <type> ...
		//	2) a <type> <name> ....
		SadlTypeReference type = element.getType();
		SadlResource sr = sadlResourceFromSadlInstance(element);
		if (sr != null) {
			String instName = declarationExtensions.getConcreteName(sr);
			OntClass cls;
			try {
				cls = sadlTypeReferenceToOntResource(type).asClass();
				return createIndividual(getModelNamespace(), instName, cls);
			} catch (JenaProcessorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
//							issueAcceptor.accept(new Issue());
// TODO should this use the issueAccpetor?							
				return null;	// fatal
			}
		}
		return null;
	}

	private SadlResource sadlResourceFromSadlInstance(SadlInstance element) {
		SadlResource sr = element.getNameOrRef();
		if (sr == null) {
			Iterator<SadlResource> instItr = element.getInstance().iterator();
			if (instItr.hasNext()) {
				sr = instItr.next();
			}
		}
		return sr;
	}

	private void processSadlDisjointClasses(SadlDisjointClasses element,
			ValidationAcceptor issueAcceptor, CancelIndicator cancelIndicator) throws JenaProcessorException {
		List<OntClass> disjointClses = new ArrayList<OntClass>();
		if (element.getClasses() != null) {
			Iterator<SadlResource> dcitr = element.getClasses().iterator();
			while (dcitr.hasNext()) {
				SadlResource sr = dcitr.next();
				String declUri = declarationExtensions.getConcreteName(sr);
				OntClass cls = getTheJenaModel().getOntClass(validateUri(declUri));
				disjointClses.add(cls.asClass());
			}
		}
		Iterator<SadlClassOrPropertyDeclaration> dcitr = element.getTypes().iterator();
		while(dcitr.hasNext()) {
			SadlClassOrPropertyDeclaration decl = dcitr.next();
			Iterator<SadlResource> djitr = decl.getClassOrProperty().iterator();
			while (djitr.hasNext()) {
				SadlResource sr = djitr.next();
				String declUri = declarationExtensions.getConcreteName(sr);
				OntClass cls = getTheJenaModel().getOntClass(validateUri(declUri));
				disjointClses.add(cls.asClass());
			}
		}
		// must set them disjoint pairwise
		for (int i = 0; i < disjointClses.size(); i++) {
			for (int j = i + 1; j < disjointClses.size(); j++) {
				disjointClses.get(i).addDisjointWith(disjointClses.get(j));
			}
		}
	}

	private ObjectProperty getOrCreateObjectProperty(String propName) {
		ObjectProperty prop = getTheJenaModel().getObjectProperty(validateUri(propName));
		if (prop == null) {
			prop = getTheJenaModel().createObjectProperty(validateUri(propName));
			logger.debug("New object property '" + prop.getURI() + "' created");
		}
		return prop;
	}

	private boolean checkForExistingCompatibleDatatypeProperty(
			String propUri, RDFNode rngNode) {
		DatatypeProperty prop = getTheJenaModel().getDatatypeProperty(propUri);
		if (prop != null) {
			OntResource rng = prop.getRange();
			if (rng.equals(rngNode)) {
				return true;
			}
		}
		return false;
	}

	private void addPropertyDomain(OntProperty prop, OntResource cls) {
		prop.addDomain(cls);
		logger.debug("Domain '" + cls.toString() + "' added to property '" + prop.getURI() + "'");
	}

	private RDFNode primitiveDatatypeToRDFNode(String name) {
		return getTheJenaModel().getResource(XSD.getURI() + name);
	}

	/**
	 * Call this method before doing semantic validity checks that require imports to be loaded as Jena OntModels.
	 */
	private void loadImportsInJena() {
		
	}
	
	private OntClass getOrCreateOntClass(String modelNamespace, String name) {
		OntClass cls = getTheJenaModel().getOntClass(getUri(modelNamespace, name));
		if (cls == null) {
			cls = createOntClass(modelNamespace, name, (OntClass)null);
		}
		return cls;
	}
	
	private OntClass createOntClass(String modelNamespace, String newName, String superSRUri) {
		if (superSRUri != null) {
			OntClass superCls = getTheJenaModel().getOntClass(validateUri(superSRUri));
			return createOntClass(modelNamespace, newName, superCls);
		}
		return createOntClass(modelNamespace, newName, (OntClass)null);
	}
	
	private OntClass createOntClass(String modelNamespace, String newName, OntClass superCls) {
		OntClass newCls = getTheJenaModel().createClass(getUri(modelNamespace, newName));
		logger.debug("New class '" + newCls.getURI() + "' created");
		if (superCls != null) {
			newCls.addSuperClass(superCls);
			logger.debug("    Class '" + newCls.getURI() + "' given super class '" + superCls.toString() + "'");
		}
		return newCls;
	}
	
	private OntProperty createObjectProperty(String modelNamespace, String newName, String superSRUri) throws JenaProcessorException {
		OntProperty newProp = getTheJenaModel().createObjectProperty(getUri(modelNamespace, newName));
		logger.debug("New object property '" + newProp.getURI() + "' created");
		if (superSRUri != null) {
			OntProperty superProp = getTheJenaModel().getOntProperty(validateUri(superSRUri));
			if (superProp == null) {
				throw new JenaProcessorException("Unable to find super property '" + superSRUri + "'");
			}
			newProp.addSuperProperty(superProp);
			logger.debug("   Object property '" + newProp.getURI() + "' given super property '" + superSRUri + "'");
		}
		return newProp;
	}
	
	private DatatypeProperty createDatatypeProperty(String modelNamespace, String newName, String superSRUri) throws JenaProcessorException {
		DatatypeProperty newProp = getTheJenaModel().createDatatypeProperty(getUri(modelNamespace, newName));
		logger.debug("New datatype property '" + newProp.getURI() + "' created");
		if (superSRUri != null) {
			OntProperty superProp = getTheJenaModel().getOntProperty(superSRUri);
			if (superProp == null) {
				throw new JenaProcessorException("Unable to find super property '" + superSRUri + "'");
			}
			newProp.addSuperProperty(superProp);
			logger.debug("    Datatype property '" + newProp.getURI() + "' given super property '" + superSRUri + "'");
		}
		return newProp;
	}
	
	private Individual createIndividual(String modelNamespace, String newName, String superSRUri) {
		OntClass cls = getTheJenaModel().getOntClass(superSRUri);
		return createIndividual(modelNamespace, newName, cls);
	}
	
	private Individual createIndividual(String modelNamespace, String newName, OntClass supercls) {
		Individual inst = getTheJenaModel().createIndividual(getUri(modelNamespace, newName), supercls);
		logger.debug("New instance '" + inst.getURI() + "' created");
		return inst;
	}
	
	private AnnotationProperty createAnnotationProperty(String modelNamespace, String newName) {
		AnnotationProperty annProp = getTheJenaModel().createAnnotationProperty(getUri(modelNamespace, newName));
		logger.debug("New annotation property '" + annProp.getURI() + "' created");
		return annProp;
	}

	private OntResource sadlTypeReferenceToOntResource(SadlTypeReference sadlTypeRef) throws JenaProcessorException {
		OntResource rsrc = null;
		// TODO How do we tell if this is a union versus an intersection?						
		if (sadlTypeRef instanceof SadlSimpleTypeReference) {
			SadlResource strSR = ((SadlSimpleTypeReference)sadlTypeRef).getType();
			OntConceptType ctype = declarationExtensions.getOntConceptType(strSR);
//TODO Is this just the local name or is it the URI?
			String strSRUri = declarationExtensions.getConcreteName(strSR);	
//			strSRUri = declarationExtensions.getUri(strSR);
//TODO how do I get the URIt?	
			if (ctype.equals(OntConceptType.CLASS)) {
				rsrc = getTheJenaModel().getOntClass(validateUri(strSRUri));
				if (rsrc == null) {
					return createOntClass(getModelNamespace(), strSRUri, (OntClass)null);
				}
			}
			else if (ctype.equals(OntConceptType.INSTANCE)) {
				rsrc = getTheJenaModel().getIndividual(validateUri(strSRUri));
				if (rsrc == null) {
					// is it OK to create Individual without knowing class??
					return createIndividual(getModelNamespace(), strSRUri, (OntClass)null);
				}
			}
		}
		else if (sadlTypeRef instanceof SadlPrimitiveDataType) {
			SadlDataType pt = ((SadlPrimitiveDataType)sadlTypeRef).getPrimitiveType();
			throw new JenaProcessorException("Cannot convert primitive data type (" + pt.toString() + ") to OntResource");
		}
		else if (sadlTypeRef instanceof SadlPropertyCondition) {
			return processSadlPropertyCondition((SadlPropertyCondition) sadlTypeRef);		
		}
		else if (sadlTypeRef instanceof SadlUnionType) {
			SadlTypeReference lft = ((SadlUnionType)sadlTypeRef).getLeft();
			OntClass lftcls = sadlTypeReferenceToOntResource(lft).asClass();
			SadlTypeReference rht = ((SadlUnionType)sadlTypeRef).getRight();
			OntClass rhtcls = sadlTypeReferenceToOntResource(rht).asClass();
			OntClass unionCls = createUnionClass(lftcls, rhtcls);
			return unionCls;
		}
		else if (sadlTypeRef instanceof SadlIntersectionType) {
			SadlTypeReference lft = ((SadlIntersectionType)sadlTypeRef).getLeft();
			OntClass lftcls = sadlTypeReferenceToOntResource(lft).asClass();
			SadlTypeReference rht = ((SadlIntersectionType)sadlTypeRef).getRight();
			OntClass rhtcls = sadlTypeReferenceToOntResource(rht).asClass();
			OntClass intersectCls = createIntersectionClass(lftcls, rhtcls);
			return intersectCls;
		}
		return rsrc;
	}

	private OntClass processSadlPropertyCondition(SadlPropertyCondition sadlPropCond) throws JenaProcessorException {
		OntClass retval = null;
		SadlResource sr = ((SadlPropertyCondition)sadlPropCond).getProperty();
		String propName = declarationExtensions.getConcreteName(sr);
		OntConceptType propType = declarationExtensions.getOntConceptType(sr);
		OntProperty prop = getTheJenaModel().getOntProperty(validateUri(propName));
		Iterator<SadlCondition> conditer = ((SadlPropertyCondition)sadlPropCond).getCond().iterator();
		while (conditer.hasNext()) {
			SadlCondition cond = conditer.next();
			retval = processSadlCondition(cond, prop, propType);
			if (conditer.hasNext()) {
				throw new JenaProcessorException("Multiple property conditions not currently handled");
			}
		}
		return retval;
	}

	private OntClass processSadlCondition(SadlCondition cond, OntProperty prop, OntConceptType propType) throws JenaProcessorException {
		OntClass retval = null;
		if (cond instanceof SadlAllValuesCondition) {
			SadlTypeReference type = ((SadlAllValuesCondition)cond).getType();
			OntResource typersrc = sadlTypeReferenceToOntResource(type);
			AllValuesFromRestriction avf = getTheJenaModel().createAllValuesFromRestriction(null, prop, typersrc);
			logger.debug("New all values from restriction on '" + prop.getURI() + "' to values of type '" + typersrc.toString() + "'");
			retval = avf;
		}
		else if (cond instanceof SadlHasValueCondition) {
			SadlExplicitValue value = ((SadlHasValueCondition)cond).getRestriction();
			if (propType.equals(OntConceptType.CLASS_PROPERTY)) {
				if (value instanceof SadlResource) {
					SadlResource srValue = ((SadlResource)value).getName();
					OntConceptType srType = declarationExtensions.getOntConceptType(srValue);
					if (srType.equals(OntConceptType.INSTANCE)) {
						Individual valInst = getTheJenaModel().getIndividual(validateUri(declarationExtensions.getConcreteName(srValue)));
						if (valueInObjectTypePropertyRange(prop, valInst)) {
							HasValueRestriction hvr = getTheJenaModel().createHasValueRestriction(null, prop, valInst);
							logger.debug("New has value restriction on '" + prop.getURI() + "' to value '" + valInst.toString() + "'");
							retval =  hvr;
						}
						else {
							throw new JenaProcessorException("Value '" + valInst.getURI() + "' not in range of object property '" + prop.getURI() + "'");
						}
					}
					else {
						throw new JenaProcessorException("A has value restriction on an object property must have an instance as the restricted value");
					}
				}
				else {
					throw new JenaProcessorException("A has value restriction on an object property has an unexpected restricted value type: " + value.getClass().getCanonicalName());
				}
			}
			else if (propType.equals(OntConceptType.DATATYPE_PROPERTY)) {
				Literal val = sadlExplicitValueToLiteral(value, prop);
				if (valueInDatatypePropertyRange(prop, val)) {
					HasValueRestriction hvr = getTheJenaModel().createHasValueRestriction(null, prop, val);
					logger.debug("New has value restriction on '" + prop.getURI() + "' to value '" + val.toString() + "'");
					retval =  hvr;
				}	
				else {
					throw new JenaProcessorException("Value '" + val.getLexicalForm() + "' not in range of datatype property '" + prop.getURI() + "'");
				}
			}
			else {
				throw new JenaProcessorException("Has value restriction on unexpected property type: " + propType.toString());
			}
		}
		else if (cond instanceof SadlCardinalityCondition) {
			// Note: SomeValuesFrom is embedded in cardinality in the SADL grammar--an "at least" cardinality with "one" instead of # 
			String cardinality = ((SadlCardinalityCondition)cond).getCardinality();
			SadlTypeReference type = ((SadlCardinalityCondition)cond).getType();
			OntResource typersrc = null;
			if (type != null) {
				typersrc = sadlTypeReferenceToOntResource(type);					
			}
			if (cardinality.equals("one")) {
				// this is interpreted as a someValuesFrom restriction
				if (type == null) {
					throw new JenaProcessorException("'one' means some value from class so a type must be given");
				}
				SomeValuesFromRestriction svf = getTheJenaModel().createSomeValuesFromRestriction(null, prop, typersrc);
				logger.debug("New some values from restriction on '" + prop.getURI() + "' to values of type '" + typersrc.toString() + "'");
				retval =  svf;
			}
			else {
				// cardinality restrictioin
				int cardNum = Integer.parseInt(cardinality);
				String op = ((SadlCardinalityCondition)cond).getOperator();
				if (op == null) {
					CardinalityRestriction cr = getTheJenaModel().createCardinalityRestriction(null, prop, cardNum);	
					logger.debug("New cardinality restriction " + cardNum + " on '" + prop.getURI() + "' created");
					if (type != null) {
						cr.removeAll(OWL.cardinality);
						cr.addLiteral(OWL2.qualifiedCardinality, cardNum);
						cr.addProperty(OWL2.onClass, typersrc);
					}
					retval =  cr;
				}
				else if (op.equals("least")) {
					MinCardinalityRestriction cr = getTheJenaModel().createMinCardinalityRestriction(null, prop, cardNum);							
					logger.debug("New min cardinality restriction " + cardNum + " on '" + prop.getURI() + "' created");
					if (type != null) {
						cr.removeAll(OWL.minCardinality);
						cr.addLiteral(OWL2.minQualifiedCardinality, cardNum);
						cr.addProperty(OWL2.onClass, typersrc);
					}
					retval =  cr;
				}
				else if (op.equals("most")) {
					logger.debug("New max cardinality restriction " + cardNum + " on '" + prop.getURI() + "' created");
					MaxCardinalityRestriction cr = getTheJenaModel().createMaxCardinalityRestriction(null, prop, cardNum);							
					if (type != null) {
						cr.removeAll(OWL.maxCardinality);
						cr.addLiteral(OWL2.maxQualifiedCardinality, cardNum);
						cr.addProperty(OWL2.onClass, typersrc);
					}
					retval =  cr;
				}
				if (logger.isDebugEnabled()) {
					if (type != null) {
						logger.debug("   cardinality is qualified; values must be of type '" + typersrc + "'");
					}	
				}
			}
		}
		else {
			throw new JenaProcessorException("Unhandled SadlCondition type: " + cond.getClass().getCanonicalName());
		}
		return retval;
	}

	private boolean valueInDatatypePropertyRange(OntProperty prop, Literal val) {
		String ptype = prop.getRange().getURI();
		if (ptype == null) {
			return true;
		}
		String dtype = val.getDatatypeURI();
		if (dtype.equals(ptype)) {
			return true;
		}
		return false;
	}

	private Literal sadlExplicitValueToLiteral(SadlExplicitValue value, OntProperty prop) throws JenaProcessorException {
		if (value instanceof SadlNumberLiteral) {
			String val = ((SadlNumberLiteral)value).getLiteralNumber();
			return UtilsForJena.getLiteralMatchingDataPropertyRange(getTheJenaModel(), prop, val);
		}
		else if (value instanceof SadlStringLiteral) {
			String val = ((SadlStringLiteral)value).getLiteralString();
			return UtilsForJena.getLiteralMatchingDataPropertyRange(getTheJenaModel(), prop, val);
		}
		else if (value instanceof SadlBooleanLiteral) {
			SadlBooleanLiteral val = ((SadlBooleanLiteral)value);
			return UtilsForJena.getLiteralMatchingDataPropertyRange(getTheJenaModel(), prop, val.toString());
		}
		else if (value instanceof SadlValueList) {
			throw new JenaProcessorException("A SADL value list cannot be converted to a Literal");
		}
		else if (value instanceof SadlConstantLiteral) {
			String val = ((SadlConstantLiteral)value).getTerm();
			return UtilsForJena.getLiteralMatchingDataPropertyRange(getTheJenaModel(), prop, val);
		}
		else {
			throw new JenaProcessorException("Unhandled sadl explicit vaue type: " + value.getClass().getCanonicalName());
		}
	}

	private boolean valueInObjectTypePropertyRange(OntProperty prop, Individual valInst) {
		ExtendedIterator<? extends OntResource> itr = prop.listRange();
		while (itr.hasNext()) {
			OntResource nxt = itr.next();
			if (nxt.isClass()) {
				if (instanceBelongsToClass(getTheJenaModel(), valInst, nxt)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private IntersectionClass createIntersectionClass(List<RDFNode> members) throws JenaProcessorException {
		RDFNode[] array = members.toArray(new RDFNode[members.size()]);
		return createIntersectionClass(array);
	}

	private IntersectionClass createIntersectionClass(RDFNode... members) throws JenaProcessorException {
		RDFList classes = getTheJenaModel().createList(members);
		if (!classes.isEmpty()) {
			IntersectionClass intersectCls = getTheJenaModel().createIntersectionClass(null, classes);
			logger.debug("New intersection class created");
			return intersectCls;
		}
		throw new JenaProcessorException("createIntersectionClass called with empty list of classes");
	}

	private UnionClass createUnionClass(List<RDFNode> members) throws JenaProcessorException {
		RDFNode[] array = members.toArray(new RDFNode[members.size()]);
		return createUnionClass(array);
	}
	private UnionClass createUnionClass(RDFNode... members) throws JenaProcessorException {
		RDFList classes = getTheJenaModel().createList(members);
		if (!classes.isEmpty()) {
			UnionClass unionCls = getTheJenaModel().createUnionClass(null, classes);
			logger.debug("New union class created");
			return unionCls;
		}
		throw new JenaProcessorException("createUnionClass called with empty list of classes");
	}

	private OntConceptType getSadlTypeReferenceType(SadlTypeReference sadlTypeRef) throws JenaProcessorException {
		if (sadlTypeRef instanceof SadlSimpleTypeReference) {
			SadlResource sr = ((SadlSimpleTypeReference)sadlTypeRef).getType();
			return declarationExtensions.getOntConceptType(sr);
		}
		else if (sadlTypeRef instanceof SadlPrimitiveDataType) {
			return OntConceptType.DATATYPE;
		}
		else if (sadlTypeRef instanceof SadlPropertyCondition) {
			// property conditions => OntClass
			return OntConceptType.CLASS;
		}
		else if (sadlTypeRef instanceof SadlUnionType) {
			SadlTypeReference lft = ((SadlUnionType)sadlTypeRef).getLeft();
			OntConceptType lfttype = getSadlTypeReferenceType(lft);
			return lfttype;
//			SadlTypeReference rght = ((SadlUnionType)sadlTypeRef).getRight();
		}
		else if (sadlTypeRef instanceof SadlIntersectionType) {
			SadlTypeReference lft = ((SadlIntersectionType)sadlTypeRef).getLeft();
			OntConceptType lfttype = getSadlTypeReferenceType(lft);
			return lfttype;
//			SadlTypeReference rght = ((SadlIntersectionType)sadlTypeRef).getRight();
		}
		throw new JenaProcessorException("Unexpected SadlTypeReference subtype: " + sadlTypeRef.getClass().getCanonicalName());
	}

	private String validateUri(String strSRUri) {
		if (!strSRUri.contains("#")) {
			return getUri(getModelNamespace(), strSRUri);
		}
		return strSRUri;
	}

	private String assureNamespaceEndsWithHash(String name) {
		name = name.trim();
		if (!name.endsWith("#")) {
			return name + "#";
		}
		return name;
	}

	private String getUri(String modelNamespace, String nm) {
		modelNamespace = assureNamespaceEndsWithHash(modelNamespace);
		return modelNamespace + nm;
	}

	private String getModelNamespace() {
		return modelNamespace;
	}

	private void setModelNamespace(String modelNamespace) {
		this.modelNamespace = modelNamespace;
	}

	public OntDocumentManager getJenaDocumentMgr(OntModelSpec ontModelSpec) {
		if (jenaDocumentMgr == null) {
			if (getMappingModel() != null) {
				setJenaDocumentMgr(new OntDocumentManager(getMappingModel()));
				if (ontModelSpec != null) {
					ontModelSpec.setDocumentManager(jenaDocumentMgr);
				}
			}
			else {
				setJenaDocumentMgr(OntDocumentManager.getInstance());
			}
		}
		return jenaDocumentMgr;
	}

	private void setJenaDocumentMgr(OntDocumentManager ontDocumentManager) {
		jenaDocumentMgr = ontDocumentManager;
	}

	private Model getMappingModel() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * return true if the instance belongs to the class else return false
	 * 
	 * @param inst
	 * @param cls
	 * @return
	 */
	private boolean instanceBelongsToClass(OntModel m, OntResource inst, OntResource cls) {
		// The following cases must be considered:
		// 1) The class is a union of other classes. Check to see if the instance is a member of any of
		//		the union classes and if so return true.
		// 2) The class is an intersection of other classes. Check to see if the instance is 
		//		a member of each class in the intersection and if so return true.
		// 3) The class is neither a union nor an intersection. If the instance belongs to the class return true. Otherwise
		//		check to see if the instance belongs to a subclass of the class else
		//		return false. (Superclasses do not need to be considered because even if the instance belongs to a super
		//		class that does not tell us that it belongs to the class.)
		
		/*
		 * e.g., 	Internet is a Network.
		 * 			Network is a type of Subsystem.
		 * 			Subsystem is type of System.
		 */
		if (cls.isURIResource()) {
			cls = m.getOntClass(cls.getURI());
		}
		if (cls == null) {
			return false;
		}
		if (cls.canAs(UnionClass.class)) {
			List<OntResource> uclses = getOntResourcesInUnionClass(m, cls.as(UnionClass.class));	
			for (int i = 0; i < uclses.size(); i++) {
				OntResource ucls = uclses.get(i);
				if (instanceBelongsToClass(m, inst, ucls)) {
					return true;
				}
			}
		}
		else if (cls.canAs(IntersectionClass.class)) {
			List<OntResource> uclses = getOntResourcesInIntersectionClass(m, cls.as(IntersectionClass.class));	
			for (int i = 0; i < uclses.size(); i++) {
				OntResource ucls = uclses.get(i);
				if (!instanceBelongsToClass(m, inst, ucls)) {
					return false;
				}
			}
			return true;
		}
		else if (cls.canAs(Restriction.class)) {
			Restriction rest = cls.as(Restriction.class);
			OntProperty ontp = rest.getOnProperty();				
			if (rest.isAllValuesFromRestriction()) {
				StmtIterator siter = inst.listProperties(ontp);
				while (siter.hasNext()) {
					Statement stmt = siter.nextStatement();
					RDFNode obj = stmt.getObject();
					if (obj.canAs(Individual.class)) {
						com.hp.hpl.jena.rdf.model.Resource avfc = rest.asAllValuesFromRestriction().getAllValuesFrom();
						if (!instanceBelongsToClass(m, (Individual)obj.as(Individual.class), (OntResource)avfc.as(OntResource.class))) {
							return false;
						}
					}
				}
			}
			else if (rest.isSomeValuesFromRestriction()) {
				if (inst.hasProperty(ontp)) {
					return true;
				}
			}
			else if (rest.isHasValueRestriction()) {
				RDFNode hval = rest.as(HasValueRestriction.class).getHasValue();
				if (inst.hasProperty(ontp, hval)) {
					return true;
				}
			}
			else if (rest.isCardinalityRestriction()) {
				
			}
			else if (rest.isMaxCardinalityRestriction()) {
				
			}
			else if (rest.isMinCardinalityRestriction()) {
				
			}
		}
		else {
			if (inst.canAs(Individual.class)) {
				ExtendedIterator<com.hp.hpl.jena.rdf.model.Resource> eitr = inst.asIndividual().listRDFTypes(false);
				while (eitr.hasNext()) {
					com.hp.hpl.jena.rdf.model.Resource r = eitr.next();				
					OntResource or = m.getOntResource(r);
					if (or.isURIResource()) {
						OntClass oc = m.getOntClass(or.getURI());
						if (classIsSubclassOf(oc, cls, true)) {
							eitr.close();
							return true;
						}
					}
					else if (or.canAs(OntClass.class)) {
						if (classIsSubclassOf(or.as(OntClass.class), cls, true)) {
							eitr.close();
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	/**
	 * return true if the first argument class is a subclass of the second
	 * argument class
	 * 
	 * @param subcls
	 * @param cls
	 * @return
	 */
	private boolean classIsSubclassOf(OntClass subcls, OntResource cls, boolean rootCall) {
		if (subcls == null || cls == null) {
			return false;
		}
		if (cls.isURIResource() && subcls.isURIResource()
				&& cls.getURI().equals(subcls.getURI())) {
			return true;
		}
		if (cls.isAnon()) {
			if (cls.canAs(OntClass.class)) {
				OntClass ocls = cls.as(OntClass.class);
				if (ocls.isUnionClass()) {
					UnionClass ucls = cls.as(UnionClass.class);
					try {
						ExtendedIterator<? extends OntClass> eitr = ucls
								.listOperands();
						while (eitr.hasNext()) {
							OntClass uclsmember = eitr.next();
							if (classIsSubclassOf(subcls, uclsmember, false)) {
								eitr.close();
								return true;
							}
						}
					}
					catch (Exception e) {
						logger.error("Unexpected error during deep validation: apparent Union Class does not return operands.");
					}
				}
			}
		}
		try {
			if (cls.canAs(OntClass.class)) {
				ExtendedIterator<OntClass> eitr = cls.as(OntClass.class).listSubClasses();
				while (eitr.hasNext()) {
					OntClass subClsOfCls = eitr.next();
					if (subClsOfCls.equals(subcls)) {
						eitr.close();
						return true;
					}
					else {
						if (classIsSubclassOf(subcls, subClsOfCls, false)) {
							eitr.close();
							return true;
						}
					}
				}
				eitr.close();
//				if (rootCall && classIsSuperClassOf(cls.as(OntClass.class), subcls)) {
//					return true;
//				}
			}
			if (subcls.isAnon()) {
				if (subcls.isIntersectionClass()) {
					IntersectionClass icls = subcls.asIntersectionClass();
					try {
						ExtendedIterator<? extends OntClass> eitr = icls.listOperands();
						while (eitr.hasNext()) {
							OntClass iclsmember = eitr.next();
							if (classIsSubclassOf(cls.as(OntClass.class), iclsmember, false)) {
								eitr.close();
								return true;
							}
						}
					}
					catch (Exception e) {
						logger.error("Unexpected error during deep validation: apparent Intersection Class does not return operands.");
					}
				}
			}
// TODO We need to look for equivalent classes that provide a definition for a subclass, 
//			e.g. Component is equivalent to System is class, (System and connectedTo someValueFrom Network) => Component subclass of System.
			if (cls.canAs(OntClass.class)) {
				ExtendedIterator<OntClass> eqitr = cls.as(OntClass.class).listEquivalentClasses();
				while (eqitr.hasNext()) {
					OntClass eqcls = eqitr.next();
					if (classIsSubclassOf(subcls, eqcls, false)) {
						return true;
					}
				}
			}

		} catch (Throwable t) {
			t.printStackTrace();
		}
		return false;
	}

	private boolean classIsSuperClassOf(OntClass cls, OntClass subcls) {
		ExtendedIterator<OntClass> eitr = subcls.listSuperClasses();
		try {
			while (eitr.hasNext()) {
				OntClass sprcls = eitr.next();
				if (sprcls.equals(cls)) {
					return true;
				}
				if (classIsSuperClassOf(cls, sprcls)) {
					return true;
				}
			}
			eitr.close();
			
			eitr = cls.listSuperClasses();
			while (eitr.hasNext()) {
				OntClass equivCls = eitr.next();
				if (classIsSubclassOf(subcls, equivCls, false)) {
					eitr.close();
					return true;
				}
			}
		}
		catch (Throwable t) {
			logger.error("Error checking if class '" + cls.toString() + "' is a superclass of '" + subcls.toString() + 
					"' : " + t.getMessage());
		}
		finally {
			eitr.close();
		}
		return false;
	}

	private List<OntResource> getOntResourcesInUnionClass(OntModel m, UnionClass ucls) {
		List<OntResource> results = new ArrayList<OntResource>();
		List<RDFNode> clses = ucls.getOperands().asJavaList();
		for (int i = 0; i < clses.size(); i++) {
			RDFNode mcls = clses.get(i);
			if (mcls.canAs(OntResource.class)) {
				results.add(mcls.as(OntResource.class));
			}
		}
		return results;
	}
	
	private List<OntResource> getOntResourcesInIntersectionClass(OntModel m, IntersectionClass icls) {
		List<OntResource> results = new ArrayList<OntResource>();
		List<RDFNode> clses = icls.getOperands().asJavaList();
		for (int i = 0; i < clses.size(); i++) {
			RDFNode mcls = clses.get(i);
			if (mcls.canAs(OntResource.class)) {
				results.add(mcls.as(OntResource.class));
			}
		}
		return results;
	}

}
