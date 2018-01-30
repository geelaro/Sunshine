package com.geelaro.sunboard.movies;

import android.support.v7.widget.RecyclerView;

import com.geelaro.sunboard.base.BaseListFragment;
import com.geelaro.sunboard.base.beans.MoviesBean;
import com.geelaro.sunboard.movies.presenter.MoviePresenter;
import com.geelaro.sunboard.movies.presenter.MoviePresenterImpl;
import com.geelaro.sunboard.movies.view.MovieView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by geelaro on 2018/1/10.
 */

public class MoviesFragment extends BaseListFragment implements MovieView {
    private static final String TAG = MoviesFragment.class.getSimpleName();
    private MoviePresenter mMoviePresenter = new MoviePresenterImpl(this);
    ;
    private List<MoviesBean> mData;
    private MoviesAdapter mMoviesAdapter;
    private static int start = 0;


    @Override
    public RecyclerView.Adapter bindAdapter() {
        mMoviesAdapter = new MoviesAdapter(getActivity());
        return mMoviesAdapter;
    }

    @Override
    protected void loadFromNet() {
        int start = 0;
        mMoviePresenter.loadData(start);
    }

    @Override
    protected void loadingMoreData() {
        mMoviePresenter.loadData(start);
    }


    @Override
    public void addData(List<MoviesBean> list) {
        if (mData == null) {
            mData = new ArrayList<>();
        }
        mData.clear();
        mData.addAll(list);
        if (start == 0) {
            mMoviesAdapter.setData(mData);
        } else mMoviesAdapter.notifyDataSetChanged();

//        start += SunApi.MOVIE_SUB_COUNT;

    }

    @Override
    public void showProgress() {
        mRefreshLayout.setRefreshing(true);
    }

    @Override
    public void hideProgress() {
        mRefreshLayout.setRefreshing(false);
    }

}