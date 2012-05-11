package com.lti.swtutils.image;

/**
 * Re-packs parent shell on image size change.
 * @author Ken Larson
 *
 */
public class PackImageSizeChangedListener implements ImageSizeChangedListener
{

	public void onImageSizeChanged(ImageControl sender)
	{
		if (sender.isDisposed())
			return;
		sender.getShell().pack();
	}

}
