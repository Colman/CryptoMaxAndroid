package com.maxtechnologies.cryptomax.ui.drawer.contact;

import android.content.Context;
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

import com.maxtechnologies.cryptomax.R;
import com.maxtechnologies.cryptomax.misc.Settings;
import com.maxtechnologies.cryptomax.misc.StaticVariables;
import com.maxtechnologies.cryptomax.ui.drawer.contact.misc.Contact;
import com.maxtechnologies.cryptomax.ui.drawer.contact.misc.ContactAdapter;
import com.maxtechnologies.cryptomax.ui.drawer.contact.misc.ContactsActionCallback;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class ContactsFragment extends Fragment {

    //File name declarations
    private final static String CONTACTSFILENAME = "contacts.sav";


    //UI declarations
    public ActionMode actionMode;
    private ConstraintLayout noContactsLayout;
    private TextView noContactsTitle;
    private ImageView noContactsArrow;
    private RecyclerView contactList;
    private FloatingActionButton addButton;
    public ContactAdapter adapter;
    private LinearLayoutManager manager;


    public static ContactsFragment newInstance(int index, int numToPop) {
        ContactsFragment fragment = new ContactsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("INDEX", index);
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
        int index = -1;
        int numToPop = 0;
        Bundle bundle = getArguments();
        if(bundle != null) {
            index = bundle.getInt("INDEX");
            numToPop = bundle.getInt("NUMTOPOP");
        }
        adapter = new ContactAdapter(getActivity(), index, numToPop);
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



    private static void loadContacts(Context context) {
        try {
            FileInputStream fileStream = context.openFileInput(CONTACTSFILENAME);
            ObjectInputStream objStream = new ObjectInputStream(fileStream);
            StaticVariables.contacts = (ArrayList<Contact>) objStream.readObject();
            fileStream.close();
            objStream.close();
        }

        catch(java.io.IOException | java.lang.ClassNotFoundException e) {
            StaticVariables.contacts = new ArrayList<>();
        }
    }



    private static void saveContacts(Context context) {
        context.deleteFile(CONTACTSFILENAME);
        try {
            FileOutputStream fileStream = context.openFileOutput(CONTACTSFILENAME, 0);
            ObjectOutputStream objStream = new ObjectOutputStream(fileStream);
            objStream.writeObject(StaticVariables.contacts);
            objStream.close();
            fileStream.close();
        }

        catch(java.io.IOException e) {
            context.deleteFile(CONTACTSFILENAME);
        }
    }



    public static Contact getContact(int index) {
        return contacts.get(index);
    }



    public static int getContactsSize() {
        return contacts.size();
    }



    public static void addContact(Contact contact, Context context) {
        contacts.add(contact);
        saveContacts(context);
    }



    public static void removeContacts(int[] indices, Context context) {
        ArrayList<Contact> newContacts = new ArrayList<>();
        for(int i = 0; i < contacts.size(); i++) {
            boolean in = false;
            for(int j : indices) {
                if(j == i) {
                    in = true;
                    break;
                }
            }
            if(!in) {
                newContacts.add(contacts.get(i));
            }
        }
        contacts = newContacts;

        saveContacts(context);
    }
}
