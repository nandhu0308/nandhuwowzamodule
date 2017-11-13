package com.haappyapp.wowza;
import java.util.*;

import com.wowza.wms.amf.*;
import com.wowza.wms.logging.*;
import com.wowza.wms.rtp.depacketizer.RTPDePacketizerItem;
import com.wowza.wms.rtp.depacketizer.RTPDePacketizerMPEGTS;
import com.wowza.wms.rtp.depacketizer.RTPDePacketizerMPEGTSNotifyBase;
import com.wowza.wms.rtp.model.*;
import com.wowza.wms.stream.*;
import com.wowza.wms.transport.mpeg2.*;
import com.wowza.wms.transport.mpeg2.ProgramMapTable.*;
import com.wowza.wms.transport.mpeg2.section.cue.*;
public class RTPDePacketizerMPEGTSMonitorCUE extends RTPDePacketizerMPEGTSNotifyBase{
	private static final Class<RTPDePacketizerMPEGTSMonitorCUE> CLASS = RTPDePacketizerMPEGTSMonitorCUE.class;
	private static final String CLASSNAME = "RTPDePacketizerMPEGTSMonitorCUE";

	private IMediaStream stream = null;
	private RTPDePacketizerMPEGTS rtDePacketizerMPEGTS = null;
	private boolean isTimecodeReady = false;
	private RTPTrack rtpTrack = null;
	private boolean debugLog = false;

	class MPEGTSMonitorCUE implements IMPEG2UserMonitorSectionNotify
	{
		public void onMonitorStart()
		{
		}

		public void onMonitorStop()
		{
		}

		public void onDataSection(int pid, AdaptationField field, MPEG2Section section)
		{
			if (section.getTableID() == SpliceInformationTable.SIT_TABLE_ID)
			{
				try
				{
					SpliceInformationTable spliceInformationTable = new SpliceInformationTable(section);
					if (spliceInformationTable != null)
					{
						if (debugLog)
							WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME + ".onDataSection: " + spliceInformationTable.toString());

						SpliceInformationTableSerializeAMFContext serializeContext = new SpliceInformationTableSerializeAMFContext();

						serializeContext.timeReference = rtDePacketizerMPEGTS.getVideoTC();
						serializeContext.rtpTrack = rtpTrack;

						AMFDataObj amfData = spliceInformationTable.serializeAMF(serializeContext);
						if (amfData != null)
							stream.sendDirect("onCUE", amfData);
					}
				}
				catch(Exception e)
				{
					WMSLoggerFactory.getLogger(CLASS).error(CLASSNAME+".onDataSection: ", e);
				}
			}
		}
	}

	@Override
	public void onInit(RTPDePacketizerMPEGTS rtDePacketizerMPEGTS, RTPContext rtpContext, RTPDePacketizerItem rtpDePacketizerItem)
	{
		this.debugLog = rtDePacketizerMPEGTS.getProperties().getPropertyBoolean("rtpDePacketizerMPEGTSMonitorKLVDebugLog", this.debugLog);
	}

	@Override
	public void onStartup(RTPDePacketizerMPEGTS rtDePacketizerMPEGTS, RTPTrack rtpTrack)
	{
		WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+".onStartup");

		this.rtDePacketizerMPEGTS = rtDePacketizerMPEGTS;
		this.rtpTrack = rtpTrack;

		RTPStream rtpStream = rtpTrack.getRTPStream();
		if (rtpStream != null)
			this.stream = rtpStream.getStream();
	}

	@Override
	public void onTimecodeReady(RTPDePacketizerMPEGTS rtDePacketizerMPEGTS)
	{
		WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+".onTimecodeReady");

		this.isTimecodeReady = true;
	}

	@Override
	public void onPMT(RTPDePacketizerMPEGTS rtDePacketizerMPEGTS, ProgramMapTable newPMT)
	{
		WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+".onPMT");

		boolean SCTE35RegDescFound = false;

		ArrayList<Descriptor> regDescriptors = newPMT.programDescriptors.get(Descriptor.DESCRIPTOR_TAG_REGISTRATION);

		if (regDescriptors != null && regDescriptors.size() > 0)
		{
			for (Descriptor desc : regDescriptors)
			{
				SCTE35RegDescFound |= ((RegistrationDescriptor)desc).formatIdentifier == RegistrationDescriptor.REG_IDENTIFICATION_SCTE_SPLICE_FORMAT;
			}
		}

		for (StreamInfo s : newPMT.streams.values())
		{
			if (SCTE35RegDescFound)
			{
				ArrayList<Descriptor> descriptors = null;

				if (descriptors == null)
					descriptors = s.descriptors.get(Descriptor.DESCRIPTOR_TAG_CUE_IDENTIFIER);

				if (descriptors == null)
					descriptors = s.descriptors.get(Descriptor.DESCRIPTOR_TAG_STREAM_IDENTIFIER);

				if (descriptors != null)
				{
					WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+".onPMT: Hit cue point PID: 0x"+Integer.toHexString(s.PID));

					if (!rtDePacketizerMPEGTS.containsPIDMonitorMap(s.PID))
					{
						rtDePacketizerMPEGTS.putPIDMonitorMap(s.PID, new MPEGTSMonitorCUE());
					}
				}
			}
		}
	}
}
