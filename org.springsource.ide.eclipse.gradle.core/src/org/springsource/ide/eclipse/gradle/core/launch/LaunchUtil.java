/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.gradle.core.launch;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.springsource.ide.eclipse.gradle.core.GradleProject;
import org.springsource.ide.eclipse.gradle.core.launch.GradleLaunchConfigurationDelegate;
import org.springsource.ide.eclipse.gradle.core.launch.GradleProcess;


/**
 * Utility class providing methods to help launching Gradle process executing tasks.
 * 
 * @author Kris De Volder
 */
public class LaunchUtil {

	public static GradleProcess synchLaunch(ILaunchConfiguration launchConf) throws CoreException {
		DebugPlugin mgr = DebugPlugin.getDefault();
		LaunchTerminationListener listener = null;
		try {
			//DebugUITools.launch(launchConf, "run");
			ILaunch launch = launchConf.launch("run", new NullProgressMonitor(), false, true);
			listener = new LaunchTerminationListener(launch);
			mgr.getLaunchManager().addLaunchListener(listener);
			return listener.waitForProcess();
		} finally {
			if (listener!=null) {
				mgr.getLaunchManager().removeLaunchListener(listener);
			}
		}
	}

	/**
	 * Launches a list of tasks on a given project, waits for launch to terminate.
	 * @return The (terminated) GradleProcess associated with the launch.
	 */
	public static GradleProcess launchTasks(GradleProject project, String... tasks) throws CoreException {
		ILaunchConfigurationWorkingCopy conf = (ILaunchConfigurationWorkingCopy) GradleLaunchConfigurationDelegate.createDefault(project, false);
		GradleLaunchConfigurationDelegate.setTasks(conf, Arrays.asList(tasks));
		return synchLaunch(conf);
	}
	
	/**
	 * Launches a configuration associated with a task. If such a configuration already exist it is reused, otherwise a new configuration
	 * is created with default settings. In either case, the configuration will end up as a persistent conf.  If it already it 
	 * existed before, it means it was already persisted before. If it is new, the new conf will be saved before launching it.
	 * 
	 * @throws CoreException 
	 */
	public static GradleProcess launchTaskConf(GradleProject project, String task) throws CoreException {
		ILaunchConfiguration conf = GradleLaunchConfigurationDelegate.getOrCreate(project, task);
		return synchLaunch(conf);
	}

	public static String generateConfigName(String baseName) {
		return getLaunchManager().generateLaunchConfigurationName(baseName);
	}

	public static ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}
	
}
