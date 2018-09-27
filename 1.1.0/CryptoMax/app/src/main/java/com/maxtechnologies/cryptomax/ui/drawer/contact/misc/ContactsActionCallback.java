package com.maxtechnologies.cryptomax.ui.drawer.contact.misc;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.maxtechnologies.cryptomax.R;
import com.maxtechnologies.cryptomax.misc.StaticVariables;
import com.maxtechnologies.cryptomax.ui.drawer.contact.ContactsFragment;

/**
 * Created by Colman on 07/05/2018.
 */

public class ContactsActionCallback implements ActionMode.Callback {
    private AppCompatActivity activity;
    private ContactsFragment fragment;


    public ContactsActionCallback(AppCompatActivity activity, ContactsFragment fragment) {
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
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.trash_button:
                int[] selected = new int[fragment.adapter.selectedEntries.size()];
                for(int i = 0; i < selected.length; i++) {
                    selected[i] = fragment.adapter.selectedEntries.get(i);
                }
                StaticVariables.removeContacts(selected, activity);
                fragment.setOverlayVisibility();
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
