package com.haappyapp.wowza.inject;

import com.wowza.wms.application.*;
import com.wowza.wms.amf.*;
import com.wowza.wms.client.*;
import com.wowza.wms.module.*;
import com.wowza.wms.request.*;
import com.wowza.wms.stream.*;
import com.wowza.wms.rtp.model.*;
import com.wowza.wms.httpstreamer.model.*;
import com.wowza.wms.httpstreamer.cupertinostreaming.httpstreamer.*;
import com.wowza.wms.httpstreamer.smoothstreaming.httpstreamer.*;

public class ModuleInjectData extends ModuleBase {

	public void doSomething(IClient client, RequestFunction function, AMFDataList params) {
		getLogger().info("doSomething");
		sendResult(client, params, "Hello Wowza");
	}

	public void onAppStart(IApplicationInstance appInstance) {
		String fullname = appInstance.getApplication().getName() + "/" + appInstance.getName();
		getLogger().info("onAppStart: " + fullname);
	}

	public void onAppStop(IApplicationInstance appInstance) {
		String fullname = appInstance.getApplication().getName() + "/" + appInstance.getName();
		getLogger().info("onAppStop: " + fullname);
	}

	public void onConnect(IClient client, RequestFunction function, AMFDataList params) {
		getLogger().info("onConnect: " + client.getClientId());
	}

	public void onConnectAccept(IClient client) {
		getLogger().info("onConnectAccept: " + client.getClientId());
	}

	public void onConnectReject(IClient client) {
		getLogger().info("onConnectReject: " + client.getClientId());
	}

	public void onDisconnect(IClient client) {
		getLogger().info("onDisconnect: " + client.getClientId());
	}

	public void setCaption(IClient client, RequestFunction function, AMFDataList params) {
		String streamname = params.getString(PARAM1);
		String text = params.getString(PARAM2);
		String language = params.getString(PARAM3);
		String trackid = params.getString(PARAM4);

		IMediaStream stream = client.getAppInstance().getStreams().getStream(streamname);

		// essential code
		AMFDataMixedArray data = new AMFDataMixedArray();
		data.put("text", new AMFDataItem(text));
		data.put("language", new AMFDataItem(language));
		data.put("trackid", new AMFDataItem(trackid));
		stream.sendDirect("onTextData", data);
		((MediaStream) stream).processSendDirectMessages();
		getLogger().info("Caption: " + text);
	}

	public void injectMetaData(IClient client, RequestFunction function, AMFDataList params) {
		String streamName = params.getString(PARAM1);
		String data = params.getString(PARAM2);
		IMediaStream stream = client.getAppInstance().getStreams().getStream(streamName);
		if (stream != null) {
			AMFDataList amfList = new AMFDataList();

			amfList.add(new AMFDataItem("@setDataFrame"));
			amfList.add(new AMFDataItem("onMetaData"));

			AMFDataMixedArray metaData = new AMFDataMixedArray();

			metaData.put("param1", data);
			metaData.put("param2", new AMFDataItem("data2"));

			amfList.add(metaData);

			synchronized (stream) {
				byte[] dataData = amfList.serialize();
				int size = dataData.length;
				long timecode = Math.max(stream.getAudioTC(), stream.getVideoTC());
				stream.setDataTC(timecode);
				stream.setDataSize(size);
				stream.startDataPacket();
				stream.addDataData(dataData, 0, size);
			}
		}

	}

}
