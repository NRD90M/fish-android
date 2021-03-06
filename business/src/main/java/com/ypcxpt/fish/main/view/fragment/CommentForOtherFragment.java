package com.ypcxpt.fish.main.view.fragment;

import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;

import com.ypcxpt.fish.R;
import com.ypcxpt.fish.library.view.fragment.BaseFragment;
import com.ypcxpt.fish.main.adapter.CommentWithMeAdapter;
import com.ypcxpt.fish.main.contract.CommentWithMeContract;
import com.ypcxpt.fish.main.event.OnGetCommentsWithmeEvent;
import com.ypcxpt.fish.main.model.CommentInfo;
import com.ypcxpt.fish.main.presenter.CommentWithMePresenter;

import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;

public class CommentForOtherFragment extends BaseFragment implements CommentWithMeContract.View {
    @BindView(R.id.rv) RecyclerView rv;
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout swipe_refresh_layout;

    private CommentWithMeContract.Presenter mPresenter;
    private CommentWithMeAdapter mAdapter;

    //收藏数据源
    private CommentInfo commentRes;
    private int pageIndex = 1;
    private int pageNum = 10;

    public static boolean isRefresh = true;//菊花加载默认显示

    @Override
    protected int layoutResID() {
        return R.layout.fragment_comment_other;
    }

    @Override
    protected void initData() {
        mPresenter = new CommentWithMePresenter();
        addPresenter(mPresenter);
    }

    @Override
    protected void initViews() {
        initRv();
    }

    private void initRv() {
        mAdapter = new CommentWithMeAdapter(R.layout.item_comment_other, mPresenter);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter.bindToRecyclerView(rv);
        mAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                //加载更多的请求
                swipe_refresh_layout.setEnabled(false);
                rv.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadMore();
                    }
                },1000);
            }
        }, rv);
//        mAdapter.openLoadAnimation(BaseQuickAdapter.SCALEIN);
        mAdapter.setEmptyView(R.layout.include_d_m_nomsg);
        rv.setAdapter(mAdapter);

        mPresenter.acceptData("mAdapter", mAdapter);

        swipe_refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {//设置刷新监听器
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {//模拟耗时操作
                    @Override
                    public void run() {
                        isRefresh = false;
                        pageIndex = 1;
                        mAdapter.setEnableLoadMore(false);//这里的作用是防止下拉刷新的时候还可以上拉加载

                        mPresenter.getCommentWithMeList(pageIndex + "", pageNum + "");
                        mAdapter.setEnableLoadMore(true);
                        swipe_refresh_layout.setRefreshing(false);
                    }
                }, 1500);
            }
        });
    }

    private void loadMore() {
        if (commentRes.getTotalCount() > pageNum * pageIndex) {
            pageIndex++;
            mPresenter.getCommentWithMeList(pageIndex + "", pageNum + "");
            mAdapter.loadMoreComplete();
            mAdapter.loadMoreEnd(false);
        } else {
            //第一页如果不够一页就不显示没有更多数据布局
            mAdapter.loadMoreEnd(true);
            swipe_refresh_layout.setEnabled(true);
        }
    }

    @Subscribe
    public void onEventReceived(OnGetCommentsWithmeEvent event) {
        //收到通知调用接口获取设备列表
        pageIndex = 1;
        mPresenter.getCommentWithMeList(pageIndex + "", pageNum + "");
    }

    @Override
    public void showCommentWithMeList(CommentInfo commentInfo) {
        commentRes = commentInfo;
        if (pageIndex == 1) {
            mAdapter.setNewData(commentInfo.getRows());
            mAdapter.disableLoadMoreIfNotFullPage(rv);
        } else {
            mAdapter.addData(commentRes.getRows());
        }
    }
}
