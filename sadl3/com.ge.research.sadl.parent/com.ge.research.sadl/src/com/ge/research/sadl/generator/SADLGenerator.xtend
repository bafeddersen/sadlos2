/*
 * generated by Xtext 2.9.0-SNAPSHOT
 */
package com.ge.research.sadl.generator

import com.ge.research.sadl.processing.SadlModelProcessorProvider
import com.google.inject.Inject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext

/**
 * Generates code from your model files on save.
 * 
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#code-generation
 */
class SADLGenerator extends AbstractGenerator {

	@Inject SadlModelProcessorProvider processorProvider 
	
	override doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext ctx) {
		val processor = processorProvider.getProcessor(resource.resourceSet)
		processor.onGenerate(resource, fsa, ctx.cancelIndicator)
	}
	
}
