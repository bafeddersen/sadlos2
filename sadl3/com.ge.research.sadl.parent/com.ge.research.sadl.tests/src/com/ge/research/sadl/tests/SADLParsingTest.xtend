/*
 * generated by Xtext 2.9.0-SNAPSHOT
 */
package com.ge.research.sadl.tests

import com.ge.research.sadl.sADL.Model
import com.google.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.eclipse.xtext.junit4.validation.ValidationTestHelper
import org.junit.Assert
import org.junit.runner.RunWith

@RunWith(XtextRunner)
@InjectWith(SADLInjectorProvider)
abstract class SADLParsingTest{

	@Inject ParseHelper<Model> parseHelper
	@Inject ValidationTestHelper validationTestHelper
	
	protected def void assertNoErrors(CharSequence text) {
		val model = parseHelper.parse(text)
		val issues = validationTestHelper.validate(model)
		if (issues.isEmpty)
			return;
		var String annotatedText = text.toString
		for (issue : issues.sortBy[-offset]) {
			annotatedText = annotatedText.substring(0, issue.offset) + '''[«issue.message»]''' + annotatedText.substring(issue.offset)
		}
		Assert.assertEquals(text.toString, annotatedText)
	}

}
