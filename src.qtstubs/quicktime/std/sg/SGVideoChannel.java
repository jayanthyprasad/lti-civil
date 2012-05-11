/*
 * Created on Jun 2, 2005
 */
package quicktime.std.sg;

import quicktime.QTException;
import quicktime.qd.QDRect;
import quicktime.std.image.ImageDescription;

public class SGVideoChannel extends SGChannel
{
	public SGVideoChannel(SequenceGrabber g) throws QTException {}
	public void setBounds(QDRect r) {}
	public QDRect getSrcVideoBounds() {return null;}
	public void setFrameRate(int val) {}
	public void setUsage(int val) {}
	public void setCompressorType(int val) {}
  public void disposeQTObject()
  {
    // TODO Auto-generated method stub
    
  }
  public ImageDescription getImageDescription()
  {
    // TODO Auto-generated method stub
    return null;
  }
}
