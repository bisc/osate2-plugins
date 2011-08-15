/*
*
* <copyright>
* Copyright � 2004 by Carnegie Mellon University, all rights reserved.
*
* Use of the Open Source AADL Tool Environment (OSATE) is subject to the terms of the license set forth
* at http://www.eclipse.org/legal/cpl-v10.html.
*
* NO WARRANTY
*
* ANY INFORMATION, MATERIALS, SERVICES, INTELLECTUAL PROPERTY OR OTHER PROPERTY OR RIGHTS GRANTED OR PROVIDED BY
* CARNEGIE MELLON UNIVERSITY PURSUANT TO THIS LICENSE (HEREINAFTER THE �DELIVERABLES�) ARE ON AN �AS-IS� BASIS.
* CARNEGIE MELLON UNIVERSITY MAKES NO WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED AS TO ANY MATTER INCLUDING,
* BUT NOT LIMITED TO, WARRANTY OF FITNESS FOR A PARTICULAR PURPOSE, MERCHANTABILITY, INFORMATIONAL CONTENT,
* NONINFRINGEMENT, OR ERROR-FREE OPERATION. CARNEGIE MELLON UNIVERSITY SHALL NOT BE LIABLE FOR INDIRECT, SPECIAL OR
* CONSEQUENTIAL DAMAGES, SUCH AS LOSS OF PROFITS OR INABILITY TO USE SAID INTELLECTUAL PROPERTY, UNDER THIS LICENSE,
* REGARDLESS OF WHETHER SUCH PARTY WAS AWARE OF THE POSSIBILITY OF SUCH DAMAGES. LICENSEE AGREES THAT IT WILL NOT
* MAKE ANY WARRANTY ON BEHALF OF CARNEGIE MELLON UNIVERSITY, EXPRESS OR IMPLIED, TO ANY PERSON CONCERNING THE
* APPLICATION OF OR THE RESULTS TO BE OBTAINED WITH THE DELIVERABLES UNDER THIS LICENSE.
*
* Licensee hereby agrees to defend, indemnify, and hold harmless Carnegie Mellon University, its trustees, officers,
* employees, and agents from all claims or demands made against them (and any related losses, expenses, or
* attorney�s fees) arising out of, or relating to Licensee�s and/or its sub licensees� negligent use or willful
* misuse of or negligent conduct or willful misconduct regarding the Software, facilities, or other rights or
* assistance granted by Carnegie Mellon University under this License, including, but not limited to, any claims of
* product liability, personal injury, death, damage to property, or violation of any laws or regulations.
*
* Carnegie Mellon University Software Engineering Institute authored documents are sponsored by the U.S. Department
* of Defense under Contract F19628-00-C-0003. Carnegie Mellon University retains copyrights in all material produced
* under this contract. The U.S. Government retains a non-exclusive, royalty-free license to publish or reproduce these
* documents, or allow others to do so, for U.S. Government purposes only pursuant to the copyright license
* under the contract clause at 252.227.7013.
*
* </copyright>
*
*
* %W%
* @version %I% %H%
*/
package edu.cmu.sei.aadl.resourcemanagement.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.osate.aadl2.ComponentCategory;
import org.osate.aadl2.UnitLiteral;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.aadl2.instance.SystemOperationMode;
import org.osate.aadl2.modelsupport.errorreporting.AnalysisErrorReporterManager;
import org.osate.aadl2.modelsupport.modeltraversal.ForAllElement;
import org.osate.aadl2.properties.InvalidModelException;
import org.osate.xtext.aadl2.properties.AadlProject;
import org.osate.xtext.aadl2.properties.DeploymentProperties;
import org.osate.xtext.aadl2.properties.TimingProperties;
import org.osgi.framework.Bundle;

import edu.cmu.sei.aadl.resourcemanagement.ResourcemanagementPlugin;
import edu.cmu.sei.aadl.scheduling.TimingAnalysisInvocation;


public final class Schedule extends AbstractInstanceOrDeclarativeModelModifyActionAction {
	private ScheduleProperties properties = null;
	
	protected void initPropertyReferences() {
		Property period = lookupPropertyDefinition(TimingProperties.PERIOD);
		Property deadline = lookupPropertyDefinition(TimingProperties.DEADLINE);
		Property computeExecutionTime = lookupPropertyDefinition(TimingProperties.COMPUTE_EXECUTION_TIME);
		Property actualProcessorBinding = lookupPropertyDefinition(DeploymentProperties.ACTUAL_PROCESSOR_BINDING);
		UnitLiteral microSecond = lookupUnitLiteral(AadlProject.TIME_UNITS, AadlProject.US_LITERAL);
		Property priority = lookupOptionalPropertyDefinition(SEI._NAME, SEI.PRIORITY);
		
		properties = new ScheduleProperties(period, deadline, computeExecutionTime, actualProcessorBinding, priority, microSecond);
	}
	
	
	protected Bundle getBundle() {
		return ResourcemanagementPlugin.getDefault().getBundle();
	}

	protected String getActionName() {
		return "Test scheduability";
	}
	
	protected String getMarkerType() {
		return "edu.cmu.sei.aadl.resourcemanagement.ScheduleObjectMarker";
	}

	@Override
	protected void analyzeDeclarativeModel(final IProgressMonitor monitor,
			final AnalysisErrorReporterManager errManager,
			final Element declarativeObject) {
		Dialog.showError("Schedule Error", "Can only schedule system instances");
	}

	protected void analyzeInstanceModel(final IProgressMonitor monitor,
			final AnalysisErrorReporterManager errManager,
			final SystemInstance root, final SystemOperationMode som) {
		monitor.beginTask(getActionName(), IProgressMonitor.UNKNOWN);
		try {
			ForAllElement processProcessor = new ForAllElement(errManager) {
				@Override
				public void process(Element obj) {
					ComponentInstance ci = (ComponentInstance) obj;
					boolean schedulable =
						TimingAnalysisInvocation.timingSchedulabilityAnalysis(properties,
								this.getErrorManager(), ci);
					if (schedulable) {
						info(ci, ci.getInstanceObjectPath() + " is schedulable");
					} else {
						info(ci, ci.getInstanceObjectPath() + " is not schedulable");
					}
					return;
				}
			};
			processProcessor.processPreOrderComponentInstance(root,
					ComponentCategory.PROCESSOR);
		} catch (InvalidModelException e) {
			error(e.getElement(), e.getMessage());
		}
		monitor.done();
	}
}