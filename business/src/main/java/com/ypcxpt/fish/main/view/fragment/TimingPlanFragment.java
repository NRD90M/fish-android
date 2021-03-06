package com.ypcxpt.fish.main.view.fragment;

import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.ypcxpt.fish.R;
import com.ypcxpt.fish.app.util.VpSwipeRefreshLayout;
import com.ypcxpt.fish.core.app.Path;
import com.ypcxpt.fish.main.model.Scenes;
import com.ypcxpt.fish.library.router.Router;
import com.ypcxpt.fish.library.view.fragment.BaseFragment;
import com.ypcxpt.fish.main.adapter.PlanAdapter;
import com.ypcxpt.fish.main.adapter.TriggerAdapter;
import com.ypcxpt.fish.main.contract.TimingPlanContract;
import com.ypcxpt.fish.main.event.OnGetScenesEvent;
import com.ypcxpt.fish.main.event.OnSceneInfoEvent;
import com.ypcxpt.fish.main.model.IoPlan;
import com.ypcxpt.fish.main.model.IoTrigger;
import com.ypcxpt.fish.main.presenter.TimingPlanPresenter;
import com.ypcxpt.fish.main.util.SelectScenesDialog;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class TimingPlanFragment extends BaseFragment implements TimingPlanContract.View {
    @BindView(R.id.tv_scene)
    TextView tv_scene;
    @BindView(R.id.iv_arrow)
    ImageView iv_arrow;

    @BindView(R.id.tv_check01)
    TextView tv_check01;
    @BindView(R.id.view_line01)
    View view_line01;
    @BindView(R.id.tv_check02)
    TextView tv_check02;
    @BindView(R.id.view_line02)
    View view_line02;

    @BindView(R.id.rv)
    RecyclerView rv;
    @BindView(R.id.rv2)
    RecyclerView rv2;

    @BindView(R.id.tv_addPlan)
    TextView tv_addPlan;

    @BindView(R.id.swipe_refresh_layout)
    VpSwipeRefreshLayout swipe_refresh_layout;

    private int tab = 1;

    /* 首页的切换场景数据传过来 */
    private List<Scenes> mScenes;
    private String mSceneName;
    public static String mMacAddress;

    private TimingPlanContract.Presenter mPresenter;

    private PlanAdapter mAdapter;
    private TriggerAdapter mAdapter2;

    @Override
    protected int layoutResID() {
        return R.layout.fragment_timing_plan;
    }

    @Override
    protected void initData() {
        mPresenter = new TimingPlanPresenter();
        addPresenter(mPresenter);
    }

    @Override
    protected void initViews() {
        mAdapter = new PlanAdapter(R.layout.item_plans, mPresenter, getActivity());
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv.setAdapter(mAdapter);
        ((DefaultItemAnimator) rv.getItemAnimator()).setSupportsChangeAnimations(false);
        rv.getItemAnimator().setChangeDuration(0);// 通过设置动画执行时间为0来解决闪烁问题
//        mAdapter.openLoadAnimation(BaseQuickAdapter.SCALEIN);

        mAdapter2 = new TriggerAdapter(R.layout.item_triggers, mPresenter, getActivity());
        rv2.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv2.setAdapter(mAdapter2);
        ((DefaultItemAnimator) rv.getItemAnimator()).setSupportsChangeAnimations(false);
        rv2.getItemAnimator().setChangeDuration(0);// 通过设置动画执行时间为0来解决闪烁问题
//        mAdapter2.openLoadAnimation(BaseQuickAdapter.SCALEIN);

        swipe_refresh_layout.setOnRefreshListener(() -> new Handler().postDelayed(() -> {
            swipe_refresh_layout.setRefreshing(false);//取消刷新
            if (tab == 1) {
                //根据mac调用获取所有定时任务接口
                mPresenter.getAllPlans(mMacAddress);
            } else {
                //根据mac调用获取所有触发任务接口
                mPresenter.getAllTriggers(mMacAddress);
            }
        }, 2000));
        //设置刷新时旋转图标的颜色，这是一个可变参数，当设置多个颜色时，旋转一周改变一次颜色。
        swipe_refresh_layout.setColorSchemeResources(R.color.main_color_new, R.color.bg_device_detail_yellow, R.color.bg_device_detail_top);
    }

    @OnClick(R.id.ll_select_scenes)
    public void onSelectScenesClick() {
        iv_arrow.setImageResource(R.mipmap.icon_plan_arrow_expanding);
        showSelectScenesDialog();
    }

    /**
     * 选择场景弹窗
     */
    private void showSelectScenesDialog() {
        SelectScenesDialog selectScenesDialog = new SelectScenesDialog(getActivity(), mScenes, R.style.MyDialog);
        selectScenesDialog.setOnResultListener(new SelectScenesDialog.OnResultListener() {

            @Override
            public void SelectScenes(String macAddress, String scene_name, int selected) {
                iv_arrow.setImageResource(R.mipmap.icon_plan_arrow_folding);
                selectScenesDialog.dismiss();

                MyDeviceFragment.sceneSelected = selected;
                mMacAddress = macAddress;
                tv_scene.setText(scene_name);
                if (tab == 1) {
                    //根据mac调用获取所有定时任务接口
                    mPresenter.getAllPlans(macAddress);
                } else {
                    //根据mac调用获取所有触发任务接口
                    mPresenter.getAllTriggers(macAddress);
                }
            }

            @Override
            public void Cancel() {
                iv_arrow.setImageResource(R.mipmap.icon_plan_arrow_folding);
                selectScenesDialog.dismiss();
            }
        });
        Window dialogWindow = selectScenesDialog.getWindow();
        dialogWindow.setGravity(Gravity.TOP);
        selectScenesDialog.show();
    }

    @OnClick({R.id.rl_check01, R.id.rl_check02})
    public void onCheckClick(View view) {
        switch (view.getId()) {
            case R.id.rl_check01:
                rv.setVisibility(View.VISIBLE);
                rv2.setVisibility(View.GONE);
                tab = 1;
                tv_check01.setTypeface(Typeface.DEFAULT_BOLD);
                view_line01.setVisibility(View.VISIBLE);
                tv_check02.setTypeface(Typeface.DEFAULT);
                view_line02.setVisibility(View.INVISIBLE);
                tv_addPlan.setText("添加定时");
                //调用获取所有定时任务接口
                mPresenter.getAllPlans(mMacAddress);
                break;
            case R.id.rl_check02:
                rv.setVisibility(View.GONE);
                rv2.setVisibility(View.VISIBLE);
                tab = 2;
                tv_check01.setTypeface(Typeface.DEFAULT);
                view_line01.setVisibility(View.INVISIBLE);
                tv_check02.setTypeface(Typeface.DEFAULT_BOLD);
                view_line02.setVisibility(View.VISIBLE);
                tv_addPlan.setText("添加触发任务");
                //调用获取所有触发任务接口
                mPresenter.getAllTriggers(mMacAddress);
                break;
        }
    }

    @OnClick(R.id.tv_addPlan)
    public void onAddPlanClick() {
        if (tab == 1) {
            Router.build(Path.Main.ADD_PLAN)
                    .withInt("SCENE_SELECTED", MyDeviceFragment.sceneSelected)
                    .withInt("PLAN_TYPE", 1)
                    .withString("DEVICE_MAC", mMacAddress)
                    .navigation(getActivity());
        } else {
            Router.build(Path.Main.ADD_TRIGGER)
                    .withInt("SCENE_SELECTED", MyDeviceFragment.sceneSelected)
                    .withInt("TRIGGER_TYPE", 1)
                    .withString("DEVICE_MAC", mMacAddress)
                    .navigation(getActivity());
        }

    }

    @Override
    public void showScenes(List<Scenes> scenes) {

    }

    @Override
    public void showIoPlans(List<IoPlan> ioPlans) {
        mAdapter.setNewData(ioPlans);
    }

    @Override
    public void showIoTriggers(List<IoTrigger> ioTriggers) {
        mAdapter2.setNewData(ioTriggers);
    }

    @Subscribe
    public void onEventReceived(OnGetScenesEvent event) {
        mPresenter.getScenes();
    }

    @Subscribe
    public void onEventReceived(OnSceneInfoEvent event) {
        tv_scene.setText(event.sceneName);
        mScenes = event.scenes;
        mMacAddress = event.macAddress;
        mSceneName = event.sceneName;

        if (tab == 1) {
            //调用获取所有定时任务接口
            mPresenter.getAllPlans(event.macAddress);
        } else {
            mPresenter.getAllTriggers(event.macAddress);
        }
    }
}
