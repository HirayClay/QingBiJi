package com.thinkernote.ThinkerNote.Activity;

import java.util.Vector;

import org.json.JSONObject;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;

import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Action.TNAction.TNRunner;
import com.thinkernote.ThinkerNote.Adapter.TNPreferenceAdapter;
import com.thinkernote.ThinkerNote.Data.TNCat;
import com.thinkernote.ThinkerNote.Data.TNPreferenceChild;
import com.thinkernote.ThinkerNote.Data.TNPreferenceGroup;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.General.Log;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsDialog;
import com.thinkernote.ThinkerNote.General.TNUtilsSkin;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.base.TNActBase;

// TODO
public class TNCatInfoAct extends TNActBase
	implements OnClickListener, OnChildClickListener, OnGroupClickListener{

	/* Bundle:
	 * CatLocalId
	 */
	
	private ExpandableListView mListView;
	private Vector<TNPreferenceGroup> mGroups;
	private TNPreferenceChild mCurrChild;
	private long mCatId;
	private TNCat mCurrentCat;
	
	// Activity methods
	//-------------------------------------------------------------------------------
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.catinfo);
		setViews();
		
		mCatId = getIntent().getLongExtra("CatId", -1);

		mGroups = new Vector<TNPreferenceGroup>();
		
		mListView = (ExpandableListView)findViewById(R.id.catinfo_expandablelistview);
		mListView.setAdapter(new TNPreferenceAdapter(this, mGroups));
		
		mListView.setOnGroupClickListener(this);
		mListView.setOnChildClickListener(this);
	}
	
	protected void setViews(){
		TNUtilsSkin.setViewBackground(this, null, R.id.catinfo_toolbar_layout, R.drawable.toolbg);
		TNUtilsSkin.setViewBackground(this, null, R.id.catinfo_page, R.drawable.page_bg);
		findViewById(R.id.catinfo_back).setOnClickListener(this);
	}

	// configView
	//-------------------------------------------------------------------------------
	protected void configView(){
		getCatInfos();
		
		((BaseExpandableListAdapter)mListView.getExpandableListAdapter()).notifyDataSetChanged();
		for(int i=0; i<mGroups.size(); i++){
			mListView.expandGroup(i);
		}
	}
	
	private void getCatInfos(){
		TNSettings setting = TNSettings.getInstance();
		mCurrentCat = TNDbUtils.getCat(mCatId);
		
		mGroups.clear();
		TNPreferenceGroup group = null;
		
		//文件夹
		group = new TNPreferenceGroup(getString(R.string.catinfo_folder));
		{	//名称
			{
				boolean visibleMoreBtn = true;
				group.addChild(new TNPreferenceChild(getString(R.string.catinfo_name), mCurrentCat.catName, visibleMoreBtn, new TNRunner(this, "changeFolderName")));
			}
			{//所属文件夹
				String info = mCurrentCat.catName;
				if(mCurrentCat.pCatId > 0)
					info = TNDbUtils.getCat(mCurrentCat.pCatId).catName;
				else
					info = getString(R.string.catinfo_nogroup);
				boolean visibleMoreBtn = true;
				group.addChild(new TNPreferenceChild(getString(R.string.catinfo_group), info, visibleMoreBtn, new TNRunner(this, "changeFolderParent")));
			}
			{//笔记数量
				group.addChild(new TNPreferenceChild(getString(R.string.catinfo_notecount), String.valueOf(mCurrentCat.noteCounts), false, null));
			}
			{//默认文件夹
				String info = getString(R.string.catinfo_no);
				if( mCurrentCat.catId == setting.defaultCatId ){
					info = getString(R.string.catinfo_yes);
				}
				group.addChild(new TNPreferenceChild(getString(R.string.catinfo_isdefault), info, false, null));
			}
			{//删除
				if(mCurrentCat.catId != setting.defaultCatId){
					boolean visibleMoreBtn = true;
					group.addChild(new TNPreferenceChild(getString(R.string.catinfo_delete), null, visibleMoreBtn, new TNRunner(this, "deleteFolder")));
				}
			}
			//设为默认文件夹
			if(mCurrentCat.catId != setting.defaultCatId){
				group.addChild(new TNPreferenceChild(getString(R.string.catinfo_setdefault), null, true, new TNRunner(this, "setdefault")));
			}
		}
		mGroups.add(group);
	}
	
	//Child click methods
	public void changeFolderName(){
		Bundle b = new Bundle();
		b.putString("TextType", "cat_rename");
		b.putString("TextHint", getString(R.string.textedit_folder));
		b.putString("OriginalText", mCurrentCat.catName);
		b.putLong("ParentId", mCurrentCat.catId);
		startActivity(TNTextEditAct.class, b);
	}
	
	public void changeFolderParent(){
		Bundle b = new Bundle();
		b.putLong("OriginalCatId", mCurrentCat.pCatId);
		b.putLong("ChangeFolderForFolderList", mCurrentCat.catId);
		b.putInt("Type", 0);

		startActForResult(TNCatListAct.class, b, R.string.catinfo_group);
	}

	public void deleteFolder(){
		TNUtilsDialog.deleteCatDialog(this, null, mCurrentCat);
	}

	public void setdefault(){
		setDefaultCatDialog();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if( requestCode == R.string.catinfo_group){
			if(resultCode == RESULT_OK){
				Log.i(TAG,"selectedId = " + data.getLongExtra("SelectedCatId", 0));
				long selectId = data.getLongExtra("SelectedCatId", 0);
				mCurrentCat.pCatId = selectId;
				configView();
			}
		}
	}
	
	// implements OnClickListener
	//-------------------------------------------------------------------------------
	@Override
	public boolean onGroupClick(ExpandableListView parent, View v,
			int groupPosition, long id) {
		return true;
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		mCurrChild = mGroups.get(groupPosition).getChilds().get(childPosition);

		if(mCurrChild.getTargetMethod() != null){
			mCurrChild.getTargetMethod().run();
			return true;
		}
		return false;
	}

	// implements OnClickListener
	//-------------------------------------------------------------------------------
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.catinfo_back:
			finish();
			break;
		}			
	}

	// private methods
	//-------------------------------------------------------------------------------
	
	private void setDefaultCatDialog(){
		DialogInterface.OnClickListener pbtn_Click = 
			new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				TNAction.runActionAsync(TNActionType.SetDefaultFolderId, mCurrentCat.catId);
				finish();
			}
		};
		
		JSONObject jsonData = TNUtils.makeJSON(
				"CONTEXT", this,
				"TITLE", R.string.alert_Title,
				"MESSAGE", R.string.alert_CatInfo_SetDefaultMsg,
				"POS_BTN", R.string.alert_OK,
				"POS_BTN_CLICK", pbtn_Click,
				"NEG_BTN", R.string.alert_Cancel
				);
		TNUtilsUi.alertDialogBuilder(jsonData).show();
	}
}