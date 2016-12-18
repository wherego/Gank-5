package com.bboylin.gank.UI.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bboylin.gank.Data.CategoryPref;
import com.bboylin.gank.Data.Gank;
import com.bboylin.gank.Net.CategoryRepository;
import com.bboylin.gank.R;
import com.bboylin.gank.UI.Adapter.GirlAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.nfc.tech.MifareUltralight.PAGE_SIZE;

/**
 * Created by lin on 2016/12/16.
 */

public class GirlFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    private GirlAdapter mGirlAdapter;
    private static final GirlFragment instance=new GirlFragment();
    private CategoryRepository mCategoryRepository;
    private CategoryPref mGirlPref;
    private Handler mHandler;
    private static final int REFRESH_COMPLETE = 0x123;
    private int page=1;
    private int mCurrentCounter=0;
    private List<String> mList=new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_recyclerview,container,false);
        ButterKnife.bind(this,view);
        setupToolBar("福利");
        mRecyclerView.setPadding(0,0,0,0);
        mCategoryRepository =CategoryRepository.getInstance(getActivity());
        mGirlPref=CategoryPref.Factory.create(getActivity());
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccent);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,RecyclerView.VERTICAL));
        mGirlAdapter=new GirlAdapter(getActivity(),R.layout.girl_item,getUrlList(mGirlPref.getGirlList()));
        mGirlAdapter.openLoadAnimation();
        mGirlAdapter.openLoadMore(PAGE_SIZE, true);
        mGirlAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                page++;
                mCategoryRepository.getGirlDataFromNet(10,page,false)
                        .subscribe(strings -> mList.addAll(strings),
                                throwable -> Logger.e(throwable,"error in load more"),
                                () -> {
                                    mRecyclerView.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (mCurrentCounter >= 1000) {
                                                mGirlAdapter.notifyDataChangedAfterLoadMore(false);
                                            } else {
                                                mGirlAdapter.notifyDataChangedAfterLoadMore(mList, true);
                                                mCurrentCounter = mGirlAdapter.getItemCount();
                                            }
                                        }

                                    });
                                });
            }
        });
        mRecyclerView.setAdapter(mGirlAdapter);
        mHandler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what==0x123){
                    mSwipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(), networkConnected() ? "刷新成功" : "网络无连接", Toast.LENGTH_SHORT).show();
                }
            }
        };
        if (mList==null){
            refresh();
        }
        return view;
    }

    private List<String> getUrlList(List<Gank> girlList) {
        if (girlList==null){
            return mList;
        }
        for (Gank gank : girlList){
            mList.add(gank.url);
        }
        return mList;
    }

    @Override
    public void onRefresh() {
        refresh();
        mHandler.sendEmptyMessageDelayed(REFRESH_COMPLETE, 2000);
    }

    private void refresh() {
        mCategoryRepository.getGirlDataFromNet(10,1,true)
                .subscribe(strings -> System.out.println(""),
                        throwable -> Logger.e(throwable,"error in girl refresh"),
                        () -> {
                            mList=new ArrayList<String>();
                            mGirlAdapter.setNewData(getUrlList(mGirlPref.getGirlList()));
                        });
    }

    public static GirlFragment getInstance() {
        return instance;
    }

}