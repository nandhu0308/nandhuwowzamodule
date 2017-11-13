package com.haappyapp.wowza;
import com.wowza.wms.logging.*;
import com.wowza.wms.rtp.depacketizer.*;
import com.wowza.wms.rtp.model.*;
import com.wowza.wms.transport.mpeg2.*;
public class RTPDePacketizerMPEGTSSimpleExample extends RTPDePacketizerMPEGTSNotifyBase{
	private static final Class<RTPDePacketizerMPEGTSSimpleExample> CLASS = RTPDePacketizerMPEGTSSimpleExample.class;
	private static final String CLASSNAME = "RTPDePacketizerMPEGTSSimpleExample";

	@Override
	public void onInit(RTPDePacketizerMPEGTS rtDePacketizerMPEGTS, RTPContext rtpContext, RTPDePacketizerItem rtpDePacketizerItem)
	{
		WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+".onInit: "+rtDePacketizerMPEGTS.getContextStr());
	}

	@Override
	public void onStartup(RTPDePacketizerMPEGTS rtDePacketizerMPEGTS, RTPTrack rtpTrack)
	{
		WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+".onStartup: "+rtDePacketizerMPEGTS.getContextStr());
	}

	@Override
	public void onShutdown(RTPDePacketizerMPEGTS rtDePacketizerMPEGTS, RTPTrack rtpTrack)
	{
		WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+".onShutdown: "+rtDePacketizerMPEGTS.getContextStr());
	}

	@Override
	public void onPAT(RTPDePacketizerMPEGTS rtDePacketizerMPEGTS, ProgramAssociationTable newPAT)
	{
		WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+".onPAT: "+rtDePacketizerMPEGTS.getContextStr());
	}

	@Override
	public void onPMT(RTPDePacketizerMPEGTS rtDePacketizerMPEGTS, ProgramMapTable newPMT)
	{
		WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+".onPMT: "+rtDePacketizerMPEGTS.getContextStr());
	}
}
