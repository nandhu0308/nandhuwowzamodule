package com.haappyapp.wowza;
import java.util.*;

import com.wowza.util.*;
import com.wowza.wms.amf.*;
import com.wowza.wms.logging.*;
import com.wowza.wms.rtp.depacketizer.RTPDePacketizerItem;
import com.wowza.wms.rtp.depacketizer.RTPDePacketizerMPEGTS;
import com.wowza.wms.rtp.depacketizer.RTPDePacketizerMPEGTSNotifyBase;
import com.wowza.wms.rtp.model.*;
import com.wowza.wms.stream.*;
import com.wowza.wms.transport.mpeg2.*;
import com.wowza.wms.transport.mpeg2.MPEG2PESPacket.*;
import com.wowza.wms.transport.mpeg2.ProgramMapTable.*;
public class RTPDePacketizerMPEGTSMonitorKLV extends RTPDePacketizerMPEGTSNotifyBase{
	private static final Class<RTPDePacketizerMPEGTSMonitorKLV> CLASS = RTPDePacketizerMPEGTSMonitorKLV.class;
	private static final String CLASSNAME = "RTPDePacketizerMPEGTSMonitorKLV";

	private IMediaStream stream = null;
	private RTPDePacketizerMPEGTS rtDePacketizerMPEGTS = null;
	private boolean isTimecodeReady = false;
	private RTPTrack rtpTrack = null;
	private boolean debugLog = false;

	class MPEGTSMonitorKLV implements IMPEG2UserMonitorPESNotify
	{
		public void onMonitorStart()
		{
		}

		public void onMonitorStop()
		{
		}

		// gets called when there is new section table data
		public void onDataPES(int pid, PESHeader header, byte[] buffer, int offset, int len)
		{
			// loop through each KLV access unit
			int pos = 0;
			int index = 0;
			while(true)
			{
				if ((pos+5) > len)
					break;

				int metadataServiceId = (buffer[offset+pos] & 0x0FF); pos += 1;
				int sequenceNumber = (buffer[offset+pos] & 0x0FF); pos += 1;
				int metadataFlags = (buffer[offset+pos] & 0x0FF); pos += 1;
				int cellDataLen = (BufferUtils.byteArrayToInt(buffer, pos, 2) & 0x0FFFF); pos += 2;

				if (cellDataLen > (len-pos))
					cellDataLen = (len-pos);

				if (debugLog)
					WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+".onDataPES: KLV["+index+"]: metadataServiceId:"+metadataServiceId+" sequenceNumber:"+sequenceNumber+" metadataFlags:0x"+Integer.toHexString(metadataFlags)+" cellDataLen:"+cellDataLen+" timescale:"+rtpTrack.getTimescale());

				if (stream != null && isTimecodeReady)
				{
					try
					{
						// if stream id is 0xFC then PTS is in the header if not use the last video timecode
						RolloverLong ptsTC = null;
						if ((header.streamId & 0x0FF) == 0xFC && header.PTS >= 0)
						{
							ptsTC = rtDePacketizerMPEGTS.getDataTC();
							ptsTC.set(header.PTS);
						}
						else
							ptsTC = rtDePacketizerMPEGTS.getVideoTC();

						String klvDataStr = "";

						if (cellDataLen > 0)
							klvDataStr = com.wowza.util.Base64.encodeBytes(buffer, pos, cellDataLen, com.wowza.util.Base64.DONT_BREAK_LINES);

						AMFDataObj amfData = new AMFDataObj();

						amfData.put("streamId", new AMFDataItem(header.streamId));
						amfData.put("metadataServiceId", new AMFDataItem(metadataServiceId));
						amfData.put("sequenceNumber", new AMFDataItem(sequenceNumber));
						amfData.put("metadataFlags", new AMFDataItem(metadataFlags));
						amfData.put("timecode", new AMFDataItem(((ptsTC.get()*1000)/rtpTrack.getTimescale())));
						amfData.put("data", new AMFDataItem(klvDataStr));
						amfData.put("dataLen", new AMFDataItem(cellDataLen));

						stream.sendDirect("onKLV", amfData);
					}
					catch(Exception e)
					{
						WMSLoggerFactory.getLogger(CLASS).error(CLASSNAME+"onDataPES: ", e);
					}
				}

				pos += cellDataLen;
				index++;
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

		// loop through the streams defined in the program map table (PMT) and look for PIDs with the KLV descriptor
		for (StreamInfo s : newPMT.streams.values())
		{
			boolean addMonitor = false;

			// look for metadata descriptor
			ArrayList<Descriptor> descriptors = null;
			descriptors = s.descriptors.get(Descriptor.DESCRIPTOR_TAG_METADATA);
			if (descriptors != null)
			{
				for(Descriptor descriptor : descriptors)
				{
					if (((MetadataDescriptor)descriptor).metadataFormatIdentifier == RegistrationDescriptor.REG_IDENTIFICATION_KLV)
						addMonitor = true;
				}
			}

			// see if registration descriptor is KLV
			descriptors = s.descriptors.get(Descriptor.DESCRIPTOR_TAG_REGISTRATION);
			if (descriptors != null)
			{
				for(Descriptor descriptor : descriptors)
				{
					if (((RegistrationDescriptor)descriptor).formatIdentifier == RegistrationDescriptor.REG_IDENTIFICATION_KLV)
						addMonitor = true;
				}
			}

			// add a PID section monitor
			if (addMonitor)
			{
				WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+".onPMT: Hit KLV PID: 0x"+Integer.toHexString(s.PID));

				if (!rtDePacketizerMPEGTS.containsPIDMonitorMap(s.PID))
					rtDePacketizerMPEGTS.putPIDMonitorMap(s.PID, new MPEGTSMonitorKLV());
			}
		}
	}
}
