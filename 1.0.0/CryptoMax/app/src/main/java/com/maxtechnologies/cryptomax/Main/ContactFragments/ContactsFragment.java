package com.maxtechnologies.cryptomax.Main.ContactFragments;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.maxtechnologies.cryptomax.Adapters.ContactAdapter;
import com.maxtechnologies.cryptomax.Other.Settings;
import com.maxtechnologies.cryptomax.Other.ContactsActionCallback;
import com.maxtechnologies.cryptomax.Other.StaticVariables;
import com.maxtechnologies.cryptomax.R;

import java.util.ArrayList;

public class ContactsFragment extends Fragment {

    //UI declarations
    public ActionMode actionMode;
    private ConstraintLayout noContactsLayout;
    private TextView noContactsTitle;
    private ImageView noContactsArrow;
    private RecyclerView contactList;
    private FloatingActionButton addButton;
    public ContactAdapter adapter;
    private LinearLayoutManager manager;


    public static ContactsFragment newInstance(int numToPop) {
        ContactsFragment fragment = new ContactsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("NUMTOPOP", numToPop);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_contacts, container, false);
        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.contacts);


        //UI definitions
        noContactsLayout = (ConstraintLayout) rootView.findViewById(R.id.no_contacts_layout);
        noContactsTitle = (TextView) rootView.findViewById(R.id.no_contacts_title);
        noContactsArrow = (ImageView) rootView.findViewById(R.id.no_contacts_arrow);
        contactList = (RecyclerView) rootView.findViewById(R.id.contacts);
        contactList.setHasFixedSize(false);
        int numToPop = 0;
        Bundle bundle = getArguments();
        if(bundle != null) {
            numToPop = bundle.getInt("NUMTOPOP");
        }
        adapter = new ContactAdapter(getActivity(), numToPop);
        contactList.setAdapter(adapter);
        manager = new LinearLayoutManager(getActivity());
        contactList.setLayoutManager(manager);
        DividerItemDecoration decoration;
        if(Settings.theme == 0) {
            decoration = new DividerItemDecoration(
                    contactList.getContext(),
                    manager.getOrientation()
            );

            noContactsTitle.setTextColor(Color.parseColor("#444444"));
            noContactsArrow.setColorFilter(Color.parseColor("#444444"));
        }
        else {
            decoration = new DividerItemDecoration(
                    contactList.getContext(),
                    manager.getOrientation()
            );
            Drawable drawable = getResources().getDrawable(getResources().getIdentifier(
                    "dracula_divider", "drawable", getActivity().getPackageName()));
            decoration.setDrawable(drawable);

            noContactsTitle.setTextColor(Color.parseColor("#99999D"));
            noContactsArrow.setColorFilter(Color.parseColor("#99999D"));
        }
        contactList.addItemDecoration(decoration);
        addButton = (FloatingActionButton) rootView.findViewById(R.id.add_button);


        //Set the UI visibility
        setOverlayVisibility();


        //Setup the add button listener
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                AddContactFragment1 fragment = AddContactFragment1.newInstance();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.content, fragment);
                fragmentTransaction.commit();
            }
        });


        return rootView;
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.edit_button:
                AppCompatActivity activity = (AppCompatActivity) getActivity();
                ContactsActionCallback callback = new ContactsActionCallback(activity, this);
                actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(callback);
                adapter.editMode = true;
                adapter.selectedEntries = new ArrayList<>();
                adapter.notifyDataSetChanged();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }



    public void setOverlayVisibility() {
        if(StaticVariables.getContactsSize() == 0) {
            noContactsLayout.setVisibility(View.VISIBLE);
            contactList.setVisibility(View.INVISIBLE);
        }

        else {
            noContactsLayout.setVisibility(View.INVISIBLE);
            contactList.setVisibility(View.VISIBLE);
        }
    }
}
