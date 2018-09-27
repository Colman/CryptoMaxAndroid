package com.maxtechnologies.cryptomax.ui.drawer.contact.misc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.maxtechnologies.cryptomax.api.ProfilesCallback;
import com.maxtechnologies.cryptomax.exchange.Exchange;
import com.maxtechnologies.cryptomax.api.CryptoMaxApi;
import com.maxtechnologies.cryptomax.misc.Settings;
import com.maxtechnologies.cryptomax.misc.StaticVariables;
import com.maxtechnologies.cryptomax.ui.misc.AlertController;
import com.maxtechnologies.cryptomax.R;
import com.maxtechnologies.cryptomax.ui.drawer.wallet.send.step2.SendFragment2;
import com.maxtechnologies.cryptomax.wallets.Wallet;

import java.util.ArrayList;

/**
 * Created by Colman on 09/12/2017.
 */

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
    private FragmentActivity activity;
    private int index;
    public int numToPop;
    public boolean editMode;
    public ArrayList<Integer> selectedEntries;


    public ContactAdapter(FragmentActivity activity, int index, int numToPop) {
        this.activity = activity;
        this.index = index;
        this.numToPop = numToPop;
        this.editMode = false;
    }



    @Override
    public ContactAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contactView = inflater.inflate(R.layout.contact_entry, parent, false);


        return new ContactAdapter.ViewHolder(contactView, this);
    }



    @Override
    public void onBindViewHolder(ContactAdapter.ViewHolder holder, int position) {
        Contact contact = StaticVariables.getContact(position);
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup itemGroup = (ViewGroup) holder.itemView;

        if(itemGroup.getChildCount() == 2) {
            itemGroup.removeViewAt(0);
        }

        if(editMode) {
            holder.itemView.setLayoutParams(new LinearLayout.LayoutParams(holder.itemView.getWidth(), holder.itemView.getHeight()));

            View editView = inflater.inflate(R.layout.contact_entry_edit, itemGroup, false);
            itemGroup.addView(editView, 0);
        }

        else {
            View editView = inflater.inflate(R.layout.contact_entry_image, itemGroup, false);
            itemGroup.addView(editView, 0);

            //Define the profile elements
            holder.imageBorder = (ImageView) itemGroup.findViewById(R.id.image_border);
            holder.profileImage = (ImageView) itemGroup.findViewById(R.id.profile_image);
            holder.name = (TextView) itemGroup.findViewById(R.id.name);

            Drawable drawable;
            if(Settings.theme == 0) {
                drawable = activity.getResources().getDrawable(activity.getResources().getIdentifier(
                        "image_border_black", "drawable", activity.getPackageName()));
                holder.address.setTextColor(Color.parseColor("#99000000"));
            }

            else {
                drawable = activity.getResources().getDrawable(activity.getResources().getIdentifier(
                        "image_border_white", "drawable", activity.getPackageName()));
                holder.address.setTextColor(Color.parseColor("#99FFFFFF"));
            }
            holder.imageBorder.setImageDrawable(drawable);

            getProfiles(holder, contact.address);
        }


        //Set the name of the wallet
        holder.memo.setText(contact.memo);

        //Set the address of the wallet
        holder.address.setText(contact.address);
    }

    @Override
    public int getItemCount() {
        return StaticVariables.getContactsSize();
    }



    private void getProfiles(final ContactAdapter.ViewHolder holder, String addressStr) {
        ArrayList<String> addressList = new ArrayList<>();
        addressList.add(addressStr);
        CryptoMaxApi.getProfiles(addressList, new ProfilesCallback() {
            @Override
            public void onFailure(String reason) {
                Log.e("Network", "Failed to get profiles for reason: " + reason);
                AlertController.networkError(activity, true);
            }

            @Override
            public void onSuccess(final String[] names, final Bitmap[] images) {
                if(names.length == 1 && images.length == 1) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(!names[0].equals("")) {
                                holder.name.setText(names[0]);
                                holder.name.setTypeface(null, Typeface.NORMAL);
                            }

                            if(images[0] != null) {
                                holder.profileImage.setImageBitmap(images[0]);
                            }
                        }
                    });
                }
            }
        });
    }



    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public View itemView;
        public ImageView imageBorder;
        public ImageView profileImage;
        public TextView name;
        public TextView memo;
        public TextView address;
        private ContactAdapter adapter;

        public ViewHolder(View itemView, ContactAdapter adapter) {
            super(itemView);
            this.itemView = itemView;
            memo = (TextView) itemView.findViewById(R.id.memo);
            address = (TextView) itemView.findViewById(R.id.address);
            this.adapter = adapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(adapter.editMode) {
                RadioButton selectedButton = (RadioButton) v.findViewById(R.id.selected_button);
                int pos = getAdapterPosition();
                if(selectedButton.isChecked()) {
                    int index = adapter.selectedEntries.indexOf(pos);
                    adapter.selectedEntries.remove(index);
                    selectedButton.setChecked(false);
                }
                else {
                    adapter.selectedEntries.add(pos);
                    selectedButton.setChecked(true);
                }
            }
            else {
                FragmentManager fragmentManager = adapter.activity.getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                String addressStr = StaticVariables.getContact(getAdapterPosition()).address;
                Wallet wallet = CryptoMaxApi.getWallet(adapter.index);
                if(wallet.isValidAddress(addressStr)) {
                    SendFragment2 fragment = SendFragment2.newInstance(addressStr, adapter.numToPop + 1);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.replace(R.id.content, fragment);
                    fragmentTransaction.commit();
                }

                else {
                    String name = Exchange.translateToName(wallet.exchangeSymbol);
                    String toastStr = adapter.activity.getResources().getString(R.string.wrong_contact, name);
                    Toast newToast = Toast.makeText(adapter.activity,
                            toastStr,
                            Toast.LENGTH_LONG);
                    newToast.show();
                }
            }
        }
    }
}

