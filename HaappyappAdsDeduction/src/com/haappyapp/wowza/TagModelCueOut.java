package com.haappyapp.wowza;

import java.io.*;

import com.wowza.wms.manifest.model.m3u8.tag.*;
import com.wowza.wms.manifest.writer.m3u8.*;

public class TagModelCueOut extends TagModel
{
	public TagModelCueOut()
	{
		super("EXT-X-CUE-OUT");
	}

	@Override
	public boolean validForMasterPlaylist()
	{
		return false;
	}

	@Override
	public boolean validForMediaPlaylist()
	{
		return true;
	}

	@Override
	public String toString()
	{
		return "#" + tagName;
	}

	@Override
	public void write(TagWriter writer) throws IOException
	{
		writer.writeTag(tagName);
	}

	@Override
	public boolean isMediaSegmentTag()
	{
		return false;
	}

	@Override
	public boolean isValid(Integer version)
	{
		return true;
	}
}

