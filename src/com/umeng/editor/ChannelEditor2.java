package com.umeng.editor;

import com.umeng.editor.decode.AXMLDoc;
import com.umeng.editor.decode.BTagNode;
import com.umeng.editor.decode.BTagNode.Attribute;
import com.umeng.editor.decode.BXMLNode;
import com.umeng.editor.decode.StringBlock;
import com.umeng.editor.utils.TypedValue;

import java.util.List;

public class ChannelEditor2 {
	private final String NAME_SPACE = "http://schemas.android.com/apk/res/android";
	private final String META_DATA = "meta-data";
	private final String NAME = "name";
	private final String VALUE = "value";
	
	private String mChannelName = "UMENG_CHANNEL";
	private String mChannelValue = "vipshop";

	private String mCPS_IDName="CPS_ID";
	private String mCPS_IDValue="MobileAds:cbadb924:5d214a8b";
	
	private int namespace_channel;
	private int meta_data_channel;
	private int attr_name_channel;
	private int attr_value_channel;
	private int channel_name;
	private int channel_value = -1;
	private int namespace_CPS_ID;
	private int attr_name_CPS_ID;
	private int attr_value_CPS_ID;
	private int meta_data_CPS_ID;
	private int CPS_ID_name;
	private int CPS_ID_value = -1;
	
	private AXMLDoc doc;
	
	public ChannelEditor2(AXMLDoc doc){
		this.doc = doc;
	}
	
	public void setChannel(String channel){
		mChannelValue = channel;
	}
	public void setCPS_ID(String CPS_ID){
		mCPS_IDValue = CPS_ID;
	}
	
	//First add resource and get mapping ids
	private void registStringBlockChannel(StringBlock sb){
		namespace_channel = sb.putString(NAME_SPACE);
		meta_data_channel = sb.putString(META_DATA);
		attr_name_channel = sb.putString(NAME);
		attr_value_channel = sb.putString(VALUE);
		channel_name = sb.putString(mChannelName);
		System.out.println("channel_value="+channel_value);
		if(channel_value == -1){
			channel_value = sb.addString(mChannelValue);//now we have a seat in StringBlock
		}

	}
	private void registStringBlockCPS_ID(StringBlock sb){
		namespace_CPS_ID = sb.putString(NAME_SPACE);
		meta_data_CPS_ID = sb.putString(META_DATA);
		attr_name_CPS_ID = sb.putString(NAME);
		attr_value_CPS_ID = sb.putString(VALUE);
		CPS_ID_name = sb.putString(mCPS_IDName);

		if(CPS_ID_value == -1){
			CPS_ID_value = sb.addString(mCPS_IDValue);//now we have a seat in StringBlock
		}

	}
	//put string to the seat
	private void replaceChannelValue(StringBlock sb){
		System.out.println("channel_value="+channel_value);
		sb.setString(channel_value, mChannelValue);
	}

	//put string to the seat
	private void replaceCPS_IDValue(StringBlock sb){
		System.out.println("CPS_ID_value="+CPS_ID_value);
		sb.setString(CPS_ID_value, mCPS_IDValue);
	}

	//Second find&change meta-data's value or add a new one
	private void editNode(AXMLDoc doc){
		BXMLNode application = doc.getApplicationNode(); //manifest node
		List<BXMLNode> children = application.getChildren();
		
		BTagNode umeng_meta = null;
		BTagNode CPS_ID_meta = null;
		
		end:for(BXMLNode node : children){
			BTagNode m = (BTagNode)node;
			//it's a risk that the value for "android:name" maybe not String
			if((meta_data_channel == m.getName()) && (m.getAttrStringForKey(attr_name_channel) == channel_name)){
					umeng_meta = m;
			}else if((meta_data_CPS_ID == m.getName()) && (m.getAttrStringForKey(attr_name_CPS_ID) == CPS_ID_name)){
				   CPS_ID_meta = m;
			}else if(umeng_meta!=null&&CPS_ID_meta!=null){
				break end;
			}
		}
		
		if(umeng_meta != null){
			umeng_meta.setAttrStringForKey(attr_value_channel, channel_value);
		}else{

				Attribute name_attr = new Attribute(namespace_channel, attr_name_channel, TypedValue.TYPE_STRING);
				name_attr.setString(channel_name);
				Attribute value_attr = new Attribute(namespace_channel, attr_value_channel, TypedValue.TYPE_STRING);
				value_attr.setString(channel_value);

				umeng_meta = new BTagNode(-1, meta_data_channel);
				umeng_meta.setAttribute(name_attr);
				umeng_meta.setAttribute(value_attr);

				children.add(umeng_meta);

		}

		if(CPS_ID_meta != null){
			CPS_ID_meta.setAttrStringForKey(attr_value_CPS_ID, CPS_ID_value);
		}else{

				Attribute name_attr = new Attribute(namespace_CPS_ID, attr_name_CPS_ID, TypedValue.TYPE_STRING);
				name_attr.setString(CPS_ID_name);
				Attribute value_attr = new Attribute(namespace_CPS_ID, attr_value_CPS_ID, TypedValue.TYPE_STRING);
				value_attr.setString(CPS_ID_value);

				CPS_ID_meta = new BTagNode(-1, meta_data_CPS_ID);
				CPS_ID_meta.setAttribute(name_attr);
				CPS_ID_meta.setAttribute(value_attr);

				children.add(CPS_ID_meta);

		}
	}
	
	public void commit() {
		StringBlock stringBlock=doc.getStringBlock();
		registStringBlockChannel(stringBlock);
		registStringBlockCPS_ID(stringBlock);
		editNode(doc);
		replaceChannelValue(stringBlock);
	    replaceCPS_IDValue(stringBlock);
	}

}
