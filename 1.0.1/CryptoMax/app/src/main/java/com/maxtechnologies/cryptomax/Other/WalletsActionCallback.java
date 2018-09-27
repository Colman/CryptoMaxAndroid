package com.maxtechnologies.cryptomax.Other;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.maxtechnologies.cryptomax.Main.MainActivity;
import com.maxtechnologies.cryptomax.Main.MainFragments.WalletListFragment;
import com.maxtechnologies.cryptomax.R;

/**
 * Created by Colman on 22/02/2018.
 */

public class WalletsActionCallback implements ActionMode.Callback {

    //Context declarations
    private AppCompatActivity activity;
    private WalletListFragment fragment;


    public WalletsActionCallback(AppCompatActivity activity, WalletListFragment fragment) {
        this.activity = activity;
        this.fragment = fragment;
    }



    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.menu_edit_list, menu);
        return true;
    }



    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return true;
    }



    @Override
    public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.trash_button:
                int[] selected = new int[fragment.adapter.selectedEntries.size()];
                for(int i = 0; i < selected.length; i++) {
                    selected[i] = fragment.adapter.selectedEntries.get(i);
                }
                CryptoMaxApi.removeWallets(selected, activity);
                fragment.setOverlayVisibility();
                ((MainActivity) activity).subWalletTickers();
                ((MainActivity) activity).setAssets();
                mode.finish();
                return true;
        }

        return false;
    }



    @Override
    public void onDestroyActionMode(ActionMode mode) {
        fragment.adapter.editMode = false;
        fragment.adapter.selectedEntries = null;
        fragment.actionMode = null;
        fragment.adapter.notifyDataSetChanged();
    }
}
