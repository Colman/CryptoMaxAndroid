package com.maxtechnologies.cryptomax.ui.drawer.news;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.maxtechnologies.cryptomax.api.ArticlesCallback;
import com.maxtechnologies.cryptomax.exchange.asset.Asset;
import com.maxtechnologies.cryptomax.api.CryptoMaxApi;
import com.maxtechnologies.cryptomax.misc.Settings;
import com.maxtechnologies.cryptomax.misc.StaticVariables;
import com.maxtechnologies.cryptomax.ui.misc.CustomSpinnerAdapter;
import com.maxtechnologies.cryptomax.ui.misc.AlertController;
import com.maxtechnologies.cryptomax.api.Article;
import com.maxtechnologies.cryptomax.R;

import java.util.ArrayList;

/**
 * Created by Colman on 02/04/2018.
 */

public class NewsFragment extends Fragment {

    //Asset declaration
    private Asset coin;

    //Loading declarations
    private boolean isLoading;
    private int numSpinners;
    private boolean loadArticles;

    //UI declarations
    private LinearLayout spinnerLayout;
    private Spinner sourceSpinner;
    private ArrayAdapter<String> sourceAdapter;
    private Spinner sortSpinner;
    private ArrayAdapter<String> sortAdapter;
    private SwipeRefreshLayout newsRefresh;
    private RecyclerView articleList;
    private ArticleAdapter adapter;
    private LinearLayoutManager manager;
    private ProgressBar progress;


    public static NewsFragment newInstance(Asset coin) {
        NewsFragment fragment = new NewsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("COIN", coin);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_articles, container, false);

        Bundle bundle = getArguments();
        if (bundle == null) {
            getActivity().setTitle(R.string.news);
        }
        else {
            coin = (Asset) bundle.getSerializable("COIN");
            getActivity().setTitle(coin.name + " " + getString(R.string.news));
        }


        loadArticles = false;
        if(StaticVariables.articles == null) {
            loadArticles = true;
            StaticVariables.articles = new ArrayList<>();
        }


        //Loading declaration
        isLoading = false;
        numSpinners = 0;

        //UI definitions
        spinnerLayout = (LinearLayout) rootView.findViewById(R.id.spinner_layout);
        if (Settings.theme == 0) {
            spinnerLayout.setBackgroundResource(android.R.color.white);
        }
        else {
            spinnerLayout.setBackgroundResource(R.color.colorDraculaPrimaryDark);
        }
        sourceSpinner = (Spinner) rootView.findViewById(R.id.source_spinner);
        ArrayList<String> sourceNames = new ArrayList<>();
        sourceNames.add("Asset Desk");
        sourceAdapter = new CustomSpinnerAdapter(getActivity(), sourceNames);
        sourceSpinner.setAdapter(sourceAdapter);

        sortSpinner = (Spinner) rootView.findViewById(R.id.sort_spinner);
        ArrayList<String> sortTypes = new ArrayList<>();
        sortTypes.add("Most recent");
        sortTypes.add("Most viewed");
        sortTypes.add("Most up votes");
        sortAdapter = new CustomSpinnerAdapter(getActivity(), sortTypes);
        sortSpinner.setAdapter(sortAdapter);
        sortSpinner.setSelection(Settings.sortType);

        newsRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.news_refresh);
        articleList = (RecyclerView) rootView.findViewById(R.id.articles);
        articleList.setHasFixedSize(false);
        adapter = new ArticleAdapter(getActivity(), StaticVariables.articles);
        articleList.setAdapter(adapter);
        manager = new LinearLayoutManager(getActivity());
        articleList.setLayoutManager(manager);
        articleList.setItemAnimator(new DefaultItemAnimator());
        progress = (ProgressBar) rootView.findViewById(R.id.progress);


        //Start the network call for the articles
        if(loadArticles) {
            int selectedSort = sortSpinner.getSelectedItemPosition();
            int sourceNum = sourceSpinner.getSelectedItemPosition();
            String selectedSource = sourceAdapter.getItem(sourceNum).replace(" ", "");
            getArticles(0, 8, selectedSource, selectedSort, true);
        }

        else {
            progress.setVisibility(View.INVISIBLE);
        }


        //Setup the listener for the source spinner
        sourceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(numSpinners >= 2) {
                    int selectedSort = sortSpinner.getSelectedItemPosition();
                    String selectedSource = sourceAdapter.getItem(i).replace(" ", "");
                    getArticles(0, 8, selectedSource, selectedSort, true);
                }
                else {
                    numSpinners++;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //Do nothing
            }
        });


        //Setup the listener for the sort spinner
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(numSpinners >= 2) {
                    StaticVariables.articles.clear();
                    adapter.notifyDataSetChanged();
                    progress.setVisibility(View.VISIBLE);

                    String selectedSource = sourceAdapter.getItem(sourceSpinner.getSelectedItemPosition());
                    selectedSource = selectedSource.replace(" ", "");
                    getArticles(0, 8, selectedSource, i, true);
                }
                else {
                    numSpinners++;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //Do nothing
            }
        });


        //Setup the listener for the list refresher
        newsRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String selectedSource = sourceAdapter.getItem(sourceSpinner.getSelectedItemPosition());
                selectedSource = selectedSource.replace(" ", "");
                int selectedSort = sortSpinner.getSelectedItemPosition();
                getArticles(0, 8, selectedSource, selectedSort, true);
            }
        });


        //Setup the on scroll listener for the recycler view
        articleList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int itemCount = manager.getItemCount();
                int lastVisibleItem = manager.findLastVisibleItemPosition();
                String selectedSource = sourceAdapter.getItem(sourceSpinner.getSelectedItemPosition());
                selectedSource = selectedSource.replace(" ", "");
                int selectedSort = sortSpinner.getSelectedItemPosition();
                if (!isLoading && lastVisibleItem == itemCount - 1) {
                    getArticles(itemCount, itemCount + 8, selectedSource, selectedSort, false);
                }
            }
        });


        return rootView;
    }



    private void getArticles(int start, int end, String source, int sort, final boolean overWrite) {
        isLoading = true;
        String topic = "";
        if(coin != null) {
            topic = coin.symbol.toUpperCase();
        }
        CryptoMaxApi.getArticles(start, end, source, sort, topic, new ArticlesCallback() {
            @Override
            public void onFailure(String reason) {
                Log.e("Network", "Failed to get articles for reason: " + reason);
                AlertController.networkError(getActivity(), true);
            }

            @Override
            public void onSuccess(ArrayList<Article> articles) {
                if(overWrite) {
                    StaticVariables.articles.clear();
                    StaticVariables.articles.addAll(articles);
                }

                else {
                    StaticVariables.articles.addAll(articles);
                }

                isLoading = false;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        newsRefresh.setRefreshing(false);
                        progress.setVisibility(View.INVISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }
}
