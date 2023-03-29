package com.zhaoxiao.zhiying.fragment.study;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.zhaoxiao.zhiying.R;
import com.zhaoxiao.zhiying.activity.study.ChannelActivity;
import com.zhaoxiao.zhiying.adapter.ChannelAdapter;
import com.zhaoxiao.zhiying.adapter.HotAdapter;
import com.zhaoxiao.zhiying.api.ApiConfig;
import com.zhaoxiao.zhiying.api.StudyService;
import com.zhaoxiao.zhiying.entity.Channel;
import com.zhaoxiao.zhiying.entity.Data;
import com.zhaoxiao.zhiying.entity.Hot;
import com.zhaoxiao.zhiying.entity.PageInfo;
import com.zhaoxiao.zhiying.fragment.BaseFragment;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import butterknife.BindView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SortFragment extends BaseFragment {

    @BindView(R.id.rv)
    RecyclerView rv;
    @BindView(R.id.srl)
    SmartRefreshLayout srl;

    private int ftypeId;
    private int stypeId;

    private int pageNum = 1;

    private Retrofit retrofit;
    private StudyService studyService;

    private List<Channel> hotList;
    private ChannelAdapter channelAdapter;
    private LinearLayoutManager linearLayoutManager;

    public SortFragment() {
    }

    public static SortFragment newInstance(int ftypeId, int stypeId) {
        SortFragment fragment = new SortFragment();
        fragment.ftypeId = ftypeId;
        fragment.stypeId = stypeId;
        return fragment;
    }

    @Override
    protected int initLayout() {
        return R.layout.fragment_sort;
    }

    @Override
    protected void initData() {
        //刷新和加载
        srl.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull @NotNull RefreshLayout refreshLayout) {
                pageNum=1;
                getChannelList(1);
            }
        });
        srl.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull @NotNull RefreshLayout refreshLayout) {
                pageNum++;
                getChannelList(2);
            }
        });

//        retrofit = new Retrofit.Builder()
//                .baseUrl(ApiConfig.BASE_URl)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//        studyService = retrofit.create(StudyService.class);
        studyService = (StudyService) getService(StudyService.class);

        getChannelList(0);
        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(linearLayoutManager);
        channelAdapter = new ChannelAdapter(getContext());
        rv.setAdapter(channelAdapter);
        channelAdapter.setOnItemClickListener(channelId -> navigateTo(ChannelActivity.class,"channelId",channelId));
    }

    private void getChannelList(int type) {
        Call<Data<PageInfo<Channel>>> channelCall = studyService.getChannelList(pageNum, 8,ftypeId,stypeId);
        channelCall.enqueue(new Callback<Data<PageInfo<Channel>>>() {
            @Override
            public void onResponse(Call<Data<PageInfo<Channel>>> call, Response<Data<PageInfo<Channel>>> response) {
                if (response.body() != null && response.body().getCode() == 10000) {
                    List<Channel> list = response.body().getData().getList();
                    switch (type) {
                        case 0:
                            hotList = list;
                            channelAdapter.setList(hotList);
                            channelAdapter.notifyDataSetChanged();
                            pageNum = 1;
                            break;
                        case 1:
                            hotList = list;
                            channelAdapter.setList(hotList);
                            channelAdapter.notifyDataSetChanged();
                            srl.finishRefresh();
                            pageNum = 1;
                            break;
                        case 2:
                            if (pageNum > response.body().getData().getPages()) {
                                pageNum = response.body().getData().getPageNum();
                                srl.finishLoadMore();
                                showToast("没有更多数据了");
                                break;
                            }
                            hotList.addAll(list);
                            channelAdapter.setList(hotList);
                            channelAdapter.notifyDataSetChanged();
                            srl.finishLoadMore();
                            break;
                    }
                }else System.out.println("请求失败");
            }

            @Override
            public void onFailure(Call<Data<PageInfo<Channel>>> call, Throwable t) {
                System.out.println("请求未完成");
            }
        });
    }
}