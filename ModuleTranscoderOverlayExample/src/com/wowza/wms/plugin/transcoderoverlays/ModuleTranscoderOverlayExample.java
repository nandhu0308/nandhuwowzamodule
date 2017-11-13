package com.wowza.wms.plugin.transcoderoverlays;

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

import java.awt.Color;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wowza.util.SystemUtils;
import com.wowza.wms.application.*;
import com.wowza.wms.amf.*;
import com.wowza.wms.client.*;
import com.wowza.wms.module.*;
import com.wowza.wms.request.*;
import com.wowza.wms.stream.*;
import com.wowza.wms.stream.livetranscoder.ILiveStreamTranscoder;
import com.wowza.wms.stream.livetranscoder.ILiveStreamTranscoderNotify;
import com.wowza.wms.transcoder.model.LiveStreamTranscoder;
import com.wowza.wms.transcoder.model.LiveStreamTranscoderActionNotifyBase;
import com.wowza.wms.transcoder.model.TranscoderSession;
import com.wowza.wms.transcoder.model.TranscoderSessionVideo;
import com.wowza.wms.transcoder.model.TranscoderSessionVideoEncode;
import com.wowza.wms.transcoder.model.TranscoderStream;
import com.wowza.wms.transcoder.model.TranscoderStreamDestination;
import com.wowza.wms.transcoder.model.TranscoderStreamDestinationVideo;
import com.wowza.wms.transcoder.model.TranscoderStreamSourceVideo;
import com.wowza.wms.transcoder.model.TranscoderVideoDecoderNotifyBase;
import com.wowza.wms.transcoder.model.TranscoderVideoOverlayFrame;

public class ModuleTranscoderOverlayExample extends ModuleBase {
	
	String graphicName = "logo_${com.wowza.wms.plugin.transcoderoverlays.overlayimage.step}.png";
	int overlayIndex = 1;
	    
	private IApplicationInstance appInstance = null;
	private String basePath = null;
	private Object lock = new Object();
	

	public void doSomething(IClient client, RequestFunction function, AMFDataList params) {
		getLogger().info("doSomething");
		sendResult(client, params, "Hello Wowza");
	}

	public void onAppStart(IApplicationInstance appInstance) {
		//String fullname = appInstance.getApplication().getName() + "/" + appInstance.getName();
		//getLogger().info("onAppStart: " + fullname);
		String fullname = appInstance.getApplication().getName() + "/" + appInstance.getName();
	       getLogger().info("onAppStart: " + fullname);
	       this.appInstance = appInstance;
	       String artworkPath = "${com.wowza.wms.context.VHostConfigHome}/content/" + appInstance.getApplication().getName();
	       Map<String, String> envMap = new HashMap<String, String>();
	       if (appInstance.getVHost() != null)
	       {
	            envMap.put("com.wowza.wms.context.VHost", appInstance.getVHost().getName());
	            envMap.put("com.wowza.wms.context.VHostConfigHome", appInstance.getVHost().getHomePath());
	       }
	       envMap.put("com.wowza.wms.context.Application", appInstance.getApplication().getName());
	       if (this != null)
	             envMap.put("com.wowza.wms.context.ApplicationInstance", appInstance.getName());
	       this.basePath =  SystemUtils.expandEnvironmentVariables(artworkPath, envMap);
	       this.basePath = this.basePath.replace("\", "//");
	       if (!this.basePath.endsWith("/"))
	            this.basePath = this.basePath+"/";
	       this.appInstance.addLiveStreamTranscoderListener(new TranscoderCreateNotifierExample());
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

}
