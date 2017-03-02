package com.example.pau.busyalert;


import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


public class FavouritesFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemLongClickListener{
    private SimpleCursorAdapter mAdapter;
    private static final int DELETE_ID = Menu.FIRST + 1;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText(getResources().getString(R.string.no_numbers));

        mAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.contact_list, null, new String[]{
                Contacts.DISPLAY_NAME, Contacts.CONTACT_STATUS},
                new int[]{R.id.name, R.id.status}, 0);
        setListAdapter(mAdapter);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        registerForContextMenu(root);
        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        getListView().setOnItemLongClickListener(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {Contacts._ID, Contacts.DISPLAY_NAME,
                Contacts.CONTACT_STATUS};
        return new CursorLoader(getActivity(), Contacts.CONTENT_URI,
                projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.opt_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        /*if(item.getItemId() == DELETE_ID){
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            Uri uri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, Long.toString(info.id));
            getActivity().getContentResolver().delete(uri, null, null);
        }*/
        Toast.makeText(getContext(),R.string.toast_deleted, Toast.LENGTH_LONG).show();
        return super.onContextItemSelected(item);
    }
}
