package com.ge.research.sadl.ui.syntaxcoloring

import com.ge.research.sadl.model.DeclarationExtensions
import com.ge.research.sadl.sADL.SADLPackage
import com.ge.research.sadl.sADL.SadlIsInverseOf
import com.ge.research.sadl.sADL.SadlModel
import com.ge.research.sadl.sADL.SadlPropertyCondition
import com.ge.research.sadl.sADL.SadlPropertyInitializer
import com.ge.research.sadl.sADL.SadlResource
import com.ge.research.sadl.sADL.SadlSimpleTypeReference
import com.google.inject.Inject
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EStructuralFeature
import org.eclipse.xtext.ide.editor.syntaxcoloring.IHighlightedPositionAcceptor
import org.eclipse.xtext.ide.editor.syntaxcoloring.ISemanticHighlightingCalculator
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.util.CancelIndicator

class SadlSemanticHighlightingCalculator implements ISemanticHighlightingCalculator {
	@Inject package DeclarationExtensions declarationExtensions

	override void provideHighlightingFor(XtextResource resource, IHighlightedPositionAcceptor acceptor,
		CancelIndicator cancelIndicator) {
		if (resource === null || resource.contents.isEmpty())
			return;
		var SadlModel model = resource.contents.head as SadlModel
		// Highlighting for URI strings
		for (imp : model.imports) {
			var nodesForUri = NodeModelUtils.findNodesForFeature(imp, SADLPackage.Literals.SADL_IMPORT__IMPORTED_RESOURCE)
			for (node : nodesForUri) {
				acceptor.addPosition(node.offset, node.length, SadlHighlightingConfiguration.URI_ID)
			}
		}
		if (model.alias !== null) {
			for (node : NodeModelUtils.findNodesForFeature(model, SADLPackage.Literals.SADL_MODEL__ALIAS)) {
				acceptor.addPosition(node.offset, node.length, SadlHighlightingConfiguration.URI_ID)
			}
		}
		for (element : model.eAllContents.toList) {
			switch element {
				SadlResource : {
					var node = NodeModelUtils.findActualNodeFor(element)
					var highlightingId = getHighlightingId(element)
					acceptor.addPosition(node.offset, node.length, highlightingId)
				}
				SadlPropertyCondition : {
					var highlightingId = getHighlightingId(element.property)
					acceptor.highlight(element, SADLPackage.Literals.SADL_PROPERTY_CONDITION__PROPERTY, highlightingId)
				}
				SadlPropertyInitializer : {
					var highlightingId = getHighlightingId(element.property)
					acceptor.highlight(element, SADLPackage.Literals.SADL_PROPERTY_INITIALIZER__PROPERTY, highlightingId)
				}
				SadlSimpleTypeReference : {
					var highlightingId = getHighlightingId(element.type)
					acceptor.highlight(element, SADLPackage.Literals.SADL_SIMPLE_TYPE_REFERENCE__TYPE, highlightingId)
				}
				SadlIsInverseOf : {
					var highlightingId = getHighlightingId(element.otherProperty)
					acceptor.highlight(element, SADLPackage.Literals.SADL_IS_INVERSE_OF__OTHER_PROPERTY, highlightingId)
				}
			}
		}
	}
	
	protected def void highlight(IHighlightedPositionAcceptor acceptor, EObject object, EStructuralFeature feature, String id) {
		for (node : NodeModelUtils.findNodesForFeature(object, feature)) {
			acceptor.addPosition(node.offset, node.length, id)
		}
	}

	def private String getHighlightingId(SadlResource rn) {
		switch (declarationExtensions.getOntConceptType(rn)) {
			case CLASS_PROPERTY: {
				return SadlHighlightingConfiguration.OBJECT_PROPERTY_ID
			}
			case DATATYPE_PROPERTY: {
				return SadlHighlightingConfiguration.DATA_PROPERTY_ID
			}
			case ANNOTATION_PROPERTY: {
				return SadlHighlightingConfiguration.ANNOTATION_PROPERTY_ID
			}
			case INSTANCE: {
				return SadlHighlightingConfiguration.INSTANCE_ID
			}
			case CLASS: {
				return SadlHighlightingConfiguration.CLASS_ID
			}
			case DATATYPE: {
				return SadlHighlightingConfiguration.RDFDATATYPE_ID
			}
			default: {
				return SadlHighlightingConfiguration.VARIABLE_ID
			}
		}
	}
}
