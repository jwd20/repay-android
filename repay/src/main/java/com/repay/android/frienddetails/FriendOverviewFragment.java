package com.repay.android.frienddetails;

import java.math.BigDecimal;

import com.repay.android.ContactLookup;
import com.repay.android.Friend;
import com.repay.android.R;
import com.repay.android.RetrieveContactPhoto;
import com.repay.android.RoundedImageView;
import com.repay.android.settings.SettingsFragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Property of Matt Allen
 * mattallen092@gmail.com
 * http://mattallensoftware.co.uk/
 *
 * This software is distributed under the Apache v2.0 license and use
 * of the Repay name may not be used without explicit permission from the project owner.
 *
 */

public class FriendOverviewFragment extends Fragment implements OnClickListener {

	private static final String TAG = FriendOverviewFragment.class.getName();

	private RoundedImageView 	mFriendPic, mFriendPicBg;
	private TextView 			mTotalOwed, mTotalOwedPrefix;
	private Friend 				friend;
	private int 				mTheyOweMeColour, mIOweThemColour;
	private Button 				mShareBtn;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_frienddetails, container, false);
		return view;
	}

	@Override
	public void onStart(){
		super.onStart();
		mShareBtn = (Button)getView().findViewById(R.id.fragment_frienddetails_share);
		mFriendPic = (RoundedImageView)getView().findViewById(R.id.fragment_frienddetails_headerPic);
		mFriendPicBg = (RoundedImageView)getView().findViewById(R.id.fragment_frienddetails_headerPicBG);
		mTotalOwed = (TextView)getView().findViewById(R.id.fragment_frienddetails_debttotal);
		mTotalOwedPrefix = (TextView)getView().findViewById(R.id.fragment_frienddetails_debttotal_prefix);

		mShareBtn.setOnClickListener(this);

		if(SettingsFragment.getDebtHistoryColourPreference(getActivity())==SettingsFragment.DEBTHISTORY_GREEN_RED){
			mTheyOweMeColour = R.drawable.debt_ind_green;
			mIOweThemColour = R.drawable.debt_ind_red;
		} else {
			mTheyOweMeColour = R.drawable.debt_ind_green;
			mIOweThemColour = R.drawable.debt_ind_blue;
		}
	}

	@Override
	public void onResume(){
		super.onResume();
		friend = ((FriendDetailsActivity)getActivity()).getFriend();
		showFriend();
		if (friend.getLookupURI()!=null) {
			Log.i(TAG, "Now looking for contact information in database");
			if (!ContactLookup.hasContactData(getActivity(), friend
					.getLookupURI().getLastPathSegment())) {
				mShareBtn.setEnabled(false);
				Log.i(TAG,
						"No contact information found - disabling share button");
			}
		} else {
			mShareBtn.setEnabled(false);
		}
	}

	private void showFriend(){
		// Get image
		new RetrieveContactPhoto(friend.getLookupURI(), mFriendPic, getActivity(), R.drawable.friend_image_light).execute();
		if(friend.getDebt().compareTo(BigDecimal.ZERO)==0){
			mTotalOwedPrefix.setText(""); // Set it null
			mTotalOwed.setText(SettingsFragment.getCurrencySymbol(getActivity())+"0");
			mFriendPicBg.setImageResource(mTheyOweMeColour);
		} else if (friend.getDebt().compareTo(BigDecimal.ZERO)<0){
			mTotalOwedPrefix.setText(R.string.fragment_friendoverview_prefix_iowe);
			String amount = SettingsFragment.getFormattedAmount(friend.getDebt().negate());
			mTotalOwed.setText(SettingsFragment.getCurrencySymbol(getActivity())+amount);
			mFriendPicBg.setImageResource(mIOweThemColour);
		} else if(friend.getDebt().compareTo(BigDecimal.ZERO)>0){
			mTotalOwedPrefix.setText(R.string.fragment_friendoverview_prefix_theyowe);
			String amount = SettingsFragment.getFormattedAmount(friend.getDebt());
			mTotalOwed.setText(SettingsFragment.getCurrencySymbol(getActivity())+amount);
			mFriendPicBg.setImageResource(mTheyOweMeColour);
		}
	}

	public static final FriendOverviewFragment newInstance(String message){
		FriendOverviewFragment f = new FriendOverviewFragment();
		Bundle bd1 = new Bundle(1);
		bd1.putString(TAG, message);
		f.setArguments(bd1);
		return f;
	}

	public void updateFriendInfo(){
		friend = ((FriendDetailsActivity) getActivity()).getFriend();
		showFriend();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.fragment_frienddetails_share:
			if(friend.getDebt().compareTo(BigDecimal.ZERO)!=0){
				AlertDialog.Builder shareDialog = new ShareDialog(getActivity(), friend);
				shareDialog.show();
			} else {
				Toast.makeText(getActivity(), "There's no debt between you?", Toast.LENGTH_SHORT).show();
			}
			break;
		}
	}
}
