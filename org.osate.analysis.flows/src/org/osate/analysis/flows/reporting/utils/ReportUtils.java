package org.osate.analysis.flows.reporting.utils;

import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.osate.aadl2.instance.InstanceObject;
import org.osate.aadl2.modelsupport.resources.OsateResourceUtil;

public class ReportUtils {

	/**
	 * @param root - the root object related to the report
	 * @param subDirectory - the directory where we store the report, relative to the root object
	 * @param reportType - the type of the report (latency, etc.)
	 * @param fileSuffix - any suffix for the file
	 * @param fileExtension - the file extension (.csv, .xls, etc.)
	 * @return the path for the file.
	 */
	public static IPath getReportPath(EObject root, String subDirectory, String reportType, String fileSuffix,
			String fileExtension) {
		String filename = null;
		subDirectory = subDirectory.replaceAll(" ", "");
		Resource res = root.eResource();
		URI uri = res.getURI();
		IPath path = OsateResourceUtil.getOsatePath(uri);
		if (root instanceof InstanceObject) {
			path = path.removeFileExtension();
			filename = path.lastSegment() + "__" + reportType;
			if (fileSuffix != null) {
				filename = filename + fileSuffix;
			}
			path = path.removeLastSegments(1).append("/reports/" + reportType + "/" + filename);
		} else {
			filename = path.lastSegment() + reportType;
			if (fileSuffix != null) {
				filename = filename + fileSuffix;
			}
			path = path.removeLastSegments(1).append("/reports/" + reportType + "/" + filename);
		}

		path = path.addFileExtension(fileExtension);
		return path;
	}

}
