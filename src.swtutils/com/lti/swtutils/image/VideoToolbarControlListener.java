package com.lti.swtutils.image;

/**
 * 
 * @author Ken Larson
 *
 */
public interface VideoToolbarControlListener
{
	public void onPlay(VideoToolbarControl sender);
	public void onPause(VideoToolbarControl sender);
	public void onFastForward(VideoToolbarControl sender);
	public void onRewind(VideoToolbarControl sender);
	public void onSeekBeginning(VideoToolbarControl sender);
	public void onSeekEnd(VideoToolbarControl sender);
	public void onSnap(VideoToolbarControl sender);
	public void onLive(VideoToolbarControl sender);
	public void onLoop(VideoToolbarControl sender);
	public void onStop(VideoToolbarControl sender);
	public void onPtz(VideoToolbarControl sender);
}
