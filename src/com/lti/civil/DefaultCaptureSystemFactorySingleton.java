/*
 * Created on Jun 1, 2005
 */
package com.lti.civil;

import com.lti.civil.impl.jni.NativeCaptureSystemFactory;
import com.lti.civil.impl.qtjava.QTCaptureSystemFactory;
import com.lti.utils.OSUtils;

/**
 * Singleton for the {@link CaptureSystemFactory} to be used for the running OS.
 * @author Ken Larson
 */
public class DefaultCaptureSystemFactorySingleton
{
	private static CaptureSystemFactory instance;
	/** Get the global default CaptureSystemFactory. */
	public static CaptureSystemFactory instance()
	{
		if (instance != null)
			return instance;
		if (OSUtils.isLinux())
			instance = new NativeCaptureSystemFactory();
		else if (OSUtils.isWindows())
			instance = new NativeCaptureSystemFactory();
		else if (OSUtils.isMacOSX())
			instance = new QTCaptureSystemFactory();
//		else
//			instance = new JMFCaptureSystemFactory();
		return instance;
	}
}
